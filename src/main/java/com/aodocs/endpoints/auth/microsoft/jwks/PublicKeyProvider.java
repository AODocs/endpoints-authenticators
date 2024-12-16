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
package com.aodocs.endpoints.auth.microsoft.jwks;

import java.net.URL;
import java.security.PublicKey;
import java.util.logging.Logger;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.server.spi.response.ServiceUnavailableException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

public class PublicKeyProvider {
	
	private static final Logger sLogger = Logger.getLogger(PublicKeyProvider.class.getName());
	
	public PublicKey getPublicKey(String jwksUri, JsonWebSignature.Header header) throws ServiceUnavailableException {
		try {
			URL jwkSetURL = new URL(jwksUri);
			
			JWKSet jwkSet = JWKSet.load(jwkSetURL);
			
			JWSAlgorithm algorithm = JWSAlgorithm.parse(header.getAlgorithm());
			if (algorithm.equals(JWSAlgorithm.RS256)) {
				RSAKey rsaKey = null;
				for (JWK jwk : jwkSet.getKeys()) {
					if (jwk.getKeyID().equals(header.getKeyId())) {
						rsaKey = (RSAKey) jwk;
						break;
					}
				}
				if (rsaKey == null) {
					throw new IllegalArgumentException("Cannot locate publicKey for algorithm: " + header.getAlgorithm());
				}
				
				sLogger.info("Public key: " + rsaKey.toRSAPublicKey());
				
				return rsaKey.toRSAPublicKey();
			} else {
				throw new IllegalArgumentException("Unsupported algorithm: " + header.getAlgorithm());
			}
		} catch (Exception e) {
			throw new ServiceUnavailableException("Failed to obtain public key for signature validation", e);
		}
	}
	
}
