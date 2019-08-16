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
package com.aodocs.endpoints.auth.authenticator.combined;

import com.aodocs.endpoints.context.AppengineHelper;
import com.aodocs.endpoints.storage.ClasspathStringListSupplier;
import com.aodocs.endpoints.storage.CloudStorageStringListSupplier;
import com.aodocs.endpoints.storage.DatastoreStringListSupplier;
import com.aodocs.endpoints.storage.MergingStringListSupplier;
import com.aodocs.endpoints.storage.StringListSupplier;
import com.google.common.base.Joiner;

/**
 * Utility methods to build complex string lists using a predefined convention.
 */
class CombinedStringListBuilder {

    /**
     * Combines a static list from the classpath (deploy time) and GCS (runtime)
     */
    private class CombinedStringListSupplier extends MergingStringListSupplier {
        private CombinedStringListSupplier(String listId, boolean whitelist) {
            //TODO make the sources configurable
            super(
                    new ClasspathStringListSupplier(getPath(listId, whitelist)),
                    new CloudStorageStringListSupplier("gs://" + id + "-" + getPath(listId, whitelist), ttlInSeconds
                    ),
                    new DatastoreStringListSupplier(id, ttlInSeconds)
            );
            //TODO log the sources
        }
    }

    private final int ttlInSeconds;
    private final String id;
    private final String fileExtension;

    CombinedStringListBuilder() {
        this(600, "authconfig", "txt");
    }

    private CombinedStringListBuilder(int ttlInSeconds, String id, String fileExtension) {
        this.ttlInSeconds = ttlInSeconds;
        this.id = id;
        this.fileExtension = fileExtension;
    }

    private String getPath(String listId, boolean whitelist) {
        return Joiner.on('/').join(
            AppengineHelper.getApplicationId(),
                whitelist ? "whitelist" : "blacklist",
                listId + "." + fileExtension);
    }

    StringListSupplier whitelist(String id) {
        return new CombinedStringListSupplier(id, true);
    }

    StringListSupplier blacklist(String id) {
        return new CombinedStringListSupplier(id, false);
    }

}
