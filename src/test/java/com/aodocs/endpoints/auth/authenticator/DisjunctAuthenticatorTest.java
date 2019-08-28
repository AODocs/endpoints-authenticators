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

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.response.ServiceUnavailableException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class DisjunctAuthenticatorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAtLeastOneDelegateIsMandatory() {
        expectedException.expect(NullPointerException.class);
        new DisjunctAuthenticator(null);

        //Valid
        Authenticator delegate = mock(Authenticator.class);
        new DisjunctAuthenticator(delegate);
    }

    @Test
    public void testOneDelegateAuthenticatedTheUser() throws ServiceException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        //Authenticator returning null
        Authenticator first = mock(Authenticator.class);

        Authenticator second = mock(Authenticator.class);
        User user = new User("mail@mail.com");
        when(second.authenticate(request)).thenReturn(user);

        DisjunctAuthenticator underTest = new DisjunctAuthenticator(first, second);
        User authenticatedUser = underTest.authenticate(request);
        assertSame(user, authenticatedUser);
    }

    @Test
    public void testThrowingException() throws ServiceException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        //Authenticator throwing exception
        Authenticator first = mock(Authenticator.class);
        doThrow(ServiceUnavailableException.class)
                .when(first)
                .authenticate(request);

        Authenticator second = mock(Authenticator.class);
        User user = new User("mail@mail.com");
        when(second.authenticate(request)).thenReturn(user);

        DisjunctAuthenticator underTest = new DisjunctAuthenticator(first, second);
        User authenticatedUser = underTest.authenticate(request);
        assertSame(user, authenticatedUser);
    }

    @Test
    public void testRethrowTheLatestException() throws ServiceException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        //Authenticator throwing exception
        Authenticator first = mock(Authenticator.class);
        doThrow(ServiceUnavailableException.class)
                .when(first)
                .authenticate(request);

        //Authenticator throwing exception
        Authenticator second = mock(Authenticator.class);
        ServiceUnavailableException toBeThrown = new ServiceUnavailableException("Message");
        doThrow(toBeThrown)
                .when(second)
                .authenticate(request);

        try {
            DisjunctAuthenticator underTest = new DisjunctAuthenticator(first, second);
            underTest.authenticate(request);
            fail("Expect the exception:" + toBeThrown);
        } catch (ServiceException e) {
            assertSame(toBeThrown, e);
        }
    }
}