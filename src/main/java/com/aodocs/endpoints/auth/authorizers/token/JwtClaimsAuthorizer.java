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
package com.aodocs.endpoints.auth.authorizers.token;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Checks if the token is a JWT token, and checks claims from this token.
 * 
 * Note: when passing null values to claims to check, there will be no difference between a null or a missing claim in the token.
 */
@Getter(AccessLevel.PACKAGE)
@Log
public abstract class JwtClaimsAuthorizer extends JwtOnlyAuthorizer {
    
    /**
     * Expects keys that are either:
     * - field names directly under the root of the payload 
     * - JSON pointer expressions for nested fields (starting "/" is optional)
     */
    private final Map<String, Object> claims = new LinkedHashMap<>();
    
    private final BiPredicate<Stream<Map.Entry<String, Object>>, Predicate<Map.Entry<String, Object>>> combiner;
    
    protected JwtClaimsAuthorizer(BiPredicate<Stream<Map.Entry<String, Object>>, Predicate<Map.Entry<String, Object>>> combiner) {
        this.combiner = combiner;
    }
    
    @VisibleForTesting
    Map<String, Object> getClaims() {
        return claims;
    }
    
    //mus be overridden to set the property name
    public JwtClaimsAuthorizer setClaims(Map<String, Object> claims) {
        this.claims.putAll(claims);
        return this;
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        AuthorizationResult result = super.isAuthorized(extendedUser, apiMethodConfig, request);
        if (!result.isAuthorized()) {
            return result;
        }
        return new AuthorizationResult(checkJwtClaims(getIdTokenPayload(extendedUser)));
    }
    
    private IdToken.Payload getIdTokenPayload(ExtendedUser extendedUser) {
        Object rawTokenInfo = extendedUser.getAuthInfo().getRawTokenInfo();
        Preconditions.checkState(rawTokenInfo instanceof IdToken, "Auth type is JWT, but token info is not an ID token");
        return ((IdToken) rawTokenInfo).getPayload();
    }
    
    @VisibleForTesting
    boolean checkJwtClaims(IdToken.Payload payload) throws JsonProcessingException {
        //convert to JsonNode to be able to use JsonPointer syntax
        JsonNode asJsonTree = new ObjectMapper().readTree(payload.toString());
        return combiner.test(claims.entrySet().stream(), entry -> checkClaim(asJsonTree, entry.getKey(), entry.getValue()));
    }
    
    private boolean checkClaim(JsonNode payload, String key, Object value) {
        //make the key a valid json pointer
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        JsonNode node = payload.at(key);
        JsonNodeType nodeType = node.getNodeType();
        switch (nodeType) {
            case BOOLEAN:
                return node.isBoolean() && value.equals(node.asBoolean());
            case NUMBER:
                return node.isNumber() && value instanceof Number && ((Number) value).longValue() == node.asLong();
            case STRING:
                return node.isTextual() && value.equals(node.asText());
            case NULL:
            case MISSING:
                return value == null;
            case OBJECT:
            case POJO:
            case ARRAY:
            case BINARY:
                log.log(Level.WARNING,
                        "Cannot compare node of type {0} at {1} with ''{2}'' (node value is ''{3}'')",
                        new Object[] {nodeType, key, value, node});
        }
        return false;
    }
    
}
