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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.List;

/**
 * Provides an explicit list of values
 */
@Log
public class ExplicitStringListSupplier extends StaticStringListSupplier {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ExplicitStringListSupplier(@NonNull String... values) {
        super(ImmutableList.copyOf(values));
    }

    @JsonValue
    @Override
    public List<String> get() {
        return super.get();
    }
}
