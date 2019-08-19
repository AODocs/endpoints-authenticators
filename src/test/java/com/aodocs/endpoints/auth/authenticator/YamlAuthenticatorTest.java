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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.aodocs.endpoints.auth.authorizers.dsl.DslAuthorizerFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class YamlAuthenticatorTest {
  
  @Test
  public void loadAuthorizer() throws IOException {
    Authorizer authorizer = YamlAuthenticator.loadAuthorizer();
    assertNotNull(authorizer);
  
    String expected =  Resources.toString(Resources.getResource("./authenticator.yaml"), Charsets.UTF_8);
    String asYaml = DslAuthorizerFactory.get().toString(authorizer, DslAuthorizerFactory.Format.YAML);
    assertEquals(expected, asYaml);
  }
}