/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 - 2021 AODocs (Altirnao Inc)
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
import com.aodocs.endpoints.auth.authorizers.audience.CurrentProjectAudienceAuthorizer;
import com.aodocs.endpoints.auth.authorizers.audience.EntraIdApplicationAuthorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.ClientIdsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.CurrentProjectClientIdAuthorizer;
import com.aodocs.endpoints.auth.authorizers.config.VersionContainsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.config.VersionMatchesAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.ConjunctAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.DisjunctAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.NegateAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.HttpMethodAuthorizer;
import com.aodocs.endpoints.auth.authorizers.role.ProjectMemberAuthorizer;
import com.aodocs.endpoints.auth.authorizers.role.ProjectOwnerAuthorizer;
import com.aodocs.endpoints.auth.authorizers.token.AllJwtClaimsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.token.AnyJwtClaimAuthorizer;
import com.aodocs.endpoints.auth.authorizers.token.JwtOnlyAuthorizer;
import com.aodocs.endpoints.auth.authorizers.token.OAuth2OnlyAuthorizer;
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
 * This class provides a Jackson modules that will be able to serialize / deserialize complex Authorizer configs
 * with a simple DSL, either in JSON or YAML. It handles the poymorphic de/serialization of ExtendedAuthorizer and
 * StringListSupplier instances, without the usual Jackson "tricks" to provide typing information.
 *
 * It handles three types of polymorphic abjects:
 * - "classic" objects (object with properties): these must be serializable and deserializable by Jackson.
 * Immutability is recommended (using @JsonCreator). They must also have a "discriminating" property, i.e. a property
 * whose name is unique amongst all the type hierarchy.
 * - "singleton" objects (annotated with @Singleton as a hint for Cloud Endpoints): this type of Authorizers
 * will be serialized to a simple string in the DSL, so they don't need to be serializable (and are not be by default as
 * they have no property). Obviously, the JSON name of the singleton must be unique amongst the type hierarchy.
 * - "array"  object: a unique type is allowed to be deserialized directly as an array. It must be unique because an
 * array does not provide any hint about.
 * <p>
 * This module is necessary to work around two limitation of Jackson:
 * - Type cannot be inferred on the presence of a specific property when performing polymorphic deserialization (used
 * for "classic" Authorizers)
 * - Polymorphic de/serialization does not allow to serialize objects to something else than a JSON object without a
 * custom serializer. We want to handle strings and arrays as well.
 * *
 * TODO make this extensible by third parties.
 */
public interface DslAuthorizerConfig {
    
    /**
     * ExtendedAuthorizers represented as objects in the DSL, determined by a discriminator property.
     */
    ImmutableMap<String, Class<? extends Authorizer>> DISCRIMINATOR_PROPERTY_AuthorizerS
            = ImmutableMap.<String, Class<? extends Authorizer>>builder()
            .put("and", ConjunctAuthorizer.class) //alias to all?
            .put("or", DisjunctAuthorizer.class) //alias to any?
            .put("not", NegateAuthorizer.class)
            .put("versionMatches", VersionMatchesAuthorizer.class)
            .put("versionContains", VersionContainsAuthorizer.class)
            .put("httpMethod", HttpMethodAuthorizer.class)
            .put("clientIds", ClientIdsAuthorizer.class)
            .put("entraIdAppIds", EntraIdApplicationAuthorizer.class)
            .put("anyJwtClaim", AnyJwtClaimAuthorizer.class)
            .put("allJwtClaims", AllJwtClaimsAuthorizer.class)
            .build();

    /**
     * ExtendedAuthorizers represented as strings in the DSL, determined by their name.
     */
    Map<String, Class<? extends Authorizer>> SINGLETON_AuthorizerS
            = ImmutableMap.<String, Class<? extends Authorizer>>builder()
            .put("jwt", JwtOnlyAuthorizer.class)  //alias to idToken?
            .put("oauth2", OAuth2OnlyAuthorizer.class)  //alias to accessToken?
            .put("currentProjectClientId", CurrentProjectClientIdAuthorizer.class)
            .put("currentProjectAudience", CurrentProjectAudienceAuthorizer.class)
            .put("projectMember", ProjectMemberAuthorizer.class)
            .put("projectOwner", ProjectOwnerAuthorizer.class)
            .build();

    DslDeserializer<Authorizer> Authorizer_DESERIALIZER
            = new DslDeserializer<>(Authorizer.class,
            DISCRIMINATOR_PROPERTY_AuthorizerS, SINGLETON_AuthorizerS);

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
                .addDeserializer(Authorizer.class, Authorizer_DESERIALIZER)
                .addDeserializer(StringListSupplier.class, STRING_SUPPLIER_DESERIALIZER);
        SINGLETON_AuthorizerS.forEach((stringValue, singletonClass) ->
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
