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

    AuthType authType;
    String token;
    String userId;
    String email;
    Boolean verifiedEmail;
    String hd;
    String clientId;
    List<String> audience;
    ImmutableSet<String> scopes;
    Long expiresInSeconds;
    Object rawTokenInfo;

    public AuthInfo(String token, GoogleIdToken idToken) {
        this.authType = AuthType.JWT;
        this.token = token;
        GoogleIdToken.Payload payload = idToken.getPayload();
        this.userId = payload.getSubject();
        this.email = payload.getEmail();
        this.verifiedEmail = payload.getEmailVerified();
        this.hd = payload.getHostedDomain();
        this.clientId = payload.getAuthorizedParty();
        this.audience = payload.getAudienceAsList();
        this.scopes = null;
        this.expiresInSeconds = payload.getExpirationTimeSeconds();
        this.rawTokenInfo = idToken;
    }

    public AuthInfo(String token, GoogleAuth.TokenInfo tokenInfo) {
        this.authType = AuthType.OAUTH2;
        this.token = token;
        this.userId = tokenInfo.userId;
        this.email = tokenInfo.email;
        this.verifiedEmail = tokenInfo.verifiedEmail;
        this.hd = StringUtils.substringAfter(tokenInfo.email, "@");
        this.clientId = tokenInfo.clientId;
        this.audience = tokenInfo.audience == null ? null : Collections.singletonList(tokenInfo.audience);
        this.scopes = tokenInfo.scopes != null
                ? ImmutableSet.copyOf(Splitter.on(' ').split(tokenInfo.scopes)) : ImmutableSet.of();
        this.expiresInSeconds = tokenInfo.expiresIn == null ? 0L : tokenInfo.expiresIn;
        this.rawTokenInfo = tokenInfo;
    }
}
