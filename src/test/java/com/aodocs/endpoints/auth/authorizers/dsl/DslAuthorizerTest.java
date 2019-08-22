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
package com.aodocs.endpoints.auth.authorizers.dsl;

import com.aodocs.endpoints.auth.authorizers.clientid.ClientIdsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.HttpMethodAuthorizer;
import com.aodocs.endpoints.storage.*;
import com.google.api.server.spi.Client;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.contrib.nio.CloudStorageFileSystemProvider;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.and;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.clientIds;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.httpMethod;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.jwt;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.not;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.versionContains;
import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.versionMatches;
import static com.aodocs.endpoints.auth.authorizers.dsl.DslAuthorizer.Format.JSON;
import static com.aodocs.endpoints.auth.authorizers.dsl.DslAuthorizer.Format.YAML;
import static org.junit.Assert.assertEquals;

public class DslAuthorizerTest {

    private static final DslAuthorizer TEST_AUTHENTICATOR = new DslAuthorizer(and(
            jwt(),
            httpMethod(HttpMethodAuthorizer.HttpMethod.GET),
            not(versionContains("beta")),
            versionMatches("prod"),
            clientIds(new DatastoreStringListSupplier("A", 60))
    ));
    private static final Map<DslAuthorizer.Format, String> EXPECTED = ImmutableMap.of(
            JSON, "{\"and\":[\"jwt\",{\"httpMethod\":\"GET\"},{\"not\":{\"versionContains\":\"beta\"}},{\"versionMatches\":\"prod\"},{\"clientIds\":{\"ttlInSeconds\":60,\"datastoreEntity\":\"A\"}}]}",
            YAML, "and:\n" +
                    "- \"jwt\"\n" +
                    "- httpMethod: \"GET\"\n" +
                    "- not:\n" +
                    "    versionContains: \"beta\"\n" +
                    "- versionMatches: \"prod\"\n" +
                    "- clientIds:\n" +
                    "    ttlInSeconds: 60\n" +
                    "    datastoreEntity: \"A\"\n");

    private Storage storage = LocalStorageHelper.getOptions().getService();

    @Before
    public void setUp() {
        storage.create(BlobInfo.newBuilder("test", "test").build(), "value".getBytes());
        CloudStorageFileSystemProvider.setStorageOptions(storage.getOptions());
    }

    @After
    public void tearDown() {
        CloudStorageFileSystemProvider.setStorageOptions(StorageOptions.getDefaultInstance());
    }

    @Test
    public void testAuthenticatorsRoundtrip() throws IOException {
        for (DslAuthorizer.Format format : DslAuthorizer.Format.values()) {
            roundtrip(format);
        }
    }

    private void roundtrip(DslAuthorizer.Format format) throws IOException {
        final String asJson1 = TEST_AUTHENTICATOR.toString(format);
        assertEquals(EXPECTED.get(format), asJson1);
        final DslAuthorizer deserialized = new DslAuthorizer(asJson1, format);
        final String asJson2 = deserialized.toString(format);
        assertEquals(asJson1, asJson2);
        System.out.println(asJson2);
    }

    @Test
    public void testStringListSuppliersRoundtrip() throws IOException {
        //TODO implment individual seriualization tests
        roundtrip(new ExplicitStringListSupplier("a", "b", "c"));
        roundtrip(new ClasspathStringListSupplier("list.txt", false));
        roundtrip(new DatastoreStringListSupplier("A", 60));
        roundtrip(new DatastoreStringListSupplier("A", "nonDefault", 600));
        roundtrip(new CloudStorageStringListSupplier("gs://test/test", 6000));
        roundtrip(new CloudStorageStringListSupplier("gs://test/test", 6000, false));
        roundtrip(new MergingStringListSupplier(
                new ExplicitStringListSupplier("a", "b", "c"),
                new ClasspathStringListSupplier("list.txt", true)
        ));
    }

    private void roundtrip(StringListSupplier supplier) throws IOException {
        final ClientIdsAuthorizer object = new ClientIdsAuthorizer(supplier);
        final String asJson1 = JSON.writeValueAsString(object);
        final ClientIdsAuthorizer deserialized = JSON.reader().forType(ClientIdsAuthorizer.class).readValue(asJson1);
        final String asJson2 = JSON.writeValueAsString(deserialized);
        assertEquals(asJson1, asJson2);
        System.out.println(asJson2);
    }

}
