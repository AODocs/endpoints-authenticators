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
package com.aodocs.endpoints.auth.authenticator.clientid;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authenticator.ExtendedAuthenticator;
import com.aodocs.endpoints.context.ProjectConfigProvider;
import com.aodocs.endpoints.storage.StringListSupplier;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
 * This authenticator allows any token issued by the provided list of project numbers (digit-only id).
 * Does not support service accounts !! Only web client ids from other projects can be identified.
 */
public class ProjectsAuthenticator extends ExtendedAuthenticator {

    private final StringListSupplier projectNumberSupplier;

    public ProjectsAuthenticator(@NonNull StringListSupplier projectNumberSupplier) {
        this.projectNumberSupplier = projectNumberSupplier;
    }

    public AuthorizationResult isAuthorized(final ExtendedUser extendedUser, ApiMethodConfig methodConfig, HttpServletRequest request) {
        String projectNumber = ProjectConfigProvider.extractProjectNumber(extendedUser.getAuthInfo().getClientId());
        return new AuthorizationResult(projectNumber != null && projectNumberSupplier.get().contains(projectNumber));
    }

}
