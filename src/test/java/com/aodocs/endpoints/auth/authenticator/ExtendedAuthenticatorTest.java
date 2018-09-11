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
import com.aodocs.endpoints.auth.AuthType;
import com.aodocs.endpoints.auth.ExtendedUser;
import com.google.api.server.spi.EnvUtil;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Singleton;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.request.Attribute;
import com.google.api.server.spi.response.ServiceUnavailableException;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableSet;
import lombok.extern.java.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.reflections.Reflections;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

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
    public static abstract class TestAuthenticator extends ExtendedAuthenticator {

        @Override
        protected User performPrimaryAuthentication(HttpServletRequest request, AuthType authType) {
            return new User("123456789", "dummyuser@gmail.com");
        }

        @Override
        AuthInfo getAuthInfo(AuthType authType, HttpServletRequest request, String token) {
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

    @Singleton
    public static class PassAuthenticator extends TestAuthenticator {
        @Override
        public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
            return new AuthorizationResult(true);
        }
    }

    @Singleton
    public static class DenyAuthenticator extends TestAuthenticator {
        @Override
        public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
            return new AuthorizationResult(false);
        }
    }

    public static class NotSingletonAuthenticator extends ExtendedAuthenticator {
        @Override
        public AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request) {
            return new AuthorizationResult(true);
        }
    }

    @Test
    public void withoutHeader_thenNotPass() throws ServiceUnavailableException {
        assertNull(new PassAuthenticator().authenticate(request));
    }

    @Test
    public void withHeader_thenPass() throws ServiceUnavailableException {
        request.addHeader("Authorization", "Bearer ya29.abcdefg");
        assertNotNull(new PassAuthenticator().authenticate(request));
        assertNull(new DenyAuthenticator().authenticate(request));
    }

    @Test(expected = IllegalStateException.class)
    public void notSingleton_thenFail() throws ServiceUnavailableException {
        new NotSingletonAuthenticator().authenticate(request);
    }

    @Test
    public void defaultConstructorAuthenticatotorsAreAllSingletons() {
        Reflections reflections = new Reflections("com.aodocs.endpoints.auth.authenticator");
        Set<Class<? extends ExtendedAuthenticator>> authenticatorTypes = reflections.getSubTypesOf(ExtendedAuthenticator.class);
        for (Class<? extends ExtendedAuthenticator> authenticatorType : authenticatorTypes) {
            if (Modifier.isAbstract(authenticatorType.getModifiers()) //do not check abstract classes
                    || authenticatorType.getName().contains(getClass().getName())) //exclude current class' inner classes
                continue;
            try {
                authenticatorType.getDeclaredConstructor();
                log.info("Checking @Singleton presence on " + authenticatorType);
                assertTrue(Arrays.stream(authenticatorType.getAnnotations()).anyMatch(input -> input.annotationType().getSimpleName().equals("Singleton")));
            } catch (NoSuchMethodException e) {
                log.info("No default constructor on " + authenticatorType);
            }
        }
    }

}
