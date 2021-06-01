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
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public final class ColorUtils {
    /**
     * Gray color object
     */
    public static final Color GREY_COLOR = Color.getHSBColor(0, 0, 62); // grey

    /**
     * Color array of all current columns
     */
    public static final Color[] MD_PALETTE = new Color[] {new Color(0xf44034), // red
        new Color(0xe92063), // pink
        new Color(0x9b27b0), // purple
        new Color(0x673ab6), // deep purple
        new Color(0x4051b5), // indigo
        new Color(0x2094f3), // blue
        new Color(0x02a6f2), // light blue
        new Color(0x00bdd6), // cyan
        new Color(0x009485), // teal
        new Color(0x4cae4f), // green
        new Color(0x8bc34b), // light green
        new Color(0xcbdc38), // lime
        new Color(0xff9105), // amber 0xffc105
        new Color(0xff9900), // orange
        new Color(0xff5724), // deep orange
        new Color(0x795649), // brown
        new Color(0x607c8a), // blue gray
        new Color(0xbdaa00), // yellow 0xffec3d
    };

    /**
     * Current method color array
     */
    public static final Color[] FUNC_COLOR = new Color[] {new Color(0x9b27b0), // purple
        new Color(0x317d31), new Color(0x673ab6), // deep purple
        new Color(0xa25b57), new Color(0x4051b5), // indigo
        new Color(0x2094f3), // blue
        new Color(0x99ba36), new Color(0x4051b5) };

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
