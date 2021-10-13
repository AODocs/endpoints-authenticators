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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Common abstraction for String list suppliers, that cleans up before supplying the list
 * - comments are removed
 * - empty / blank lines are removed
 */
public abstract class StringListSupplier implements Supplier<List<String>> {

    @Override
    public final List<String> get() {
        return clean(getRaw());
    }

    protected abstract List<String> getRaw();

    /**
     * Remove comments (starting with #), trim and remove empty lines
     * @param lines the lines
     * @return the cleaned lines
     */
    private List<String> clean(List<String> lines) {
        if (lines == null) {
            return Collections.emptyList();
        }
        
        return lines.stream().map(input -> {
            int commentIndex = input.indexOf("#");
            String noComment = commentIndex != -1 ? input.substring(0, commentIndex) : input;
            return noComment.trim();
        }).filter(input -> !input.isEmpty()).collect(Collectors.toList());
    }

}
