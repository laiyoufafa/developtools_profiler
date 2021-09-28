/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ohos.devtools.views.applicationtrace.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * TimeUtils
 *
 * @date: 2021/5/13 13:06
 */
public class TimeUtils {
    /**
     * getTimeWithUnit function
     *
     * @param nsL nsL
     * @return String time
     */
    public static String getTimeWithUnit(final long nsL) {
        long ns = nsL;
        StringBuilder time = new StringBuilder();
        long hours = TimeUnit.NANOSECONDS.toHours(ns);
        if (hours > 1) {
            time.append(hours).append("h");
            return time.toString();
        }
        long minute = TimeUnit.NANOSECONDS.toMinutes(ns);
        long second;
        if (minute > 0) {
            ns = ns - TimeUnit.MINUTES.toNanos(minute);
            long longTime = TimeUnit.NANOSECONDS.toMillis(ns);
            BigDecimal value = new BigDecimal(longTime)
                .divide(new BigDecimal(6D), 0, ROUND_HALF_UP).multiply(new BigDecimal(10));
            second = value.longValue();
            time.append(minute).append(".").append(String.format(Locale.ENGLISH, "%02d", second)).append("m");
            return time.toString();
        }
        long millis;
        second = TimeUnit.NANOSECONDS.toSeconds(ns);
        if (second > 0) {
            ns = ns - TimeUnit.SECONDS.toNanos(second);
            long longTime = TimeUnit.NANOSECONDS.toMillis(ns);
            BigDecimal divide = new BigDecimal(longTime).divide(new BigDecimal(10D), 0, ROUND_HALF_UP);
            millis = divide.longValue();
            time.append(second).append(".").append(String.format(Locale.ENGLISH, "%02d", millis)).append("s");
            return time.toString();
        }
        long micros;
        millis = TimeUnit.NANOSECONDS.toMillis(ns);
        if (millis > 0) {
            ns = ns - TimeUnit.MILLISECONDS.toNanos(millis);
            long longTime = TimeUnit.NANOSECONDS.toMicros(ns);
            BigDecimal divide = new BigDecimal(longTime).divide(new BigDecimal(10D), 0, ROUND_HALF_UP);
            micros = divide.longValue();
            time.append(millis).append(".").append(String.format(Locale.ENGLISH, "%02d", micros)).append("ms");
            return time.toString();
        }
        micros = TimeUnit.NANOSECONDS.toMicros(ns);
        if (micros > 0) {
            ns = ns - TimeUnit.MICROSECONDS.toNanos(micros);
            BigDecimal divide = new BigDecimal(ns).divide(new BigDecimal(1000D), 0, ROUND_HALF_UP);
            if (divide.longValue() > 0) {
                micros++;
            }
            time.append(micros).append("μs");
            return time.toString();
        }
        return "0μs";
    }

    /**
     * get the time range is in other range function
     *
     * @param startNs startNs
     * @param endNs endNs
     * @param startRNs startRNs
     * @param endLNs endLNs
     * @return boolean
     */
    public static boolean isInRange(long startNs, long endNs, long startRNs, long endLNs) {
        return endLNs >= startNs && startRNs <= endNs;
    }

    /**
     * get the time range is cross function
     *
     * @param startNs startNs
     * @param endNs endNs
     * @param startRNs startRNs
     * @param endLNs endLNs
     * @return boolean
     */
    public static boolean isRangeCross(long startNs, long endNs, long startRNs, long endLNs) {
        long leftMin = Math.max(startNs, startRNs);
        long rightMax = Math.min(endNs, endLNs);
        return rightMax > leftMin;
    }

    /**
     * get the time string format function
     *
     * @param nsL nsL
     * @return String time
     */
    public static String getTimeFormatString(final long nsL) {
        long ns = nsL;
        long hours = TimeUnit.NANOSECONDS.toHours(ns);
        ns = ns - TimeUnit.HOURS.toNanos(hours);
        long minute = TimeUnit.NANOSECONDS.toMinutes(ns);
        ns = ns - TimeUnit.MINUTES.toNanos(minute);
        long second = TimeUnit.NANOSECONDS.toSeconds(ns); // second
        ns = ns - TimeUnit.SECONDS.toNanos(second);
        long millis = TimeUnit.NANOSECONDS.toMillis(ns);
        StringBuffer time = new StringBuffer();
        time.append(String.format(Locale.ENGLISH, "%02d", minute));
        time.append(":");
        time.append(String.format(Locale.ENGLISH, "%02d", second));
        time.append(".");
        time.append(String.format(Locale.ENGLISH, "%03d", millis));
        return time.toString();
    }

    /**
     * get Intersection function
     *
     * @param fStartNs fStartNs
     * @param fEndNs fEndNs
     * @param sStartNs sStartNs
     * @param sEndNs sEndNs
     * @return long time
     */
    public static long getIntersection(long fStartNs, long fEndNs, long sStartNs, long sEndNs) {
        long leftMin = Math.max(fStartNs, sStartNs);
        long rightMax = Math.min(fEndNs, sEndNs);
        if (rightMax > leftMin) {
            return TimeUnit.NANOSECONDS.toMicros(rightMax) - TimeUnit.NANOSECONDS.toMicros(leftMin);
        }
        return 0L;
    }

    /**
     * get Intersection unit nanos function
     *
     * @param fStartNs fStartNs
     * @param fEndNs fEndNs
     * @param sStartNs sStartNs
     * @param sEndNs sEndNs
     * @return long time
     */
    public static long getNanoIntersection(long fStartNs, long fEndNs, long sStartNs, long sEndNs) {
        long leftMin = Math.max(fStartNs, sStartNs);
        long rightMax = Math.min(fEndNs, sEndNs);
        if (rightMax > leftMin) {
            return rightMax - leftMin;
        }
        return 0L;
    }

    /**
     * Get the current formatting time
     *
     * @param nsL nsL
     * @return String
     */
    public static String getTimeString(final long nsL) {
        long ns = nsL;
        long hours = TimeUnit.NANOSECONDS.toHours(ns);
        ns = ns - TimeUnit.HOURS.toNanos(hours);
        long minutes = TimeUnit.NANOSECONDS.toMinutes(ns);
        ns = ns - TimeUnit.MINUTES.toNanos(minutes);
        long second = TimeUnit.NANOSECONDS.toSeconds(ns); // second
        ns = ns - TimeUnit.SECONDS.toNanos(second);
        long millis = TimeUnit.NANOSECONDS.toMillis(ns); // millisecond
        ns = ns - TimeUnit.MILLISECONDS.toNanos(millis);
        long micros = TimeUnit.NANOSECONDS.toMicros(ns); // microsecond
        ns = ns - TimeUnit.MICROSECONDS.toNanos(micros);
        List<String> list = new ArrayList<>();
        if (hours > 0) {
            list.add(hours + "h");
        }
        if (minutes > 0) {
            list.add(minutes + "m");
        }
        if (second > 0) {
            list.add(second + "s");
        }
        if (millis > 0) {
            list.add(millis + "ms");
        }
        if (micros > 0) {
            list.add(micros + "μs");
        }
        long nanos = ns;
        if (nanos > 0) {
            list.add(nanos + "ns");
        }
        return list.stream().collect(Collectors.joining(" "));
    }

    /**
     * get time ns by time string
     *
     * @param timeStr time string
     * @return long
     */
    public static long getNSByTimeString(final String timeStr) {
        try {
            if (timeStr.contains("ns")) {
                String timeString = timeStr.replaceAll("ns", "");
                double timeDouble = Double.parseDouble(timeString);
                return (long) (timeDouble * 1);
            } else if (timeStr.contains("μs")) {
                String timeString = timeStr.replaceAll("μs", "");
                double timeDouble = Double.parseDouble(timeString);
                return (long) (timeDouble * 1000);
            } else if (timeStr.contains("ms")) {
                String timeString = timeStr.replaceAll("ms", "");
                double timeDouble = Double.parseDouble(timeString);
                return (long) (timeDouble * 1000 * 1000);
            } else if (timeStr.contains("s")) {
                String timeString = timeStr.replaceAll("s", "");
                double timeDouble = Double.parseDouble(timeString);
                return (long) (timeDouble * 1000 * 1000 * 1000);
            } else if (timeStr.contains("m")) {
                String timeString = timeStr.replaceAll("m", "");
                double timeDouble = Double.parseDouble(timeString);
                return (long) (timeDouble * 60 * 1000 * 1000 * 1000);
            } else if (timeStr.contains("h")) {
                String timeString = timeStr.replaceAll("h", "");
                double timeDouble = Double.parseDouble(timeString);
                return (long) (timeDouble * 3600 * 1000 * 1000 * 1000);
            } else {
                return 0;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get the current formatting time
     *
     * @param nsL nsL
     * @return String
     */
    public static String getDistributedTotalTime(final long nsL) {
        long ns = nsL;
        long hours = TimeUnit.NANOSECONDS.toHours(ns);
        ns = ns - TimeUnit.HOURS.toNanos(hours);
        long minutes = TimeUnit.NANOSECONDS.toMinutes(ns);
        ns = ns - TimeUnit.MINUTES.toNanos(minutes);
        long second = TimeUnit.NANOSECONDS.toSeconds(ns); // second
        ns = ns - TimeUnit.SECONDS.toNanos(second);
        long millis = TimeUnit.NANOSECONDS.toMillis(ns); // millisecond
        ns = ns - TimeUnit.MILLISECONDS.toNanos(millis);
        long micros = TimeUnit.NANOSECONDS.toMicros(ns); // microsecond
        ns = ns - TimeUnit.MICROSECONDS.toNanos(micros);
        List<String> list = new ArrayList<>();
        if (hours > 0) {
            list.add(hours + "");
        } else {
            list.add("00");
        }
        if (minutes > 0) {
            list.add(minutes + "");
        } else {
            list.add("00");
        }
        if (second > 0) {
            list.add(second + "");
        } else {
            list.add("00");
        }
        return list.stream().collect(Collectors.joining(":")) + "." + millis;
    }
}
