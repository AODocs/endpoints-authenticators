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
/**
 * Provides various authenticators for various use cases:
 * - AllowedClientIdsAuthenticator: allows or denies a list of client ids
 * - AllowedProjectsAuthenticator: allows all web client ids from a list of project
 * - ProjectClientIdsAuthenticator: allows all client ids (including service accounts)
 * - Conjunct/Disjunct/NegateAuthenticator: combines multiple authenticators with and / or / not logic
 * - Path/VerbBasedAuthenticator: applies a different authenticator based on the API rest path or HTTP verb
 * - ProjectRolesAuthenticator and Project[Owner/Member]OnlyAuthenticator: authorizes users based on their role in the project
 * - Jwt/Oauth2OnlyAuthenticator: restricts the type of allowed authentication
 *
 * WARNING! Using any of these authenticators disables the build-in client ids checking.
 * It still enforces the scope requirements when using OAuth2.
 */
package com.aodocs.endpoints.auth.authenticator;
