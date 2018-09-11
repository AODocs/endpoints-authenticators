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
package com.aodocs.endpoints.auth.authenticator.request;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authenticator.ExtendedAuthenticator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
 * This authenticator allows any request with a provided parameter, regardless of the value.
 */
public class QueryParameterAuthenticator extends ExtendedAuthenticator {

    @JsonProperty
    private final String requiredQueryParam;

    public QueryParameterAuthenticator(@NonNull String requiredQueryParam) {
        this.requiredQueryParam = requiredQueryParam;
    }

    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        return new AuthorizationResult(!Strings.isNullOrEmpty(request.getParameter(requiredQueryParam)));
    }
}
