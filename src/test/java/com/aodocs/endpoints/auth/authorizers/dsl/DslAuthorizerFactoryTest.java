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

import com.aodocs.endpoints.auth.authorizers.Authorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.ClientIdsAuthorizer;
import com.aodocs.endpoints.auth.authorizers.clientid.CurrentProjectClientIdAuthorizer;
import com.aodocs.endpoints.auth.authorizers.logic.DisjunctAuthorizer;
import com.aodocs.endpoints.auth.authorizers.request.HttpMethodAuthorizer;
import com.aodocs.endpoints.storage.*;
import com.google.cloud.datastore.KeyQuery;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.contrib.nio.CloudStorageFileSystemProvider;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.aodocs.endpoints.auth.authorizers.AuthorizerBuilder.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DslAuthorizerFactoryTest {

    private static final Authorizer TEST_AUTHORIZER = and(
            jwt(),
            httpMethod(HttpMethodAuthorizer.HttpMethod.GET),
            not(versionContains("beta")),
            versionMatches("prod"),
            clientIds(DatastoreStringListSupplier.builder().kind("A").ttlInSeconds(60).build())
    );

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
    public void testLoadYaml() throws IOException {
        Authorizer authorizer = DslAuthorizerFactory.get()
                .build(Resources.toString(Resources.getResource("authenticator.yaml"), StandardCharsets.UTF_8), DslAuthorizerFactory.Format.YAML);
        assertTrue(authorizer instanceof DisjunctAuthorizer);
        DisjunctAuthorizer or = (DisjunctAuthorizer) authorizer;
        ImmutableList<Authorizer> authorizers = or.getAuthorizers();
        assertEquals(3, authorizers.size());
        assertTrue(authorizers.get(0) instanceof CurrentProjectClientIdAuthorizer);
        assertTrue(authorizers.get(1) instanceof ClientIdsAuthorizer);
        assertTrue(authorizers.get(2) instanceof ClientIdsAuthorizer);
        ClientIdsAuthorizer datastoreSource = (ClientIdsAuthorizer) authorizers.get(2);
        StringListSupplier clientIdSupplier = datastoreSource.getClientIdSupplier();
        assertTrue(clientIdSupplier instanceof DatastoreStringListSupplier);
        DatastoreStringListSupplier datastoreSupplier = (DatastoreStringListSupplier) clientIdSupplier;
        KeyQuery query = datastoreSupplier.getQuery();
        assertEquals(500, (int) query.getLimit());
    }
    
    @Test
    public void testAuthenticatorsRoundtrip() throws IOException {
        roundtripall(TEST_AUTHORIZER);
    }

    @Test
    public void testStringListSuppliersRoundtrip() throws IOException {
        //TODO implment individual serialization tests
        roundtrip(new ExplicitStringListSupplier("a", "b", "c"));
        roundtrip(new ClasspathStringListSupplier("list.txt", false));
        roundtrip(DatastoreStringListSupplier.builder().kind("A").ttlInSeconds(60).build());
        roundtrip(DatastoreStringListSupplier.builder().kind("A").namespace("nonDefault").ttlInSeconds(600).build());
        roundtrip(new CloudStorageStringListSupplier("gs://test/test", 6000));
        roundtrip(new CloudStorageStringListSupplier("gs://test/test", 6000, false));
        roundtrip(new MergingStringListSupplier(
                new ExplicitStringListSupplier("a", "b", "c"),
                new ClasspathStringListSupplier("list.txt", true)
        ));
    }

    private void roundtrip(StringListSupplier supplier) throws IOException {
        final ClientIdsAuthorizer authorizer = new ClientIdsAuthorizer(supplier);
        roundtripall(authorizer);
    }

    private void roundtripall(Authorizer authorizer) throws IOException {
        for (DslAuthorizerFactory.Format format : DslAuthorizerFactory.Format.values()) {
            roundtrip(authorizer, format);
        }
    }

    private void roundtrip(Authorizer authorizer, DslAuthorizerFactory.Format format) throws IOException {
        DslAuthorizerFactory factory = DslAuthorizerFactory.get();
        String serialized = factory.toString(authorizer, format);
        System.err.println(format.name() + "==> " + serialized);

        Authorizer deserialized = factory.build(serialized, format);
        String serialized1 = factory.toString(deserialized, format);

        assertEquals(serialized, serialized1);
    }
}
