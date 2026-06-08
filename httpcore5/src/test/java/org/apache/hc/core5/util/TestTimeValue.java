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

package org.apache.hc.core5.util;


import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestTimeValue {

    private void checkToDays(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toDays(value), TimeValue.of(value, timeUnit).toDays());
    }

    private void checkToHours(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toHours(value), TimeValue.of(value, timeUnit).toHours());
    }

    private void checkToMicroseconds(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toMicros(value), TimeValue.of(value, timeUnit).toMicroseconds());
    }

    private void checkToMilliseconds(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toMillis(value), TimeValue.of(value, timeUnit).toMilliseconds());
    }

    private void checkToMinutes(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toMinutes(value), TimeValue.of(value, timeUnit).toMinutes());
    }

    private void checkToNanoseconds(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toNanos(value), TimeValue.of(value, timeUnit).toNanoseconds());
    }

    private void checkToSeconds(final long value, final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit.toSeconds(value), TimeValue.of(value, timeUnit).toSeconds());
    }

    private void test(final long value) {
        for (final TimeUnit timeUnit : TimeUnit.values()) {
            checkToDays(value, timeUnit);
            checkToHours(value, timeUnit);
            checkToMinutes(value, timeUnit);
            checkToSeconds(value, timeUnit);
            checkToMilliseconds(value, timeUnit);
            checkToMicroseconds(value, timeUnit);
            checkToNanoseconds(value, timeUnit);
        }
    }

    @Test
    void test0() {
        test(0);
    }

    @Test
    void test1() {
        test(1);
    }

    @Test
    void testConvert() {
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).convert(TimeUnit.DAYS));
        Assertions.assertEquals(1000, TimeValue.ofSeconds(1).convert(TimeUnit.MILLISECONDS));
    }

    @Test
    void testDivide() {
        // nominator is 0, result should be 0.
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toDays());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toHours());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toMicroseconds());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toMilliseconds());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toMinutes());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toNanoseconds());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toSeconds());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toMillisecondsIntBound());
        Assertions.assertEquals(0, TimeValue.ofMilliseconds(0).divide(2).toSecondsIntBound());
        //
        Assertions.assertEquals(50, TimeValue.ofMilliseconds(100).divide(2).toMilliseconds());
        Assertions.assertEquals(0, TimeValue.ofMinutes(1).divide(2).toSeconds());
        Assertions.assertEquals(30, TimeValue.ofMinutes(1).divide(2, TimeUnit.SECONDS).toSeconds());
        Assertions.assertEquals(30000, TimeValue.ofMinutes(1).divide(2, TimeUnit.MILLISECONDS).toMilliseconds());
    }

    @Test
    void testDivideBy0() {
        Assertions.assertThrows(ArithmeticException.class, () ->
                TimeValue.ofMilliseconds(0).divide(0));
    }

    private void testFactory(final TimeUnit timeUnit) {
        Assertions.assertEquals(timeUnit, TimeValue.of(1, timeUnit).getTimeUnit());
        //
        final Duration duration = Duration.of(1, TimeValue.toChronoUnit(timeUnit));
        assertConvertion(duration);
    }

    @Test
    void testFactoryForDays() {
        testFactory(TimeUnit.DAYS);
    }

    @Test
    void testFactoryForDuration() {
        assertConvertion(Duration.ZERO);
        assertConvertion(Duration.ofDays(1));
        assertConvertion(Duration.ofHours(1));
        assertConvertion(Duration.ofMillis(1));
        assertConvertion(Duration.ofNanos(1));
        assertConvertion(Duration.ofSeconds(1));
        assertConvertion(Duration.ofSeconds(1, 1));
    }

    private void assertConvertion(final Duration duration) {
        Assertions.assertEquals(duration, TimeValue.of(duration).toDuration());
    }

    @Test
    void testFactoryForHours() {
        testFactory(TimeUnit.HOURS);
    }

    @Test
    void testFactoryForMicroseconds() {
        testFactory(TimeUnit.MICROSECONDS);
    }

    @Test
    void testFactoryForMilliseconds() {
        testFactory(TimeUnit.MILLISECONDS);
    }

    @Test
    void testFactoryForMinutes() {
        testFactory(TimeUnit.MINUTES);
    }

    @Test
    void testFactoryForNanoseconds() {
        testFactory(TimeUnit.NANOSECONDS);
    }

    @Test
    void testFactoryForSeconds() {
        testFactory(TimeUnit.SECONDS);
    }

    @Test
    void testMin() {
        final TimeValue nanos1 = TimeValue.ofNanoseconds(1);
        final TimeValue micros1 = TimeValue.ofMicroseconds(1);
        final TimeValue millis1 = TimeValue.ofMilliseconds(1);
        final TimeValue seconds1 = TimeValue.ofSeconds(1);
        final TimeValue minutes1 = TimeValue.ofMinutes(1);
        final TimeValue hours1 = TimeValue.ofHours(1);
        final TimeValue days1 = TimeValue.ofDays(1);
        //
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(nanos1));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(micros1));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(millis1));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(seconds1));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(minutes1));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(hours1));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.ZERO_MILLISECONDS.min(days1));
        //
        Assertions.assertEquals(nanos1, nanos1.min(nanos1));
        Assertions.assertEquals(nanos1, nanos1.min(micros1));
        Assertions.assertEquals(nanos1, nanos1.min(millis1));
        Assertions.assertEquals(nanos1, nanos1.min(seconds1));
        Assertions.assertEquals(nanos1, nanos1.min(minutes1));
        Assertions.assertEquals(nanos1, nanos1.min(hours1));
        Assertions.assertEquals(nanos1, nanos1.min(days1));
        //
        Assertions.assertEquals(nanos1, micros1.min(nanos1));
        Assertions.assertEquals(micros1, micros1.min(micros1));
        Assertions.assertEquals(micros1, micros1.min(millis1));
        Assertions.assertEquals(micros1, micros1.min(seconds1));
        Assertions.assertEquals(micros1, micros1.min(minutes1));
        Assertions.assertEquals(micros1, micros1.min(hours1));
        Assertions.assertEquals(micros1, micros1.min(days1));
        //
        Assertions.assertEquals(nanos1, millis1.min(nanos1));
        Assertions.assertEquals(micros1, millis1.min(micros1));
        Assertions.assertEquals(millis1, millis1.min(millis1));
        Assertions.assertEquals(millis1, millis1.min(seconds1));
        Assertions.assertEquals(millis1, millis1.min(minutes1));
        Assertions.assertEquals(millis1, millis1.min(hours1));
        Assertions.assertEquals(millis1, millis1.min(days1));
        //
        Assertions.assertEquals(nanos1, seconds1.min(nanos1));
        Assertions.assertEquals(micros1, seconds1.min(micros1));
        Assertions.assertEquals(millis1, seconds1.min(millis1));
        Assertions.assertEquals(seconds1, seconds1.min(seconds1));
        Assertions.assertEquals(seconds1, seconds1.min(minutes1));
        Assertions.assertEquals(seconds1, seconds1.min(hours1));
        Assertions.assertEquals(seconds1, seconds1.min(days1));
        //
        Assertions.assertEquals(nanos1, minutes1.min(nanos1));
        Assertions.assertEquals(micros1, minutes1.min(micros1));
        Assertions.assertEquals(millis1, minutes1.min(millis1));
        Assertions.assertEquals(seconds1, minutes1.min(seconds1));
        Assertions.assertEquals(minutes1, minutes1.min(minutes1));
        Assertions.assertEquals(minutes1, minutes1.min(hours1));
        Assertions.assertEquals(minutes1, minutes1.min(days1));
        //
        Assertions.assertEquals(nanos1, hours1.min(nanos1));
        Assertions.assertEquals(micros1, hours1.min(micros1));
        Assertions.assertEquals(millis1, hours1.min(millis1));
        Assertions.assertEquals(seconds1, hours1.min(seconds1));
        Assertions.assertEquals(minutes1, hours1.min(minutes1));
        Assertions.assertEquals(hours1, hours1.min(hours1));
        Assertions.assertEquals(hours1, hours1.min(days1));
        //
        Assertions.assertEquals(nanos1, days1.min(nanos1));
        Assertions.assertEquals(micros1, days1.min(micros1));
        Assertions.assertEquals(millis1, days1.min(millis1));
        Assertions.assertEquals(seconds1, days1.min(seconds1));
        Assertions.assertEquals(minutes1, days1.min(minutes1));
        Assertions.assertEquals(hours1, days1.min(hours1));
        Assertions.assertEquals(days1, days1.min(days1));
    }

    @Test
    void testMaxInt() {
        test(Integer.MAX_VALUE);
    }

    @Test
    void testMaxLong() {
        test(Long.MAX_VALUE);
    }

    @Test
    void testNegative1() {
        test(-1);
    }

    @Test
    void testToString() {
        Assertions.assertEquals("9223372036854775807 SECONDS", TimeValue.ofSeconds(Long.MAX_VALUE).toString());
        Assertions.assertEquals("0 MILLISECONDS", TimeValue.ZERO_MILLISECONDS.toString());
    }

    @Test
    void testFromString() throws ParseException {
        final TimeValue maxSeconds = TimeValue.ofSeconds(Long.MAX_VALUE);
        Assertions.assertEquals(maxSeconds, TimeValue.parse("9223372036854775807 SECONDS"));
        Assertions.assertEquals(maxSeconds, TimeValue.parse("9223372036854775807 SECONDS"));
        Assertions.assertEquals(maxSeconds, TimeValue.parse(" 9223372036854775807 SECONDS "));
        Assertions.assertEquals(maxSeconds, TimeValue.parse("9223372036854775807 Seconds"));
        Assertions.assertEquals(maxSeconds, TimeValue.parse("9223372036854775807  Seconds"));
        Assertions.assertEquals(maxSeconds, TimeValue.parse("9223372036854775807\tSeconds"));
        Assertions.assertEquals(TimeValue.ZERO_MILLISECONDS, TimeValue.parse("0 MILLISECONDS"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(1), TimeValue.parse("1 MILLISECOND"));
    }

    @Test
    void testToDuration() throws ParseException {
        Assertions.assertEquals(Long.MAX_VALUE, TimeValue.parse("9223372036854775807 SECONDS").toDuration().getSeconds());
    }

    @Test
    void testEqualsAndHashCode() {
        final TimeValue tv1 = TimeValue.ofMilliseconds(1000L);
        final TimeValue tv2 = TimeValue.ofMilliseconds(1001L);
        final TimeValue tv3 = TimeValue.ofMilliseconds(1000L);
        final TimeValue tv4 = TimeValue.ofSeconds(1L);
        final TimeValue tv5 = TimeValue.ofSeconds(1000L);

        Assertions.assertEquals(tv1, tv1);
        Assertions.assertNotEquals(null, tv1);
        Assertions.assertNotEquals(tv1, tv2);
        Assertions.assertEquals(tv1, tv3);
        Assertions.assertEquals(tv1, tv4);
        Assertions.assertEquals(tv4, tv1);
        Assertions.assertNotEquals(tv1, tv5);

        Assertions.assertNotEquals(tv1.hashCode(), tv2.hashCode());
        Assertions.assertEquals(tv1.hashCode(), tv3.hashCode());
        Assertions.assertEquals(tv1.hashCode(), tv4.hashCode());
        Assertions.assertEquals(tv4.hashCode(), tv1.hashCode());
        Assertions.assertNotEquals(tv1.hashCode(), tv5.hashCode());
    }

    @Test
    void testCompareTo() {
        final TimeValue tv1 = TimeValue.ofMilliseconds(1000L);
        final TimeValue tv2 = TimeValue.ofMilliseconds(1001L);
        final TimeValue tv3 = TimeValue.ofMilliseconds(1000L);
        final TimeValue tv4 = TimeValue.ofSeconds(1L);
        final TimeValue tv5 = TimeValue.ofSeconds(60L);
        final TimeValue tv6 = TimeValue.ofMinutes(1L);

        Assertions.assertTrue(tv1.compareTo(tv1) == 0);
        Assertions.assertTrue(tv1.compareTo(tv2) < 0);
        Assertions.assertTrue(tv1.compareTo(tv3) == 0);
        Assertions.assertTrue(tv1.compareTo(tv4) == 0);
        Assertions.assertTrue(tv1.compareTo(tv5) < 0);
        Assertions.assertTrue(tv6.compareTo(tv5) == 0);
        Assertions.assertTrue(tv6.compareTo(tv4) > 0);
        Assertions.assertThrows(NullPointerException.class, () -> tv1.compareTo(null));
    }

    // --- Compact format tests ---

    @Test
    void testParseCompactAllUnits() throws ParseException {
        Assertions.assertEquals(TimeValue.ofNanoseconds(100), TimeValue.parse("100ns"));
        Assertions.assertEquals(TimeValue.ofNanoseconds(100), TimeValue.parse("100 ns"));
        Assertions.assertEquals(TimeValue.ofMicroseconds(100), TimeValue.parse("100us"));
        Assertions.assertEquals(TimeValue.ofMicroseconds(100), TimeValue.parse("100 us"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(250), TimeValue.parse("250ms"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(250), TimeValue.parse("250 ms"));
        Assertions.assertEquals(TimeValue.ofSeconds(30), TimeValue.parse("30s"));
        Assertions.assertEquals(TimeValue.ofSeconds(30), TimeValue.parse("30 s"));
        Assertions.assertEquals(TimeValue.ofMinutes(5), TimeValue.parse("5m"));
        Assertions.assertEquals(TimeValue.ofMinutes(5), TimeValue.parse("5 m"));
        Assertions.assertEquals(TimeValue.ofHours(2), TimeValue.parse("2h"));
        Assertions.assertEquals(TimeValue.ofHours(2), TimeValue.parse("2 h"));
        Assertions.assertEquals(TimeValue.ofDays(1), TimeValue.parse("1d"));
        Assertions.assertEquals(TimeValue.ofDays(1), TimeValue.parse("1 d"));
    }

    @Test
    void testParseCompactCaseInsensitive() throws ParseException {
        Assertions.assertEquals(TimeValue.ofMilliseconds(250), TimeValue.parse("250MS"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(250), TimeValue.parse("250Ms"));
        Assertions.assertEquals(TimeValue.ofDays(5), TimeValue.parse("5D"));
        Assertions.assertEquals(TimeValue.ofDays(5), TimeValue.parse("5d"));
        Assertions.assertEquals(TimeValue.ofHours(2), TimeValue.parse("2H"));
        Assertions.assertEquals(TimeValue.ofHours(2), TimeValue.parse("2 h"));
    }

    @Test
    void testParseCompactSigns() throws ParseException {
        Assertions.assertEquals(TimeValue.ofSeconds(30), TimeValue.parse("+30s"));
        Assertions.assertEquals(TimeValue.ofSeconds(30), TimeValue.parse("+30 s"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(-1), TimeValue.parse("-1ms"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(-1), TimeValue.parse("-1 ms"));
        Assertions.assertEquals(TimeValue.ofNanoseconds(-100), TimeValue.parse("-100ns"));
        Assertions.assertEquals(TimeValue.ofDays(-1), TimeValue.parse("-1d"));
    }

    // --- Legacy format regression tests ---

    @Test
    void testParseLegacyRegression() throws ParseException {
        Assertions.assertEquals(TimeValue.ofSeconds(1), TimeValue.parse("1 SECOND"));
        Assertions.assertEquals(TimeValue.ofSeconds(1), TimeValue.parse("1 SECONDS"));
        Assertions.assertEquals(TimeValue.ofSeconds(1), TimeValue.parse("1 Seconds"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(1), TimeValue.parse("1 MILLISECOND"));
        Assertions.assertEquals(TimeValue.ofMilliseconds(1), TimeValue.parse("1 MILLISECONDS"));
        Assertions.assertEquals(TimeValue.ofMinutes(1), TimeValue.parse("1 MINUTE"));
        Assertions.assertEquals(TimeValue.ofMinutes(1), TimeValue.parse("1 M"));
        Assertions.assertEquals(TimeValue.ofSeconds(Long.MAX_VALUE), TimeValue.parse("9223372036854775807 SECONDS"));
    }

    // --- Round-trip tests ---

    @Test
    void testParseRoundTrip() throws ParseException {
        final TimeValue[] values = {
                TimeValue.ofNanoseconds(1),
                TimeValue.ofMicroseconds(1),
                TimeValue.ofMilliseconds(1),
                TimeValue.ofSeconds(1),
                TimeValue.ofMinutes(1),
                TimeValue.ofHours(1),
                TimeValue.ofDays(1),
                TimeValue.ofNanoseconds(999),
                TimeValue.ofMilliseconds(250),
                TimeValue.ofSeconds(3661),
                TimeValue.ofMinutes(90),
                TimeValue.ofHours(48),
                TimeValue.ofDays(365),
                TimeValue.ofSeconds(Long.MAX_VALUE),
                TimeValue.ofNanoseconds(Long.MAX_VALUE),
                TimeValue.ofMilliseconds(Long.MIN_VALUE),
        };
        for (final TimeValue tv : values) {
            final TimeValue parsed = TimeValue.parse(tv.toString());
            Assertions.assertEquals(tv, parsed, "Round-trip failed for: " + tv);
        }
    }

    // --- ISO-8601 duration tests ---

    @Test
    void testParseISO8601() throws ParseException {
        // PT0.25S = 250ms
        final TimeValue pt025s = TimeValue.parse("PT0.25S");
        Assertions.assertEquals(TimeValue.ofMilliseconds(250), pt025s);
        Assertions.assertEquals(Duration.ofMillis(250), pt025s.toDuration());

        // PT0.000001S = 1us
        final TimeValue pt000001s = TimeValue.parse("PT0.000001S");
        Assertions.assertEquals(TimeValue.ofMicroseconds(1), pt000001s);
        Assertions.assertEquals(Duration.ofNanos(1000), pt000001s.toDuration());

        // PT2H = 2 hours
        final TimeValue pt2h = TimeValue.parse("PT2H");
        Assertions.assertEquals(TimeValue.ofHours(2), pt2h);
        Assertions.assertEquals(Duration.ofHours(2), pt2h.toDuration());

        // PT15M = 15 minutes
        final TimeValue pt15m = TimeValue.parse("PT15M");
        Assertions.assertEquals(TimeValue.ofMinutes(15), pt15m);
        Assertions.assertEquals(Duration.ofMinutes(15), pt15m.toDuration());

        // P1D = 1 day
        final TimeValue p1d = TimeValue.parse("P1D");
        Assertions.assertEquals(TimeValue.ofDays(1), p1d);
        Assertions.assertEquals(Duration.ofDays(1), p1d.toDuration());

        // PT1H30M = 90 minutes (coarsest lossless unit)
        final TimeValue pt1h30m = TimeValue.parse("PT1H30M");
        Assertions.assertEquals(TimeValue.ofMinutes(90), pt1h30m);
        Assertions.assertEquals(Duration.ofMinutes(90), pt1h30m.toDuration());
    }

    @Test
    void testParseISO8601Complex() throws ParseException {
        // PT1H30M20S = 5420 seconds
        final TimeValue complex = TimeValue.parse("PT1H30M20S");
        Assertions.assertEquals(TimeValue.ofSeconds(5420), complex);
        Assertions.assertEquals(Duration.ofSeconds(5420), complex.toDuration());

        // P1DT2H = 26 hours
        final TimeValue p1dt2h = TimeValue.parse("P1DT2H");
        Assertions.assertEquals(TimeValue.ofHours(26), p1dt2h);
        Assertions.assertEquals(Duration.ofHours(26), p1dt2h.toDuration());
    }

    // --- Long boundary tests ---

    @Test
    void testParseLongBoundary() throws ParseException {
        final TimeValue maxNs = TimeValue.parse("9223372036854775807ns");
        Assertions.assertEquals(Long.MAX_VALUE, maxNs.getDuration());
        Assertions.assertEquals(TimeUnit.NANOSECONDS, maxNs.getTimeUnit());

        final TimeValue minMs = TimeValue.parse("-9223372036854775808ms");
        Assertions.assertEquals(Long.MIN_VALUE, minMs.getDuration());
        Assertions.assertEquals(TimeUnit.MILLISECONDS, minMs.getTimeUnit());
    }

    // --- Invalid input tests ---

    @Test
    void testParseInvalidEmpty() {
        assertParseThrows("");
        assertParseThrows("   ");
    }

    @Test
    void testParseInvalidMissingUnit() {
        assertParseThrows("12");
    }

    @Test
    void testParseInvalidMissingNumber() {
        assertParseThrows("ms");
    }

    @Test
    void testParseInvalidUnknownUnit() {
        assertParseThrows("12 fortnight");
    }

    @Test
    void testParseInvalidNonNumeric() {
        assertParseThrows("abc ms");
    }

    @Test
    void testParseInvalidISO8601() {
        assertParseThrows("PT-nope");
    }

    @Test
    void testParseInvalidDecimalWithLegacyUnit() {
        assertParseThrows("1.5 ms");
    }

    @Test
    void testParseInvalidISO8601Overflow() {
        assertParseThrows("PT9223372036854775808S");
    }

    private void assertParseThrows(final String input) {
        Assertions.assertThrows(Exception.class, () -> TimeValue.parse(input),
                "Expected exception for input: '" + input + "'");
    }

    // --- toString stability ---

    @Test
    void testToStringStability() throws ParseException {
        Assertions.assertEquals("1 SECONDS", TimeValue.parse("1s").toString());
        Assertions.assertEquals("250 MILLISECONDS", TimeValue.parse("250ms").toString());
        Assertions.assertEquals("90 MINUTES", TimeValue.parse("PT1H30M").toString());
        Assertions.assertEquals("2 HOURS", TimeValue.parse("PT2H").toString());
    }

}
