/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 AODocs (Altirnao Inc)
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.aodocs.endpoints.auth.authorizers.dsl;

import java.io.IOException;
import java.util.Map;

import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.ClientIdsAuthenticator;
import com.aodocs.endpoints.auth.authorizers.clientid.CurrentProjectClientIdAuthenticator;
import com.aodocs.endpoints.auth.authorizers.config.VersionContainsAuthenticator;
import com.aodocs.endpoints.auth.authorizers.config.VersionMatchesAuthenticator;
import com.aodocs.endpoints.auth.authorizers.logic.ConjunctAuthenticator;
import com.aodocs.endpoints.auth.authorizers.logic.DisjunctAuthenticator;
import com.aodocs.endpoints.auth.authorizers.logic.NegateAuthenticator;
import com.aodocs.endpoints.auth.authorizers.request.HttpMethodAuthenticator;
import com.aodocs.endpoints.auth.authorizers.role.ProjectMemberAuthenticator;
import com.aodocs.endpoints.auth.authorizers.role.ProjectOwnerAuthenticator;
import com.aodocs.endpoints.auth.authorizers.token.JwtOnlyAuthenticator;
import com.aodocs.endpoints.auth.authorizers.token.OAuth2OnlyAuthenticator;
import com.aodocs.endpoints.storage.ClasspathStringListSupplier;
import com.aodocs.endpoints.storage.CloudStorageStringListSupplier;
import com.aodocs.endpoints.storage.DatastoreStringListSupplier;
import com.aodocs.endpoints.storage.ExplicitStringListSupplier;
import com.aodocs.endpoints.storage.MergingStringListSupplier;
import com.aodocs.endpoints.storage.StringListSupplier;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableMap;

/**
 * This class provides a Jackson modules that will be able to serialize / deserialize complex authenticator configs
 * with a simple DSL, either in JSON or YAML. It handles the poymorphic de/serialization of ExtendedAuthenticator and
 * StringListSupplier instances, without the usual Jackson "tricks" to provide typing information.
 *
 * It handles three types of polymorphic abjects:
 * - "classic" objects (object with properties): these must be serializable and deserializable by Jackson.
 * Immutability is recommended (using @JsonCreator). They must also have a "discriminating" property, i.e. a property
 * whose name is unique amongst all the type hierarchy.
 * - "singleton" objects (annotated with @Singleton as a hint for Cloud Endpoints): this type of authenticators
 * will be serialized to a simple string in the DSL, so they don't need to be serializable (and are not be by default as
 * they have no property). Obviously, the JSON name of the singleton must be unique amongst the type hierarchy.
 * - "array"  object: a unique type is allowed to be deserialized directly as an array. It must be unique because an
 * array does not provide any hint about.
 * <p>
 * This module is necessary to work around two limitation of Jackson:
 * - Type cannot be inferred on the presence of a specific property when performing polymorphic deserialization (used
 * for "classic" authenticators)
 * - Polymorphic de/serialization does not allow to serialize objects to something else than a JSON object without a
 * custom serializer. We want to handle strings and arrays as well.
 * *
 * TODO make this extensible by third parties.
 */
public interface DslAuthenticatorConfig {


    /**
     * ExtendedAuthenticators represented as objects in the DSL, determined by a discriminator property.
     */
    ImmutableMap<String, Class<? extends Authorizer>> DISCRIMINATOR_PROPERTY_AUTHENTICATORS
            = ImmutableMap.<String, Class<? extends Authorizer>>builder()
            .put("and", ConjunctAuthenticator.class) //alias to all?
            .put("or", DisjunctAuthenticator.class) //alias to any?
            .put("not", NegateAuthenticator.class)
            .put("versionMatches", VersionMatchesAuthenticator.class)
            .put("versionContains", VersionContainsAuthenticator.class)
            .put("httpMethod", HttpMethodAuthenticator.class)
            .put("clientIds", ClientIdsAuthenticator.class)
            .build();

    /**
     * ExtendedAuthenticators represented as strings in the DSL, determined by their name.
     */
    Map<String, Class<? extends Authorizer>> SINGLETON_AUTHENTICATORS
            = ImmutableMap.<String, Class<? extends Authorizer>>builder()
            .put("jwt", JwtOnlyAuthenticator.class)  //alias to idToken?
            .put("oauth2", OAuth2OnlyAuthenticator.class)  //alias to accessToken?
            .put("currentProjectClientId", CurrentProjectClientIdAuthenticator.class)
            .put("projectMember", ProjectMemberAuthenticator.class)
            .put("projectOwner", ProjectOwnerAuthenticator.class)
            .build();

    DslDeserializer<Authorizer> AUTHENTICATOR_DESERIALIZER
            = new DslDeserializer<>(Authorizer.class,
            DISCRIMINATOR_PROPERTY_AUTHENTICATORS, SINGLETON_AUTHENTICATORS);

    /**
     * StringListSuppliers represented as objects in the DSL, determined by a discriminator property.
     */
    ImmutableMap<String, Class<? extends StringListSupplier>> DISCRIMINATOR_PROPERTY_SUPPLIERS
            = ImmutableMap.<String, Class<? extends StringListSupplier>>builder()
            .put("merge", MergingStringListSupplier.class)
            .put("classpathResource", ClasspathStringListSupplier.class)
            .put("datastoreEntity", DatastoreStringListSupplier.class)
            .put("cloudStorageUrl", CloudStorageStringListSupplier.class)
            .build();

    DslDeserializer<StringListSupplier> STRING_SUPPLIER_DESERIALIZER
            = new DslDeserializer<>(StringListSupplier.class,
            DISCRIMINATOR_PROPERTY_SUPPLIERS,
            ExplicitStringListSupplier.class); //StringListSupplier represented as an array


    SimpleModule MODULE = buildModule();

    static SimpleModule buildModule() {
        final SimpleModule module = new SimpleModule()
                .addDeserializer(Authorizer.class, AUTHENTICATOR_DESERIALIZER)
                .addDeserializer(StringListSupplier.class, STRING_SUPPLIER_DESERIALIZER);
        SINGLETON_AUTHENTICATORS.forEach((stringValue, singletonClass) ->
                module.addSerializer(singletonClass, getSingletonSerializer(stringValue)));
        return module;
    }

    static StdSerializer<Authorizer> getSingletonSerializer(final String stringValue) {
        return new StdSerializer<Authorizer>(Authorizer.class) {
            @Override
            public void serialize(Authorizer value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeString(stringValue);
            }
        };
    }
}
