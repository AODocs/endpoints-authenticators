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

import com.aodocs.endpoints.auth.AppEngineTest;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.contrib.nio.CloudStorageFileSystemProvider;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by Clement on 11/10/2016.
 */
public class CloudStorageStringListSupplierTest extends AppEngineTest {

    private static String bucket;
    private static Storage storage = LocalStorageHelper.getOptions().getService();

    @BeforeClass
    public static void initHelper() {
        bucket = RemoteStorageHelper.generateBucketName();
        CloudStorageFileSystemProvider.setStorageOptions(storage.getOptions());
    }

    @AfterClass
    public static void cleanUp() {
        CloudStorageFileSystemProvider.setStorageOptions(StorageOptions.getDefaultInstance());
    }

    @Test
    public void testGet() throws Exception {
        String pathString = "gs://" + bucket + "/test.txt";
        Path path = Paths.get(URI.create(pathString));
        ImmutableList<String> lines = ImmutableList.of("1", "2", "3");
        Files.write(path, lines, StandardCharsets.UTF_8);
        List<String> strings = new CloudStorageStringListSupplier(pathString, 60).get();
        assertEquals(lines, strings);
    }

    @Test
    public void testCaching() throws IOException, InterruptedException {
        //TODO test with ticker
        String pathString = "gs://" + bucket + "/cached.txt";
        Path path = Paths.get(URI.create(pathString));
        ImmutableList<String> lines = ImmutableList.of("1", "2", "3");
        Files.write(path, lines, StandardCharsets.UTF_8);
        CloudStorageStringListSupplier supplier = new CloudStorageStringListSupplier(pathString, 2);
        assertEquals(lines, supplier.get());
        //add a new value
        ImmutableList<String> lines2 = ImmutableList.of("1", "2", "3", "4");
        Files.write(path, lines2, StandardCharsets.UTF_8);
        
        TimeUnit.SECONDS.sleep(2); //Let the cache expire
        supplier.get(); //Return the old value but trigger a reload
        
        TimeUnit.SECONDS.sleep(1); //Let the reload occur
        assertEquals(lines2, supplier.get());
    }

    @Test
    public void testMissingWithoutFailOnMissing() {
        //non-existing files should give an empty list
        assertEquals(ImmutableList.of(), new CloudStorageStringListSupplier("gs://" + bucket + "/doesnotexist.txt", 60, false).get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingWithFailOnMissing() {
        //enable fail on unexisting resources
        new CloudStorageStringListSupplier("gs://" + bucket + "/doesnotexist.txt", 60);
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        //enable fail on unexisting resources
        new CloudStorageStringListSupplier(null, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUrl1() {
        new CloudStorageStringListSupplier("gs://nopath/", 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUrl2() {
        new CloudStorageStringListSupplier("notavalidUrl", 60);
    }

}
