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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.java.Log;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.microsoft.MicrosoftIdToken;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

/**
 *  Common data for access and id tokens
 */
@Value
@Builder
@AllArgsConstructor
@Log
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
    Claims claims;
    
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public final static class Claims {
        private JsonNode payload;
    
        /**
         * Extract the given claim from the token.
         *
         * This method extract claims that have primitive value only (boolean, numerical and string). For any other
         * types (object, array, binary) this method returns empty.
         *
         * @param key A jsonp key pointing the claim to extract (for example {@code \iss} to query the issuer, or {code \firebase\tenant} to query
         *            the tenant id for a firebase token.
         * @return {@code null} if the key points to a non-primitive object ; {@link Optional#empty()} If the claim is not present in the token or present with null value.
         */
        public Optional<Object> get(String key) {
            //make the key a valid json pointer
            if (!key.startsWith("/")) {
                key = "/" + key;
            }
            JsonNode node = payload.at(key);
            JsonNodeType nodeType = node.getNodeType();
            switch (nodeType) {
                case BOOLEAN:
                    return Optional.of(node.asBoolean());
                case NUMBER:
                    return Optional.of(node.asLong());
                case STRING:
                    return Optional.of(node.asText());
                case NULL:
                case MISSING:
                    return Optional.empty();
                case OBJECT:
                case POJO:
                case ARRAY:
                case BINARY:
                    log.log(Level.WARNING,
                            "Please extract primitive type claim only. Node at ''{0}'' is of type=''{1}'' with the content ''{2}''",
                            new Object[] {key, nodeType, node});
            }
            return null;
        }
    }
    
    @SneakyThrows(JsonProcessingException.class)
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
        this.claims = new Claims(new ObjectMapper().readTree(payload.toString()));
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
        this.claims = new Claims(new ObjectMapper().createObjectNode());
    }
    
    @SneakyThrows(JsonProcessingException.class)
    public AuthInfo(String token, MicrosoftIdToken microsoftIdToken) {
        this.authType = AuthType.MS_OAUTH2;
        this.token = token;
        MicrosoftIdToken.Payload payload = microsoftIdToken.getPayload();
        this.userId = payload.getObjectId();
        this.email = payload.getEmail();
        this.verifiedEmail = true;
        this.hd = null;
        this.clientId = null;
        this.audience = payload.getAudienceAsList();
        this.scopes = null;
        this.expiresInSeconds = payload.getExpirationTimeSeconds();
        this.rawTokenInfo = microsoftIdToken;
        this.claims = new Claims(new ObjectMapper().readTree(payload.toString()));
    }

}
