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
package com.aodocs.endpoints.storage;

import com.aodocs.endpoints.util.AsyncRefreshMemoizingSupplier;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.List;

/**
 * Provides a string list with caching, and refreshes asynchronously to avoid pauses.
 */
public abstract class AsyncRefreshCachingStringListSupplier extends StringListSupplier {

    public static final int DEFAULT_EXPIRATION = 600;

    private final AsyncRefreshMemoizingSupplier<List<String>> cache;
    //used for serialization only
    @JsonProperty
    private final int ttlInSeconds;

    protected AsyncRefreshCachingStringListSupplier(Integer ttlInSeconds) {
        this.ttlInSeconds = MoreObjects.firstNonNull(ttlInSeconds, DEFAULT_EXPIRATION);
        this.cache = AsyncRefreshMemoizingSupplier.create(this.ttlInSeconds, this::getUncached);
    }

    @Override
    protected List<String> getRaw() {
        return cache.get();
    }

    protected abstract List<String> getUncached();

}
