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
import com.aodocs.endpoints.storage.StringListSupplier;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
 * This authenticator allows any request with a provided parameter paramName that has a value in the provided list.
 * Can be used to implement custom whitelisting logic for the "key" parameter used in the API management
 * features of Cloud Endpoints v2.
 */
public class QueryParameterValueAuthenticator extends ExtendedAuthenticator {

    @JsonProperty
    private final String paramName;
    @JsonProperty
    private final boolean allowIfAbsent;
    @JsonProperty("values")
    private final StringListSupplier valuesSupplier;

    public QueryParameterValueAuthenticator(@NonNull String paramName,
                                            boolean allowIfAbsent,
                                            @NonNull StringListSupplier valuesSupplier) {
        this.paramName = paramName;
        this.allowIfAbsent = allowIfAbsent;
        this.valuesSupplier = valuesSupplier;
    }

    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        String parameter = request.getParameter(paramName);
        return new AuthorizationResult(parameter == null ? allowIfAbsent : valuesSupplier.get().contains(parameter));
    }
}
