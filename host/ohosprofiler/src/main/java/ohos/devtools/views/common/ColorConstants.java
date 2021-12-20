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

package ohos.devtools.views.common;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import java.awt.Color;

/**
 * ColorConstants
 */
public final class ColorConstants {
    /**
     * 构造函数
     */
    private ColorConstants() {
    }

    /**
     * color RGB 12 14 18
     */
    public static final Color BLACK_COLOR = new Color(13, 14, 19);

    /**
     * The color black. In the default sRGB space.
     */
    public static final Color BLACK = new Color(0, 0, 0);

    /**
     * The color cyan. In the default sRGB space.
     */
    public static final Color CYAN = new Color(0, 255, 255);

    /**
     * HOME_PANE颜色
     */
    public static final Color HOME_PANE = new Color(48, 50, 52);

    /**
     * SELECT_PANEL颜色
     */
    public static final Color SELECT_PANEL = new Color(69, 73, 74);

    /**
     * Chart background color
     */
    public static final Color CHART_BG = new Color(0x0D0E13);

    /**
     * Vertical ruler color
     */
    public static final Color RULER = new Color(0x757784);

    /**
     * Timeline scale color
     */
    public static final Color TIMELINE_SCALE = new Color(0x35353E);

    /**
     * Scrollbar background color
     */
    public static final Color SCROLLBAR = new Color(0x66FFFFFF, true);

    /**
     * Font color
     */
    public static final JBColor FONT_COLOR = new JBColor(Color.white, Color.white);

    /**
     * 1st level interface memory item chart color
     */
    public static final Color MEMORY = new Color(0xFF3391FF, true);

    /**
     * 1st level interface CPU item chart color
     */
    public static final Color CPU = new Color(0xFF3391FF, true);

    /**
     * 2nd level interface memory item chart color: Java
     */
    public static final Color MEM_JAVA = new Color(0xFF5BBED4, true);

    /**
     * 2nd level interface memory item chart color: Native
     */
    public static final Color MEM_NATIVE = new Color(0xFFC3FFCA, true);

    /**
     * 2nd level interface memory item chart color: Graphics
     */
    public static final Color MEM_GRAPHICS = new Color(0xFFFFA9A9, true);

    /**
     * 2nd level interface memory item chart color: Stack
     */
    public static final Color MEM_STACK = new Color(0xFF9E6EDA, true);

    /**
     * 2nd level interface memory item chart color: Code
     */
    public static final Color MEM_CODE = new Color(0xFFFF9167, true);

    /**
     * 2nd level interface memory item chart color: Others
     */
    public static final Color MEM_OTHERS = new Color(0xFF50B4F3, true);

    /**
     * Network Legend color: Received
     */
    public static final Color NETWORK_RCV = new Color(0xFF66ADFF, true);

    /**
     * Network Legend color: Sent
     */
    public static final Color NETWORK_SENT = new Color(0xFFFF9201, true);

    /**
     * Network Legend color: Connections
     */
    public static final Color NETWORK_CONN = new Color(0xFF7D6B64, true);

    /**
     * Energy Legend color: CPU
     */
    public static final Color ENERGY_CPU = new Color(0xFF535DA6, true);

    /**
     * Energy Legend color: Network
     */
    public static final Color ENERGY_NETWORK = new Color(0xFF878FCA, true);

    /**
     * Energy Legend color: Location
     */
    public static final Color ENERGY_LOCATION = new Color(0xFFB9BFE8, true);

    /**
     * Energy Legend color: System event Location
     */
    public static final Color ENERGY_EVENT_LOCATION = new Color(65, 155, 249);

    /**
     * Energy Legend color: System event Wake Locks
     */
    public static final Color ENERGY_EVENT_LOCKS = new Color(0xFFC15A65, true);

    /**
     * Energy Legend color: System event Alarms&Jobs
     */
    public static final Color ENERGY_EVENT_ALARMS_JOBS = new Color(0xFFFFC880, true);

    /**
     * DISK_IO_READ
     */
    public static final Color DISK_IO_READ = new Color(0xFFDD9A16, true);

    /**
     * DISK_IO_WRITE
     */
    public static final Color DISK_IO_WRITE = new Color(0xFF04A9AC, true);

    /**
     * trace table COlOR
     */
    public static final Color TRACE_TABLE_COLOR = new Color(20, 21, 25);

    /**
     * native record border color
     */
    public static final Color NATIVE_RECORD_BORDER = new Color(0x474F59);

    /**
     * JComBoBox border color
     */
    public static final Color COMBOBOX_BORDER = new Color(71, 79, 89);

    /**
     * Hyperlinks color
     */
    public static final Color HYPERLINK = new Color(0, 118, 255);

    /**
     * selected color
     */
    public static final Color SELECTED_COLOR = new Color(12, 101, 209);

    /**
     * url color
     */
    public static final JBColor URL_COLOR = new JBColor(new Color(0x0076FF, true), new Color(0, 118, 255));

    /**
     * ability background color
     */
    public static final Color ABILITY_COLOR = new JBColor(new Color(0xFF494E52, true), new Color(73, 78, 82));

    /**
     * ability active color
     */
    public static final Color ABILITY_ACTIVE_COLOR = new JBColor(new Color(0xFF0C65D1, true), new Color(12, 101, 209));

    /**
     * ability initial color
     */
    public static final Color ABILITY_INITIAL_COLOR =
        new JBColor(new Color(0xFFAEB1B3, true), new Color(174, 177, 179));

    /**
     * network request color
     */
    public static final Color NETWORK_REQUEST_COLOR = new JBColor(new Color(0xFF9201), new Color(255, 146, 1));

    /**
     * network response color
     */
    public static final Color NETWORK_RESPONSE_COLOR = new JBColor(new Color(0x3391FF, true), new Color(51, 145, 255));

    /**
     * network overview text color
     */
    public static final Color NETWORK_OVER_VIEW_COLOR = new JBColor(new Color(0xFF999999, true), Gray._153);

    /**
     * ability home color
     */
    public static final Color ABILITY_HOME_COLOR = new JBColor(new Color(0xE88FAE, true), new Color(232, 143, 174));

    /**
     * ability home other color
     */
    public static final Color ABILITY_HOME_OTHER_COLOR =
        new JBColor(new Color(0xECA5BF, true), new Color(236, 165, 191));

    /**
     * ability back event color
     */
    public static final Color ABILITY_BACK_COLOR = new JBColor(new Color(0xACACAC), Gray._172);
}
