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
package com.aodocs.endpoints.auth.authorizers.config;

import javax.servlet.http.HttpServletRequest;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.AbstractAuthorizer;
import com.google.api.server.spi.config.model.ApiMethodConfig;

/**
 * Authorizes request based on API version. Can be useful to deny access to beta versions.
 */
abstract class VersionAuthorizer extends AbstractAuthorizer {
   
    @Override
    public final AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        //version is a standard metric, no need to add it
        return new AuthorizationResult(isAuthorized(apiMethodConfig.getApiClassConfig().getApiConfig().getVersion()));
    }

    protected abstract boolean isAuthorized(String version);
}
