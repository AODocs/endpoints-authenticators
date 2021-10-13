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
package com.aodocs.endpoints.auth;

import com.google.api.server.spi.auth.common.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * Extends built-in Endpoints User type to add information related to authentication.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ExtendedUser extends User {

    /**
     * Token info of the authentication token. Only valid on OAuth2 and JWT authTypes.
     */
    @Delegate
    private final AuthInfo authInfo;

    //Token-based auth (Oauth2 or GWT)
    public ExtendedUser(User user, AuthInfo authInfo) {
        super(user.getId(), user.getEmail());
        this.authInfo = authInfo;
    }

}

