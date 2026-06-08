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

package org.apache.hc.core5.http.io.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.WWWFormCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EntityUtils}.
 *
 */
class TestEntityUtils {

    @Test
    void testNullEntityToByteArray() {
        Assertions.assertThrows(NullPointerException.class, () ->
                EntityUtils.toByteArray(null));
    }

    @Test
    void testMaxIntContentToByteArray() {
        final byte[] content = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(content),
                Integer.MAX_VALUE + 100L, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.US_ASCII));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                EntityUtils.toByteArray(entity));
    }

    @Test
    void testUnknownLengthContentToByteArray() throws Exception {
        final byte[] bytes = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes), -1, null);
        final byte[] bytes2 = EntityUtils.toByteArray(entity);
        Assertions.assertNotNull(bytes2);
        Assertions.assertEquals(bytes.length, bytes2.length);
        for (int i = 0; i < bytes.length; i++) {
            Assertions.assertEquals(bytes[i], bytes2[i]);
        }
    }

    @Test
    void testKnownLengthContentToByteArray() throws Exception {
        final byte[] bytes = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes), bytes.length, null);
        final byte[] bytes2 = EntityUtils.toByteArray(entity);
        Assertions.assertNotNull(bytes2);
        Assertions.assertEquals(bytes.length, bytes2.length);
        for (int i = 0; i < bytes.length; i++) {
            Assertions.assertEquals(bytes[i], bytes2[i]);
        }
    }

    @Test
    void testNullEntityToString() {
        Assertions.assertThrows(NullPointerException.class, () -> EntityUtils.toString(null));
    }

    @Test
    void testMaxIntContentToString() {
        final byte[] content = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(content),
                Integer.MAX_VALUE + 100L, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.US_ASCII));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                EntityUtils.toString(entity, "US-ASCII"));
    }

    @Test
    void testUnknownLengthContentToString() throws Exception {
        final byte[] bytes = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes), -1, null);
        final String s = EntityUtils.toString(entity, "US-ASCII");
        Assertions.assertEquals("Message content", s);
    }

    @Test
    void testKnownLengthContentToString() throws Exception {
        final byte[] bytes = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes), bytes.length,
                ContentType.TEXT_PLAIN.withCharset(StandardCharsets.US_ASCII));
        final String s = EntityUtils.toString(entity, StandardCharsets.US_ASCII);
        Assertions.assertEquals("Message content", s);
    }

    static final int SWISS_GERMAN_HELLO [] = {
        0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4
    };

    static final int RUSSIAN_HELLO [] = {
        0x412, 0x441, 0x435, 0x43C, 0x5F, 0x43F, 0x440, 0x438,
        0x432, 0x435, 0x442
    };

    private static String constructString(final int [] unicodeChars) {
        final StringBuilder buffer = new StringBuilder();
        if (unicodeChars != null) {
            for (final int unicodeChar : unicodeChars) {
                buffer.append((char)unicodeChar);
            }
        }
        return buffer.toString();
    }

    @Test
    void testNoCharsetContentToString() {
        final String content = constructString(SWISS_GERMAN_HELLO);
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes), ContentType.TEXT_PLAIN);
        Assertions.assertDoesNotThrow(() -> EntityUtils.toString(entity));
    }

    @Test
    void testDefaultCharsetContentToString() throws Exception {
        final String content = constructString(RUSSIAN_HELLO);
        final byte[] bytes = content.getBytes(Charset.forName("KOI8-R"));
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes),
                ContentType.parse("text/plain"));
        final String s = EntityUtils.toString(entity, Charset.forName("KOI8-R"));
        Assertions.assertEquals(content, s);
    }

    @Test
    void testContentWithContentTypeToString() throws Exception {
        final String content = constructString(RUSSIAN_HELLO);
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes),
                ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
        final String s = EntityUtils.toString(entity, "ISO-8859-1");
        Assertions.assertEquals(content, s);
    }

    @Test
    void testContentWithInvalidContentTypeToString() throws Exception {
        final String content = constructString(RUSSIAN_HELLO);
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        final HttpEntity entity = new AbstractHttpEntity("text/plain; charset=nosuchcharset", null) {

            @Override
            public InputStream getContent() throws IOException, UnsupportedOperationException {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public boolean isStreaming() {
                return false;
            }

            @Override
            public long getContentLength() {
                return bytes.length;
            }

            @Override
            public void close() throws IOException {
            }

        };
        final String s = EntityUtils.toString(entity, "UTF-8");
        Assertions.assertEquals(content, s);
    }

    private static void assertNameValuePair (
            final NameValuePair parameter,
            final String expectedName,
            final String expectedValue) {
        Assertions.assertEquals(parameter.getName(), expectedName);
        Assertions.assertEquals(parameter.getValue(), expectedValue);
    }

    @Test
    void testParseEntity() throws Exception {
        final StringEntity entity1 = new StringEntity("Name1=Value1", ContentType.APPLICATION_FORM_URLENCODED);
        final List<NameValuePair> result = EntityUtils.parse(entity1);
        Assertions.assertEquals(1, result.size());
        assertNameValuePair(result.get(0), "Name1", "Value1");

        final StringEntity entity2 = new StringEntity("Name1=Value1", ContentType.parse("text/test"));
        Assertions.assertTrue(EntityUtils.parse(entity2).isEmpty());
    }

    @Test
    void testParseUTF8Entity() throws Exception {
        final String ru_hello = constructString(RUSSIAN_HELLO);
        final String ch_hello = constructString(SWISS_GERMAN_HELLO);
        final List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("russian", ru_hello));
        parameters.add(new BasicNameValuePair("swiss", ch_hello));

        final String s = WWWFormCodec.format(parameters, StandardCharsets.UTF_8);

        Assertions.assertEquals("russian=%D0%92%D1%81%D0%B5%D0%BC_%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82" +
                "&swiss=Gr%C3%BCezi_z%C3%A4m%C3%A4", s);
        final StringEntity entity = new StringEntity(s,
                ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8));
        final List<NameValuePair> result = EntityUtils.parse(entity);
        Assertions.assertEquals(2, result.size());
        assertNameValuePair(result.get(0), "russian", ru_hello);
        assertNameValuePair(result.get(1), "swiss", ch_hello);
    }

    @Test
    void testByteArrayMaxResultLength() throws IOException {
        final byte[] allBytes = "Message content".getBytes(StandardCharsets.US_ASCII);
        final Map<Integer, byte[]> testCases = new HashMap<>();
        testCases.put(0, new byte[]{});
        testCases.put(1, Arrays.copyOfRange(allBytes, 0, 1));
        testCases.put(2, Arrays.copyOfRange(allBytes, 0, 2));
        testCases.put(allBytes.length - 1, Arrays.copyOfRange(allBytes, 0, allBytes.length - 1));
        testCases.put(allBytes.length, allBytes);
        testCases.put(Integer.MAX_VALUE, allBytes);

        for (final Map.Entry<Integer, byte[]> tc : testCases.entrySet()) {
            final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), allBytes.length, null);

            final byte[] bytes = EntityUtils.toByteArray(entity, tc.getKey());
            final byte[] expectedBytes = tc.getValue();
            Assertions.assertNotNull(bytes);
            Assertions.assertEquals(expectedBytes.length, bytes.length);
            for (int i = 0; i < expectedBytes.length; i++) {
                Assertions.assertEquals(expectedBytes[i], bytes[i]);
            }
        }
    }

    @Test
    void testByteArrayMaxResultLengthUnknownLargeEntity() throws IOException {
        final byte b = 'X';
        final int size = 8192;
        final byte[] allBytes = new byte[size];
        Arrays.fill(allBytes, b);

        final int[] limits = {0, 1, 10, 4095, 4096, 4097, size - 1, size};
        for (final int maxLen : limits) {
            final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), null);
            final byte[] bytes = EntityUtils.toByteArray(entity, maxLen);
            Assertions.assertNotNull(bytes, "maxLen=" + maxLen);
            Assertions.assertEquals(Math.min(maxLen, size), bytes.length, "maxLen=" + maxLen);
            for (int i = 0; i < bytes.length; i++) {
                Assertions.assertEquals(b, bytes[i], "maxLen=" + maxLen + " index=" + i);
            }
        }
    }

    @Test
    void testByteArrayMaxResultLengthWithNoContentLength() throws IOException {
        final byte b = 'b';
        final byte[] allBytes = new byte[5000];
        Arrays.fill(allBytes, b);
        final Map<Integer, byte[]> testCases = new HashMap<>();
        testCases.put(0, new byte[]{});
        testCases.put(2, Arrays.copyOfRange(allBytes, 0, 2));
        testCases.put(allBytes.length, allBytes);
        testCases.put(Integer.MAX_VALUE, allBytes);

        for (final Map.Entry<Integer, byte[]> tc : testCases.entrySet()) {
            final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), null);

            final byte[] bytes = EntityUtils.toByteArray(entity, tc.getKey());
            final byte[] expectedBytes = tc.getValue();
            Assertions.assertNotNull(bytes);
            Assertions.assertEquals(expectedBytes.length, bytes.length);
            for (int i = 0; i < expectedBytes.length; i++) {
                Assertions.assertEquals(expectedBytes[i], bytes[i]);
            }
        }
    }

    @Test
    void testStringMaxResultLength() throws IOException, ParseException {
        final String allMessage = "Message content";
        final byte[] allBytes = allMessage.getBytes(StandardCharsets.US_ASCII);
        final Map<Integer, String> testCases = new HashMap<>();
        testCases.put(1, allMessage.substring(0, 1));
        testCases.put(7, allMessage.substring(0, 7));
        testCases.put(allMessage.length(), allMessage);
        testCases.put(Integer.MAX_VALUE, allMessage);

        for (final Map.Entry<Integer, String> tc : testCases.entrySet()) {
            final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), allBytes.length, null);
            final String string = EntityUtils.toString(entity, StandardCharsets.US_ASCII, tc.getKey());
            final String expectedString = tc.getValue();
            Assertions.assertNotNull(string);
            Assertions.assertEquals(expectedString, string);
        }
    }

    @Test
    void testStringMaxResultLengthMultiByteUTF8() throws IOException, ParseException {
        final String allMessage = "你好世界Hello";
        final byte[] allBytes = allMessage.getBytes(StandardCharsets.UTF_8);
        final int[] limits = {1, 2, 4, 6, allMessage.length()};
        for (final int maxLen : limits) {
            final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), allBytes.length,
                    ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
            final String result = EntityUtils.toString(entity, StandardCharsets.UTF_8, maxLen);
            Assertions.assertNotNull(result, "maxLen=" + maxLen);
            Assertions.assertTrue(result.length() <= maxLen,
                    "result.length()=" + result.length() + " > maxLen=" + maxLen);
            Assertions.assertEquals(allMessage.substring(0, result.length()), result,
                    "maxLen=" + maxLen);
        }
    }

    @Test
    void testParseMaxStreamLength() throws IOException {
        final String fullContent = "key1=value1&key2=value2&key3=value3";
        final byte[] allBytes = fullContent.getBytes(StandardCharsets.UTF_8);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), allBytes.length,
                ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8));

        final List<NameValuePair> result = EntityUtils.parse(entity, 16);
        Assertions.assertNotNull(result);
        final String truncated = fullContent.substring(0, 16);
        final List<NameValuePair> expected = WWWFormCodec.parse(truncated, StandardCharsets.UTF_8);
        Assertions.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertNameValuePair(result.get(i), expected.get(i).getName(), expected.get(i).getValue());
        }
    }

    @Test
    void testParseMaxStreamLengthTruncatesNameValuePair() throws IOException {
        final String fullContent = "key1=value1&key2=value2";
        final byte[] allBytes = fullContent.getBytes(StandardCharsets.UTF_8);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), allBytes.length,
                ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8));

        final List<NameValuePair> result = EntityUtils.parse(entity, 10);
        Assertions.assertNotNull(result);
        final String truncated = fullContent.substring(0, 10);
        final List<NameValuePair> expected = WWWFormCodec.parse(truncated, StandardCharsets.UTF_8);
        Assertions.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertNameValuePair(result.get(i), expected.get(i).getName(), expected.get(i).getValue());
        }
    }

}
