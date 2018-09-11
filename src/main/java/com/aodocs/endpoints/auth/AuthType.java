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
package com.aodocs.endpoints.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.GoogleJwtAuthenticator;
import com.google.api.server.spi.auth.GoogleOAuth2Authenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.request.Attribute;
import com.google.api.server.spi.response.ServiceUnavailableException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.aodocs.endpoints.auth.AuthType.ThrowingSupplier.unchecked;

/**
 * Wraps built-in authenticators to detect the type of authentication in the request.
 */
public enum AuthType {

    /**
     * OAuth2 authentication (can authorize any valid scope)
     */
    OAUTH2 {
        @Override
        public User authenticate(HttpServletRequest request) throws ServiceUnavailableException {
            return new GoogleOAuth2Authenticator().authenticate(request);
        }

        @Override
        public AuthInfo getAuthInfo(HttpServletRequest request, String token) throws ServiceUnavailableException {
            final GoogleAuth.TokenInfo cached = (GoogleAuth.TokenInfo) request.getAttribute(Attribute.TOKEN_INFO);
            return new AuthInfo(token, Optional.ofNullable(cached).orElseGet(unchecked(() -> getTokenInfoRemote(token))));
        }

        GoogleAuth.TokenInfo getTokenInfoRemote(String token) throws ServiceUnavailableException {
            return GoogleAuth.getTokenInfoRemote(token);
        }
    },
    /**
     * JWT authentication (no authorization, only authentication)
     */
    JWT {
        @Override
        public User authenticate(HttpServletRequest request) {
            return new GoogleJwtAuthenticator().authenticate(request);
        }

        @Override
        public AuthInfo getAuthInfo(HttpServletRequest request, String token) throws ServiceUnavailableException {
            final GoogleIdToken cached = (GoogleIdToken) request.getAttribute(Attribute.ID_TOKEN);
            return new AuthInfo(token, Optional.ofNullable(cached).orElseGet(unchecked(() -> getGoogleIdToken(token))));
        }

        GoogleIdToken getGoogleIdToken(String token) throws ServiceUnavailableException {
            try {
                return new GoogleIdTokenVerifier.Builder(Client.getInstance().getHttpTransport(),
                        Client.getInstance().getJsonFactory()).build().verify(token);
            } catch (GeneralSecurityException | IOException e) {
                throw new ServiceUnavailableException("Failed to perform id token validation", e);
            }
        }
    };

    //TODO support ESP authenticator

    public abstract User authenticate(HttpServletRequest request) throws ServiceUnavailableException;

    public abstract AuthInfo getAuthInfo(HttpServletRequest request, String token) throws ServiceUnavailableException;

    @FunctionalInterface
    public interface ThrowingSupplier<R> {
        R get() throws Exception;

        static <R> Supplier<R> unchecked(ThrowingSupplier<R> s) {
            return () -> {
                try {
                    return s.get();
                } catch (Exception ex) {
                    return sneakyThrow(ex);
                }
            };
        }

        @SuppressWarnings("unchecked")
        static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
            throw (T) t;
        }
    }

}
