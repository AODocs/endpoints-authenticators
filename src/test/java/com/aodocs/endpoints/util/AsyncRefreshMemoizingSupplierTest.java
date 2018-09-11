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

import com.aodocs.endpoints.auth.AppEngineTest;
import com.aodocs.endpoints.auth.FakeTicker;
import com.google.appengine.api.ThreadManager;
import com.google.common.base.Stopwatch;
import org.junit.Test;

import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * Created by Clement on 17/10/2016.
 */
public class AsyncRefreshMemoizingSupplierTest extends AppEngineTest {

    @Test
    public void testRefreshIsAsync() throws Exception {
        final FakeTicker fakeTicker = new FakeTicker();
        AsyncRefreshMemoizingSupplier<String> cache = new AsyncRefreshMemoizingSupplier<>(1, new Supplier<String>() {
            int value = 0;

            @Override
            public String get() {
                fakeTicker.advance(200, MILLISECONDS);
                return value++ + "";
            }
        }, ThreadManager.backgroundThreadFactory(), fakeTicker);
        //initial value is 0, takes a long time to get
        Stopwatch stopwatch = Stopwatch.createStarted(fakeTicker);
        assertEquals("0", cache.get());
        assertEquals(200, stopwatch.elapsed(MILLISECONDS));
        //next call returns same value quickly
        stopwatch = Stopwatch.createStarted(fakeTicker);
        assertEquals("0", cache.get());
        assertEquals(0, stopwatch.elapsed(MILLISECONDS));
        //wait for the value to expire
        fakeTicker.advance(2000, MILLISECONDS);
        stopwatch = Stopwatch.createStarted(fakeTicker);
        //value is now expired, a refresh is started, but still returns the old one
        assertEquals("0", cache.get());
        //this pause let the async refresh execute (cannot be controlled externally)
        MILLISECONDS.sleep(100);
        //the refresh should have happened now, the new value is returned quickly
        assertEquals("1", cache.get());
        assertEquals(200, stopwatch.elapsed(MILLISECONDS));
    }

}
