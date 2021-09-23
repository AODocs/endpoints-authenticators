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
package com.aodocs.endpoints.auth.authorizers.token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.aodocs.endpoints.auth.authorizers.dsl.DslAuthorizerFactory;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.server.spi.Client;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

public class JwtClaimsAuthorizerTest {
	
	private final static Map<String, Object> EXPECTED_CLAIMS = ImmutableMap.of("iss", "123", "exp", 12345, "email_verified", true);
	
	@Test
	public void testLoadAllClaims() throws IOException {
		JwtClaimsAuthorizer allClaims = (JwtClaimsAuthorizer) loadAuthorizer("allClaims");
		assertEquals(EXPECTED_CLAIMS, allClaims.getClaims());
	}
	
	@Test
	public void testLoadAnyClaim() throws IOException {
		JwtClaimsAuthorizer anyClaim = (JwtClaimsAuthorizer) loadAuthorizer("anyClaim");
		assertEquals(EXPECTED_CLAIMS, anyClaim.getClaims());
	}
	
	@Test
	public void testValidAll() throws IOException {
		JwtClaimsAuthorizer allClaims = new AllJwtClaimsAuthorizer(ImmutableMap.of(
				"aud", "ao-docs-staging", //valid
				"email_verified", false, //valid
				"exp", 1633700106L //valid
				));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertTrue(valid);
	}
	
	@Test
	public void testValidAny() throws IOException {
		JwtClaimsAuthorizer allClaims = new AnyJwtClaimAuthorizer(ImmutableMap.of(
				"aud", "ao-docs-staging", //valid
				"email_verified", true, //invalid
				"exp", "azfefzefzrgf" //invalid
		));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertTrue(valid);
	}
	
	@Test
	public void testInvalidAll() throws IOException {
		JwtClaimsAuthorizer allClaims = new AllJwtClaimsAuthorizer(ImmutableMap.of(
				"aud", "ao-docs-staging", //valid
				"email_verified", true, //invalid
				"exp", "azfefzefzrgf" //invalid
		));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertFalse(valid);
	}
	
	@Test
	public void testInvalidAny() throws IOException {
		JwtClaimsAuthorizer allClaims = new AllJwtClaimsAuthorizer(ImmutableMap.of(
				"aud", "ao-docs", //invalid
				"email_verified", true, //invalid
				"exp", "azfefzefzrgf" //invalid
		));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertFalse(valid);
	}
	
	@Test
	public void testNested() throws IOException {
		JwtClaimsAuthorizer allClaims = new AllJwtClaimsAuthorizer(ImmutableMap.of(
				"firebase/sign_in_provider", "password", //valid
				"firebase/identities/email/0", "vitob14022@tst999.com", //valid
				"email_verified", false //valid
		));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertTrue(valid);
	}
	
	@Test
	public void testUnsupportedArray() throws IOException {
		assertFalse( new AllJwtClaimsAuthorizer(ImmutableMap.of(
				"firebase/identities/email", "vitob14022@tst999.com" //valid
		)).checkJwtClaims(loadPayload()));
	}
	
	@Test
	public void testUnsupportedObject() throws IOException {
		assertFalse(new AllJwtClaimsAuthorizer(ImmutableMap.of(
				"firebase", "vitob14022@tst999.com" //valid
		)).checkJwtClaims(loadPayload()));
	}
	
	@Test
	public void testNull() throws IOException {
		JwtClaimsAuthorizer allClaims = new AllJwtClaimsAuthorizer(Collections.singletonMap(
				"nullClaim", null
		));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertTrue(valid);
	}
	
	@Test
	public void testMissing() throws IOException {
		JwtClaimsAuthorizer allClaims = new AllJwtClaimsAuthorizer(Collections.singletonMap(
				"doesnotexist", null
		));
		boolean valid = allClaims.checkJwtClaims(loadPayload());
		assertTrue(valid);
	}
	
	private Authorizer loadAuthorizer(String resourceName) throws IOException {
		return DslAuthorizerFactory.get()
				.build(Resources.toString(Resources.getResource("jwtClaims/" + resourceName + ".yaml"), StandardCharsets.UTF_8), DslAuthorizerFactory.Format.YAML);
	}
	
	private IdToken.Payload loadPayload() throws IOException {
		return Client.getInstance().getJsonFactory().fromInputStream(Resources.getResource("jwtClaims/payload.json").openStream(), IdToken.Payload.class);
	}
}
