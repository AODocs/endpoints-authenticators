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
package com.aodocs.endpoints.auth.authenticator.microsoft.util;

public class UrlFormatter {
	
	public static final String TENANT_ID_PLACEHOLDER = "{tenantid}";
	
	/**
	 * Replaces the tenant id placeholder <i>{tenantId}</i> in the  url with the given tenant id.
	 * If there's no placeholder in the url, the url is returned as is.
	 */
	public static String withTenantId(String patternUrl, String tenantId) {
		return patternUrl.replace(TENANT_ID_PLACEHOLDER, tenantId);
	}
	
}
