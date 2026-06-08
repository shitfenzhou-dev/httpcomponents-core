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
import java.util.List;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.NameValuePairListMatcher;
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
        Assertions.assertArrayEquals(bytes, bytes2);
    }

    @Test
    void testKnownLengthContentToByteArray() throws Exception {
        final byte[] bytes = "Message content".getBytes(StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(bytes), bytes.length, null);
        final byte[] bytes2 = EntityUtils.toByteArray(entity);
        Assertions.assertNotNull(bytes2);
        Assertions.assertArrayEquals(bytes, bytes2);
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

    private static void assertNameValuePair(
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
        assertToByteArrayMaxLength(allBytes, allBytes.length, 0);
        assertToByteArrayMaxLength(allBytes, allBytes.length, 1);
        assertToByteArrayMaxLength(allBytes, allBytes.length, 2);
        assertToByteArrayMaxLength(allBytes, allBytes.length, allBytes.length - 1);
        assertToByteArrayMaxLength(allBytes, allBytes.length, allBytes.length);
        assertToByteArrayMaxLength(allBytes, allBytes.length, Integer.MAX_VALUE);
    }

    @Test
    void testByteArrayMaxResultLengthWithNoContentLength() throws IOException {
        final byte[] allBytes = new byte[5000];
        for (int i = 0; i < allBytes.length; i++) {
            allBytes[i] = 'b';
        }
        assertToByteArrayMaxLength(allBytes, -1, 0);
        assertToByteArrayMaxLength(allBytes, -1, 2);
        assertToByteArrayMaxLength(allBytes, -1, 7);
        assertToByteArrayMaxLength(allBytes, -1, allBytes.length);
        assertToByteArrayMaxLength(allBytes, -1, Integer.MAX_VALUE);
    }

    @Test
    void testStringMaxResultLength() throws IOException, ParseException {
        final String allMessage = "Message content";
        final byte[] allBytes = allMessage.getBytes(StandardCharsets.US_ASCII);
        assertToStringMaxLength(allBytes, allBytes.length, StandardCharsets.US_ASCII, 1, allMessage.substring(0, 1));
        assertToStringMaxLength(allBytes, allBytes.length, StandardCharsets.US_ASCII, 7, allMessage.substring(0, 7));
        assertToStringMaxLength(allBytes, allBytes.length, StandardCharsets.US_ASCII,
                allMessage.length() - 1, allMessage.substring(0, allMessage.length() - 1));
        assertToStringMaxLength(allBytes, allBytes.length, StandardCharsets.US_ASCII, allMessage.length(), allMessage);
        assertToStringMaxLength(allBytes, allBytes.length, StandardCharsets.US_ASCII, Integer.MAX_VALUE, allMessage);
    }

    @Test
    void testStringMaxResultLengthUtf8MultiByteContent() {
        final String allMessage = "Grüße世界Привет";
        final byte[] allBytes = allMessage.getBytes(StandardCharsets.UTF_8);
        final int maxResultLength = 5;

        Assertions.assertDoesNotThrow(() -> {
            final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), -1,
                    ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
            final String result = EntityUtils.toString(entity, StandardCharsets.UTF_8, maxResultLength);
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.length() <= maxResultLength);
            Assertions.assertEquals(allMessage.substring(0, maxResultLength), result);
        });
    }

    @Test
    void testParseMaxStreamLengthUsesTruncatedPrefix() throws Exception {
        final String form = "Name1=Value1&Name2&Name3=Value3";
        final int maxStreamLength = "Name1=Value1&Name2".length();
        final List<NameValuePair> expected = WWWFormCodec.parse(form.substring(0, maxStreamLength), StandardCharsets.US_ASCII);
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(form.getBytes(StandardCharsets.US_ASCII)),
                -1, ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.US_ASCII));

        final List<NameValuePair> result = EntityUtils.parse(entity, maxStreamLength);

        NameValuePairListMatcher.assertEqualsTo(result, expected.toArray(new NameValuePair[0]));
    }

    private static void assertToByteArrayMaxLength(final byte[] allBytes, final int contentLength, final int maxResultLength)
            throws IOException {
        final BasicHttpEntity entity = contentLength >= 0
                ? new BasicHttpEntity(new ByteArrayInputStream(allBytes), contentLength, null)
                : new BasicHttpEntity(new ByteArrayInputStream(allBytes), null);
        final byte[] bytes = EntityUtils.toByteArray(entity, maxResultLength);
        Assertions.assertNotNull(bytes);
        Assertions.assertArrayEquals(copyOfPrefix(allBytes, Math.min(allBytes.length, maxResultLength)), bytes);
    }

    private static void assertToStringMaxLength(final byte[] allBytes, final int contentLength,
            final Charset charset, final int maxResultLength, final String expected) throws IOException, ParseException {
        final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(allBytes), contentLength,
                ContentType.TEXT_PLAIN.withCharset(charset));
        final String string = EntityUtils.toString(entity, charset, maxResultLength);
        Assertions.assertNotNull(string);
        Assertions.assertEquals(expected, string);
    }

    private static byte[] copyOfPrefix(final byte[] bytes, final int length) {
        final byte[] copy = new byte[length];
        System.arraycopy(bytes, 0, copy, 0, length);
        return copy;
    }

}
