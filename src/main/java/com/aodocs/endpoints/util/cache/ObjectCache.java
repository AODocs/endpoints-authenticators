/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 - 2021 AODocs (Altirnao Inc)
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

import com.aodocs.endpoints.context.AppengineHelper;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.json.JsonFactory;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A cache fro JSON serializable or serializable objects.
 * The cache operation MUST clone the objects (not reuse instances).
 */
public interface ObjectCache {

	JsonFactory JSON_FACTORY = Utils.getDefaultJsonFactory();

	<T> T getCachedJson(
			String key, Class<T> valueClass,
			Function<String, T> valueFunction,
			int expirationInSeconds);

	<T extends Serializable> T getCachedSerializable(
			String key, String namespace,
			Function<String, T> valueFunction,
			int  expirationInSeconds);

	static ObjectCache get() {
		if (AppengineHelper.isRunningOnAppengineStandard()) {
			return new AppEngineMemcacheObjectCache();
		}
		return new InstanceMemoryObjectCache();
	}

}
