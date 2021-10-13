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
package com.aodocs.endpoints.auth.authorizers.logic;

import java.util.Arrays;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import lombok.NonNull;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.AbstractAuthorizer;
import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Requires all provided authorizers to authorize the user.
 */
public final class ConjunctAuthorizer extends AbstractAuthorizer {

    @JsonProperty("and")
    private final ImmutableList<Authorizer> authorizers;

    @JsonCreator
    public ConjunctAuthorizer(@NonNull Authorizer... authorizers) {
        Preconditions.checkArgument(!Arrays.stream(authorizers).anyMatch(Objects::isNull), "Authorizer should be non-null");
        this.authorizers = ImmutableList.copyOf(authorizers);
    }

    @Override
    public AuthorizationResult isAuthorized(final ExtendedUser extendedUser, final ApiMethodConfig methodConfig, final HttpServletRequest request) {
        boolean authorized = authorizers.stream()
                .map(input -> input.isAuthorized(extendedUser, methodConfig, request))
                .allMatch(AuthorizationResult::isAuthorized);
        return new AuthorizationResult(authorized);
    }
}
