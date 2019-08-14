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
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 *  Common data for access and id tokens
 */
@Value
@Builder
@AllArgsConstructor
public class AuthInfo {

    private AuthType authType;
    private String userId;
    private String email;
    private Boolean verifiedEmail;
    private String hd;
    private String clientId;
    private List<String> audience;
    private ImmutableSet<String> scopes;
    private Long expiresInSeconds;

    public AuthInfo(GoogleIdToken tokenInfo) {
        this.authType = AuthType.JWT;
        this.userId = tokenInfo.getPayload().getSubject();
        this.email = tokenInfo.getPayload().getEmail();
        this.verifiedEmail = tokenInfo.getPayload().getEmailVerified();
        this.hd = tokenInfo.getPayload().getHostedDomain();
        this.clientId = tokenInfo.getPayload().getIssuer();
        this.audience = tokenInfo.getPayload().getAudienceAsList();
        this.scopes = null;
        this.expiresInSeconds = tokenInfo.getPayload().getExpirationTimeSeconds();
    }

    public AuthInfo(GoogleAuth.TokenInfo tokenInfo) {
        this.authType = AuthType.OAUTH2;
        this.userId = tokenInfo.userId;
        this.email = tokenInfo.email;
        this.verifiedEmail = tokenInfo.verifiedEmail;
        this.hd = StringUtils.substringAfter(tokenInfo.email, "@");
        this.clientId = tokenInfo.clientId;
        this.audience = tokenInfo.audience == null ? null : Collections.singletonList(tokenInfo.audience);
        this.scopes = tokenInfo.scopes != null
                ? ImmutableSet.copyOf(Splitter.on(' ').split(tokenInfo.scopes)) : ImmutableSet.of();
        this.expiresInSeconds = tokenInfo.expiresIn == null ? 0L : tokenInfo.expiresIn;
    }
}
