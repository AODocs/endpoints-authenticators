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
package com.aodocs.endpoints.auth;

import com.google.common.base.Ticker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Clement on 20/10/2016.
 */
public class FakeTicker extends Ticker {

    private final AtomicLong nanos = new AtomicLong();

    /**
     * Advances the ticker value by {@code time} in {@code timeUnit}.
     */
    public FakeTicker advance(long time, TimeUnit timeUnit) {
        nanos.addAndGet(timeUnit.toNanos(time));
        return this;
    }

    @Override
    public long read() {
        return nanos.getAndAdd(0);
    }
}
