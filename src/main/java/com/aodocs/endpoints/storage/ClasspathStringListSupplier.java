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
import com.google.api.client.util.Throwables;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Load a string list from the classpath (no reload).
 */
@Log
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ClasspathStringListSupplier extends StaticStringListSupplier {

    //used only for serialization
    @JsonProperty
    private final String classpathResource;
    @JsonProperty
    private final boolean failOnMissing;

    //used to define defaults for serialization
    private ClasspathStringListSupplier() {
        this("", true);
    }

    /**
     * Will fail on missing resource.
     *
     * @param classpathResource
     */
    public ClasspathStringListSupplier(String classpathResource) {
        this(classpathResource, true);
    }

    /**
     *
     * @param classpathResource
     * @param failOnMissing defines if should fail if resource is missing. When false, provides empty list.
     */
    public ClasspathStringListSupplier(String classpathResource, boolean failOnMissing) {
        super(read(classpathResource, failOnMissing));
        this.classpathResource = Preconditions.checkNotNull(classpathResource);
        this.failOnMissing = failOnMissing;
    }

    private static List<String> read(String resourceName, boolean failOnMissing) {
        try {
            return Resources.readLines(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            if (failOnMissing)
                throw Throwables.propagate(e);
            log.log(Level.WARNING, "Cannot load " + resourceName, e);
            return Collections.emptyList();
        }
    }

}
