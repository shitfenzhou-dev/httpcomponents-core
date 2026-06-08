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
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("([+-]?\\d+)\\s*([a-zA-Z]+)");

    private static final Map<String, TimeUnit> SHORT_UNIT_MAP = new HashMap<>();
    static {
        SHORT_UNIT_MAP.put("NS", TimeUnit.NANOSECONDS);
        SHORT_UNIT_MAP.put("US", TimeUnit.MICROSECONDS);
        SHORT_UNIT_MAP.put("MS", TimeUnit.MILLISECONDS);
        SHORT_UNIT_MAP.put("S", TimeUnit.SECONDS);
        SHORT_UNIT_MAP.put("M", TimeUnit.MINUTES);
        SHORT_UNIT_MAP.put("H", TimeUnit.HOURS);
        SHORT_UNIT_MAP.put("D", TimeUnit.DAYS);
    }

    /**
     * Parses a TimeValue from a string representation.
     * <p>
     * Supported formats:
     * </p>
     * <ul>
     * <li>{@code "1200 MILLISECONDS"} — legacy long-unit format.</li>
     * <li>{@code "1 MINUTE"} — legacy singular unit.</li>
     * <li>{@code "250ms"}, {@code "250 ms"} — short unit with optional space.</li>
     * <li>{@code "+30s"}, {@code "-1 ms"} — signed compact forms.</li>
     * <li>{@code "PT0.25S"}, {@code "PT1H30M"}, {@code "P1D"} — ISO-8601 duration.</li>
     * </ul>
     *
     * @param value the TimeValue string to parse.
     * @return a new TimeValue.
     * @throws ParseException if the value cannot be parsed.
     */
    public static TimeValue parse(final String value) throws ParseException {
        if (value == null) {
            throw new ParseException("TimeValue must not be null", 0);
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ParseException("TimeValue must not be empty or only whitespace", 0);
        }

        if (isIso8601(trimmed)) {
            return parseIso8601(trimmed);
        }

        final Matcher matcher = NUMBER_UNIT_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            final String numStr = matcher.group(1);
            final String unitStr = matcher.group(2);
            final long number;
            try {
                number = Long.parseLong(numStr);
            } catch (final NumberFormatException e) {
                throw new ParseException(
                        "Invalid numeric value '" + numStr + "' in TimeValue '" + value
                                + "'. Supported formats: '<long> <unit>' (e.g. '250ms', '1 SECOND'), "
                                + "'P...' ISO-8601 duration (e.g. 'PT2H', 'P1D')",
                        0);
            }
            final TimeUnit unit = resolveTimeUnit(unitStr, value);
            return TimeValue.of(number, unit);
        }

        throw new ParseException(
                "Invalid TimeValue format: '" + value
                        + "'. Supported formats: '<long><unit>' or '<long> <unit>' "
                        + "(e.g. '250ms', '1 SECOND', '+30s', '-1 ms'), "
                        + "'P...' ISO-8601 duration (e.g. 'PT2H', 'PT0.25S', 'P1D')",
                0);
    }

    private static boolean isIso8601(final String trimmed) {
        final int len = trimmed.length();
        if (len < 2) {
            return false;
        }
        char c = trimmed.charAt(0);
        int start = 0;
        if (c == '+' || c == '-') {
            if (len < 3) {
                return false;
            }
            start = 1;
        }
        c = trimmed.charAt(start);
        return c == 'P' || c == 'p';
    }

    private static TimeValue parseIso8601(final String value) throws ParseException {
        final Duration duration;
        try {
            duration = Duration.parse(value);
        } catch (final DateTimeParseException e) {
            throw new ParseException(
                    "Invalid ISO-8601 duration: '" + value
                            + "'. Expected format: PnDTnHnMn.nS (e.g. 'PT2H', 'PT0.25S', 'P1D')",
                    0);
        }
        return fromDurationLossless(duration, value);
    }

    private static TimeValue fromDurationLossless(final Duration duration, final String original) throws ParseException {
        final long seconds = duration.getSeconds();
        final int nanos = duration.getNano();

        if (seconds == 0 && nanos == 0) {
            return ZERO_MILLISECONDS;
        }

        if (nanos == 0) {
            if (seconds % 86400 == 0) {
                return of(seconds / 86400, TimeUnit.DAYS);
            }
            if (seconds % 3600 == 0) {
                return of(seconds / 3600, TimeUnit.HOURS);
            }
            if (seconds % 60 == 0) {
                return of(seconds / 60, TimeUnit.MINUTES);
            }
            return of(seconds, TimeUnit.SECONDS);
        }

        if (nanos % 1000000 == 0) {
            try {
                final long millis = Math.addExact(Math.multiplyExact(seconds, 1000L), (long) (nanos / 1000000));
                return of(millis, TimeUnit.MILLISECONDS);
            } catch (final ArithmeticException ignored) {
            }
        }
        if (nanos % 1000 == 0) {
            try {
                final long micros = Math.addExact(Math.multiplyExact(seconds, 1000000L), (long) (nanos / 1000));
                return of(micros, TimeUnit.MICROSECONDS);
            } catch (final ArithmeticException ignored) {
            }
        }
        try {
            final long totalNanos = Math.addExact(Math.multiplyExact(seconds, 1000000000L), (long) nanos);
            return of(totalNanos, TimeUnit.NANOSECONDS);
        } catch (final ArithmeticException e) {
            throw new ParseException(
                    "ISO-8601 duration value too large to represent as nanoseconds: '" + original + "'",
                    0);
        }
    }

    private static TimeUnit resolveTimeUnit(final String unitStr, final String originalValue) throws ParseException {
        final String upper = unitStr.toUpperCase(Locale.ROOT);
        final TimeUnit shortUnit = SHORT_UNIT_MAP.get(upper);
        if (shortUnit != null) {
            return shortUnit;
        }
        try {
            final String normalized = upper.endsWith("S") ? upper : upper + "S";
            return TimeUnit.valueOf(normalized);
        } catch (final IllegalArgumentException e) {
            throw new ParseException(
                    "Unknown time unit '" + unitStr + "' in '" + originalValue
                            + "'. Supported units: ns, us, ms, s, m, h, d, "
                            + "NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS",
                    0);
        }
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
