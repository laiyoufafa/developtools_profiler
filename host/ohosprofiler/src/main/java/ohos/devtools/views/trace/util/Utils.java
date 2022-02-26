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

import java.awt.Point;
import java.awt.Rectangle;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Tools
 *
 * @since 2021/04/22 12:25
 */
public final class Utils {
    private static final String[] UNITS = new String[] {"", "K", "M", "G", "T", "E"};
    private static Map<String, String> statusMap = new HashedMap();
    private static Utils instance = new Utils();
    private static ExecutorService pool = new ThreadPoolExecutor(8, 8,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>());

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
     * Get singleton object
     *
     * @return Utils
     */
    public static Utils getInstance() {
        return instance;
    }

    /**
     * Calculate whether it is within the range of Rectangle according to the x and y coordinates
     *
     * @param rect rect
     * @param xAxis xAxis
     * @param yAxis yAxis
     * @return boolean
     */
    public static boolean pointInRect(final Rectangle rect, final int xAxis, final int yAxis) {
        if (rect == null) {
            return false;
        }
        return xAxis >= Utils.getX(rect) && xAxis <= Utils.getX(rect) + rect.width && yAxis >= Utils.getY(rect)
            && yAxis < Utils.getY(rect) + rect.height;
    }

    /**
     * Calculate whether it is within the range of Rectangle according to the x and y coordinates
     *
     * @param rect rect
     * @param point event point
     * @return boolean
     */
    public static boolean pointInRect(final Rectangle rect, final Point point) {
        if (rect == null || point == null) {
            return false;
        }
        int xAxis = Utils.getX(point);
        int yAxis = Utils.getY(point);
        return xAxis >= Utils.getX(rect) && xAxis <= Utils.getX(rect) + rect.width && yAxis >= Utils.getY(rect)
            && yAxis < Utils.getY(rect) + rect.height;
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
            if ("".equals(state) || state == null) {
                return "";
            }
            return "Unknown State";
        }
    }

    /**
     * transform time unit ns to ms
     *
     * @param data time
     * @return String
     */
    public static String transformTimeToMs(Number data) {
        double ms;
        if (data instanceof Long) {
            ms = (Long) data / 1000000.0;
        } else if (data instanceof Double) {
            ms = (Double) data / 1000000.0;
        } else {
            ms = 0.0;
        }
        return new DecimalFormat("#.#######").format(ms);
    }

    /**
     * transform str to md5
     *
     * @param str str
     * @return String str to md5 string
     */
    public static String md5String(String str) {
        String result = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(str.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte val : digest) {
                builder.append(Integer.toHexString((0x000000FF & val) | 0xFFFFFF00).substring(6));
            }
            result = builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
        return result;
    }

    /**
     * get the db pool
     *
     * @return ExecutorService db pool
     */
    public static ExecutorService getPool() {
        return pool;
    }

    /**
     * reset the db pool
     */
    public static void resetPool() {
        pool.shutdownNow();
        pool = new ThreadPoolExecutor(8, 8,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    }

    /**
     * reset the db pool
     *
     * @param num pool size
     */
    public static void resetPool(int num) {
        pool.shutdownNow();
        pool = new ThreadPoolExecutor(num, num,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    }

    /**
     * get rectangle x
     *
     * @param rectangle rectangle
     * @return rectangleX
     */
    public static int getX(Rectangle rectangle) {
        return rectangle.x;
    }

    /**
     * get point x
     *
     * @param point point
     * @return pointX
     */
    public static int getX(Point point) {
        return point.x;
    }

    /**
     * set rectangle x
     *
     * @param rectangle rectangle
     * @param xVal xVal
     */
    public static void setX(Rectangle rectangle, int xVal) {
        rectangle.x = xVal;
    }

    /**
     * set point x
     *
     * @param point rectangle
     * @param xVal xVal
     */
    public static void setX(Point point, int xVal) {
        point.x = xVal;
    }

    /**
     * set rectangle y
     *
     * @param rectangle rectangle
     * @param yVal yVal
     */
    public static void setY(Rectangle rectangle, int yVal) {
        rectangle.y = yVal;
    }

    /**
     * set point y
     *
     * @param point point
     * @param yVal yVal
     */
    public static void setY(Point point, int yVal) {
        point.y = yVal;
    }

    /**
     * get rectangle y
     *
     * @param rectangle rectangle
     * @return rectangleY
     */
    public static int getY(Rectangle rectangle) {
        return rectangle.y;
    }

    /**
     * get point y
     *
     * @param point point
     * @return rectangleY
     */
    public static int getY(Point point) {
        return point.y;
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
     * get the unit cpu freq
     *
     * @param maxFreq maxFreq
     * @return CpuFreq
     */
    public static String getHzUnit(Long maxFreq) {
        StringBuilder builder = new StringBuilder();
        if (maxFreq > 1000000000L) {
            builder.append(String.format("%.2f", maxFreq / 1000000000D));
            builder.append("G");
        } else if (maxFreq > 1000000L) {
            builder.append(String.format("%.2f", maxFreq / 1000000D));
            builder.append("M");
        } else if (maxFreq > 1000L) {
            builder.append(String.format("%.2f", maxFreq / 1000D));
            builder.append("K");
        } else {
            builder.toString();
        }
        builder.append("hz");
        return builder.toString();
    }
}
