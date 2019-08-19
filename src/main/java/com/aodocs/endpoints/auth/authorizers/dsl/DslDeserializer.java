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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.Map;

class DslDeserializer<T> extends StdDeserializer<T> {

    private final Map<String, Class<? extends T>> classByDiscriminatorProperty;
   
    private final Map<String, Class<? extends T>> singletonClassByName;
   
    private final Class<? extends T> arrayType;

    /**
     * Caches for singleton instances
     */
    private final LoadingCache<Class<? extends T>, T> singletonCache = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<Class<? extends T>, T>() {
                @Override
                public T load(Class<? extends T> key) throws Exception {
                    return key.newInstance();
                }
            });

    protected DslDeserializer(Class<T> vc,
                              Map<String, Class<? extends T>> classByDiscriminatorProperty,
                              Map<String, Class<? extends T>> singletonClassByName) {
        this(vc, classByDiscriminatorProperty, singletonClassByName, null);
    }

    public DslDeserializer(Class<T> vc, Map<String, Class<? extends T>> classByDiscriminatorProperty, Class<? extends T> arrayType) {
        this(vc, classByDiscriminatorProperty, Collections.emptyMap(), arrayType);
    }

    public DslDeserializer(Class<?> vc, Map<String, Class<? extends T>> classByDiscriminatorProperty, Map<String, Class<? extends T>> singletonClassByName, Class<? extends T> arrayType) {
        super(vc);
        this.classByDiscriminatorProperty = classByDiscriminatorProperty;
        this.singletonClassByName = singletonClassByName;
        this.arrayType = arrayType;
    }

    @Override
    @SneakyThrows
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
        TreeNode node = objectMapper.readTree(p);
        if (node instanceof ObjectNode) {
            for (String propertyName : classByDiscriminatorProperty.keySet()) {
                if (((ObjectNode) node).has(propertyName)) {
                    return objectMapper.treeToValue(node, classByDiscriminatorProperty.get(propertyName));
                }
            }
            throw new IllegalArgumentException("Don't know how to deserialize " + node + " as an " + handledType().getSimpleName());
        }
        if (node instanceof TextNode) {
            return singletonCache.get(Preconditions.checkNotNull(singletonClassByName.get(((TextNode) node).asText()),
                    "Don't know how to deserialize " + node + " as an " + handledType().getSimpleName()));
        }
        if (node instanceof ArrayNode) {
            return objectMapper.treeToValue(node, Preconditions.checkNotNull(arrayType,
                    "Cannot deserialize array as " + handledType().getSimpleName()));
        }
        throw new IllegalStateException("Cannot deserialize " + node + " as an " + handledType().getSimpleName());
    }

}
