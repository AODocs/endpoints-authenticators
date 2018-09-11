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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.datastore.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads a string list from the ids of a kind of entity in the namespace.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DatastoreStringListSupplier extends AsyncRefreshCachingStringListSupplier {

    private final Datastore datastore;
    private final KeyQuery query;

    //used for serialization only
    @JsonProperty
    private final String namespace;
    @JsonProperty("datastoreEntity")
    private final String kind;

    //used to define defaults for serialization
    private DatastoreStringListSupplier() {
        super(null);
        this.kind = null;
        this.namespace = null;
        this.datastore = null;
        this.query = null;
    }

    public DatastoreStringListSupplier(String kind) {
        this(kind, null);
    }

    public DatastoreStringListSupplier(String kind, String namespace) {
        this(kind, namespace, null, DatastoreOptions.getDefaultInstance());
    }

    public DatastoreStringListSupplier(String kind, int ttlInSeconds) {
        this(kind, null, ttlInSeconds);
    }

    public DatastoreStringListSupplier(String kind, String namespace, int ttlInSeconds) {
        this(kind, namespace, ttlInSeconds, DatastoreOptions.getDefaultInstance());
    }

    @VisibleForTesting
    DatastoreStringListSupplier(String kind, String namespace, Integer ttlInSeconds,
                                DatastoreOptions datastoreOptions) {
        super(ttlInSeconds);
        this.kind = Preconditions.checkNotNull(kind);
        this.namespace = namespace;
        this.datastore = datastoreOptions.getService();
        this.query = Query.newKeyQueryBuilder().setKind(kind).setNamespace(namespace).setLimit(100).build();
    }

    @Override
    protected List<String> getUncached() {
        QueryResults<Key> results = datastore.run(query);
        return Lists.newArrayList(results).stream()
                .map(input -> input.getNameOrId().toString()).collect(Collectors.toList());
    }

}
