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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.jackson2.JsonResultSink;
import org.apache.hc.core5.jackson2.http.RequestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonBulkArrayReaderTest {

    @Test
    void testJsonArrayReadingWithTypeReference() throws Exception {
        final List<RequestData> jsonDataList = readSample6WithTypeReference();

        assertFirstThreeRequestData(jsonDataList);
    }

    @Test
    void testJsonArrayReadingWithClass() throws Exception {
        final List<RequestData> jsonDataList = readSample6WithClass();

        assertFirstThreeRequestData(jsonDataList);
    }

    @Test
    void testJsonArrayReadingWithClassChunkedInput() throws Exception {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final List<RequestData> jsonDataList = new ArrayList<>();
        bulkArrayReader.initialize(RequestData.class, jsonDataList::add);

        final byte[] content = readResourceBytes("/sample6.json");
        consumeInChunks(bulkArrayReader, content, 7);
        bulkArrayReader.streamEnd();

        assertFirstThreeRequestData(jsonDataList);
    }

    @Test
    void testClassInitializationWithSimplePojoArrayLifecycle() throws Exception {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final List<String> events = new ArrayList<>();
        bulkArrayReader.initialize(SimplePojo.class, new JsonResultSink<SimplePojo>() {

            @Override
            public void begin(final int sizeHint) {
                events.add("begin:" + sizeHint);
            }

            @Override
            public void accept(final SimplePojo simplePojo) {
                events.add("accept:" + simplePojo.getId());
            }

            @Override
            public void end() {
                events.add("end");
            }

        });

        consumeInChunks(bulkArrayReader, "[{\"id\":1},{\"id\":2}]".getBytes(StandardCharsets.UTF_8), 3);
        bulkArrayReader.streamEnd();

        Assertions.assertThat(events).containsExactly("begin:-1", "accept:1", "accept:2", "end");
    }

    @Test
    void testInitializeRejectsNullArguments() {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final JsonResultSink<RequestData> sink = requestData -> {
        };

        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> bulkArrayReader.initialize((Class<RequestData>) null, sink))
                .withMessage("Class");
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> bulkArrayReader.initialize((TypeReference<RequestData>) null, sink))
                .withMessage("Type reference");
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> bulkArrayReader.initialize(RequestData.class, null))
                .withMessage("Result sink");
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> bulkArrayReader.initialize(new TypeReference<RequestData>() {
                }, null))
                .withMessage("Result sink");
    }

    @Test
    void testConsumePropagatesTypeMismatchAsIOException() throws Exception {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final AtomicBoolean ended = new AtomicBoolean(false);
        bulkArrayReader.initialize(SimplePojo.class, new JsonResultSink<SimplePojo>() {

            @Override
            public void accept(final SimplePojo simplePojo) {
            }

            @Override
            public void end() {
                ended.set(true);
            }

        });

        Assertions.assertThatIOException()
                .isThrownBy(() -> bulkArrayReader.consume(ByteBuffer.wrap("[{\"id\":\"x\"}]".getBytes(StandardCharsets.UTF_8))))
                .withMessageContaining("Cannot deserialize");
        Assertions.assertThat(ended.get()).isFalse();
    }

    @Test
    void testStreamEndPropagatesMalformedJsonAsIOException() throws Exception {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final AtomicBoolean ended = new AtomicBoolean(false);
        bulkArrayReader.initialize(SimplePojo.class, new JsonResultSink<SimplePojo>() {

            @Override
            public void accept(final SimplePojo simplePojo) {
            }

            @Override
            public void end() {
                ended.set(true);
            }

        });

        bulkArrayReader.consume(ByteBuffer.wrap("[{\"id\":1}".getBytes(StandardCharsets.UTF_8)));

        Assertions.assertThatIOException()
                .isThrownBy(bulkArrayReader::streamEnd);
        Assertions.assertThat(ended.get()).isFalse();
    }

    private List<RequestData> readSample6WithTypeReference() throws Exception {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final List<RequestData> jsonDataList = new ArrayList<>();
        bulkArrayReader.initialize(new TypeReference<RequestData>() {
        }, jsonDataList::add);

        final URL resource = getClass().getResource("/sample6.json");
        Assertions.assertThat(resource).isNotNull();

        try (final InputStream inputStream = resource.openStream()) {
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = inputStream.read(tmp)) != -1) {
                bulkArrayReader.consume(ByteBuffer.wrap(tmp, 0, l));
            }
        }
        bulkArrayReader.streamEnd();
        return jsonDataList;
    }

    private List<RequestData> readSample6WithClass() throws Exception {
        final JsonBulkArrayReader bulkArrayReader = new JsonBulkArrayReader(newObjectMapper());
        final List<RequestData> jsonDataList = new ArrayList<>();
        bulkArrayReader.initialize(RequestData.class, jsonDataList::add);

        final URL resource = getClass().getResource("/sample6.json");
        Assertions.assertThat(resource).isNotNull();

        try (final InputStream inputStream = resource.openStream()) {
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = inputStream.read(tmp)) != -1) {
                bulkArrayReader.consume(ByteBuffer.wrap(tmp, 0, l));
            }
        }
        bulkArrayReader.streamEnd();
        return jsonDataList;
    }

    private byte[] readResourceBytes(final String resourcePath) throws IOException {
        final URL resource = getClass().getResource(resourcePath);
        Assertions.assertThat(resource).isNotNull();
        try (final InputStream inputStream = resource.openStream();
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final byte[] tmp = new byte[64];
            int l;
            while ((l = inputStream.read(tmp)) != -1) {
                outputStream.write(tmp, 0, l);
            }
            return outputStream.toByteArray();
        }
    }

    private void consumeInChunks(final JsonBulkArrayReader bulkArrayReader, final byte[] content, final int chunkSize) throws IOException {
        for (int i = 0; i < content.length; i += chunkSize) {
            final int len = Math.min(chunkSize, content.length - i);
            bulkArrayReader.consume(ByteBuffer.wrap(content, i, len));
        }
    }

    private ObjectMapper newObjectMapper() {
        return new ObjectMapper(new JsonFactory());
    }

    private void assertFirstThreeRequestData(final List<RequestData> jsonDataList) {
        Assertions.assertThat(jsonDataList).hasSizeGreaterThanOrEqualTo(3);
        Assertions.assertThat(jsonDataList.get(0)).usingRecursiveComparison().isEqualTo(expectedRequestData(0));
        Assertions.assertThat(jsonDataList.get(1)).usingRecursiveComparison().isEqualTo(expectedRequestData(1));
        Assertions.assertThat(jsonDataList.get(2)).usingRecursiveComparison().isEqualTo(expectedRequestData(2));
    }

    private RequestData expectedRequestData(final int id) {
        final RequestData expectedObject = new RequestData();
        expectedObject.setId(id);
        expectedObject.setUrl(URI.create("http://httpbin.org/stream/3"));
        expectedObject.setArgs(new HashMap<>());
        expectedObject.generateHeaders(
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
        expectedObject.setOrigin("xxx.xxx.xxx.xxx");
        return expectedObject;
    }

    public static class SimplePojo {

        private int id;

        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }
    }

}
