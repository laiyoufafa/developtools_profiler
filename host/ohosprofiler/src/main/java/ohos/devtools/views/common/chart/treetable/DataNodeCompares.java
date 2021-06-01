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

package ohos.devtools.views.common.chart.treetable;

import java.util.Comparator;

import static ohos.devtools.views.common.LayoutConstants.ASC;

/**
 * @Description dataPoller回调类
 * @Date 2021/4/20 13:30
 **/
public class DataNodeCompares {
    /**
     * classNameString
     */
    public static Comparator classNameString = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int res = o1.compareTo(o2);
            if (res == 0) {
                return 0;
            }
            return res > 0 ? 1 : -1;
        }
    };

    private static Comparator classNameCompareDsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int res = o1.getClassName().compareTo(o2.getClassName());
            if (res == 0) {
                return 0;
            }
            return res < 0 ? 1 : -1;
        }
    };

    private static Comparator shallowSizeCompareAsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            long res = o1.getShallowSize() - o2.getShallowSize();
            if (res == 0) {
                return 0;
            }
            return res > 0 ? 1 : -1;
        }
    };

    private static Comparator shallowSizeCompareDsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            long res = o1.getShallowSize() - o2.getShallowSize();
            if (res == 0) {
                return 0;
            }
            return res < 0 ? 1 : -1;
        }
    };

    private static Comparator totalCompareAsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int res = o1.getTotalCount() - o2.getTotalCount();
            if (res == 0) {
                return 0;
            }
            return res > 0 ? 1 : -1;
        }
    };

    private static Comparator totalCompareDsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            int res = o1.getTotalCount() - o2.getTotalCount();
            if (res == 0) {
                return 0;
            }
            return res < 0 ? 1 : -1;
        }
    };

    private static Comparator allocationsCompareAsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int res = o1.getAllocations() - o2.getAllocations();
            if (res == 0) {
                return 0;
            }
            return res > 0 ? 1 : -1;
        }
    };

    private static Comparator allocationsCompareDsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            int res = o1.getAllocations() - o2.getAllocations();
            if (res == 0) {
                return 0;
            }
            return res < 0 ? 1 : -1;
        }
    };

    private static Comparator deallocationsCompareAsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int res = o1.getDeallocations() - o2.getDeallocations();
            if (res == 0) {
                return 0;
            }
            return res > 0 ? 1 : -1;
        }
    };

    private static Comparator deallocationsCompareDsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            int res = o1.getDeallocations() - o2.getDeallocations();
            if (res == 0) {
                return 0;
            }
            return res < 0 ? 1 : -1;
        }
    };

    /**
     * classNameCompareAsc
     */
    private Comparator classNameCompareAsc = new Comparator<DataNode>() {
        @Override
        public int compare(DataNode o1, DataNode o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int res = o1.getClassName().compareTo(o2.getClassName());
            if (res == 0) {
                return 0;
            }
            return res > 0 ? 1 : -1;
        }
    };

    /**
     * 选择出比较器
     *
     * @param keyColumn keyColumn
     * @param sortOrder sortOrder
     * @return Comparator
     */
    public Comparator chooseCompare(int keyColumn, String sortOrder) {
        Comparator comparator = null;
        if (ASC.equals(sortOrder)) {
            switch (keyColumn) {
                case 0:
                    comparator = classNameCompareAsc;
                    break;
                case 1:
                    comparator = allocationsCompareAsc;
                    break;
                case 2:
                    comparator = deallocationsCompareAsc;
                    break;
                case 3:
                    comparator = totalCompareAsc;
                    break;
                case 4:
                    comparator = shallowSizeCompareAsc;
                    break;
                default:
                    break;
            }
        } else {
            switch (keyColumn) {
                case 0:
                    comparator = classNameCompareDsc;
                    break;
                case 1:
                    comparator = allocationsCompareDsc;
                    break;
                case 2:
                    comparator = deallocationsCompareDsc;
                    break;
                case 3:
                    comparator = totalCompareDsc;
                    break;
                case 4:
                    comparator = shallowSizeCompareDsc;
                    break;
                default:
                    break;
            }
        }
        return comparator;
    }

}
