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

public class StringListSupplierBuilder {

    MergingStringListSupplier merge(StringListSupplier... suppliers) {
        return new MergingStringListSupplier(suppliers);
    }

    ExplicitStringListSupplier classpath(String resource) {
        return new ExplicitStringListSupplier(resource);
    }

    DatastoreStringListSupplier datastore(String kind) {
        return DatastoreStringListSupplier.builder().kind(kind).build();
    }

    DatastoreStringListSupplier datastore(String kind, String namespace) {
        return DatastoreStringListSupplier.builder().kind(kind).namespace(namespace).build();
    }

    CloudStorageStringListSupplier cloudStorage(String url) {
        return new CloudStorageStringListSupplier(url);
    }

}
