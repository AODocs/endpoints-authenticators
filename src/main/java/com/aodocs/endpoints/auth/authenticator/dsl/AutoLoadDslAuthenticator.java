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
package com.aodocs.endpoints.auth.authenticator.dsl;

import com.google.api.server.spi.config.Singleton;

import java.io.IOException;

@Singleton
public class AutoLoadDslAuthenticator extends DslAuthenticator {

    public AutoLoadDslAuthenticator() throws IOException {
        super(autoLoadConfiguration(detectFormat()), detectFormat());
        // TODO: find a strategy to auto-load the DSL without a subclass
    }

    private static String autoLoadConfiguration(Format detectFormat) {
        return null;
    }

    private static Format detectFormat() {
        return null;
    }

}
