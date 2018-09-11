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
package com.aodocs.endpoints.auth.authenticator.token;

import com.aodocs.endpoints.auth.AuthType;
import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authenticator.ExtendedAuthenticator;
import com.aodocs.endpoints.auth.authenticator.logic.ConjunctAuthenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.api.server.spi.config.model.ApiMethodConfig;

import javax.servlet.http.HttpServletRequest;

/**
 * Only allows Oauth2 tokens. Should be used with {@link ConjunctAuthenticator}
 */
@Singleton
public class OAuth2OnlyAuthenticator extends ExtendedAuthenticator {

    @Override
    public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
        return new AuthorizationResult(extendedUser.getAuthType() == AuthType.OAUTH2);
    }

}
