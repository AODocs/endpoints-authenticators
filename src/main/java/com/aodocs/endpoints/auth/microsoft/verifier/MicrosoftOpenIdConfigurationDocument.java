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

import com.google.api.client.util.Key;

/**
 * Represents the OpenID discovery document for Microsoft.
 * Unused fields are omitted.
 */
public class MicrosoftOpenIdConfigurationDocument {
	
	@Key("issuer")
	private String issuer;
	
	@Key("userinfo_endpoint")
	private String userinfo_endpoint;
	
	@Key("jwks_uri")
	private String jwks_uri;
	
	public String getIssuer() {
		return issuer;
	}
	
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	
	public String getUserinfo_endpoint() {
		return userinfo_endpoint;
	}
	
	public void setUserinfo_endpoint(String userinfo_endpoint) {
		this.userinfo_endpoint = userinfo_endpoint;
	}
	
	public String getJwks_uri() {
		return jwks_uri;
	}
	
	public void setJwks_uri(String jwks_uri) {
		this.jwks_uri = jwks_uri;
	}
	
}
