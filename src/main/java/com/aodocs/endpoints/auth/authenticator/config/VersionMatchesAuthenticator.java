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
package com.aodocs.endpoints.auth.authenticator.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API version must match a regex
 */
public final class VersionMatchesAuthenticator extends VersionAuthenticator {

    @JsonProperty
    private final String versionMatches;

    public VersionMatchesAuthenticator(@JsonProperty("versionMatches") String versionMatches) {
        this.versionMatches = versionMatches;
    }

    @Override
    protected boolean isAuthorized(String version) {
        return version.matches(versionMatches);
    }
}
