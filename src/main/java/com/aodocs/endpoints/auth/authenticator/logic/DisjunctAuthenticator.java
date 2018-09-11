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
package com.aodocs.endpoints.auth.authenticator.logic;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authenticator.ExtendedAuthenticator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
 * Requires any provided authenticator to authorize the user. Evaluates authenticators in provided order.
 */
public class DisjunctAuthenticator extends ExtendedAuthenticator {

    @JsonProperty("or")
    private final ImmutableList<ExtendedAuthenticator> authenticators;

    @JsonCreator
    public DisjunctAuthenticator(@NonNull ExtendedAuthenticator... authenticators) {
        this.authenticators = ImmutableList.copyOf(authenticators);
    }

    @Override
    public AuthorizationResult isAuthorized(final ExtendedUser extendedUser, final ApiMethodConfig methodConfig, final HttpServletRequest request) {
        return new AuthorizationResult(authenticators.stream()
                .map(input -> input.isAuthorized(extendedUser, methodConfig, request))
                .anyMatch(AuthorizationResult::isAuthorized));
    }

}
