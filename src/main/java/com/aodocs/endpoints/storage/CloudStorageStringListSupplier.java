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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Loads a string list from a file in GCS.
 */
@Log
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CloudStorageStringListSupplier extends AsyncRefreshCachingStringListSupplier {

    //used for serialization only
    @JsonProperty
    private final String cloudStorageUrl;
    @JsonProperty
    private final boolean failOnMissing;

    //used to define defaults for serialization
    private CloudStorageStringListSupplier() {
        super(null);
        this.cloudStorageUrl = null;
        this.failOnMissing = true;
    }

    public CloudStorageStringListSupplier(String cloudStorageUrl) {
        this(cloudStorageUrl, null, true);
    }

    public CloudStorageStringListSupplier(String cloudStorageUrl, int ttlInSeconds) {
        this(cloudStorageUrl, ttlInSeconds, true);
    }

    public CloudStorageStringListSupplier(String cloudStorageUrl, int ttlInSeconds, boolean failOnMissing) {
        this(cloudStorageUrl, (Integer) ttlInSeconds, (Boolean) failOnMissing);
    }

    private CloudStorageStringListSupplier(String cloudStorageUrl, Integer ttlInSeconds, Boolean failOnMissing) {
        super(ttlInSeconds);
        Preconditions.checkNotNull(cloudStorageUrl);
        Preconditions.checkArgument(cloudStorageUrl.matches("gs://[^/]+/.+"));
        this.cloudStorageUrl = cloudStorageUrl;
        this.failOnMissing = MoreObjects.firstNonNull(failOnMissing, true);
        getUncached(); //validates failOnMissing
    }

    @Override
    protected List<String> getUncached() {
        try {
            return getWithException();
        } catch (IOException e) {
            if (failOnMissing) {
                throw new IllegalArgumentException("Cannot load " + cloudStorageUrl, e);
            }
            log.log(Level.WARNING, "Cannot load " + cloudStorageUrl, e);
            return Collections.emptyList();
        }
    }

    private List<String> getWithException() throws IOException {
        Path path = Paths.get(URI.create(cloudStorageUrl));
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

}
