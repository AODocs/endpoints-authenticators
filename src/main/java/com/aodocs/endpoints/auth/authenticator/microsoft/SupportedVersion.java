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
package com.aodocs.endpoints.auth.authenticator.microsoft;

import java.util.Arrays;

public enum SupportedVersion {
	
	V1_0("1.0"),
	V2_0("2.0");
	
	private final String version;
	
	SupportedVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public static boolean isSupported(String version) {
		return Arrays.stream(SupportedVersion.values()).anyMatch(v -> v.version.equals(version));
	}
	
	public static SupportedVersion of(String version) {
		return Arrays.stream(SupportedVersion.values())
				.filter(v -> v.version.equals(version))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported Microsoft Oauth2 token version: " + version));
	}
}
