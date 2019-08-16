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

import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.clientIds;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.currentProjectClientId;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.not;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.or;
import static com.aodocs.endpoints.auth.authenticator.AuthenticatorBuilder.projects;

import javax.servlet.http.HttpServletRequest;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authenticator.Authorizer;
import com.aodocs.endpoints.auth.authenticator.logic.ConjunctAuthenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.api.server.spi.config.model.ApiMethodConfig;

/**
 * Simple combined authenticator that provide the following behaviour:
 * - allows same project's client ids
 * - allows a static list of client ids in [projetId]/whitelist/clientIds.txt
 * - allows a static list of projects in [projetId]/whitelist/projects.txt
 * - allows a configurable list of client ids in gs://authconfig-[projetId]/whitelist/clientIds.txt
 * - allows a configurable list of projects in gs://authconfig-projetId]/whitelist/projects.txt
 * - denies a static list of client ids in [projetId]/blacklist/clientIds.txt
 * - denies a static list of projects in [projetId]/blacklist/projects.txt
 * - denies a configurable list of client ids in gs://authconfig-[projetId]/blacklist/clientIds.txt
 * - denies a configurable list of projects in gs://authconfig-projetId]/blacklist/projects.txt
 * <p>
 * A client ID must be in the allowed list AND NOT in the denied list.
 */
@Singleton
public class WhitelistBlacklistAuthenticator implements Authorizer {

    private final Authorizer delegate;
  
    public WhitelistBlacklistAuthenticator() {
        this(new CombinedStringListBuilder());
    }

    public WhitelistBlacklistAuthenticator(CombinedStringListBuilder slb) {
        delegate = new ConjunctAuthenticator(
                or(
                        currentProjectClientId(),
                        clientIds(slb.whitelist("clientIds")),
                        projects(slb.whitelist("projects"))
                ),
                not(or(
                        clientIds(slb.blacklist("clientIds")),
                        projects(slb.blacklist("projects"))
                ))
        );
    }
  
  @Override
  public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
    return delegate.isAuthorized(extendedUser, apiMethodConfig, request);
  }
}
