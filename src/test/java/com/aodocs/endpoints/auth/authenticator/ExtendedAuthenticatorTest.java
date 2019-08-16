/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 AODocs (Altirnao Inc)
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

import com.aodocs.endpoints.auth.AuthInfo;
import com.aodocs.endpoints.auth.ExtendedUser;
import com.aodocs.endpoints.auth.authorizers.AbstractAuthorizer;
import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.google.api.server.spi.EnvUtil;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.request.Attribute;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableSet;
import lombok.extern.java.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created by Clement on 20/10/2016.
 */
@Log
@RunWith(MockitoJUnitRunner.class)
public class ExtendedAuthenticatorTest {

    private LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalURLFetchServiceTestConfig());
    @Mock
    protected ApiMethodConfig config;

    private MockHttpServletRequest request;
    private Attribute attr;
    
    @Mock
    Authenticator delegate;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        helper.setUp();
        request = new MockHttpServletRequest();
        attr = Attribute.from(request);
        attr.set(Attribute.API_METHOD_CONFIG, config);
        System.setProperty(EnvUtil.ENV_APPENGINE_RUNTIME, "Production");
    }

    @After
    public void tearDown() {
        helper.tearDown();
        System.clearProperty(EnvUtil.ENV_APPENGINE_RUNTIME);
    }

    //mocks all required remote calls
    public static class TestAuthenticator extends ExtendedAuthenticator {
        
        TestAuthenticator(Authenticator delegate, Authorizer authorizer) {
            super(delegate, authorizer);
        }

        @Override
        AuthInfo getAuthInfo(HttpServletRequest request) {
            return createDummyAuthInfo("dummyuser@gmail.com", "12345", "scope1", "123456789");
        }

        private AuthInfo createDummyAuthInfo(final String email, final String clientId,
                final String scopes, final String userId) {
            if (email == null) {
                return null;
            }
            return AuthInfo.builder()
                    .email(email).clientId(clientId).scopes(ImmutableSet.of(scopes)).userId(userId)
                    .build();
        }
    }

    static class PassthroughsAuthorizer extends AbstractAuthorizer {
        @Override
        public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
            return newResultBuilder().authorized(true).build();
        }
    }
    
    static class DenyAll extends AbstractAuthorizer {
    
        @Override
        public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
            return newResultBuilder().authorized(false).build();
        }
    }
    
    @Test
    public void notAuthenticated() throws ServiceException {
        when(delegate.authenticate(request)).thenReturn(null);
        TestAuthenticator authenticator = new TestAuthenticator(delegate, new PassthroughsAuthorizer());
    
        User user = authenticator.authenticate(request);
        assertNull(user);
    }
    
    @Test
    public void authenticationThrowsException_NotAuthenticated() throws ServiceException {
        doThrow(ServiceException.class).when(delegate).authenticate(request);
        
        TestAuthenticator authenticator = new TestAuthenticator(delegate, new PassthroughsAuthorizer());
        
        thrown.expect(ServiceException.class);
        authenticator.authenticate(request);
    }
    
    @Test
    public void notAuthorized() throws ServiceException {
        User user = new User("123456789", "dummyuser@gmail.com");
        when(delegate.authenticate(any(HttpServletRequest.class))).thenReturn(user);
        TestAuthenticator authenticator = new TestAuthenticator(delegate, new DenyAll());
    
        User actual = authenticator.authenticate(request);
        assertNull(actual);
    }
    
    @Test
    public void authorized() throws ServiceException {
        User user = new User("123456789", "dummyuser@gmail.com");
        when(delegate.authenticate(any(HttpServletRequest.class))).thenReturn(user);
        TestAuthenticator authenticator = new TestAuthenticator(delegate, new PassthroughsAuthorizer());
    
        User actual = authenticator.authenticate(request);
        assertEquals(user, actual); //notice that assertEquals(actual, user) fails due to non-symmetric equals implementation of ExtendedUser
    }
}
