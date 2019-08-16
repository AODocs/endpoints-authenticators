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
package com.aodocs.endpoints.auth.authenticator.combined;

import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.and;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.not;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.or;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.versionContains;

import javax.servlet.http.HttpServletRequest;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authenticator.Authorizer;
import com.aodocs.endpoints.auth.authenticator.logic.DisjunctAuthenticator;
import com.google.api.server.spi.config.model.ApiMethodConfig;

/**
 * Applies a specific authenticator on versions containing a specific string.
 * Can be useful to restrict usage of beta / internal API versions.
 * Must be subclassed or used in another authenticator.
 */
public class RestrictedVersionAuthenticator implements Authorizer {
    
    public static RestrictedVersionAuthenticator beta(
            Authorizer defaultAuthenticator, Authorizer betaAuthenticator) {
        return new RestrictedVersionAuthenticator(defaultAuthenticator, "beta", betaAuthenticator, true);
    }

    public static RestrictedVersionAuthenticator internal(
            Authorizer defaultAuthenticator, Authorizer betaAuthenticator) {
        return new RestrictedVersionAuthenticator(defaultAuthenticator, "internal", betaAuthenticator, true);
    }
    
    private final Authorizer delegate;

    public RestrictedVersionAuthenticator(Authorizer defaultAuthenticator,
                                          String ifVersionContains, Authorizer specificAuthenticator, boolean includeSpecificInDefault) {
        delegate = new DisjunctAuthenticator(
                and(
                        not(versionContains(ifVersionContains)),
                        includeSpecificInDefault
                                ? or(defaultAuthenticator, specificAuthenticator)
                                : defaultAuthenticator
                ),
                and(
                        versionContains(ifVersionContains),
                        specificAuthenticator
                )
        );
    }
    
    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        return delegate.isAuthorized(extendedUser, apiMethodConfig, request);
    }
}
