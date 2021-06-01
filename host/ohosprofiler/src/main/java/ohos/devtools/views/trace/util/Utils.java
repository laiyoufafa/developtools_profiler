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

import org.apache.commons.collections.map.HashedMap;

import java.awt.Rectangle;
import java.util.Map;

/**
 * Tools
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public final class Utils {
    private static Map<String, String> statusMap = new HashedMap();
    private static Utils instance;

    private Utils() {
        statusMap.put("D", "Uninterruptible Sleep");
        statusMap.put("S", "Sleeping");
        statusMap.put("R", "Runnable");
        statusMap.put("Running", "Running");
        statusMap.put("R+", "Runnable (Preempted)");
        statusMap.put("DK", "Uninterruptible Sleep + Wake Kill");
        statusMap.put("I", "Task Dead");
        statusMap.put("T", "Stopped");
        statusMap.put("t", "Traced");
        statusMap.put("X", "Exit (Dead)");
        statusMap.put("Z", "Exit (Zombie)");
        statusMap.put("K", "Wake Kill");
        statusMap.put("W", "Waking");
        statusMap.put("P", "Parked");
        statusMap.put("N", "No Load");
    }

    /**
     * Gets the value of statusMap .
     *
     * @return Get state collection
     */
    public Map<String, String> getStatusMap() {
        return statusMap;
    }

    /**
     * Get singleton object
     *
     * @return Utils
     */
    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    /**
     * Calculate whether it is within the range of Rectangle according to the x and y coordinates
     *
     * @param rect  rect
     * @param xAxis xAxis
     * @param yAxis yAxis
     * @return boolean
     */
    public static boolean pointInRect(final Rectangle rect, final int xAxis, final int yAxis) {
        if (rect == null) {
            return false;
        }
        return xAxis >= rect.x && xAxis <= rect.x + rect.width && yAxis >= rect.y && yAxis < rect.y + rect.height;
    }

    /**
     * Get the last status description
     *
     * @param state state
     * @return String
     */
    public static String getEndState(final String state) {
        if (Utils.getInstance().getStatusMap().containsKey(state)) {
            return Utils.getInstance().getStatusMap().get(state);
        } else {
            return "Unknown State";
        }
    }
}
