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
package com.aodocs.endpoints.auth.microsoft;

import java.io.IOException;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.util.Key;

public class MicrosoftIdToken extends JsonWebSignature {
	
	/**
	 * @param header             header
	 * @param payload            payload
	 * @param signatureBytes     bytes of the signature
	 * @param signedContentBytes bytes of the signed content
	 */
	public MicrosoftIdToken(Header header, Payload payload, byte[] signatureBytes, byte[] signedContentBytes) {
		super(header, payload, signatureBytes, signedContentBytes);
	}
	
	/**
	 * Parses the given token string and returns the parsed {@link MicrosoftIdToken}.
	 *
	 * @param jsonFactory JSON factory
	 * @param tokenString token string
	 * @return parsed Microsoft OAuth2 ID token
	 */
	public static MicrosoftIdToken parse(JsonFactory jsonFactory, String tokenString)
			throws IOException {
		JsonWebSignature jws =
				JsonWebSignature.parser(jsonFactory).setPayloadClass(MicrosoftIdToken.Payload.class).parse(tokenString);
		return new MicrosoftIdToken(
				jws.getHeader(),
				(MicrosoftIdToken.Payload) jws.getPayload(),
				jws.getSignatureBytes(),
				jws.getSignedContentBytes());
	}
	
	@Override
	public MicrosoftIdToken.Payload getPayload() {
		return (MicrosoftIdToken.Payload) super.getPayload();
	}
	
	/**
	 * List of Microsoft ID token payload claims.
	 * Unused/irrelevant claims are omitted.
	 * For a complete list of claims, see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/id-token-claims-reference">ID token claims reference</a>
	 */
	public static class Payload extends JsonWebToken.Payload {
		
		/**
		 * Present by default for guest accounts that have an email address.
		 * The email claim can be requested for managed users (from the same tenant as the resource) using the email optional claim.
		 * On the v2.0 endpoint, your app can also request the email OpenID Connect scope to receive this claim, see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/scopes-oidc#the-email-scope">Email scope</a>
		 */
		@Key("email")
		private String email;
		
		/**
		 * The name claim provides a human-readable value that identifies the subject of the token.
		 * The profile scope is required to receive this claim.
		 */
		@Key("name")
		private String name;
		
		/**
		 * The primary username that represents the user. It could be an email address, phone number, or a generic username without a specified format.
		 * Present only in v2.0 tokens.
		 */
		@Key("preferred_username")
		private String preferred_username;
		
		/**
		 * The immutable identifier for the requestor, which is the verified identity of the user or service principal.
		 * This ID uniquely identifies the requestor across applications.
		 * Two different applications signing in the same user receive the same value in the oid claim. The oid can be used when making queries to Microsoft online services, such as the Microsoft Graph.
		 * The Microsoft Graph returns this ID as the id property for a given user account. Because the oid allows multiple applications to correlate principals, to receive this claim for users use the profile scope.
		 * If a single user exists in multiple tenants, the user contains a different object ID in each tenant. Even though the user logs into each account with the same credentials, the accounts are different.
		 * <p>
		 * To receive this claim, the application must request the profile scope.
		 */
		@Key("oid")
		private String objectId;
		
		/**
		 * Represents the tenant that the user is signing in to.
		 * For work and school accounts, the GUID is the immutable tenant ID of the organization that the user is signing in to.
		 * For sign-ins to the personal Microsoft account tenant (services like Xbox, Teams for Life, or Outlook), the value is 9188040d-6c67-4c5b-b112-36a304b66dad.
		 */
		@Key("tid")
		private String tenantId;
		
		/**
		 * Indicates the version of the access token.
		 * Either "1.0" or "2.0"
		 */
		@Key("ver")
		private String version;
		
		public String getEmail() {
			return email;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getPreferred_username() {
			return preferred_username;
		}
		
		public void setPreferred_username(String preferred_username) {
			this.preferred_username = preferred_username;
		}
		
		public String getObjectId() {
			return objectId;
		}
		
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}
		
		public String getTenantId() {
			return tenantId;
		}
		
		public void setTenantId(String tenantId) {
			this.tenantId = tenantId;
		}
		
		public String getVersion() {
			return version;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
	}
}
