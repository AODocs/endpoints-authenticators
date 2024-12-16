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
package com.aodocs.endpoints.auth.authenticator.microsoft.verifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.aodocs.endpoints.auth.authenticator.microsoft.SupportedVersion;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ServiceUnavailableException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class MicrosoftOpenIDConfigurationDocumentProvider {
	
	private static final String MICROSOFT_OPENID_CONFIGURATION_DOCUMENT_URL_PATTERN_V1 = "https://login.microsoftonline.com/%s/.well-known/openid-configuration";
	private static final String MICROSOFT_OPENID_CONFIGURATION_DOCUMENT_URL_PATTERN_V2 = "https://login.microsoftonline.com/%s/v2.0/.well-known/openid-configuration";
	
	private static LoadingCache<TenantWithVersion, MicrosoftOpenIdConfigurationDocument> DOCUMENT_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<TenantWithVersion, MicrosoftOpenIdConfigurationDocument>() {
				
				@Override
				public MicrosoftOpenIdConfigurationDocument load(TenantWithVersion key) throws Exception {
					HttpRequest request = Client.getInstance().getJsonHttpRequestFactory()
							.buildGetRequest(new GenericUrl(
									String.format(key.version.equals(SupportedVersion.V2_0.getVersion())
													? MICROSOFT_OPENID_CONFIGURATION_DOCUMENT_URL_PATTERN_V2
													: MICROSOFT_OPENID_CONFIGURATION_DOCUMENT_URL_PATTERN_V1,
											key.tenantId)));
					configureErrorHandling(request);
					return parseDocument(request);
				}
			});
	
	public MicrosoftOpenIdConfigurationDocument getConfigurationDocument(@Nonnull SupportedVersion version, @Nonnull String tenantId) throws ServiceException {
		try {
			return DOCUMENT_CACHE.get(new TenantWithVersion(version.getVersion(), tenantId));
		} catch (Exception e) {
			throw new ServiceUnavailableException("Failed to obtain OpenId Connect configuration document", e);
		}
	}
	
	private static MicrosoftOpenIdConfigurationDocument parseDocument(HttpRequest request) throws IOException, ServiceException {
		HttpResponse response = request.execute();
		int statusCode = response.getStatusCode();
		MicrosoftOpenIdConfigurationDocument document = response.parseAs(MicrosoftOpenIdConfigurationDocument.class);
		if (statusCode >= HttpStatusCodes.STATUS_CODE_SERVER_ERROR) {
			throw new ServiceUnavailableException("Failed to parse OpenId Connect configuration document");
		}
		return document;
	}
	
	private static void configureErrorHandling(HttpRequest request) {
		request.setNumberOfRetries(1)
				.setThrowExceptionOnExecuteError(false)
				.setIOExceptionHandler(new HttpIOExceptionHandler() {
					@Override
					public boolean handleIOException(HttpRequest request, boolean supportsRetry) {
						return true; // consider all IOException as transient
					}
				})
				.setUnsuccessfulResponseHandler(new HttpUnsuccessfulResponseHandler() {
					@Override
					public boolean handleResponse(HttpRequest request, HttpResponse response,
							boolean supportsRetry) {
						return response.getStatusCode() >= HttpStatusCodes.STATUS_CODE_SERVER_ERROR; // only retry backend errors
					}
				});
	}
	
	private static class TenantWithVersion {
		
		private final String version;
		private final String tenantId;
		
		public TenantWithVersion(String version, String tenantId) {
			this.version = version;
			this.tenantId = tenantId;
		}
		
		@Override
		public int hashCode() {
			return version.hashCode() + tenantId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TenantWithVersion)) {
				return false;
			}
			TenantWithVersion other = (TenantWithVersion) obj;
			return version.equals(other.version) && tenantId.equals(other.tenantId);
		}
	}

}
