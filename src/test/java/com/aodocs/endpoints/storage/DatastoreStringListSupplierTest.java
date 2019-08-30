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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.threeten.bp.Duration;

import com.aodocs.endpoints.auth.AppEngineTest;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.ImmutableList;

/**
 * This test is very slow, because of the local DS emulator.
 * <p>
 * This test requires local installation of gcloud (with the "beta" component installed)
 */
public class DatastoreStringListSupplierTest extends AppEngineTest {

    private static LocalDatastoreHelper helper;

    @BeforeClass
    public static void createAndStartHelper() throws IOException, InterruptedException {
        helper = LocalDatastoreHelper.create(1);
        helper.start();
    }

    @AfterClass
    public static void stopHelper() throws IOException, InterruptedException, TimeoutException {
        helper.stop(Duration.ofMinutes(1));
    }

    @Before
    public void startHelper() throws IOException, InterruptedException {
        helper.reset();
    }

    @Test
    public void testGet() {
        Datastore datastoreService = helper.getOptions().getService();
        //without namespace
        datastoreService.put(Entity.newBuilder(Key.newBuilder(helper.getProjectId(), "ClientId", "12345").build()).build());
        assertEquals(
                ImmutableList.of("12345"),
                DatastoreStringListSupplier.builder()
                        .kind("ClientId").ttlInSeconds(10)
                        .datastore(datastoreService)
                        .build().get()
        );
        //with namespace
        datastoreService.put(Entity.newBuilder(Key.newBuilder(helper.getProjectId(), "ClientId", "12345").setNamespace("ns").build()).build());
        assertEquals(
                ImmutableList.of("12345"),
                DatastoreStringListSupplier.builder()
                        .kind("ClientId").namespace("ns")
                        .datastore(datastoreService)
                        .build().get());
    }

    @Test
    public void testCaching() throws Exception {
        //TODO test with ticker
        Datastore datastoreService = helper.getOptions().getService();
        datastoreService.put(Entity.newBuilder(Key.newBuilder(helper.getProjectId(), "ClientId", "12345").build()).build());
        DatastoreStringListSupplier supplier = DatastoreStringListSupplier.builder()
                .kind("ClientId").ttlInSeconds(2)
                .datastore(datastoreService)
                .build();
        assertEquals(ImmutableList.of("12345"), supplier.get());
        //add a new value
        datastoreService.put(Entity.newBuilder(Key.newBuilder(helper.getProjectId(), "ClientId", "123456").build()).build());
        //still has the cached value
        assertEquals(ImmutableList.of("12345"), supplier.get());
        //let the cache expire
        TimeUnit.SECONDS.sleep(3);
        //still returns the old value, but triggered a refresh
        assertEquals(ImmutableList.of("12345"), supplier.get());
        //let the refresh happen
        TimeUnit.SECONDS.sleep(1);
        assertEquals(ImmutableList.of("12345", "123456"), supplier.get());
    }

}
