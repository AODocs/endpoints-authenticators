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
package com.aodocs.endpoints.util.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import lombok.extern.java.Log;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.function.Function;
import java.util.logging.Level;

@Log
class InstanceMemoryObjectCache implements ObjectCache {

	private static final Cache<String, String> JSON_CACHE = CacheBuilder.newBuilder()
			.weigher((Weigher<String, String>) (key, value) -> key.length() + value.length())
			.maximumWeight(0x40000000L).build();
	private static final Cache<String, byte[]> SERIALIZED_CACHE = CacheBuilder.newBuilder()
			.weigher((Weigher<String, byte[]>) (key, value) -> key.length() + value.length)
			.maximumWeight(0x40000000L).build();

	@Override
	public <T> T getCachedJson(String key, Class<T> valueClass, Function<String, T> valueFunction, int expirationInSeconds) {
		try {
			final String cachedOrNew = JSON_CACHE.get(key, () -> JSON_FACTORY.toString(valueFunction.apply(key)));
			return JSON_FACTORY.createJsonParser(cachedOrNew).parse(valueClass);
		} catch (Exception e) {
			log.log(Level.SEVERE,"Error when fetching " + valueClass.getName() + "/" + key + " from instance cache", e);
			return valueFunction.apply(key);
		}
	}

	@Override
	public <T extends Serializable> T getCachedSerializable(String key, Class<T> valueClass, Function<String, T> valueFunction, int expirationInSeconds) {
		try {
			final byte[] cachedOrNew = SERIALIZED_CACHE.get(key, () -> SerializationUtils.serialize(valueFunction.apply(key)));
			return SerializationUtils.deserialize(cachedOrNew);
		} catch (Exception e) {
			log.log(Level.SEVERE,"Error when fetching " + valueClass.getName() + "/" + key + " from instance cache", e);
			return valueFunction.apply(key);
		}
	}

}
