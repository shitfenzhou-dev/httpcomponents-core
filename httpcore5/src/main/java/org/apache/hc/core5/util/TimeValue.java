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
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    /**
     * Parses a TimeValue in the format {@code <Long><SPACE><TimeUnit>}, for example {@code "1200 MILLISECONDS"}.
     * <p>
     * Parses:
     * </p>
     * <ul>
     * <li>{@code "1200 MILLISECONDS"}.</li>
     * <li>{@code " 1200 MILLISECONDS "}, spaces are ignored.</li>
     * <li>{@code "1 MINUTE"}, singular units.</li>
     * <li></li>
     * </ul>
     *
     *
     * @param value the TimeValue to parse
     * @return a new TimeValue
     * @throws ParseException if the number cannot be parsed
     */
    public static TimeValue parse(final String value) throws ParseException {
        if (value == null) {
            throw new ParseException("TimeValue string cannot be null", 0);
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ParseException("TimeValue string cannot be empty or blank", 0);
        }
        if (trimmed.startsWith("P") || trimmed.startsWith("-P") || trimmed.startsWith("+P") ||
                trimmed.startsWith("p") || trimmed.startsWith("-p") || trimmed.startsWith("+p")) {
            return parseIso8601(trimmed, value);
        }
        return parseFormat(trimmed);
    }

    private static TimeValue parseIso8601(final String trimmed, final String originalValue) throws ParseException {
        try {
            final Duration duration = Duration.parse(trimmed);
            return fromDurationLossless(duration, originalValue);
        } catch (final Exception e) {
            if (e instanceof ParseException) {
                throw (ParseException) e;
            }
            throw (ParseException) new ParseException(String.format("Invalid ISO-8601 duration: '%s'.", originalValue), 0).initCause(e);
        }
    }

    private static TimeValue fromDurationLossless(final Duration duration, final String originalValue) throws ParseException {
        final long seconds = duration.getSeconds();
        final int nanos = duration.getNano();

        try {
            if (nanos != 0) {
                if (nanos % 1_000_000 == 0) {
                    final long millis = Math.addExact(Math.multiplyExact(seconds, 1000L), nanos / 1_000_000L);
                    return TimeValue.of(millis, TimeUnit.MILLISECONDS);
                }
                if (nanos % 1_000 == 0) {
                    final long micros = Math.addExact(Math.multiplyExact(seconds, 1_000_000L), nanos / 1_000L);
                    return TimeValue.of(micros, TimeUnit.MICROSECONDS);
                }
                final long totalNanos = Math.addExact(Math.multiplyExact(seconds, 1_000_000_000L), nanos);
                return TimeValue.of(totalNanos, TimeUnit.NANOSECONDS);
            } else {
                if (seconds % (24 * 3600) == 0) {
                    return TimeValue.of(seconds / (24 * 3600), TimeUnit.DAYS);
                }
                if (seconds % 3600 == 0) {
                    return TimeValue.of(seconds / 3600, TimeUnit.HOURS);
                }
                if (seconds % 60 == 0) {
                    return TimeValue.of(seconds / 60, TimeUnit.MINUTES);
                }
                return TimeValue.of(seconds, TimeUnit.SECONDS);
            }
        } catch (final ArithmeticException e) {
            throw (ParseException) new ParseException(String.format("ISO-8601 duration too large to represent as TimeValue: '%s'", originalValue), 0).initCause(e);
        }
    }

    private static TimeValue parseFormat(final String value) throws ParseException {
        int unitIndex = -1;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (!Character.isDigit(c) && c != '+' && c != '-' && c != '.' && !Character.isWhitespace(c)) {
                unitIndex = i;
                break;
            }
        }
        if (unitIndex == -1) {
            throw new ParseException(String.format("Missing time unit in TimeValue: '%s'. Expected format <number><unit> (e.g., 250ms, 2 h).", value), value.length());
        }
        if (unitIndex == 0) {
            throw new ParseException(String.format("Missing number in TimeValue: '%s'. Expected format <number><unit> (e.g., 250ms, 2 h).", value), 0);
        }

        final String numberPart = value.substring(0, unitIndex).trim();
        if (numberPart.isEmpty()) {
            throw new ParseException(String.format("Missing number in TimeValue: '%s'. Expected format <number><unit> (e.g., 250ms, 2 h).", value), 0);
        }

        final String unitPart = value.substring(unitIndex).trim();

        long duration;
        try {
            duration = Long.parseLong(numberPart);
        } catch (final NumberFormatException e) {
            throw (ParseException) new ParseException(String.format("Invalid number in TimeValue: '%s'. Number must be a valid long.", value), 0).initCause(e);
        }

        final TimeUnit timeUnit = parseTimeUnit(unitPart, value);
        return TimeValue.of(duration, timeUnit);
    }

    private static TimeUnit parseTimeUnit(final String unitStr, final String originalValue) throws ParseException {
        final String upperUnit = unitStr.toUpperCase(Locale.ROOT);
        switch (upperUnit) {
            case "NS":
            case "NANOSECOND":
            case "NANOSECONDS":
                return TimeUnit.NANOSECONDS;
            case "US":
            case "MICROSECOND":
            case "MICROSECONDS":
                return TimeUnit.MICROSECONDS;
            case "MS":
            case "MILLISECOND":
            case "MILLISECONDS":
                return TimeUnit.MILLISECONDS;
            case "S":
            case "SECOND":
            case "SECONDS":
                return TimeUnit.SECONDS;
            case "M":
            case "MINUTE":
            case "MINUTES":
                return TimeUnit.MINUTES;
            case "H":
            case "HOUR":
            case "HOURS":
                return TimeUnit.HOURS;
            case "D":
            case "DAY":
            case "DAYS":
                return TimeUnit.DAYS;
            default:
                throw new ParseException(String.format("Invalid time unit '%s' in '%s'. Supported units: ns, us, ms, s, m, h, d, and their full names.", unitStr, originalValue), 0);
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
