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

package ohos.devtools.views.perftrace;

import com.intellij.ui.JBColor;
import ohos.devtools.views.applicationtrace.bean.TreeTableBean;
import ohos.devtools.views.perftrace.bean.PrefFunc;

import java.awt.Color;

/**
 * The PerfColorUtil
 *
 * @since 2021/04/22 12:25
 */
public class PerfColorUtil {
    /**
     * PERF_VENDOR color
     */
    public static final Color PERF_VENDOR = new JBColor(0xA2DEFF, 0xA2DEFF);

    /**
     * PERF_PLATFORM color
     */
    public static final Color PERF_PLATFORM = new JBColor(0xFECC82, 0xFECC82);

    /**
     * PERF_APP color
     */
    public static final Color PERF_APP = new JBColor(0x9FEAAD, 0x9FEAAD);

    /**
     * PERF_FLAME_VENDOR color
     */
    public static final Color PERF_FLAME_VENDOR = new JBColor(0xFFC56F, 0xFFC56F);

    /**
     * PERF_FLAME_PLATFORM color
     */
    public static final Color PERF_FLAME_PLATFORM = new JBColor(0xFF855E, 0xFF855E);

    /**
     * PERF_FLAME_APP color
     */
    public static final Color PERF_FLAME_APP = new JBColor(0xFFE0B2, 0xFFE0B2);

    /**
     * get the func color by type
     *
     * @param func func
     * @return Color Color
     */
    public static Color getJavaMethod(PrefFunc func) {
        String funcName = func.getFuncName();
        boolean result = funcName.startsWith("java.") || funcName.startsWith("sun.") || funcName.startsWith("javax.");
        if (result || funcName.startsWith("apple.") || funcName.startsWith("com.apple.")) {
            return PERF_VENDOR;
        } else {
            return PERF_APP;
        }
    }

    /**
     * get the TreeTableBean color by type
     *
     * @param func func
     * @return Color Color
     */
    public static Color getJavaMethod(TreeTableBean func) {
        String funcName = func.getName();
        boolean nameResult =
            funcName.startsWith("java.") || funcName.startsWith("sun.") || funcName.startsWith("javax.");
        if (nameResult || funcName.startsWith("apple.") || funcName.startsWith("com.apple.")) {
            return PERF_FLAME_VENDOR;
        } else {
            return PERF_FLAME_APP;
        }
    }

    /**
     * get the PrefFunc color by type
     *
     * @param func func
     * @return Color Color
     */
    public static Color getPerfMethod(PrefFunc func) {
        String funcName = func.getFuncName();
        if (func.isUserWrite() && funcName.contains("(")) {
            return PERF_APP;
        } else {
            return PERF_VENDOR;
        }
    }

    /**
     * get the TreeTableBean color by type
     *
     * @param func func
     * @return Color Color
     */
    public static Color getPerfMethod(TreeTableBean func) {
        if (func.isUserWrite()) {
            return PERF_FLAME_APP;
        } else {
            return PERF_FLAME_VENDOR;
        }
    }
}
