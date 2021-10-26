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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletRequest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.testing.http.FixedClock;
import com.google.api.client.util.Clock;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.auth.GoogleJwtAuthenticator;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.request.Attribute;

@RunWith(MockitoJUnitRunner.class)
@Ignore("This test can't work long term, as the Google public keys change quite often")
public class GoogleIdentityPlatformAuthenticatorTest {
	
	//the tokens below are expired, so they're fine to be in cleartext here
	
	public static final String FIREBASE_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImYwNTM4MmFlMTgxYWJlNjFiOTYwYjA1Yzk3ZmE0MDljNDdhNDQ0ZTciLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQ2zDqW1lbnQgVGVzdCIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9hby1kb2NzLXN0YWdpbmciLCJhdWQiOiJhby1kb2NzLXN0YWdpbmciLCJhdXRoX3RpbWUiOjE2MzIzOTA0MTAsInVzZXJfaWQiOiJ6WFpWRGlvSkExUmgxand4YzdnNnV4czRoT3oyIiwic3ViIjoielhaVkRpb0pBMVJoMWp3eGM3ZzZ1eHM0aE96MiIsImlhdCI6MTYzMzk2NzUyMywiZXhwIjoxNjMzOTcxMTIzLCJlbWFpbCI6InZpdG9iMTQwMjJAdHN0OTk5LmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJ2aXRvYjE0MDIyQHRzdDk5OS5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.YjiEsFuWP6tw0EGasTFb9mc3fLySdh0bJm_2YF6airQAZ2XAPHlyFJ2APpokWV6c_0vtislMzmnzUtWj2wSh_gq3geCtVPUA47njCn8xWoMrzgGUhFrD9srCCdcDuS6VTJ-fpM9xZua4OkC-ZHyEZP48BYNe1hbaEvn2pT7VLZV1Kpcz_SDA5oSPtRMpvXb5aSD8VRm-nm_1YY-EySApR7k7CJOAorW8xN7IIo3BBXUGuZEKjnVzm7yC-_0OEv5fMQY_Hx2xm7YOT3xoIrMtOvpWgaoRxgeEWgxu7rWbvUKVrXRjoCZoJ9uWC8HB_y2Jq-oiICQtapTA0U0wWiX-nQ";
	
	public static final String GOOGLE_ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImY0MTk2YWVlMTE5ZmUyMTU5M2Q0OGJmY2ZiNWJmMDAxNzdkZDRhNGQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0MDc0MDg3MTgxOTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0MDc0MDg3MTgxOTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDc2MjM3Mzk1NDYxNDg3NDgyNjQiLCJoZCI6InRlc3QuYW9kb2NzLmNvbSIsImVtYWlsIjoiY2xlbWVudC1ub2FvZG9jc0B0ZXN0LmFvZG9jcy5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IjFLbW9BNHpYQUZpU2taY0hIQVBTbGciLCJuYW1lIjoiQ2zDqW1lbnQgRGVuaXMgTk8gQU9ET0NTIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FBVFhBSnk1MHU3UzRHYl9NTDVOekVjRWNzdWtJbEdVa1MweGc5V2kwSUJPPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6IkNsw6ltZW50IiwiZmFtaWx5X25hbWUiOiJEZW5pcyBOTyBBT0RPQ1MiLCJsb2NhbGUiOiJlbiIsImlhdCI6MTYzMzk2NzYwMCwiZXhwIjoxNjMzOTcxMjAwfQ.flUzBRLh_QttLUtk5uJNTU7ijis1Gz2Eg8_Jv2QATr_1Jr5PtQY9rMkrnmypKmgGCeBUdAJEFZO2RgHC59-g7iP71zgr6GC7WIrowaUkA0lN4WvZLksmDlJrA5UVppjlo3ooNBJhmgI2Ep_W0m6LLrqnRRhjJSKai9r1ZD82yilBmstoJo49m0NUhW_XnEuCSz-7AVZkYml79k35Ho6RDEsGIEqsY2LlHQaacWh2Ap56yAgZIvlQQSwH6W2q_Y4M3k6CZbF-tKkuEv0O23L6S9RNV6k4kiIUOyhkP2Jf5BeIg1g8hezydOiyXriKPp2Urolli59KCdzAPAXdB7VHdg";
	
	//the two tokens above were issued shortly before that time => they are valid with this overridden clock
	private static final Clock CLOCK = new FixedClock(1633967600000L);
	
	//this instance allows to override the clock
	public class TestGoogleJwtAuthenticator extends GoogleJwtAuthenticator {
		public TestGoogleJwtAuthenticator(Clock clock) {
			//TODO find a way to mock the public keys to have the test work
			super(new GoogleIdTokenVerifier.Builder(Client.getInstance().getHttpTransport(),
					Client.getInstance().getJsonFactory()).setClock(clock).build());
		}
	}
	
	@Mock
	protected ApiMethodConfig config;
	
	@Test
	public void testIdentityPlatformAuthenticator() throws ServiceException {
		Authenticator authenticator = new GoogleIdentityPlatformAuthenticator(CLOCK, "ao-docs-staging");
		//accepts Firebase token
		assertNotNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
		//but reject Google token
		assertNull(authenticator.authenticate(configureRequest(GOOGLE_ID_TOKEN)));
	}
	
	@Test
	public void testGoogleToken() throws ServiceException {
		Authenticator authenticator = new TestGoogleJwtAuthenticator(CLOCK);
		//accepts Google token
		assertNotNull(authenticator.authenticate(configureRequest(GOOGLE_ID_TOKEN)));
		//but reject Firebase token
		assertNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
	}
	
	@Test
	public void testNoProjectIdCheck() throws ServiceException {
		Authenticator authenticator = new GoogleIdentityPlatformAuthenticator(CLOCK);
		assertNotNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
	}
	
	@Test
	public void testDifferentProjectId() throws ServiceException {
		Authenticator authenticator = new GoogleIdentityPlatformAuthenticator(CLOCK, "not-the-same-project");
		assertNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
	}
	
	@Test
	public void testMultipleProjectId() throws ServiceException {
		Authenticator authenticator = new GoogleIdentityPlatformAuthenticator(CLOCK, "not-the-same-project", "ao-docs-staging");
		assertNotNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
		authenticator = new GoogleIdentityPlatformAuthenticator(CLOCK, "ao-docs-staging", "not-the-same-project");
		assertNotNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
	}
	
	@Test
	public void testExpiration() throws ServiceException {
		Authenticator authenticator = new GoogleIdentityPlatformAuthenticator("ao-docs-staging");
		assertNull(authenticator.authenticate(configureRequest(FIREBASE_TOKEN)));
		authenticator = new TestGoogleJwtAuthenticator(Clock.SYSTEM);
		assertNull(authenticator.authenticate(configureRequest(GOOGLE_ID_TOKEN)));
	}
	
	private HttpServletRequest configureRequest(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Attribute attr = Attribute.from(request);
		attr.set(Attribute.API_METHOD_CONFIG, config);
		request.addHeader("Authorization", "Bearer " + token);
		return request;
	}
	
}
