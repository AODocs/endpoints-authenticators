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
import java.util.List;

import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.collect.ImmutableList;

/**
 * This factory builds a complex authenticator using a DSL.
 *
 * TODO: describe the DSL
 **
 */
public class DslAuthorizerFactory {
    private static DslAuthorizerFactory INSTANCE = new DslAuthorizerFactory();
    
    public static DslAuthorizerFactory get() {
        return INSTANCE;
    }
    
    public enum Format {
        YAML(new YAMLMapper()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)),
        JSON(new ObjectMapper());

        private final ObjectMapper objectMapper;

        Format(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper
                    .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
                    .registerModules(MODULES);
        }
    }

    private static final List<Module> MODULES = ImmutableList.of(
            DslAuthorizerConfig.MODULE,
            new GuavaModule(),
            new ParameterNamesModule(JsonCreator.Mode.PROPERTIES)
    );

    public Authorizer build(String dslConfig, Format format) throws IOException {
        return (format.objectMapper
                .reader().forType(Authorizer.class)
                .readValue(dslConfig));
    }

    public String toString(Authorizer authorizer, Format format) throws JsonProcessingException {
        return format.objectMapper.writeValueAsString(authorizer);
    }
}
