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

import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Checks if the token is a JWT token, and checks claims
 */
public class AllJwtClaimsAuthorizer extends JwtClaimsAuthorizer {
    
    public AllJwtClaimsAuthorizer() {
        super(Stream::allMatch);
    }
    
    public AllJwtClaimsAuthorizer(Map<String, Object> claims) {
        super(Stream::allMatch);
        setClaims(claims);
    }
    
    @JsonProperty("allJwtClaims")
    @Override
    public JwtClaimsAuthorizer setClaims(Map<String, Object> claims) {
        return super.setClaims(claims);
    }
    
}
