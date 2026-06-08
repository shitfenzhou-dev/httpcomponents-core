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
package org.apache.hc.core5.jackson2.bulk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.jackson2.JsonResultSink;
import org.apache.hc.core5.jackson2.http.RequestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonBulkArrayReaderTest {

    private List<RequestData> createExpectedObjects() {
        final List<RequestData> list = new ArrayList<>();
        final RequestData expectedObject1 = new RequestData();
        expectedObject1.setId(0);
        expectedObject1.setUrl(URI.create("http://httpbin.org/stream/3"));
        expectedObject1.setArgs(new HashMap<>());
        expectedObject1.generateHeaders(
                new BasicHeader("Host", "httpbin.org"),
                new BasicHeader("Connection", "close"),
                new BasicHeader("Referer", "http://httpbin.org/"),
                new BasicHeader("Accept", "application/json"),
                new BasicHeader("Accept-Encoding", "gzip, deflate"),
                new BasicHeader("Accept-Language", "en-US,en;q=0.9"),
                new BasicHeader("Cookie", "_gauges_unique_year=1; _gauges_unique=1; _gauges_unique_month=1; " +
                        "_gauges_unique_day=1; _gauges_unique_hour=1"),
                new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "snap Chromium/71.0.3578.98 Chrome/71.0.3578.98 Safari/537.36"));
        expectedObject1.setOrigin("xxx.xxx.xxx.xxx");
        list.add(expectedObject1);

        final RequestData expectedObject2 = new RequestData();
        expectedObject2.setId(1);
        expectedObject2.setUrl(URI.create("http://httpbin.org/stream/3"));
        expectedObject2.setArgs(new HashMap<>());
        expectedObject2.generateHeaders(
                new BasicHeader("Host", "httpbin.org"),
                new BasicHeader("Connection", "close"),
                new BasicHeader("Referer", "http://httpbin.org/"),
                new BasicHeader("Accept", "application/json"),
                new BasicHeader("Accept-Encoding", "gzip, deflate"),
                new BasicHeader("Accept-Language", "en-US,en;q=0.9"),
                new BasicHeader("Cookie", "_gauges_unique_year=1; _gauges_unique=1; _gauges_unique_month=1; " +
                        "_gauges_unique_day=1; _gauges_unique_hour=1"),
                new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "snap Chromium/71.0.3578.98 Chrome/71.0.3578.98 Safari/537.36"));
        expectedObject2.setOrigin("xxx.xxx.xxx.xxx");
        list.add(expectedObject2);

        final RequestData expectedObject3 = new RequestData();
        expectedObject3.setId(2);
        expectedObject3.setUrl(URI.create("http://httpbin.org/stream/3"));
        expectedObject3.setArgs(new HashMap<>());
        expectedObject3.generateHeaders(
                new BasicHeader("Host", "httpbin.org"),
                new BasicHeader("Connection", "close"),
                new BasicHeader("Referer", "http://httpbin.org/"),
                new BasicHeader("Accept", "application/json"),
                new BasicHeader("Accept-Encoding", "gzip, deflate"),
                new BasicHeader("Accept-Language", "en-US,en;q=0.9"),
                new BasicHeader("Cookie", "_gauges_unique_year=1; _gauges_unique=1; _gauges_unique_month=1; " +
                        "_gauges_unique_day=1; _gauges_unique_hour=1"),
                new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "snap Chromium/71.0.3578.98 Chrome/71.0.3578.98 Safari/537.36"));
        expectedObject3.setOrigin("xxx.xxx.xxx.xxx");
        list.add(expectedObject3);

        return list;
    }

    @Test
    void testJsonArrayReading() throws Exception {
        final JsonFactory factory = new JsonFactory();
        final ObjectMapper objectMapper = new ObjectMapper(factory);

        final URL resource = getClass().getResource("/sample6.json");
        Assertions.assertThat(resource).isNotNull();

        final List<RequestData> jsonDataList = new ArrayList<>();
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(objectMapper);
        bulkArrayReader.initialize(new TypeReference<RequestData>() {
        }, jsonDataList::add);
        try (final InputStream inputStream = resource.openStream()) {
            int l;
            final byte[] tmp = new byte[4096];
            while ((l = inputStream.read(tmp)) != -1) {
                bulkArrayReader.consume(ByteBuffer.wrap(tmp, 0, l));
            }
            bulkArrayReader.streamEnd();
        }

        final List<RequestData> expectedObjects = createExpectedObjects();
        Assertions.assertThat(jsonDataList.get(0)).usingRecursiveComparison().isEqualTo(expectedObjects.get(0));
        Assertions.assertThat(jsonDataList.get(1)).usingRecursiveComparison().isEqualTo(expectedObjects.get(1));
        Assertions.assertThat(jsonDataList.get(2)).usingRecursiveComparison().isEqualTo(expectedObjects.get(2));
    }

    @Test
    void testJsonArrayReadingWithClass() throws Exception {
        final JsonFactory factory = new JsonFactory();
        final ObjectMapper objectMapper = new ObjectMapper(factory);

        final URL resource = getClass().getResource("/sample6.json");
        Assertions.assertThat(resource).isNotNull();

        final List<RequestData> jsonDataList = new ArrayList<>();
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(objectMapper);
        bulkArrayReader.initialize(RequestData.class, jsonDataList::add);
        try (final InputStream inputStream = resource.openStream()) {
            int l;
            final byte[] tmp = new byte[4096];
            while ((l = inputStream.read(tmp)) != -1) {
                bulkArrayReader.consume(ByteBuffer.wrap(tmp, 0, l));
            }
            bulkArrayReader.streamEnd();
        }

        final List<RequestData> expectedObjects = createExpectedObjects();
        Assertions.assertThat(jsonDataList.get(0)).usingRecursiveComparison().isEqualTo(expectedObjects.get(0));
        Assertions.assertThat(jsonDataList.get(1)).usingRecursiveComparison().isEqualTo(expectedObjects.get(1));
        Assertions.assertThat(jsonDataList.get(2)).usingRecursiveComparison().isEqualTo(expectedObjects.get(2));
    }

    static class SimplePojo {
        private int id;
        public int getId() { return id; }
        public void setId(final int id) { this.id = id; }
    }

    @Test
    void testMemoryJsonArrayReadingWithClass() throws Exception {
        final JsonFactory factory = new JsonFactory();
        final ObjectMapper objectMapper = new ObjectMapper(factory);

        final List<SimplePojo> list = new ArrayList<>();
        final List<String> events = new ArrayList<>();
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(objectMapper);
        bulkArrayReader.initialize(SimplePojo.class, new JsonResultSink<SimplePojo>() {
            @Override
            public void begin(final int sizeHint) {
                events.add("begin");
            }

            @Override
            public void accept(final SimplePojo result) {
                list.add(result);
                events.add("accept");
            }

            @Override
            public void end() {
                events.add("end");
            }
        });

        final byte[] data = "[{\"id\":1},{\"id\":2}]".getBytes(StandardCharsets.UTF_8);
        bulkArrayReader.consume(ByteBuffer.wrap(data));
        bulkArrayReader.streamEnd();

        Assertions.assertThat(list).hasSize(2);
        Assertions.assertThat(list.get(0).getId()).isEqualTo(1);
        Assertions.assertThat(list.get(1).getId()).isEqualTo(2);

        Assertions.assertThat(events).containsExactly("begin", "accept", "accept", "end");
    }

    @Test
    void testNullChecks() {
        final JsonFactory factory = new JsonFactory();
        final ObjectMapper objectMapper = new ObjectMapper(factory);
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(objectMapper);

        Assertions.assertThrows(NullPointerException.class, () ->
                bulkArrayReader.initialize((Class<Object>) null, result -> {}));
        Assertions.assertThrows(NullPointerException.class, () ->
                bulkArrayReader.initialize(Object.class, null));

        Assertions.assertThrows(NullPointerException.class, () ->
                bulkArrayReader.initialize((TypeReference<Object>) null, result -> {}));
        Assertions.assertThrows(NullPointerException.class, () ->
                bulkArrayReader.initialize(new TypeReference<Object>() {}, null));
    }

    @Test
    void testChunkedReadingWithClass() throws Exception {
        final JsonFactory factory = new JsonFactory();
        final ObjectMapper objectMapper = new ObjectMapper(factory);

        final List<SimplePojo> list = new ArrayList<>();
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(objectMapper);
        bulkArrayReader.initialize(SimplePojo.class, list::add);

        final byte[] data = "[{\"id\":1},{\"id\":2}]".getBytes(StandardCharsets.UTF_8);
        for (final byte b : data) {
            bulkArrayReader.consume(ByteBuffer.wrap(new byte[] { b }));
        }
        bulkArrayReader.streamEnd();

        Assertions.assertThat(list).hasSize(2);
        Assertions.assertThat(list.get(0).getId()).isEqualTo(1);
        Assertions.assertThat(list.get(1).getId()).isEqualTo(2);
    }

    @Test
    void testMalformedJsonException() {
        final JsonFactory factory = new JsonFactory();
        final ObjectMapper objectMapper = new ObjectMapper(factory);

        final List<SimplePojo> list = new ArrayList<>();
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(objectMapper);
        bulkArrayReader.initialize(SimplePojo.class, list::add);

        final byte[] data = "[{\"id\":1},{\"id\":bad}]".getBytes(StandardCharsets.UTF_8);
        Assertions.assertThrows(IOException.class, () -> {
            bulkArrayReader.consume(ByteBuffer.wrap(data));
            bulkArrayReader.streamEnd();
        });
    }

}
