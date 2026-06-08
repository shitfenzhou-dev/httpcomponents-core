/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.http2.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class H2ConfigTest {

    @Test
    void builder() {
        // Create and start requester
        final H2Config h2Config = H2Config.custom()
                .setPushEnabled(false)
                .build();
        assertNotNull(h2Config);
    }

    @Test
    void checkValues() {
        // Create and start requester
        final H2Config h2Config = H2Config.custom()
                .setHeaderTableSize(1)
                .setMaxConcurrentStreams(1)
                .setMaxFrameSize(16384)
                .setPushEnabled(true)
                .setCompressionEnabled(true)
                .build();

        assertEquals(1, h2Config.getHeaderTableSize());
        assertEquals(1, h2Config.getMaxConcurrentStreams());
        assertEquals(16384, h2Config.getMaxFrameSize());
        assertTrue(h2Config.isPushEnabled());
        assertTrue(h2Config.isCompressionEnabled());
    }

    @Test
    void copy() {
        // Create and start requester
        final H2Config h2Config = H2Config.custom()
                .setHeaderTableSize(1)
                .setMaxConcurrentStreams(1)
                .setMaxFrameSize(16384)
                .setPushEnabled(true)
                .setCompressionEnabled(true)
                .setMaxContinuations(7)
                .build();

        final H2Config.Builder builder = H2Config.copy(h2Config);
        final H2Config h2Config2 = builder.build();

        assertAll(
                () -> assertEquals(h2Config.getHeaderTableSize(), h2Config2.getHeaderTableSize()),
                () -> assertEquals(h2Config.getInitialWindowSize(), h2Config2.getInitialWindowSize()),
                () -> assertEquals(h2Config.getMaxConcurrentStreams(), h2Config2.getMaxConcurrentStreams()),
                () -> assertEquals(h2Config.getMaxFrameSize(), h2Config2.getMaxFrameSize()),
                () -> assertEquals(h2Config.getMaxHeaderListSize(), h2Config2.getMaxHeaderListSize()),
                () -> assertEquals(h2Config.getMaxContinuations(), h2Config2.getMaxContinuations())
        );

        // 测试 copy 0 值
        final H2Config h2ConfigZero = H2Config.custom()
                .setMaxContinuations(0)
                .build();
        final H2Config h2ConfigZeroCopy = H2Config.copy(h2ConfigZero).build();
        assertEquals(0, h2ConfigZeroCopy.getMaxContinuations());
    }

    @Test
    void setMaxContinuationsNegative() {
        // 测试 setMaxContinuations(-1) 应该抛出异常
        final H2Config.Builder builder = H2Config.custom();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            builder.setMaxContinuations(-1);
        });
    }

    @Test
    void toStringIncludesMaxContinuations() {
        // 测试 toString() 包含 maxContinuations
        final H2Config h2Config = H2Config.custom()
                .setMaxContinuations(42)
                .build();
        final String str = h2Config.toString();
        Assertions.assertTrue(str.contains("maxContinuations=42"));
    }

}