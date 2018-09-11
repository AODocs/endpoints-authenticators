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

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Object cache implementation using AppEngine's memcache.
 */
@Log
class AppEngineMemcacheObjectCache implements ObjectCache {

    @Override
    public <T> T getCachedJson(
            String key, Class<T> valueClass,
            Function<String, T> valueFunction,
            int expirationInSeconds) {
        try {
            MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService(valueClass.getCanonicalName());
            String json = (String) memcacheService.get(key);
            if (json != null) {
                return JSON_FACTORY.createJsonParser(json).parse(valueClass);
            }
            T value = valueFunction.apply(key);
            memcacheService.put(key, JSON_FACTORY.toString(value), Expiration.byDeltaSeconds(expirationInSeconds));
            return value;
        } catch (Exception e) {
            log.log(Level.SEVERE,"Error when fetching " + valueClass.getName() + "/" + key + " from memcache", e);
            return valueFunction.apply(key);
        }
    }

    @Override
    public <T extends Serializable> T getCachedSerializable(
            String key, Class<T> valueClass,
            Function<String, T> valueFunction,
            int  expirationInSeconds) {
        try {
            MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService(valueClass.getName());
            T value = (T) memcacheService.get(key);
            if (value != null) {
                return value;
            }
            value = valueFunction.apply(key);
            memcacheService.put(key, value, Expiration.byDeltaSeconds(expirationInSeconds));
            return value;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error when fetching " + valueClass.getName() + "/" + key + " from memcache", e);
            return valueFunction.apply(key);
        }
    }

}
