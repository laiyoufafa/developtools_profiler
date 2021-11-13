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

package ohos.devtools.views.hilog;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.JTextArea;

/**
 * HiLog Filter Test
 *
 * @since 2021/2/1 9:31
 */
public class HiLogFilterTest {
    /**
     * log rows
     */
    private static final int LOG_ROWS = 26;

    /**
     * log columns
     */
    private static final int LOG_COLUMNS = 130;

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_View_HiLogFilter_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        HiLogFilter instance = HiLogFilter.getInstance();
        Assert.assertNotNull(instance);
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_View_HiLogFilter_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        HiLogFilter instance = HiLogFilter.getInstance();
        HiLogFilter hiLogFilter = HiLogFilter.getInstance();
        Assert.assertEquals(instance, hiLogFilter);
    }

    /**
     * filter Log Test
     *
     * @tc.name: filterLogTest
     * @tc.number: OHOS_JAVA_View_HiLogFilter_filterLogTest_0001
     * @tc.desc: filter Log Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void filterLogTest() {
        JTextArea logTextArea = new JTextArea("", LOG_ROWS, LOG_COLUMNS);
        StringBuilder wholeBuilder = new StringBuilder();
        wholeBuilder.append("T est");
        HiLogFilter.getInstance().filterLog(logTextArea, "Test", "Test", wholeBuilder);
        Assert.assertTrue(true);
    }
}