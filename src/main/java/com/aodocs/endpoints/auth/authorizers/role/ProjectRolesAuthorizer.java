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
package com.aodocs.endpoints.auth.authorizers.role;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.AbstractAuthorizer;
import com.aodocs.endpoints.context.ProjectConfigProvider;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.collect.ImmutableSet;

/**
 * Only authorizes project members.
 * Must be subclassed to implement authorized roles / role combination.
 */
public abstract class ProjectRolesAuthorizer extends AbstractAuthorizer {

    @Override
    public final AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig methodConfig, HttpServletRequest request) {
        ImmutableSet<String> userRoles =
                ProjectConfigProvider.get().getCachedProjectConfig().getRolesFor(extendedUser.getEmail());
        return new AuthorizationResult(authorizeRoles(Optional.ofNullable(userRoles).orElse(ImmutableSet.of())));
    }

    protected abstract boolean authorizeRoles(@Nonnull ImmutableSet<String> userRoles);

}
