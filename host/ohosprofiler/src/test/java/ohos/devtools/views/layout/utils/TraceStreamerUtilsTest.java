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

package ohos.devtools.views.layout.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TraceStreamerUtilsTest
 */
public class TraceStreamerUtilsTest {
    private TraceStreamerUtils traceStreamerUtils;

    /**
     * get Instance init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Before
    public void init() {
        traceStreamerUtils = TraceStreamerUtils.getInstance();
    }

    /**
     * get Instance getInstanceTest
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getInstanceTest_0001
     * @tc.desc: getInstanceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest01() {
        TraceStreamerUtils traceStreamerUtilsInstance = TraceStreamerUtils.getInstance();
        Assert.assertNotNull(traceStreamerUtilsInstance);
    }

    /**
     * get Instance getInstanceTest
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getInstanceTest_0002
     * @tc.desc: getInstanceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest02() {
        TraceStreamerUtils traceStreamerUtils1 = TraceStreamerUtils.getInstance();
        TraceStreamerUtils traceStreamerUtils2 = TraceStreamerUtils.getInstance();
        Assert.assertEquals(traceStreamerUtils1, traceStreamerUtils2);
    }

    /**
     * get Instance getBaseDirTest
     *
     * @tc.name: getBaseDirTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getBaseDirTest_0001
     * @tc.desc: getBaseDirTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getBaseDirTest01() {
        String str = traceStreamerUtils.getBaseDir();
        Assert.assertNotEquals("", str);
    }

    /**
     * get Instance getLogPathTest
     *
     * @tc.name: getLogPathTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getLogPathTest_0001
     * @tc.desc: getLogPathTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getLogPathTest01() {
        String str = traceStreamerUtils.getLogPath();
        Assert.assertNotEquals("", str);
    }

    /**
     * get Instance getDbPathTest
     *
     * @tc.name: getDbPathTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getDbPathTest_0001
     * @tc.desc: getDbPathTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDbPathTest01() {
        String str = traceStreamerUtils.getDbPath();
        Assert.assertNotEquals("", str);
    }

    /**
     * get Instance getDbPathTwoTest
     *
     * @tc.name: getDbPathTwoTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getDbPathTwoTest_0001
     * @tc.desc: getDbPathTwoTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDbPathTwoTest01() {
        String str = traceStreamerUtils.getDbPath("dbName");
        Assert.assertNotEquals("", str);
    }

    /**
     * get Instance getTraceStreamerAppTest
     *
     * @tc.name: getTraceStreamerAppTest
     * @tc.number: OHOS_JAVA_layout_TraceStreamerUtils_getTraceStreamerAppTest_0001
     * @tc.desc: getTraceStreamerAppTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getTraceStreamerAppTest01() {
        String str = traceStreamerUtils.getTraceStreamerApp();
        Assert.assertNotEquals("", str);
    }
}
