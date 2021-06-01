/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ohos.devtools.views.common.treetable;

import ohos.devtools.views.common.chart.treetable.DataNodeCompares;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

/**
 * DataNodeCompares test
 *
 * @Description DataNodeCompares test
 * @Date 2021/4/3 20:29
 **/
public class DataNodeComparesTest {
    /**
     * test int
     */
    private DataNodeCompares dataNodeCompares = new DataNodeCompares();

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNodeCompares_classNameString
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testclassNameString() {
        Comparator classNameCompareAsc = dataNodeCompares.chooseCompare(0, "ASCENDING");
        Comparator allocationsCompareAsc = dataNodeCompares.chooseCompare(1, "ASCENDING");
        Comparator deallocationsCompareAsc = dataNodeCompares.chooseCompare(2, "ASCENDING");
        Comparator totalCompareAsc = dataNodeCompares.chooseCompare(3, "ASCENDING");
        Comparator shallowSizeCompareAsc = dataNodeCompares.chooseCompare(4, "ASCENDING");
        Comparator classNameCompareDsc = dataNodeCompares.chooseCompare(0, "ASCENDING1");
        Comparator allocationsCompareDsc = dataNodeCompares.chooseCompare(1, "ASCENDING1");
        Comparator deallocationsCompareDsc = dataNodeCompares.chooseCompare(2, "ASCENDING1");
        Comparator totalCompareDsc = dataNodeCompares.chooseCompare(3, "ASCENDING1");
        Comparator shallowSizeCompareDsc = dataNodeCompares.chooseCompare(4, "ASCENDING1");
        Comparator classNameString = DataNodeCompares.classNameString;
        Assert.assertNotNull(classNameCompareAsc);
        Assert.assertNotNull(classNameCompareDsc);
        Assert.assertNotNull(allocationsCompareAsc);
        Assert.assertNotNull(allocationsCompareDsc);
        Assert.assertNotNull(deallocationsCompareAsc);
        Assert.assertNotNull(deallocationsCompareDsc);
        Assert.assertNotNull(totalCompareAsc);
        Assert.assertNotNull(totalCompareDsc);
        Assert.assertNotNull(shallowSizeCompareAsc);
        Assert.assertNotNull(shallowSizeCompareDsc);
        Assert.assertNotNull(classNameString);
    }

}
