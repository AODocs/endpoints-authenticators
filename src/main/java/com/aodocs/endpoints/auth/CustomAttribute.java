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
package com.aodocs.endpoints.auth;

/**
 * Custom attributes to extends the default attributes provided by the Endpoints Framework:
 * {@link com.google.api.server.spi.request.Attribute}
 */
public class CustomAttribute {
	
	/**
	 * If set, contains a cached instance of a parsed and valid Microsoft ID Token (JWT)
	 * {@link com.aodocs.endpoints.auth.microsoft.MicrosoftIdToken}
	 */
	public static final String MICROSOFT_ID_TOKEN = "endpoints:MS-Token";
	
}
