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
package com.aodocs.endpoints.auth.authorizers;

import java.util.Arrays;

import javax.annotation.Nonnull;

import lombok.NonNull;

import com.aodocs.endpoints.auth.authorizers.audience.EntraIdApplicationAuthorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.ClientIdsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.CurrentProjectClientIdAuthorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.ProjectsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.config.VersionContainsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.config.VersionMatchesAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.ConjunctAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.DisjunctAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.NegateAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.HttpMethodAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.PathPrefixAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.QueryParameterAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.QueryParameterValueAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.ServletPathAuthorizer;
import com.aodocs.endpoints.auth.authorizers.role.ProjectMemberAuthorizer;
import com.aodocs.endpoints.auth.authorizers.role.ProjectOwnerAuthorizer;
import com.aodocs.endpoints.auth.authorizers.role.ProjectRolesAuthorizer;
import com.aodocs.endpoints.auth.authorizers.token.JwtOnlyAuthorizer;
import com.aodocs.endpoints.auth.authorizers.token.OAuth2OnlyAuthorizer;
import com.aodocs.endpoints.storage.StringListSupplier;
import com.google.common.collect.ImmutableSet;

/**
 * Utility methods to build complex Authorizers.
 */
public class AuthorizerBuilder {

    /**
     * Checks all authorizers pass.
     */
    public static ConjunctAuthorizer and(Authorizer... Authorizers) {
        return new ConjunctAuthorizer(Authorizers);
    }

    /**
     * Checks any authorizer pass.
     */
    public static DisjunctAuthorizer or(Authorizer... Authorizers) {
        return new DisjunctAuthorizer(Authorizers);
    }

    /**
     * Checks Authorizer is not authorized.
     */
    public static NegateAuthorizer not(Authorizer Authorizer) {
        return new NegateAuthorizer(Authorizer);
    }

    /**
     * Checks if the client id used for authentication is from current project.
     */
    public static CurrentProjectClientIdAuthorizer currentProjectClientId() {
        return currentProjectClientId;
    }
    private final static CurrentProjectClientIdAuthorizer currentProjectClientId = new CurrentProjectClientIdAuthorizer();

    /**
     * Checks if the client id used for authentication is in the provided list.
     */
    public static ClientIdsAuthorizer clientIds(StringListSupplier clientIdSupplier) {
        return new ClientIdsAuthorizer(clientIdSupplier);
    }
    
    /**
     * Checks if the Entra ID application ID used for authentication is in the provided list.
     */
    public static EntraIdApplicationAuthorizer entraIdAppIds(StringListSupplier appIdSupplier) {
        return new EntraIdApplicationAuthorizer(appIdSupplier);
    }

    /**
     * Checks if the client id used for authentication is from any of the supplied projects.
     * It can't check service account client ids.
     */
    public static ProjectsAuthorizer projects(StringListSupplier projectNumbersSupplier) {
        return new ProjectsAuthorizer(projectNumbersSupplier);
    }

    /**
     * Checks if API version contains a subtring.
     */
    public static VersionContainsAuthorizer versionContains(String mustContain) {
        return new VersionContainsAuthorizer(mustContain);
    }

    /**
     * Checks if API version matches a regular expression
     *
     * @param mustMatch
     * @return
     */
    public static VersionMatchesAuthorizer versionMatches(String mustMatch) {
        return new VersionMatchesAuthorizer(mustMatch);
    }

    /**
     * Checks if a query parameter is present and with a value
     * in the provided list.
     *
     * @param paramName
     * @param values
     * @return
     */
    public static QueryParameterValueAuthorizer requiredQueryParamValue(String paramName, StringListSupplier values) {
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
    public static QueryParameterValueAuthorizer optionalQueryParamValue(String paramName, StringListSupplier values) {
        return queryParamValue(paramName, true, values);
    }

    private static QueryParameterValueAuthorizer queryParamValue(String paramName,
                                                              boolean allowIfAbsent,
                                                              StringListSupplier values) {
        return new QueryParameterValueAuthorizer(paramName, allowIfAbsent, values);
    }

    /**
     * Checks if a query parameter is present.
     *
     * @param paramName
     * @return
     */
    public static QueryParameterAuthorizer requiredQueryParam(String paramName) {
        return new QueryParameterAuthorizer(paramName);
    }

    /**
     * Checks if a request has the provided prefix.
     *
     * @param prefix
     * @return
     */
    public static PathPrefixAuthorizer pathPrefix(String prefix) {
        return new PathPrefixAuthorizer(prefix);
    }

    /**
     * Checks if a request has the provided servlet path.
     * Useful when an API is mapped on multiple paths.
     *
     * @param path
     * @return
     */
    public static ServletPathAuthorizer servletPath(String path) {
        return new ServletPathAuthorizer(path);
    }

    /**
     * Checks if the request has the required Http method.
     * Useful to implement "read-only" API access for example.
     *
     * @param method
     * @return
     */
    public static HttpMethodAuthorizer httpMethod(HttpMethodAuthorizer.HttpMethod method) {
        return new HttpMethodAuthorizer(method);
    }

    /**
     * Checks if the user has at least a role on the current project (i.e. is a "member")
     *
     * @return
     */
    public static ProjectMemberAuthorizer isProjectMember() {
        return isProjectMember;
    }
    private final static ProjectMemberAuthorizer isProjectMember = new ProjectMemberAuthorizer();


    /**
     * Checks if the user is an owner of the current project
     *
     * @return
     */
    public static ProjectOwnerAuthorizer isProjectOwner() {
        return isProjectOwner;
    }
    private final static ProjectOwnerAuthorizer isProjectOwner = new ProjectOwnerAuthorizer();

    /**
     * Checks if the user has the required roles on the current project.
     *
     * @param roles
     * @return
     */
    public static ProjectRolesAuthorizer hasRolesInProject(@NonNull String ... roles) {
        return new ProjectRolesAuthorizer() {
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
    public static JwtOnlyAuthorizer jwt() {
        return jwt;
    }
    private final static JwtOnlyAuthorizer jwt = new JwtOnlyAuthorizer();

    /**
     * Accepts only OAuth2 authentication.
     *
     * @return
     */
    public static OAuth2OnlyAuthorizer oauth2() {
        return oauth2;
    }
    private final static OAuth2OnlyAuthorizer oauth2 = new OAuth2OnlyAuthorizer();

    /**
     * Alias of jwt()
     *
     * @return
     */
    public static JwtOnlyAuthorizer idToken() {
        return jwt();
    }

    /**
     * Alias of oauth2()
     *
     * @return
     */
    public static OAuth2OnlyAuthorizer accessToken() {
        return oauth2();
    }

}
