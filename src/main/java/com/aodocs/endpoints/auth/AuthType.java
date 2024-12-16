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
package com.aodocs.endpoints.auth;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import com.aodocs.endpoints.auth.authenticator.MicrosoftOAuth2Authenticator;
import com.google.api.server.spi.auth.GoogleJwtAuthenticator;
import com.google.api.server.spi.auth.GoogleOAuth2Authenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.response.ServiceUnavailableException;

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
    },
    /**
     * JWT authentication (no authorization, only authentication)
     */
    JWT {
        @Override
        public User authenticate(HttpServletRequest request) {
            return new GoogleJwtAuthenticator().authenticate(request);
        }

    },
    
    /**
     * Microsoft OAuth2 JWT authentication (no authorization, only authentication)
     */
    MS_OAUTH2 {
        @Override
        public User authenticate(HttpServletRequest request) {
            return new MicrosoftOAuth2Authenticator().authenticate(request);
        }
    };

    //TODO support ESP authenticator

    public abstract User authenticate(HttpServletRequest request) throws ServiceUnavailableException;

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
