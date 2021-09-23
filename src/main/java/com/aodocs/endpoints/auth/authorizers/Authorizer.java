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
package com.aodocs.endpoints.auth.authorizers;

import javax.servlet.http.HttpServletRequest;

import lombok.Builder;
import lombok.Value;

import com.aodocs.endpoints.auth.ExtendedUser;
import com.google.api.server.spi.config.model.ApiMethodConfig;

public interface Authorizer {
  
  @Value
  @Builder
  class AuthorizationResult {
    boolean authorized;
    
    static AuthorizationResultBuilder builder() {
      return new AuthorizationResultBuilder();
    }
  }

  /**
   * Implements authorization logic.
   *
   * @param extendedUser    a user containing additional information
   * @param apiMethodConfig the config for the current API method
   * @param request the current http request
   * @return true to authorize, false to deny access.
   */
  AuthorizationResult isAuthorized(ExtendedUser extendedUser, ApiMethodConfig apiMethodConfig, HttpServletRequest request);
}
