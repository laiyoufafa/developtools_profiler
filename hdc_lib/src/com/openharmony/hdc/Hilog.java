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

package com.openharmony.hdc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Hilog for base log print ¡¢create at 20210912
 */
public final class Hilog {
    /**
     * Log Level enum.
     */
    public enum LogLevel {
        VERBOSE(2, "verbose", 'V'), DEBUG(3, "debug", 'D'), INFO(4, "info", 'I'), WARN(5, "warn", 'W'),
        ERROR(6, "error", 'E');

        private int mPriorityLevel;
        private String mStringValue;
        private char mPriorityLetter;

        LogLevel(int intPriority, String stringValue, char priorityChar) {
            mPriorityLevel = intPriority;
            mStringValue = stringValue;
            mPriorityLetter = priorityChar;
        }

        /**
         * get log Priority Letter
         *
         * @return log Priority Letter
         */
        public char getPriorityLetter() {
            return mPriorityLetter;
        }

        /**
         * get log Priority
         *
         * @return log Priority
         */
        public int getPriority() {
            return mPriorityLevel;
        }

        /**
         * get log msg
         *
         * @return string value
         */
        public String getStringValue() {
            return mStringValue;
        }
    }

    private static LogLevel sLevel = LogLevel.ERROR; // default is all

    private Hilog() {
    }

    /**
     * lib v level log
     *
     * @param tag TAG
     * @param message print log
     */
    public static void v(String tag, String message) {
        println(LogLevel.VERBOSE, tag, message);
    }

    /**
     * lib d level log
     *
     * @param tag TAG
     * @param message print log
     */
    public static void d(String tag, String message) {
        println(LogLevel.DEBUG, tag, message);
    }

    /**
     * lib i level log
     *
     * @param tag TAG
     * @param message print log
     */
    public static void i(String tag, String message) {
        println(LogLevel.INFO, tag, message);
    }

    /**
     * lib w level log
     *
     * @param tag TAG
     * @param message print log
     */
    public static void w(String tag, String message) {
        println(LogLevel.WARN, tag, message);
    }

    /**
     * lib e level log
     *
     * @param tag TAG
     * @param message print log
     */
    public static void e(String tag, String message) {
        println(LogLevel.ERROR, tag, message);
    }

    /**
     * print error log
     *
     * @param tag TAG
     * @param throwable exception
     */
    public static void e(String tag, Throwable throwable) {
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            throwable.printStackTrace(pw);
            println(LogLevel.ERROR, tag, throwable.getMessage() + '\n' + sw.toString());
        }
    }

    private static void println(LogLevel logLevel, String tag, String message) {
        if (logLevel.getPriority() >= sLevel.getPriority()) {
            printLog(logLevel, tag, message);
        }
    }

    /**
     * print log and set log level,we need add System out print here when we use it
     * para is (getLogFormatString(logLevel, tag, message));
     *
     * @param logLevel log level
     * @param tag TAG print
     * @param message message we prib
     */
    public static void printLog(LogLevel logLevel, String tag, String message) {
    }

    private static String getLogFormatString(LogLevel logLevel, String tag, String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        return String.format("%s %c/%s: %s\n", formatter.format(new Date()), logLevel.getPriorityLetter(), tag,
                message);
    }
}