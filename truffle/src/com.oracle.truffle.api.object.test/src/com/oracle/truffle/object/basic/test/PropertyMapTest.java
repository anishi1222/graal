/*
 * Copyright (c) 2017, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.object.basic.test;

import static com.oracle.truffle.object.basic.test.DOTestAsserts.invokeGetter;
import static com.oracle.truffle.object.basic.test.DOTestAsserts.invokeMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.oracle.truffle.api.object.Location;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

@SuppressWarnings("deprecation")
public class PropertyMapTest {

    final Shape rootShape = Shape.newBuilder().build();
    final Map<Object, Property> empty = invokeMethod("getPropertyMap", rootShape);

    @Test
    public void testPropertyMap() {
        Map<Object, Property> map = empty;
        Map<Object, Property> referenceMap = new LinkedHashMap<>();

        Random rnd = new Random();
        final int size = 1000;
        int[] randomSequence = rnd.ints().limit(size).toArray();
        int[] shuffledSequence = randomSequence.clone();
        shuffle(shuffledSequence, rnd);

        // fill the map
        for (int i = 0; i < size; i++) {
            int id = randomSequence[i];
            String key = String.valueOf(id);
            Property value = Property.create(key, newLocation(id), 0);
            map = copyAndPut(map, key, value);
            referenceMap.put(key, value);
            assertEqualsOrdered(referenceMap, map);
        }

        // put the same values again, should not modify the map
        var initial = map;
        for (int i = 0; i < size; i++) {
            int id = randomSequence[i];
            String key = String.valueOf(id);
            Property value = Property.create(key, newLocation(id), 0);
            map = copyAndPut(map, key, value);
            assertSame(initial, map);
        }
        assertEqualsOrdered(referenceMap, map);

        // update existing values
        for (int i = 0; i < size; i++) {
            int id = randomSequence[i];
            String key = String.valueOf(id);
            Property value = Property.create(key, newLocation((double) id), 0);
            map = copyAndPut(map, key, value);
            referenceMap.put(key, value);
        }
        assertEqualsOrdered(referenceMap, map);
        for (int i = size - 1; i >= 0; i--) {
            int id = randomSequence[i];
            String key = String.valueOf(id);
            Property value = Property.create(key, newLocation((double) id), 0);
            map = copyAndPut(map, key, value);
            referenceMap.put(key, value);
        }
        assertEqualsOrdered(referenceMap, map);

        // update existing values, in random order
        for (int i = 0; i < size; i++) {
            int id = shuffledSequence[i];
            String key = String.valueOf(id);
            Property value = Property.create(key, newLocation((long) id), 0);
            map = copyAndPut(map, key, value);
            referenceMap.put(key, value);
        }
        assertEqualsOrdered(referenceMap, map);

        // remove keys
        for (int i = size - 10; i < size; i++) {
            int id = randomSequence[i];
            String key = String.valueOf(id);
            map = copyAndRemove(map, key);
            referenceMap.remove(key);
            assertEqualsOrdered(referenceMap, map);
        }
        for (int i = 10; i >= 0; i--) {
            int id = randomSequence[i];
            String key = String.valueOf(id);
            map = copyAndRemove(map, key);
            referenceMap.remove(key);
            assertEqualsOrdered(referenceMap, map);
        }
        for (int i = 0; i < size; i++) {
            int id = shuffledSequence[i];
            String key = String.valueOf(id);
            map = copyAndRemove(map, key);
            referenceMap.remove(key);
            assertEqualsOrdered(referenceMap, map);
        }
    }

    private static Map<Object, Property> copyAndPut(Map<Object, Property> map, String key, Property value) {
        return invokeMethod("copyAndPut", map, key, value);
    }

    private static Map<Object, Property> copyAndRemove(Map<Object, Property> map, String key) {
        return invokeMethod("copyAndRemove", map, key);
    }

    private Location newLocation(Object id) {
        return invokeMethod("locationForValue", invokeGetter("allocator", rootShape), id);
    }

    void assertEqualsOrdered(Map<Object, Property> referenceMap, Map<Object, Property> map) {
        assertEquals(referenceMap, map);
        for (Iterator<Map.Entry<Object, Property>> it1 = referenceMap.entrySet().iterator(), it2 = map.entrySet().iterator(); it1.hasNext() && it2.hasNext();) {
            Map.Entry<Object, Property> e1 = it1.next();
            Map.Entry<Object, Property> e2 = it2.next();
            assertEquals(e1.getKey(), e2.getKey());
            assertEquals(e1.getValue(), e2.getValue());
            assertEquals(it1.hasNext(), it2.hasNext());
        }
        for (Iterator<Object> it1 = List.copyOf(referenceMap.keySet()).listIterator(referenceMap.size()), it2 = invokeGetter("reverseOrderedKeyIterator", map); it1.hasNext() && it2.hasNext();) {
            assertEquals(it1.next(), it2.next());
            assertEquals(it1.hasNext(), it2.hasNext());
        }
        for (Iterator<Property> it1 = List.copyOf(referenceMap.values()).listIterator(referenceMap.size()), it2 = invokeGetter("reverseOrderedValueIterator", map); it1.hasNext() && it2.hasNext();) {
            assertEquals(it1.next(), it2.next());
            assertEquals(it1.hasNext(), it2.hasNext());
        }
        for (Iterator<Object> it1 = referenceMap.keySet().iterator(), it2 = map.keySet().iterator(); it1.hasNext() && it2.hasNext();) {
            assertEquals(it1.next(), it2.next());
            assertEquals(it1.hasNext(), it2.hasNext());
        }
        for (Iterator<Property> it1 = referenceMap.values().iterator(), it2 = map.values().iterator(); it1.hasNext() && it2.hasNext();) {
            assertEquals(it1.next(), it2.next());
            assertEquals(it1.hasNext(), it2.hasNext());
        }
    }

    private static void shuffle(int[] array, Random rnd) {
        for (int i = array.length; i > 1; i--) {
            int j = rnd.nextInt(i);
            int tmp = array[i - 1];
            array[i - 1] = array[j];
            array[j] = tmp;
        }
    }
}
