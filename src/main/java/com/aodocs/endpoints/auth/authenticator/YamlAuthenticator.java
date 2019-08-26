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

import java.io.IOException;

import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.aodocs.endpoints.auth.authorizers.dsl.DslAuthorizerFactory;
import com.google.api.client.util.Throwables;
import com.google.api.server.spi.config.Authenticator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.flogger.FluentLogger;
import com.google.common.io.Resources;

/**
 * Extended authenticator that loads authorization configuration from an YAML file.
 *
 * The file should be available in the classpath at the following path: ./authenticator.yaml.
 */
public final class YamlAuthenticator extends ExtendedAuthenticator {
  private final static FluentLogger logger = FluentLogger.forEnclosingClass();
  
  public YamlAuthenticator() {
    super(loadAuthorizer());
  }
  
  public YamlAuthenticator(Authenticator delegate) {
    super(delegate, loadAuthorizer());
  }
  
  @VisibleForTesting
  static Authorizer loadAuthorizer() {
    logger.atInfo().log("Loading authorizers configuration...");
  
    try {
      String authorizerConfig =  Resources.toString(Resources.getResource("authenticator.yaml"), Charsets.UTF_8);
      return DslAuthorizerFactory.get().build(authorizerConfig, DslAuthorizerFactory.Format.YAML);
    } catch (IOException | IllegalArgumentException e) {
      throw Throwables.propagate(e);
    }
  }
}
