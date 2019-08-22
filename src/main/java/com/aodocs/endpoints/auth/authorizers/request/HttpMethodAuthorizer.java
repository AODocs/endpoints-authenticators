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
package com.aodocs.endpoints.auth.authorizers.request;

import javax.servlet.http.HttpServletRequest;

import lombok.NonNull;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.AbstractAuthorizer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.server.spi.config.model.ApiMethodConfig;

/**
 * This authenticators allows only the provided HTTP method
 */
public final class HttpMethodAuthorizer extends AbstractAuthorizer {

    public enum HttpMethod {
        DELETE, GET, POST, PUT, PATCH;
        //OPTIONS is used for CORS only
        //HEAD is not supported by Endpoints
    }

    @JsonProperty
    private final HttpMethod httpMethod;

    public HttpMethodAuthorizer(@JsonProperty("httpMethod") @NonNull HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig methodConfig, HttpServletRequest request) {
        return newResultBuilder().authorized(methodConfig.getHttpMethod().equalsIgnoreCase(httpMethod.name())).build();
    }
}
