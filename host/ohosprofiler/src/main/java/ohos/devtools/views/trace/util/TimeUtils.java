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

package ohos.devtools.views.trace.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Time formatting tool
 *
 * @since 2021/04/22 12:25
 */
public final class TimeUtils {
    private static DecimalFormat df = new DecimalFormat("#.0");

    private TimeUtils() {
    }

    /**
     * Convert to s according to ns
     *
     * @param ns ns
     * @return String
     */
    public static String getSecondFromNSecond(final long ns) {
        final long second1 = 1_000_000_000L; // 1 second
        final long millisecond1 = 1_000_000L; // 1 millisecond
        final long microsecond1 = 1_000L; // 1 microsecond
        final double nanosecond1 = 1000.0D;
        String res;
        if (ns >= second1) {
            res = df.format(TimeUnit.MILLISECONDS.convert(ns, TimeUnit.NANOSECONDS) / nanosecond1) + "s";
        } else if (ns >= millisecond1) {
            res = df.format(TimeUnit.MICROSECONDS.convert(ns, TimeUnit.NANOSECONDS) / nanosecond1) + "ms";
        } else if (ns >= microsecond1) {
            res = df.format(ns / nanosecond1) + "μs";
        } else {
            res = ns + "ns";
        }
        return res;
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
     * Get the current formatting time
     *
     * @param ns ns
     * @return String
     */
    public static String getLogTimeString(long ns) {
        long second = TimeUnit.NANOSECONDS.toSeconds(ns);
        long millis = TimeUnit.NANOSECONDS.toMillis(ns) - TimeUnit.SECONDS.toMillis(second);
        long micros = TimeUnit.NANOSECONDS.toMicros(ns) - TimeUnit.SECONDS.toMicros(second) - TimeUnit.MILLISECONDS
            .toMicros(millis);
        long nanos =
            TimeUnit.NANOSECONDS.toNanos(ns) - TimeUnit.SECONDS.toNanos(second) - TimeUnit.MILLISECONDS.toNanos(millis)
                - TimeUnit.MICROSECONDS.toNanos(micros);
        return Stream.of(second + "." + String.format("%03d", millis), String.format("%03d", micros),
            String.format("%03d", nanos)).collect(Collectors.joining(" "));
    }
}
