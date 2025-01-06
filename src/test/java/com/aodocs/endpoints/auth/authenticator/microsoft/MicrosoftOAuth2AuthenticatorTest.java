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
package com.aodocs.endpoints.auth.authenticator.microsoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import com.aodocs.endpoints.auth.CustomAttribute;
import com.aodocs.endpoints.auth.authenticator.microsoft.MicrosoftIdToken;
import com.aodocs.endpoints.auth.authenticator.microsoft.MicrosoftOAuth2Authenticator;
import com.aodocs.endpoints.auth.authenticator.microsoft.verifier.MicrosoftTokenVerifier;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.request.Attribute;

@RunWith(MockitoJUnitRunner.class)
public class MicrosoftOAuth2AuthenticatorTest {
	
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String TOKEN = "abcdefjh.abcdefjh.abcdefjh";
	private static final String EMAIL = "dummy@gmail.com";
	private static final String AUDIENCE = "audience1";
	private static final String USER_ID = "1234567";
	
	private MicrosoftIdToken.Payload payload;
	private MicrosoftOAuth2Authenticator authenticator;
	private MockHttpServletRequest request;
	private Attribute attr;
	
	@Mock
	private MicrosoftTokenVerifier verifier;
	@Mock
	private MicrosoftIdToken token;
	@Mock
	protected ApiMethodConfig config;
	
	@Before
	public void setUp() throws Exception {
		payload = new MicrosoftIdToken.Payload();
		payload.setAudience(AUDIENCE);
		payload.setEmail(EMAIL);
		payload.setObjectId(USER_ID);
		
		authenticator = new MicrosoftOAuth2Authenticator(verifier);
		
		request = new MockHttpServletRequest();
		attr = Attribute.from(request);
		attr.set(Attribute.API_METHOD_CONFIG, config);
		attr.set(Attribute.ENABLE_CLIENT_ID_WHITELIST, true);
		request.addHeader(AUTHORIZATION_HEADER, "Bearer " + TOKEN);
		
		when(token.getPayload()).thenReturn(payload);
	}
	
	@Test
	public void testVerifyToken_verificationExceptionsHandled() throws Exception {
		when(verifier.verify(TOKEN))
				.thenReturn(token)
				.thenThrow(new GeneralSecurityException())
				.thenThrow(new IOException())
				.thenThrow(new IllegalArgumentException())
				.thenThrow(new ServiceException(1, ""));
		assertEquals(token, authenticator.verifyToken(TOKEN));
		for (int i = 0; i < 4; i++) {
			assertNull(authenticator.verifyToken(TOKEN));
		}
	}
	
	@Test
	public void testAuthenticate_skipTokenAuth() {
		attr.set(Attribute.SKIP_TOKEN_AUTH, true);
		assertNull(authenticator.authenticate(request));
		assertNull(attr.get(CustomAttribute.MICROSOFT_ID_TOKEN));
	}
	
	@Test
	public void testAuthenticate_notJwt() {
		request.addHeader(AUTHORIZATION_HEADER, "Bearer abc.abc");
		assertNull(authenticator.authenticate(request));
		assertNull(attr.get(CustomAttribute.MICROSOFT_ID_TOKEN));
	}
	
	@Test
	public void testAuthenticate_invalidToken() throws Exception {
		when(verifier.verify(TOKEN)).thenReturn(null);
		assertNull(authenticator.authenticate(request));
		assertNull(attr.get(CustomAttribute.MICROSOFT_ID_TOKEN));
	}
	
	@Test
	public void testAuthenticate() throws Exception {
		when(verifier.verify(TOKEN)).thenReturn(token);
		
		User user = authenticator.authenticate(request);
		
		assertEquals(EMAIL, user.getEmail());
		assertEquals(USER_ID, user.getId());
		
		MicrosoftIdToken idToken = attr.get(CustomAttribute.MICROSOFT_ID_TOKEN);
		assertNotNull(idToken);
		assertEquals(EMAIL, idToken.getPayload().getEmail());
		assertEquals(USER_ID, idToken.getPayload().getObjectId());
	}
	
	@Test
	public void testAuthenticate_appEngineUser() throws Exception {
		attr.set(Attribute.REQUIRE_APPENGINE_USER, true);
		when(verifier.verify(TOKEN)).thenReturn(token);
		
		User user = authenticator.authenticate(request);
		assertEquals(EMAIL, user.getEmail());
		assertEquals(USER_ID, user.getId());
		
		com.google.appengine.api.users.User appEngineuser =
				attr.get(Attribute.AUTHENTICATED_APPENGINE_USER);
		
		assertEquals(EMAIL, appEngineuser.getEmail());
		assertNull(appEngineuser.getUserId());
	}
}

