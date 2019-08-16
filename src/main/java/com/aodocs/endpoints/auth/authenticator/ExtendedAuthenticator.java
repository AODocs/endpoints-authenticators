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
package com.aodocs.endpoints.auth.authenticator;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

import com.aodocs.endpoints.auth.AuthInfo;
import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.request.Attribute;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Base class for all extended authenticators provided by this project.
 */
@Log
@Singleton
public class ExtendedAuthenticator implements Authenticator {

    private final Authenticator delegateAuthenticator;
    private final Authorizer authorizer;

    ExtendedAuthenticator(Authenticator delegate, Authorizer authorizer) {
        this.delegateAuthenticator = delegate;
        this.authorizer = authorizer;
    }

    @Override
    public User authenticate(final HttpServletRequest request) throws ServiceException {
        long start = System.currentTimeMillis();

        //disable client id checking, the whole point of this authenticator is to bypass it
        Attribute attribute = Attribute.from(request);
        attribute.remove(Attribute.ENABLE_CLIENT_ID_WHITELIST);
    
        final User user = delegateAuthenticator.authenticate(request);
        long standardAuthTime = System.currentTimeMillis() - start;
        
        //the user could be null at this point because the token does
        //not contain the email scope, or any of the required scopes
        if (user == null) {
            return null;
        }

        //Authorization
        try {
            ExtendedUser extendedUser = getExtendedUser(getAuthInfo(request), user);
            ApiMethodConfig methodConfig = attribute.get(Attribute.API_METHOD_CONFIG);
            Authorizer.AuthorizationResult authorizationResult = authorizer.isAuthorized(extendedUser, methodConfig, request);
            
            if (log.isLoggable(Level.INFO)) {
                long totalAuthTime = System.currentTimeMillis() - start;
                String status = authorizationResult.isAuthorized() ? "AUTHORIZED" : "FORBIDDEN";
                long overheadTime = totalAuthTime - standardAuthTime;
                log.log(Level.INFO, "{0} authorization checked in {1} ms (overhead {2} ms) with status {3}",
                        new Object[] { extendedUser.getEmail(), totalAuthTime, overheadTime, status });
            }
            
            return authorizationResult.isAuthorized() ? extendedUser : null;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot authenticate user with custom authenticator, returning standard User", e);
            return user;
        }
    }

    @SneakyThrows(ServiceException.class)
    private User performPrimaryAuthentication(HttpServletRequest request) {
        return delegateAuthenticator.authenticate(request);
    }

    private ExtendedUser getExtendedUser(AuthInfo authInfo, User basicUser) {
        log.fine("User: " + basicUser);

        Preconditions.checkState(
                authInfo.getEmail() == null || authInfo.getEmail().equals(basicUser.getEmail()),
                "Mismatch in authorized user email");
        Preconditions.checkState(
                authInfo.getUserId() == null || authInfo.getUserId().equals(basicUser.getId()),
                "Mismatch in authorized user id");

        return new ExtendedUser(basicUser, authInfo);
    }

    @VisibleForTesting
    AuthInfo getAuthInfo(HttpServletRequest request) {
        //The user was authenticated with either one of the two.
        final GoogleAuth.TokenInfo tokenInfo = (GoogleAuth.TokenInfo) request.getAttribute(Attribute.TOKEN_INFO);
        if (tokenInfo != null) {
            return new AuthInfo(tokenInfo);
        }
    
        final GoogleIdToken tokenId = (GoogleIdToken) request.getAttribute(Attribute.ID_TOKEN);
        return new AuthInfo(tokenId);
    }
}
