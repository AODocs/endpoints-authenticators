/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 - 2024 AODocs (Altirnao Inc)
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
package com.aodocs.endpoints.auth.microsoft.verifier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import com.aodocs.endpoints.auth.microsoft.MicrosoftIdToken;
import com.aodocs.endpoints.auth.microsoft.SupportedVersion;
import com.aodocs.endpoints.auth.microsoft.jwks.PublicKeyProvider;
import com.aodocs.endpoints.auth.microsoft.util.UrlFormatter;
import com.google.api.client.json.JsonFactory;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ServiceUnavailableException;

public class MicrosoftTokenVerifier {
	
	public static final long DEFAULT_TIME_SKEW_SECONDS = 300;
	
	private final JsonFactory jsonFactory;
	private final MicrosoftOpenIDConfigurationDocumentProvider configurationProvider;
	private final PublicKeyProvider publicKeyProvider;
	
	public MicrosoftTokenVerifier(JsonFactory jsonFactory, MicrosoftOpenIDConfigurationDocumentProvider configurationProvider, PublicKeyProvider publicKeyProvider) {
		this.jsonFactory = jsonFactory;
		this.configurationProvider = configurationProvider;
		this.publicKeyProvider = publicKeyProvider;
	}
	
	public MicrosoftIdToken verify(String token) throws GeneralSecurityException, IOException, ServiceException {
		MicrosoftIdToken idToken = MicrosoftIdToken.parse(jsonFactory, token);
		return verify(idToken) ? idToken : null;
	}
	
	private boolean verify(MicrosoftIdToken idToken) throws ServiceException, GeneralSecurityException {
		String version = idToken.getPayload().getVersion();
		String tenantId = idToken.getPayload().getTenantId();
		MicrosoftOpenIdConfigurationDocument discoveryDocument = configurationProvider.getConfigurationDocument(SupportedVersion.of(version), tenantId);
		
		return verifyVersion(idToken) &&
				verifyIssuer(idToken, getExpectedIssuer(discoveryDocument, tenantId)) &&
				verifyTime(idToken) &&
				idToken.verifySignature(getPublicKey(discoveryDocument, idToken));
	}
	
	private boolean verifyIssuer(MicrosoftIdToken idToken, String issuer) {
		return issuer.equals(idToken.getPayload().getIssuer());
	}
	
	private boolean verifyVersion(MicrosoftIdToken idToken) {
		return SupportedVersion.isSupported(idToken.getPayload().getVersion());
	}
	
	private boolean verifyTime(MicrosoftIdToken idToken) {
		long currentTimeMillis = System.currentTimeMillis();
		return verifyExpirationTime(idToken, currentTimeMillis)
				&& verifyIssuedAtTime(idToken, currentTimeMillis);
	}
	
	public final boolean verifyExpirationTime(MicrosoftIdToken idToken, long currentTimeMillis) {
		return currentTimeMillis <= (idToken.getPayload().getExpirationTimeSeconds() + DEFAULT_TIME_SKEW_SECONDS) * 1000;
	}
	
	private boolean verifyIssuedAtTime(MicrosoftIdToken idToken, long currentTimeMillis) {
		return currentTimeMillis >= (idToken.getPayload().getIssuedAtTimeSeconds() - DEFAULT_TIME_SKEW_SECONDS) * 1000;
	}
	
	private PublicKey getPublicKey(MicrosoftOpenIdConfigurationDocument discoveryDocument, MicrosoftIdToken idToken) throws ServiceUnavailableException {
		return publicKeyProvider.getPublicKey(discoveryDocument.getJwks_uri(), idToken.getHeader());
	}
	
	private String getExpectedIssuer(MicrosoftOpenIdConfigurationDocument discoveryDocument, String tenantId) {
		return UrlFormatter.withTenantId(discoveryDocument.getIssuer(), tenantId);
	}
	
}
