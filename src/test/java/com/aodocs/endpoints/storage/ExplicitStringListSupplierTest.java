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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Clement on 14/10/2016.
 */
public class ExplicitStringListSupplierTest {

    @Test
    public void testGet() {
        assertEquals(ImmutableList.of("1"), new ExplicitStringListSupplier("1").get());
        //vamlues can be repeated
        assertEquals(ImmutableList.of("1", "1"), new ExplicitStringListSupplier("1", "1").get());
        assertEquals(ImmutableList.of("1", "2"), new ExplicitStringListSupplier("1" , "2").get());
    }

    @Test
    public void testTrim() {
        assertEquals(ImmutableList.of("1"), new ExplicitStringListSupplier("1 ").get());
        assertEquals(ImmutableList.of("1"), new ExplicitStringListSupplier(" 1").get());
        assertEquals(ImmutableList.of("1"), new ExplicitStringListSupplier(" 1 ").get());
    }

    @Test
    public void testEmptyLines() {
        assertEquals(ImmutableList.of(), new ExplicitStringListSupplier("").get());
        //space
        assertEquals(ImmutableList.of(), new ExplicitStringListSupplier(" ").get());
        //tab
        assertEquals(ImmutableList.of(), new ExplicitStringListSupplier(" ").get());
    }

    @Test
    public void testComments() {
        assertEquals(ImmutableList.of(), new ExplicitStringListSupplier("#empty line").get());
        assertEquals(ImmutableList.of("with content"), new ExplicitStringListSupplier("with content #empty line").get());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        new ExplicitStringListSupplier((String)null);
    }

}
