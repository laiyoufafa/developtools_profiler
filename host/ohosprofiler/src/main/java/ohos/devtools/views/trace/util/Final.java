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

import java.awt.Font;

/**
 * Font color information tool
 *
 * @since 2021/04/22 12:25
 */
public final class Final {
    /**
     * is resource sql db file
     */
    public static final boolean IS_RESOURCE_SQL = true;

    /**
     * Font name
     */
    public static final String FONT_NAME = "宋体";

    /**
     * Default font style
     */
    public static final int PLAIN_STYLE = Font.PLAIN;

    /**
     * Minimum font size
     */
    public static final int SMALL_FONT_SIZE = 10;

    /**
     * litter font size
     */
    public static final int LITTER_FONT_SIZE = 8;

    /**
     * Normal font size
     */
    public static final int NORMAL_FONT_SIZE = 11;

    /**
     * Runtime color constants
     */
    public static final int RUNNING_COLOR = 0x467b3b;

    /**
     * R_COLOR
     */
    public static final int R_COLOR = 0xa0b84d;

    /**
     * UNINTERRUPTIBLE_SLEEP_COLOR
     */
    public static final int UNINTERRUPTIBLE_SLEEP_COLOR = 0xf19d38;

    /**
     * exit color
     */
    public static final int EXIT_COLOR = 0x795649;

    /**
     * S_COLOR
     */
    public static final int S_COLOR = 0xFBFBFB;

    /**
     * NORMAL_FONT
     */
    public static final Font NORMAL_FONT = new Font(FONT_NAME, PLAIN_STYLE, NORMAL_FONT_SIZE);

    /**
     * NORMAL_FONT
     */
    public static final Font SMALL_FONT = new Font(FONT_NAME, PLAIN_STYLE, SMALL_FONT_SIZE);

    /**
     * LITTER_FONT
     */
    public static final Font LITTER_FONT = new Font(FONT_NAME, PLAIN_STYLE, LITTER_FONT_SIZE);

    /**
     * db capacity ,if row number big then capacity use async load db data
     */
    public static final long CAPACITY = 5000L;

    private Final() {
    }
}
