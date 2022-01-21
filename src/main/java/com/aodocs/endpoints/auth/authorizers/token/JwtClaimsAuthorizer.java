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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.java.Log;

import com.aodocs.endpoints.auth.AuthInfo;
import com.aodocs.endpoints.auth.ExtendedUser;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.annotations.VisibleForTesting;

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
    
    //must be overridden to set the property name
    public JwtClaimsAuthorizer setClaims(Map<String, Object> claims) {
        this.claims.putAll(claims);
        return this;
    }
    
    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        AuthorizationResult result = super.isAuthorized(extendedUser, apiMethodConfig, request);
        if (!result.isAuthorized()) {
            return result;
        }
        return new AuthorizationResult(checkJwtClaims(extendedUser.getAuthInfo()));
    }
    
    @VisibleForTesting
    boolean checkJwtClaims(AuthInfo authInfo) {
        return combiner.test(claims.entrySet().stream(), entry -> checkClaim(authInfo.getClaims(), entry.getKey(), entry.getValue()));
    }
    
    private boolean checkClaim(AuthInfo.Claims claims, String key, Object value) {
        Optional<Object> claim = claims.get(key);
        return claim == null ? false : claim.map(claimValue -> Objects.equals(claimValue, value)).orElse(value == null);
    }
}
