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

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;

public final class DisjunctAuthenticator implements Authenticator {
    private final ImmutableList<Authenticator> authenticators;

    public DisjunctAuthenticator(@NonNull Authenticator delegate, Authenticator... delegates) {
        ImmutableList.Builder<Authenticator> authenticatorBuilder = new ImmutableList.Builder<>();
        authenticatorBuilder.add(delegate);
        if (delegates != null) {
            authenticatorBuilder.add(delegates);
        }
        authenticators = authenticatorBuilder.build();
    }

    @Override
    public User authenticate(HttpServletRequest httpServletRequest) throws ServiceException {
        ServiceException latestException = null;
        for (Authenticator authenticator : authenticators) {
            try {
                User user = authenticator.authenticate(httpServletRequest);
                if (user != null) {
                    return user;
                }
            } catch (ServiceException e) {
                latestException = e;
            }
        }

        if (latestException != null) {
            throw latestException;
        }

        return null;
    }
}
