/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 - 2021 AODocs (Altirnao Inc)
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Merges multiple list suppliers into one. Can be useful to load from a "static" list of values
 * (for example from the Classpath) and a dynamic one (Cloud Storage or Datastore).
 */
public class MergingStringListSupplier extends StringListSupplier {

    @JsonProperty("merge")
    private final ImmutableList<StringListSupplier> suppliers;

    @JsonCreator
    public MergingStringListSupplier(@NonNull StringListSupplier... suppliers) {
        this.suppliers = ImmutableList.copyOf(suppliers);
    }

    protected List<String> getRaw() {
        return suppliers.stream()
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
