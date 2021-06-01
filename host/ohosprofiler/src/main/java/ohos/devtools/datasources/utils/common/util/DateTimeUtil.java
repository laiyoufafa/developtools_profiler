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

package ohos.devtools.datasources.utils.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @Description Date and time utilities
 * @Date 2021/2/7 15:00
 **/
public final class DateTimeUtil {
    private DateTimeUtil() {
    }

    /**
     * Format: yyyy-MM-dd HH:mm:ss
     */
    public static final String COMMON_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Format: HH:mm
     */
    public static final String HOUR_MINUTES_PATTERN = "HH:mm";

    /**
     * Format: HH:mm:ss
     */
    public static final String HOUR_MINUTES_SECONDS_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter COMMON_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.of("+8");

    /**
     * Convert the date to a string.
     *
     * @param dateTime Indicates the date and time to be converted.
     * @return Returns a string converted from the date and time.
     */
    public static String dateToString(LocalDateTime dateTime) {
        assert dateTime != null;
        return COMMON_FORMATTER.format(dateTime);
    }

    /**
     * Converts a string to date and time.
     *
     * @param dateStr Indicates the string to be converted to date and time.
     * @return Returns the date and time converted from the given string.
     */
    public static LocalDateTime stringToDate(String dateStr) {
        assert dateStr != null;
        return LocalDateTime.parse(dateStr, COMMON_FORMATTER);
    }

    /**
     * Gets the current date and time.
     *
     * @return Returns the curernt date and time.
     */
    public static LocalDateTime getNowTime() {
        return LocalDateTime.now();
    }

    /**
     * Converts the date and time to a string.
     *
     * @param dateTime  Indicates the date and time to be converted.
     * @param formatter Indicates the formatter used for formatting.
     * @return Returns the date and time converted from the given string.
     */
    public static String dateToString(LocalDateTime dateTime, DateTimeFormatter formatter) {
        assert dateTime != null;
        return formatter.format(dateTime);
    }

    /**
     * Converts a string to date and time.
     *
     * @param dateStr   Indicates the string to be converted to date and time.
     * @param formatter Indicates the formatter used for formatting.
     * @return Returns the date and time converted from the given string.
     */
    public static LocalDateTime stringToDate(String dateStr, DateTimeFormatter formatter) {
        assert dateStr != null;
        return LocalDateTime.parse(dateStr, formatter);
    }

    /**
     * Converts the date and time to Epoch time (in ms).
     *
     * @param dateTime Indicates the date and time to be converted.
     * @return Returns a long string representing the Epoch time.
     */
    public static long dateToTimeMillis(LocalDateTime dateTime) {
        assert dateTime != null;
        return dateTime.toInstant(DEFAULT_ZONE_OFFSET).toEpochMilli();
    }

    /**
     * Converts an Epoch time (in ms) to the date and time.
     *
     * @param timeMills Indicates the Epoch time (in ms) to be converted.
     * @return Returns the date and time converted from the given Epoch time.
     */
    public static LocalDateTime timeMillisToDate(long timeMills) {
        Instant instant = Instant.ofEpochMilli(timeMills);
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE_OFFSET);
    }

    /**
     * Gets the date and time in the given pattern.
     *
     * @param pattern Indicates the pattern of the date and time.
     * @return Returns a string representing the date and time in the specified pattern.
     */
    public static String getNowTimeString(String pattern) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return now.format(formatter);
    }

    /**
     * Gets the timestamp (in ms).
     *
     * @return Returns a long string representing the timestamp.
     */
    public static Long getNowTimeLong() {
        LocalDateTime now = LocalDateTime.now();
        return now.toInstant(DEFAULT_ZONE_OFFSET).toEpochMilli();
    }
}
