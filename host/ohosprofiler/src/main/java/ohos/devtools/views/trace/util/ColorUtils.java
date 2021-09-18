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

import ohos.devtools.views.trace.bean.CpuData;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Color tool
 *
 * @date 2021/04/22 12:25
 */
public final class ColorUtils {
    /**
     * Gray color object
     */
    public static final Color GREY_COLOR = Color.getHSBColor(0, 0, 62); // grey

    /**
     * Color array of all current columns
     */
    public static final Color[] MD_PALETTE = new Color[] {new Color(0x3391ff), // red
        new Color(0x0076ff), // pink
        new Color(0x66adff), // purple
        new Color(0x2db3aa), // deep purple
        new Color(0x008078), // indigo
        new Color(0x73e6de), // blue
        new Color(0x535da6), // light blue
        new Color(0x38428c), // cyan
        new Color(0x7a84cc), // teal
        new Color(0xff9201), // green
        new Color(0xff7500), // light green
        new Color(0xffab40), // lime
        new Color(0x2db4e2), // amber 0xffc105
        new Color(0x0094c6), // orange
        new Color(0x7cdeff), // deep orange
        new Color(0xffd44a), // brown
        new Color(0xfbbf00), // blue gray
        new Color(0xffe593), // yellow 0xffec3d
    };

    /**
     * Current method color array
     */
    public static final Color[] FUNC_COLOR = new Color[] {new Color(0x3391ff), // purple
        new Color(0x2db4e2), new Color(0x2db3aa), // deep purple
        new Color(0xffd44a), new Color(0x535da6), // indigo
        new Color(0x008078), // blue
        new Color(0xff9201), new Color(0x38428c)};

    private static Map<Integer, Color> colorHashMap = new ConcurrentHashMap();

    private ColorUtils() {
    }

    /**
     * Get color according to id
     *
     * @param id id
     * @return Color Color
     */
    public static Color getColor(final int id) {
        if (colorHashMap.containsKey(id)) {
            return colorHashMap.get(id);
        } else {
            final int red = ((id * 10000000) & 0xff0000) >> 16;
            final int green = ((id * 10000000) & 0x00ff00) >> 8;
            final int blue = id * 10000000 & 0x0000ff;
            final Color color = new Color(red, green, blue, 255);
            colorHashMap.put(id, color);
            return color;
        }
    }

    /**
     * Get the color value according to the length of the string
     *
     * @param str str
     * @param max max
     * @return int
     */
    public static int hash(final String str, final int max) {
        final int colorA = 0x811c9dc5;
        final int colorB = 0xfffffff;
        final int colorC = 16777619;
        final int colorD = 0xffffffff;
        int hash = colorA & colorB;
        for (int index = 0; index < str.length(); index++) {
            hash ^= str.charAt(index);
            hash = (hash * colorC) & colorD;
        }
        return Math.abs(hash) % max;
    }

    /**
     * Get color based on cpu object data
     *
     * @param thread thread
     * @return Color
     */
    public static Color colorForThread(final CpuData thread) {
        if (thread == null) {
            return GREY_COLOR;
        }
        int tid = thread.getProcessId() >= 0 ? thread.getProcessId() : thread.getTid();
        return colorForTid(tid);
    }

    /**
     * Get color according to tid
     *
     * @param tid tid
     * @return Color
     */
    public static Color colorForTid(final int tid) {
        int colorIdx = hash(String.valueOf(tid), MD_PALETTE.length);
        return MD_PALETTE[colorIdx];
    }
}
