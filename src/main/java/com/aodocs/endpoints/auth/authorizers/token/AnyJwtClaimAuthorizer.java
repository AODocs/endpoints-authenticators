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
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Checks if the token is a JWT token, and checks claims
 */
public class AnyJwtClaimAuthorizer extends JwtClaimsAuthorizer {
    
    public AnyJwtClaimAuthorizer() {
        super(Stream::anyMatch);
    }
    
    public AnyJwtClaimAuthorizer(Map<String, Object> claims) {
        super(Stream::anyMatch);
        setClaims(claims);
    }
    
    @JsonProperty("anyJwtClaim")
    @Override
    public JwtClaimsAuthorizer setClaims(Map<String, Object> claims) {
        return super.setClaims(claims);
    }
    
}
