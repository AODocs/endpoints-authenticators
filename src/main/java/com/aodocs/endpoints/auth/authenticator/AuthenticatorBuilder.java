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

import com.aodocs.endpoints.auth.authenticator.clientid.ClientIdsAuthenticator;
import com.aodocs.endpoints.auth.authenticator.clientid.CurrentProjectClientIdAuthenticator;
import com.aodocs.endpoints.auth.authenticator.clientid.ProjectsAuthenticator;
import com.aodocs.endpoints.auth.authenticator.config.VersionContainsAuthenticator;
import com.aodocs.endpoints.auth.authenticator.config.VersionMatchesAuthenticator;
import com.aodocs.endpoints.auth.authenticator.logic.ConjunctAuthenticator;
import com.aodocs.endpoints.auth.authenticator.logic.DisjunctAuthenticator;
import com.aodocs.endpoints.auth.authenticator.logic.NegateAuthenticator;
import com.aodocs.endpoints.auth.authenticator.request.*;
import com.aodocs.endpoints.auth.authenticator.role.ProjectMemberAuthenticator;
import com.aodocs.endpoints.auth.authenticator.role.ProjectOwnerAuthenticator;
import com.aodocs.endpoints.auth.authenticator.role.ProjectRolesAuthenticator;
import com.aodocs.endpoints.auth.authenticator.token.JwtOnlyAuthenticator;
import com.aodocs.endpoints.auth.authenticator.token.OAuth2OnlyAuthenticator;
import com.aodocs.endpoints.storage.StringListSupplier;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Utility methods to build complex authenticators.
 */
public class AuthenticatorBuilder {

    /**
     * Checks all authenticators are authorized.
     *
     * @param authenticators
     * @return
     */
    public static ConjunctAuthenticator and(ExtendedAuthenticator... authenticators) {
        return new ConjunctAuthenticator(authenticators);
    }

    /**
     * Checks any authenticator is authorized.
     *
     * @param authenticators
     * @return
     */
    public static DisjunctAuthenticator or(ExtendedAuthenticator... authenticators) {
        return new DisjunctAuthenticator(authenticators);
    }

    /**
     * Checks authenticator is not authorized.
     *
     * @param authenticator
     * @return
     */
    public static NegateAuthenticator not(ExtendedAuthenticator authenticator) {
        return new NegateAuthenticator(authenticator);
    }

    /**
     * Checks if the client id used for authentication is from current project.
     *
     * @return
     */
    public static CurrentProjectClientIdAuthenticator currentProjectClientId() {
        return currentProjectClientId;
    }
    private final static CurrentProjectClientIdAuthenticator currentProjectClientId = new CurrentProjectClientIdAuthenticator();

    /**
     * Checks if the client id used for authentication is in the provided list.
     *
     * @return
     */
    public static ClientIdsAuthenticator clientIds(StringListSupplier clientIdSupplier) {
        return new ClientIdsAuthenticator(clientIdSupplier);
    }

    /**
     * Checks if the client id used for authentication is from any of the supplied projects.
     * It can't check service account client ids.
     *
     * @return
     */
    public static ProjectsAuthenticator projects(StringListSupplier projectNumbersSupplier) {
        return new ProjectsAuthenticator(projectNumbersSupplier);
    }

    /**
     * Checks if API version contains a subtring.
     *
     * @param mustContain
     * @return
     */
    public static VersionContainsAuthenticator versionContains(String mustContain) {
        return new VersionContainsAuthenticator(mustContain);
    }

    /**
     * Checks if API version matches a regular expression
     *
     * @param mustMatch
     * @return
     */
    public static VersionMatchesAuthenticator versionMatches(String mustMatch) {
        return new VersionMatchesAuthenticator(mustMatch);
    }

    /**
     * Checks if a query parameter is present and with a value
     * in the provided list.
     *
     * @param paramName
     * @param values
     * @return
     */
    public static QueryParameterValueAuthenticator requiredQueryParamValue(String paramName, StringListSupplier values) {
        return queryParamValue(paramName, false, values);
    }

    /**
     * Checks if a query parameter is absent, or present with a value
     * in the provided list.
     *
     * @param paramName
     * @param values
     * @return
     */
    public static QueryParameterValueAuthenticator optionalQueryParamValue(String paramName, StringListSupplier values) {
        return queryParamValue(paramName, true, values);
    }

    private static QueryParameterValueAuthenticator queryParamValue(String paramName,
                                                              boolean allowIfAbsent,
                                                              StringListSupplier values) {
        return new QueryParameterValueAuthenticator(paramName, allowIfAbsent, values);
    }

    /**
     * Checks if a query parameter is present.
     *
     * @param paramName
     * @return
     */
    public static QueryParameterAuthenticator requiredQueryParam(String paramName) {
        return new QueryParameterAuthenticator(paramName);
    }

    /**
     * Checks if a request has the provided prefix.
     *
     * @param prefix
     * @return
     */
    public static PathPrefixAuthenticator pathPrefix(String prefix) {
        return new PathPrefixAuthenticator(prefix);
    }

    /**
     * Checks if a request has the provided servlet path.
     * Useful when an API is mapped on multiple paths.
     *
     * @param path
     * @return
     */
    public static ServletPathAuthenticator servletPath(String path) {
        return new ServletPathAuthenticator(path);
    }

    /**
     * Checks if the request has the required Http method.
     * Useful to implement "read-only" API access for example.
     *
     * @param method
     * @return
     */
    public static HttpMethodAuthenticator httpMethod(HttpMethodAuthenticator.HttpMethod method) {
        return new HttpMethodAuthenticator(method);
    }

    /**
     * Checks if the user has at least a role on the current project (i.e. is a "member")
     *
     * @return
     */
    public static ProjectMemberAuthenticator isProjectMember() {
        return isProjectMember;
    }
    private final static ProjectMemberAuthenticator isProjectMember = new ProjectMemberAuthenticator();


    /**
     * Checks if the user is an owner of the current project
     *
     * @return
     */
    public static ProjectOwnerAuthenticator isProjectOwner() {
        return isProjectOwner;
    }
    private final static ProjectOwnerAuthenticator isProjectOwner = new ProjectOwnerAuthenticator();

    /**
     * Checks if the user has the required roles on the current project.
     *
     * @param roles
     * @return
     */
    public static ProjectRolesAuthenticator hasRolesInProject(@NonNull String ... roles) {
        return new ProjectRolesAuthenticator() {
            @Override
            protected boolean authorizeRoles(@Nonnull ImmutableSet<String> userRoles) {
                return userRoles.containsAll(Arrays.asList(roles));
            }
        };
    }

    /**
     * Accepts only JWT authentication.
     *
     * @return
     */
    public static JwtOnlyAuthenticator jwt() {
        return jwt;
    }
    private final static JwtOnlyAuthenticator jwt = new JwtOnlyAuthenticator();

    /**
     * Accepts only OAuth2 authentication.
     *
     * @return
     */
    public static OAuth2OnlyAuthenticator oauth2() {
        return oauth2;
    }
    private final static OAuth2OnlyAuthenticator oauth2 = new OAuth2OnlyAuthenticator();

    /**
     * Alias of jwt()
     *
     * @return
     */
    public static JwtOnlyAuthenticator idToken() {
        return jwt();
    }

    /**
     * Alias of oauth2()
     *
     * @return
     */
    public static OAuth2OnlyAuthenticator accessToken() {
        return oauth2();
    }

}
