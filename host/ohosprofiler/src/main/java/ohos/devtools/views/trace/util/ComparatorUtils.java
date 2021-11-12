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

import java.util.Comparator;

/**
 * ComparatorUtils
 *
 * @since 2021/5/27 12:29
 */
public class ComparatorUtils {
    /**
     * generate Comparator Ruler
     *
     * @param filter filter the row when row value equals filter
     * @return Comparator
     */
    public static Comparator<Object> generateComparator(String filter) {
        Comparator<Object> comparator = (str1, str2) -> {
            if (filter == null) {
                return 0;
            }
            if (filter.equals(String.valueOf(str1)) || filter.equals(String.valueOf(str2))) {
                return 0;
            }
            try {
                Double d1 = Double.parseDouble(String.valueOf(str1));
                Double d2 = Double.parseDouble(String.valueOf(str2));
                int val = 0;
                if (d1 < d2) {
                    val = -1;
                } else if (d1 == d2) {
                    val = 0;
                } else {
                    val = 1;
                }
                return val;
            } catch (NumberFormatException exception) {
                String s1 = String.valueOf(str1);
                String s2 = String.valueOf(str2);
                return s1.compareTo(s2);
            }
        };
        return comparator;
    }
}
