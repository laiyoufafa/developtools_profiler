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

package ohos.devtools.views.hilog;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * HiLog Filter
 *
 * @since 2021/08/07 13:41
 */
public class HiLogFilter {
    private static final Logger LOGGER = LogManager.getLogger(HiLogFilter.class);

    /**
     * max row num
     */
    public static final int MAX_ROW_NUM = 7500;

    /**
     * line break num
     */
    public static final int LINE_BREAK_NUM = 2;
    private static final int VERBOSE_LEVEL = 7;
    private static final int DEBUG_LEVEL = 6;
    private static final int INFO_LEVEL = 5;
    private static final int WARN_LEVEL = 4;
    private static final int ERROR_LEVEL = 3;
    private static final int FATAL_LEVEL = 2;
    private static final int ASSERT_LEVEL = 1;
    private static final int SPLIT_LENGTH = 6;
    private static final HiLogFilter INSTANCE = new HiLogFilter();

    /**
     * regex yyyy-MM-dd、MM-dd or yyyy-M-dd
     */
    private final String dataRegex =
        "^([1-9]\\d{3}-)(([0]{0,1}[1-9]-)|([1][0-2]-))(([0-3]{0,1}[0-9]))$|^(([0]{0,1}[1-9]-)|([1][0-2]-))"
            + "(([0-3]{0,1}[0-9]))$";

    /**
     * regex hh-mm-ss
     */
    private final String timeRegex = "^([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])\\.([0-9]{3})$";
    private String type = "";
    private Map<String, Integer> levelMap = new HashMap<>();

    private HiLogFilter() {
        levelMap.put("V", VERBOSE_LEVEL);
        levelMap.put("D", DEBUG_LEVEL);
        levelMap.put("I", INFO_LEVEL);
        levelMap.put("W", WARN_LEVEL);
        levelMap.put("E", ERROR_LEVEL);
        levelMap.put("F", FATAL_LEVEL);
        levelMap.put("A", ASSERT_LEVEL);
    }

    /**
     * getInstance
     *
     * @return HiLogFilter
     */
    public static HiLogFilter getInstance() {
        return INSTANCE;
    }

    /**
     * According to the conditions filterLog
     *
     * @param logTextArea logTextArea
     * @param logLevel logLevel
     * @param searchValue searchValue
     * @param wholeBuilder wholeBuilder
     */
    public void filterLog(JTextArea logTextArea, String logLevel, String searchValue, StringBuilder wholeBuilder) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("filterLog");
        }
        StringBuilder stringBuilder = new StringBuilder();
        String[] lineStr = wholeBuilder.toString().split(System.lineSeparator());
        logTextArea.setText("");
        for (String str : lineStr) {
            String[] lineStrs = str.split(" ");
            getLineLogLevel(lineStrs);
            boolean dataMatches = Pattern.matches(dataRegex, lineStrs[0]);
            boolean timeMatches = Pattern.matches(timeRegex, lineStrs[1]);
            if (lineStrs.length >= SPLIT_LENGTH && dataMatches && timeMatches) {
                // W
                if (levelMap.get(type) <= levelMap.get(logLevel) && str.contains(searchValue)) {
                    stringBuilder.append(str).append(System.lineSeparator());
                }
            } else {
                if (levelMap.containsKey(type) && levelMap.get(type) <= levelMap.get(logLevel) && str
                    .contains(searchValue)) {
                    stringBuilder.append(str).append(System.lineSeparator());
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * run
             */
            public void run() {
                logTextArea.setText(stringBuilder.toString());
            }
        });
    }

    private void getLineLogLevel(String[] lineStrs) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLineLogLevel");
        }
        if (lineStrs.length <= 1) {
            return;
        }
        for (String item : lineStrs) {
            if (item.length() == 1 && levelMap.containsKey(item)) {
                type = item;
                break;
            }
        }
    }

    /**
     * Row filtering
     *
     * @param str str
     * @param searchValue searchValue
     * @param logLevel logLevel
     * @param logTextArea logTextArea
     */
    public void lineFilter(String str, String searchValue, String logLevel, JTextArea logTextArea) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("lineFilter");
        }
        if (logTextArea.getLineCount() >= MAX_ROW_NUM) {
            String[] lines = logTextArea.getText().split(System.lineSeparator());
            try {
                logTextArea.getDocument().remove(0, lines[0].length() + LINE_BREAK_NUM);
            } catch (BadLocationException locationException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("Error deleting the top row when the maximum number of rows is exceeded {}",
                        locationException.getMessage());
                }
            }
        }
        String[] lineStrs = str.split(" ");
        getLineLogLevel(lineStrs);
        boolean dataMatches = Pattern.matches(dataRegex, lineStrs[0]);
        boolean timeMatches = Pattern.matches(timeRegex, lineStrs[1]);
        if (lineStrs.length >= SPLIT_LENGTH && dataMatches && timeMatches) {
            if (levelMap.get(type) <= levelMap.get(logLevel) && str.contains(searchValue)) {
                logTextArea.append(str + System.lineSeparator());
            }
        } else {
            if (levelMap.containsKey(type) && levelMap.get(type) <= levelMap.get(logLevel) && str
                .contains(searchValue)) {
                logTextArea.append(str + System.lineSeparator());
            }
        }
    }

    /**
     * is error log
     *
     * @param line line
     * @return boolean boolean
     */
    public boolean isErrorLog(String line) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("isErrorLog");
        }
        String[] lines = line.split(" ");
        boolean dataMatches = Pattern.matches(dataRegex, lines[0]);
        boolean timeMatches = Pattern.matches(timeRegex, lines[1]);
        if (lines.length >= SPLIT_LENGTH && dataMatches && timeMatches) {
            return false;
        } else {
            return true;
        }
    }
}
