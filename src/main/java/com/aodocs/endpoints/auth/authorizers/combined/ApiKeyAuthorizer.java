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
package com.aodocs.endpoints.auth.authorizers.combined;


import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.not;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.requiredQueryParamValue;

import javax.servlet.http.HttpServletRequest;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.aodocs.endpoints.auth.authorizers.logic.ConjunctAuthorizer;
import com.google.api.server.spi.config.model.ApiMethodConfig;

/**
 * Authenticator that checks API key, with both whitelist and blacklist.
 * Must be subclassed or used in another authenticator.
 */
public class ApiKeyAuthorizer implements Authorizer {
    
    private final Authorizer delegate;
    

    protected ApiKeyAuthorizer(CombinedStringListBuilder slb, Authorizer authenticator) {
        delegate = new ConjunctAuthorizer(
                requiredQueryParamValue("key", slb.whitelist("apiKeys")),
                not(requiredQueryParamValue("key", slb.blacklist("apiKeys"))),
                authenticator
        );
    }
    
    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        return delegate.isAuthorized(extendedUser, apiMethodConfig, request);
    }
}
