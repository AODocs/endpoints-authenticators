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
package com.aodocs.endpoints.auth.authorizers.clientid;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.NonNull;

import com.aodocs.endpoints.auth.AuthInfo;
import com.aodocs.endpoints.auth.AuthType;
import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.AbstractAuthorizer;
import com.aodocs.endpoints.storage.StringListSupplier;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.common.flogger.FluentLogger;

/**
 * This authenticator allows any token issued by a client id in the provided list.
 * In case of a Microsoft Oauth2 ID token, the audience (Entra ID application ID that the token was issued for) is checked instead of the client id.
 */
public final class ClientIdsAuthorizer extends AbstractAuthorizer {
	
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	
	@JsonProperty("clientIds")
	@Getter
	private final StringListSupplier clientIdSupplier;
	
	@JsonCreator
	public ClientIdsAuthorizer(@NonNull StringListSupplier clientIdSupplier) {
		this.clientIdSupplier = clientIdSupplier;
	}
	
	public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig methodConfig, HttpServletRequest request) {
		AuthInfo authInfo = extendedUser.getAuthInfo();
		List<String> allowedClientIds = clientIdSupplier.get();
		
		if (AuthType.MS_OAUTH2 == authInfo.getAuthType()) {
			List<String> audience = authInfo.getAudience();
			if (audience == null || audience.isEmpty()) {
				return AuthorizationResult.notAuthorized();
			}
			
			logger.atFine().log("Class=%s, Audience=%s, Allowed=%s", getClass(), audience.get(0), allowedClientIds);
			return new AuthorizationResult(clientIdSupplier.get().contains(audience.get(0)));
		} else {
			String clientId = authInfo.getClientId();
			if (clientId == null) {
				return AuthorizationResult.notAuthorized();
			}
			
			logger.atFine().log("Class=%s, ClientId=%s, Allowed=%s", getClass(), clientId, allowedClientIds);
			return new AuthorizationResult(clientIdSupplier.get().contains(clientId));
		}
	}
}
