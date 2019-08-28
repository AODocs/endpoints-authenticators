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
package com.aodocs.endpoints.util;

import com.aodocs.endpoints.context.AppengineHelper;
import com.google.appengine.api.ThreadManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A supplier with memoization. On memoized value expiration, the new value is refreshed acynchronously.
 *
 * This supplier does not work on frontend instances of appengine due to usage of background thread to refresh the cache.
 */
public class AsyncRefreshMemoizingSupplier<T> implements Supplier<T> {


    public static <T> AsyncRefreshMemoizingSupplier<T> create(int ttlInSeconds, final Supplier<T> supplier) {
        if (AppengineHelper.isRunningOnAppengineStandard())  {
            return new AsyncRefreshMemoizingSupplier<T>(ttlInSeconds, supplier, ThreadManager.backgroundThreadFactory());
        }
        return new AsyncRefreshMemoizingSupplier<T>(ttlInSeconds, supplier, Executors.defaultThreadFactory());
    }

    private final LoadingCache<String, T> cache;

    /**
     *
     * @param ttlInSeconds
     * @param supplier
     */
    AsyncRefreshMemoizingSupplier(int ttlInSeconds, final Supplier<T> supplier, ThreadFactory threadFactory) {
        this(ttlInSeconds, supplier, threadFactory, null);
    }

    @VisibleForTesting
    AsyncRefreshMemoizingSupplier(int ttlInSeconds, final Supplier<T> supplier, ThreadFactory threadFactory, Ticker ticker) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

        if (ticker != null) {
            builder.ticker(ticker);
        }

        Executor singleThreadedExecutor = runnable -> threadFactory.newThread(runnable).start();

        this.cache = builder
                .initialCapacity(1)
                .refreshAfterWrite(ttlInSeconds, TimeUnit.SECONDS)
                .build(CacheLoader.asyncReloading(new CacheLoader<String, T>() {
                        @Override
                        public T load(String key) {
                            return supplier.get();
                        }
                    }, singleThreadedExecutor)
                );
    }

    @Override
    public T get() {
        return cache.getUnchecked("");
    }

}
