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

import java.math.BigInteger;
import java.text.ParseException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

/**
 * Represents a time value as a {@code long} time and a {@link TimeUnit}.
 *
 * @since 5.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class TimeValue implements Comparable<TimeValue> {

    static final int INT_UNDEFINED = -1;

    private static final Pattern SIMPLE_TIME_VALUE_PATTERN = Pattern.compile("^([+-]?\\d+)\\s+([A-Za-z]+)$");
    private static final Pattern COMPACT_TIME_VALUE_PATTERN = Pattern.compile("^([+-]?\\d+)([A-Za-z]+)$");
    private static final BigInteger NANOS_PER_SECOND = BigInteger.valueOf(1000000000L);
    private static final BigInteger NANOS_PER_MINUTE = BigInteger.valueOf(60L).multiply(NANOS_PER_SECOND);
    private static final BigInteger NANOS_PER_HOUR = BigInteger.valueOf(60L).multiply(NANOS_PER_MINUTE);
    private static final BigInteger NANOS_PER_DAY = BigInteger.valueOf(24L).multiply(NANOS_PER_HOUR);
    private static final TimeUnit[] PARSE_EXACT_TIME_UNITS = {
            TimeUnit.DAYS,
            TimeUnit.HOURS,
            TimeUnit.MINUTES,
            TimeUnit.SECONDS,
            TimeUnit.MILLISECONDS,
            TimeUnit.MICROSECONDS,
            TimeUnit.NANOSECONDS
    };

    /**
     * A constant holding the maximum value a {@code TimeValue} can have: {@code Long.MAX_VALUE} days.
     */
    public static final TimeValue MAX_VALUE = ofDays(Long.MAX_VALUE);

    /**
     * A negative one millisecond {@link TimeValue}.
     */
    public static final TimeValue NEG_ONE_MILLISECOND = TimeValue.of(INT_UNDEFINED, TimeUnit.MILLISECONDS);

    /**
     * A negative one second {@link TimeValue}.
     */
    public static final TimeValue NEG_ONE_SECOND = TimeValue.of(INT_UNDEFINED, TimeUnit.SECONDS);

    /**
     * A zero milliseconds {@link TimeValue}.
     */
    public static final TimeValue ZERO_MILLISECONDS = TimeValue.of(0, TimeUnit.MILLISECONDS);

    /**
     * Returns the given {@code long} value as an {@code int} where long values out of int range are returned as
     * {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}.
     *
     * <p>
     * For example: {@code TimeValue.asBoundInt(Long.MAX_VALUE)} returns {@code Integer.MAX_VALUE}.
     * </p>
     *
     * @param value a long value to convert
     * @return an int value bound within {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}.
     */
    public static int asBoundInt(final long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    /**
     * Returns the given {@code timeValue} if it is not {@code null}, if {@code null} then returns the given
     * {@code defaultValue}.
     *
     * @param <T> The type of {@link TimeValue}.
     * @param timeValue may be {@code null}
     * @param defaultValue may be {@code null}
     * @return {@code timeValue} or {@code defaultValue}
     */
    public static <T extends TimeValue> T defaultsTo(final T timeValue, final T defaultValue) {
        return timeValue != null ? timeValue : defaultValue;
    }

    /**
     * Returns the given {@code timeValue} if it is not {@code null}, if {@code null} then returns
     * {@link #NEG_ONE_SECOND}.
     *
     * @param timeValue may be {@code null}
     * @return {@code timeValue} or {@link #NEG_ONE_SECOND}
     */
    public static TimeValue defaultsToNegativeOneMillisecond(final TimeValue timeValue) {
        return defaultsTo(timeValue, NEG_ONE_MILLISECOND);
    }

    /**
     * Returns the given {@code timeValue} if it is not {@code null}, if {@code null} then returns
     * {@link #NEG_ONE_SECOND}.
     *
     * @param timeValue may be {@code null}
     * @return {@code timeValue} or {@link #NEG_ONE_SECOND}
     */
    public static TimeValue defaultsToNegativeOneSecond(final TimeValue timeValue) {
        return defaultsTo(timeValue, NEG_ONE_SECOND);
    }

    /**
     * Returns the given {@code timeValue} if it is not {@code null}, if {@code null} then returns
     * {@link #ZERO_MILLISECONDS}.
     *
     * @param timeValue may be {@code null}
     * @return {@code timeValue} or {@link #ZERO_MILLISECONDS}
     */
    public static TimeValue defaultsToZeroMilliseconds(final TimeValue timeValue) {
        return defaultsTo(timeValue, ZERO_MILLISECONDS);
    }

    public static boolean isNonNegative(final TimeValue timeValue) {
        return timeValue != null && timeValue.getDuration() >= 0;
    }

    public static boolean isPositive(final TimeValue timeValue) {
        return timeValue != null && timeValue.getDuration() > 0;
    }

    /**
     * Creates a TimeValue.
     *
     * @param duration the time duration in the given {@code timeUnit}.
     * @param timeUnit the time unit for the given duration.
     * @return a Timeout.
     */
    public static TimeValue of(final long duration, final TimeUnit timeUnit) {
        return new TimeValue(duration, timeUnit);
    }

    /**
     * Creates a TimeValue from a Duration.
     *
     * @param duration the time duration.
     * @return a Timeout
     * @since 5.2
     */
    public static TimeValue of(final Duration duration) {
        final long seconds = duration.getSeconds();
        final long nanoOfSecond = duration.getNano();
        if (seconds == 0) {
            // no conversion
            return of(nanoOfSecond, TimeUnit.NANOSECONDS);
        } else if (nanoOfSecond == 0) {
            // no conversion
            return of(seconds, TimeUnit.SECONDS);
        }
        // conversion attempts
        try {
            return of(duration.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ArithmeticException e) {
            try {
                return of(duration.toMillis(), TimeUnit.MILLISECONDS);
            } catch (final ArithmeticException e1) {
                // backstop
                return of(seconds, TimeUnit.SECONDS);
            }
        }
    }

    public static TimeValue ofDays(final long days) {
        return of(days, TimeUnit.DAYS);
    }

    public static TimeValue ofHours(final long hours) {
        return of(hours, TimeUnit.HOURS);
    }

    public static TimeValue ofMicroseconds(final long microseconds) {
        return of(microseconds, TimeUnit.MICROSECONDS);
    }

    public static TimeValue ofMilliseconds(final long millis) {
        return of(millis, TimeUnit.MILLISECONDS);
    }

    public static TimeValue ofMinutes(final long minutes) {
        return of(minutes, TimeUnit.MINUTES);
    }

    public static TimeValue ofNanoseconds(final long nanoseconds) {
        return of(nanoseconds, TimeUnit.NANOSECONDS);
    }

    public static TimeValue ofSeconds(final long seconds) {
        return of(seconds, TimeUnit.SECONDS);
    }

    /**
     * Converts a {@link TimeUnit} to the equivalent {@link ChronoUnit}.
     *
     * @return the converted equivalent ChronoUnit
     */
    static ChronoUnit toChronoUnit(final TimeUnit timeUnit) {
        switch (Objects.requireNonNull(timeUnit)) {
        case NANOSECONDS:
            return ChronoUnit.NANOS;
        case MICROSECONDS:
            return ChronoUnit.MICROS;
        case MILLISECONDS:
            return ChronoUnit.MILLIS;
        case SECONDS:
            return ChronoUnit.SECONDS;
        case MINUTES:
            return ChronoUnit.MINUTES;
        case HOURS:
            return ChronoUnit.HOURS;
        case DAYS:
            return ChronoUnit.DAYS;
        default:
            throw new IllegalArgumentException(timeUnit.toString());
        }
    }

    /**
     * Parses a TimeValue in the format {@code <Long><SPACE><TimeUnit>}, for example {@code "1200 MILLISECONDS"}.
     * <p>
     * Parses:
     * </p>
     * <ul>
     * <li>{@code "1200 MILLISECONDS"}.</li>
     * <li>{@code " 1200 MILLISECONDS "}, spaces are ignored.</li>
     * <li>{@code "1 MINUTE"}, singular units.</li>
     * <li>{@code "250ms"}, short units without a space.</li>
     * <li>{@code "PT15M"}, ISO-8601 durations.</li>
     * </ul>
     *
     * @param value the TimeValue to parse
     * @return a new TimeValue
     * @throws ParseException if the value cannot be parsed
     */
    public static TimeValue parse(final String value) throws ParseException {
        final String trimmed = Objects.requireNonNull(value, "value").trim();
        if (trimmed.isEmpty()) {
            throw parseException(value, "missing duration value and time unit", null);
        }
        if (looksLikeIsoDuration(trimmed)) {
            return parseIsoDuration(value, trimmed);
        }
        return parseSimpleDuration(value, trimmed);
    }

    private static boolean fitsInLong(final BigInteger value) {
        return value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0
                && value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0;
    }

    private static boolean looksLikeIsoDuration(final String value) {
        final String normalized = value.toUpperCase(Locale.ROOT);
        return normalized.startsWith("P") || normalized.startsWith("+P") || normalized.startsWith("-P");
    }

    private static long parseDurationLong(final String value, final String duration) throws ParseException {
        try {
            return Long.parseLong(duration);
        } catch (final NumberFormatException ex) {
            throw parseException(value, "duration must be a signed 64-bit integer", ex);
        }
    }

    private static TimeValue parseCompactDuration(final String value, final String token) throws ParseException {
        final Matcher matcher = COMPACT_TIME_VALUE_PATTERN.matcher(token);
        if (matcher.matches()) {
            final TimeUnit timeUnit = parseShortTimeUnit(matcher.group(2));
            if (timeUnit != null) {
                return TimeValue.of(parseDurationLong(value, matcher.group(1)), timeUnit);
            }
            throw parseException(value, "compact format only supports short units ns, us, ms, s, m, h, d", null);
        }
        if (token.matches("[+-]?\\d+")) {
            throw parseException(value, "missing time unit", null);
        }
        if (token.matches("[A-Za-z]+")) {
            throw parseException(value, "missing duration value", null);
        }
        throw parseException(value, "unsupported compact duration format", null);
    }

    private static TimeValue parseIsoDuration(final String value, final String trimmed) throws ParseException {
        try {
            return toTimeValue(Duration.parse(trimmed.toUpperCase(Locale.ROOT)), value);
        } catch (final DateTimeParseException ex) {
            throw parseException(value, "invalid ISO-8601 duration", ex);
        } catch (final ArithmeticException ex) {
            throw parseException(value, "ISO-8601 duration is out of range", ex);
        }
    }

    private static TimeValue parseSimpleDuration(final String value, final String trimmed) throws ParseException {
        final Matcher matcher = SIMPLE_TIME_VALUE_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return TimeValue.of(parseDurationLong(value, matcher.group(1)), parseTimeUnit(value, matcher.group(2)));
        }
        if (trimmed.indexOf(' ') < 0 && trimmed.indexOf('\t') < 0) {
            return parseCompactDuration(value, trimmed);
        }
        throw parseException(value, "expected '<long> <unit>' or a supported compact short unit", null);
    }

    private static ParseException parseException(final String value, final String detail, final Exception cause) {
        final ParseException parseException = new ParseException(String.format(
                "Invalid time value '%s': %s. Supported formats: '<long> <TimeUnit>' such as '1 SECOND' or '1 MILLISECOND', short units ns/us/ms/s/m/h/d with or without whitespace such as '250 ms' or '250ms', and ISO-8601 durations such as 'PT15M'.",
                value, detail), 0);
        if (cause != null) {
            parseException.initCause(cause);
        }
        return parseException;
    }

    private static TimeUnit parseShortTimeUnit(final String token) {
        switch (token.toUpperCase(Locale.ROOT)) {
        case "NS":
            return TimeUnit.NANOSECONDS;
        case "US":
            return TimeUnit.MICROSECONDS;
        case "MS":
            return TimeUnit.MILLISECONDS;
        case "S":
            return TimeUnit.SECONDS;
        case "M":
            return TimeUnit.MINUTES;
        case "H":
            return TimeUnit.HOURS;
        case "D":
            return TimeUnit.DAYS;
        default:
            return null;
        }
    }

    private static TimeUnit parseTimeUnit(final String value, final String token) throws ParseException {
        final TimeUnit shortTimeUnit = parseShortTimeUnit(token);
        if (shortTimeUnit != null) {
            return shortTimeUnit;
        }
        final String normalized = token.trim().toUpperCase(Locale.ROOT);
        final String timeUnitStr = normalized.endsWith("S") ? normalized : normalized + "S";
        try {
            return TimeUnit.valueOf(timeUnitStr);
        } catch (final IllegalArgumentException ex) {
            throw parseException(value, "unknown time unit '" + token + "'", ex);
        }
    }

    private static BigInteger toBigIntegerNanos(final TimeUnit timeUnit) {
        switch (timeUnit) {
        case NANOSECONDS:
            return BigInteger.ONE;
        case MICROSECONDS:
            return BigInteger.valueOf(1000L);
        case MILLISECONDS:
            return BigInteger.valueOf(1000000L);
        case SECONDS:
            return NANOS_PER_SECOND;
        case MINUTES:
            return NANOS_PER_MINUTE;
        case HOURS:
            return NANOS_PER_HOUR;
        case DAYS:
            return NANOS_PER_DAY;
        default:
            throw new IllegalStateException();
        }
    }

    private static TimeValue toTimeValue(final Duration duration, final String value) throws ParseException {
        if (Duration.ZERO.equals(duration)) {
            return TimeValue.of(0, TimeUnit.NANOSECONDS);
        }
        final BigInteger totalNanos = BigInteger.valueOf(duration.getSeconds()).multiply(NANOS_PER_SECOND)
                .add(BigInteger.valueOf(duration.getNano()));
        for (final TimeUnit timeUnit : PARSE_EXACT_TIME_UNITS) {
            final BigInteger nanosPerUnit = toBigIntegerNanos(timeUnit);
            final BigInteger[] divRem = totalNanos.divideAndRemainder(nanosPerUnit);
            if (divRem[1].signum() == 0 && fitsInLong(divRem[0])) {
                return TimeValue.of(divRem[0].longValue(), timeUnit);
            }
        }
        throw parseException(value, "ISO-8601 duration cannot be represented exactly as a TimeValue", null);
    }

    private final long duration;

    private final TimeUnit timeUnit;

    TimeValue(final long duration, final TimeUnit timeUnit) {
        super();
        this.duration = duration;
        this.timeUnit = Args.notNull(timeUnit, "timeUnit");
    }

    public long convert(final TimeUnit targetTimeUnit) {
        Args.notNull(targetTimeUnit, "timeUnit");
        return targetTimeUnit.convert(duration, timeUnit);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TimeValue) {
            final TimeValue that = (TimeValue) obj;
            final long thisDuration = this.convert(TimeUnit.NANOSECONDS);
            final long thatDuration = that.convert(TimeUnit.NANOSECONDS);
            return thisDuration == thatDuration;
        }
        return false;
    }

    /**
     * Returns a TimeValue whose value is {@code (this / divisor)}.
     *
     * @param divisor
     *            value by which this TimeValue is to be divided.
     * @return {@code this / divisor}
     * @throws ArithmeticException
     *             if {@code divisor} is zero.
     */
    public TimeValue divide(final long divisor) {
        final long newDuration = duration / divisor;
        return of(newDuration, timeUnit);
    }

    /**
     * Returns a TimeValue whose value is {@code (this / divisor)}.
     *
     * @param divisor
     *            value by which this TimeValue is to be divided.
     * @param targetTimeUnit
     *            the target TimeUnit
     * @return {@code this / divisor}
     * @throws ArithmeticException
     *             if {@code divisor} is zero.
     */
    public TimeValue divide(final long divisor, final TimeUnit targetTimeUnit) {
        return of(convert(targetTimeUnit) / divisor, targetTimeUnit);
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.convert(TimeUnit.NANOSECONDS));
        return hash;
    }

    public TimeValue min(final TimeValue other) {
        return this.compareTo(other) > 0 ? other : this;
    }

    private TimeUnit min(final TimeUnit other) {
        return scale() > scale(other) ? other : getTimeUnit();
    }

    private int scale() {
        return scale(timeUnit);
    }

    /**
     * Returns a made up scale for TimeUnits.
     *
     * @param tUnit
     *            a TimeUnit
     * @return a number from 1 to 7, where 1 is NANOSECONDS and 7 DAYS.
     */
    private int scale(final TimeUnit tUnit) {
        switch (tUnit) {
        case NANOSECONDS:
            return 1;
        case MICROSECONDS:
            return 2;
        case MILLISECONDS:
            return 3;
        case SECONDS:
            return 4;
        case MINUTES:
            return 5;
        case HOURS:
            return 6;
        case DAYS:
            return 7;
        default:
            // Should never happens unless Java adds to the enum.
            throw new IllegalStateException();
        }
    }

    public void sleep() throws InterruptedException {
        timeUnit.sleep(duration);
    }

    public void timedJoin(final Thread thread) throws InterruptedException {
        timeUnit.timedJoin(thread, duration);
    }

    public void timedWait(final Object obj) throws InterruptedException {
        timeUnit.timedWait(obj, duration);
    }

    public long toDays() {
        return timeUnit.toDays(duration);
    }

    /**
     * Converts this instance of to a Duration.
     *
     * @return a Duration.
     * @since 5.2
     */
    public Duration toDuration() {
        return duration == 0 ? Duration.ZERO : Duration.of(duration, toChronoUnit(timeUnit));
    }

    public long toHours() {
        return timeUnit.toHours(duration);
    }

    public long toMicroseconds() {
        return timeUnit.toMicros(duration);
    }

    public long toMilliseconds() {
        return timeUnit.toMillis(duration);
    }

    public int toMillisecondsIntBound() {
        return asBoundInt(toMilliseconds());
    }

    public long toMinutes() {
        return timeUnit.toMinutes(duration);
    }

    public long toNanoseconds() {
        return timeUnit.toNanos(duration);
    }

    public long toSeconds() {
        return timeUnit.toSeconds(duration);
    }

    public int toSecondsIntBound() {
        return asBoundInt(toSeconds());
    }

    @Override
    public int compareTo(final TimeValue other) {
        final TimeUnit targetTimeUnit = min(other.getTimeUnit());
        return Long.compare(convert(targetTimeUnit), other.convert(targetTimeUnit));
    }

    @Override
    public String toString() {
        return String.format("%d %s", duration, timeUnit);
    }

    public Timeout toTimeout() {
        return Timeout.of(duration, timeUnit);
    }

}
