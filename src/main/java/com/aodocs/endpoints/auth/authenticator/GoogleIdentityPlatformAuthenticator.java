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
package com.aodocs.endpoints.auth.authenticator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.java.Log;

import com.aodocs.endpoints.context.AppengineHelper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.util.Clock;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.api.server.spi.request.Attribute;
import com.google.common.annotations.VisibleForTesting;

/**
 * This Authenticator validates the JWT tokens issued by the Google Identity Platform (aka Firebase Auth).
 */
/*
 * Implementation note: this class should have been a subclass of GoogleJwtAuthenticator as most of the code is
 * identical, but it's not possible to disable the audience check in GoogleJwtAuthenticator, which can't work
 * with these tokens, as there is no "azp" claim in the Identity Platform tokens.
 */
@Log
@Singleton
public class GoogleIdentityPlatformAuthenticator implements Authenticator {
	
	/**
	 * See Firebase documentation for more information.
	 * https://firebase.google.com/docs/auth/admin/verify-id-tokens#verify_id_tokens_using_a_third-party_jwt_library
	 */
	public static final String IDENTITY_PLATFORM_PUBLIC_CERTIFICATES 
			= "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";
	
	/**
	 * Prefix for "iss" claim in Identity Platform JWT tokens.
	 * The full issuers is https://securetoken.google.com/${PROJECT_ID}
	 * When using the empty constructor, this should be checked at a later point in the authentication process.
	 */
	public static final String ISSUER_PREFIX = "https://securetoken.google.com/";
	
	//this needs to be static to benefit from caching
	private static final GooglePublicKeysManager CLOUD_IDENTITY_KEYS = new GooglePublicKeysManager.Builder(
			Client.getInstance().getHttpTransport(), Client.getInstance().getJsonFactory())
			.setPublicCertsEncodedUrl(IDENTITY_PLATFORM_PUBLIC_CERTIFICATES)
			.build();
	
	/**
	 * 
	 * @return an authenticator that accepts only Identity Platform JWT tokens from the current project
	 */
	public static GoogleIdentityPlatformAuthenticator currentProject() {
		return new GoogleIdentityPlatformAuthenticator(AppengineHelper.getApplicationId());
	}
	
	private final GoogleIdTokenVerifier verifier;
	
	/**
	 * Creates an instance that only accepts Identity Platform tokens for the provided project IDs.
	 * 
	 * WARNING! If projectIds is empty, this instance will allow ALL Identity Platform JWT token, regardless of the issuing project.
	 * You should always validate the project id by checking the following claims:
	 * - iss: should be "https://securetoken.google.com/${PROJECT_ID}" ({@see ISSUER_PREFIX}
	 * - aud: should be exactly "${PROJECT_ID}}
	 * The JWT claim validation can be done using {@see com.aodocs.endpoints.auth.authorizers.audience.CurrentProjectAudienceAuthorizer} or
	 * {@see com.aodocs.endpoints.auth.authorizers.token.JwtClaimsAuthorizer}.
	 * 
	 * @param projectIds the project IDs to check tokens for
	 */
	public GoogleIdentityPlatformAuthenticator(String ... projectIds) {
		this(Clock.SYSTEM, projectIds);
	}
	
	@VisibleForTesting
	GoogleIdentityPlatformAuthenticator(Clock clock, String ... projectIds) {
		this.verifier = new GoogleIdTokenVerifier.Builder(CLOUD_IDENTITY_KEYS)
				.setIssuers(projectIds.length == 0 ? null : Stream.of(projectIds).map(projectId -> ISSUER_PREFIX + projectId).collect(Collectors.toList()))
				.setAudience(projectIds.length == 0 ? null : Arrays.asList(projectIds))
				.setClock(clock)
				.build();
	}
	
	//This method is a partial copy of GoogleJwtAuthenticator#authenticate
	@Override
	public User authenticate(HttpServletRequest request) {
		Attribute attr = Attribute.from(request);
		if (attr.isEnabled(Attribute.SKIP_TOKEN_AUTH)) {
			return null;
		}
		
		String token = GoogleAuth.getAuthToken(request);
		if (!GoogleAuth.isJwt(token)) {
			return null;
		}
		
		GoogleIdToken idToken = verifyToken(token);
		if (idToken == null) {
			return null;
		}
		
		attr.set(Attribute.ID_TOKEN, idToken);
		
		//The parts from GoogleJwtAuthenticator#authenticate checking client id and audience are removed
		
		String userId = idToken.getPayload().getSubject();
		String email = idToken.getPayload().getEmail();
		User user = (userId == null && email == null) ? null : new User(userId, email);
		if (attr.isEnabled(Attribute.REQUIRE_APPENGINE_USER)) {
			com.google.appengine.api.users.User appEngineUser =
					(email == null) ? null : new com.google.appengine.api.users.User(email, "");
			attr.set(Attribute.AUTHENTICATED_APPENGINE_USER, appEngineUser);
		}
		return user;
	}
	
	//This method is a partial copy of GoogleJwtAuthenticator#verifyToken
	@VisibleForTesting
	GoogleIdToken verifyToken(String token) {
		if (token == null) {
			return null;
		}
		try {
			return verifier.verify(token);
		} catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
			log.log(Level.WARNING, "error while verifying JWT", e);
			return null;
		}
	}
}
