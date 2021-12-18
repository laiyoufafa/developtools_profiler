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

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.layout.TaskPanel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * OpenFileDialogUtilsTest
 *
 * @since 2021/2/1 9:31
 */
public class OpenFileDialogUtilsTest {
    private OpenFileDialogUtils openFileDialogUtils;

    /**
     * get Instance init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_layout_OpenFileDialogUtils_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Before
    public void init() {
        openFileDialogUtils = OpenFileDialogUtils.getInstance();
    }

    /**
     * get Instance getInstanceTest
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_layout_OpenFileDialogUtils_getInstanceTest_0001
     * @tc.desc: getInstanceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest01() {
        OpenFileDialogUtils openFileDialogUtilsInstance = OpenFileDialogUtils.getInstance();
        Assert.assertNotNull(openFileDialogUtilsInstance);
    }

    /**
     * get Instance getInstanceTest
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_layout_OpenFileDialogUtils_getInstanceTest_0002
     * @tc.desc: getInstanceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest02() {
        OpenFileDialogUtils openFileDialogUtils1 = OpenFileDialogUtils.getInstance();
        OpenFileDialogUtils openFileDialogUtils2 = OpenFileDialogUtils.getInstance();
        Assert.assertEquals(openFileDialogUtils1, openFileDialogUtils2);
    }

    /**
     * get Instance loadOfflineFileHprofTest
     *
     * @tc.name: loadOfflineFileHprofTest
     * @tc.number: OHOS_JAVA_layout_OpenFileDialogUtils_loadOfflineFileHprofTest_0001
     * @tc.desc: loadOfflineFileHprofTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void loadOfflineFileHprofTest01() {
        openFileDialogUtils.loadOfflineFileHprof(new File(""), new TaskPanel(), new JBPanel());
        Assert.assertTrue(true);
    }

    /**
     * get Instance loadSimplePerfTest
     *
     * @tc.name: loadSimplePerfTest
     * @tc.number: OHOS_JAVA_layout_OpenFileDialogUtils_loadSimplePerfTest_0001
     * @tc.desc: loadSimplePerfTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void loadPerfTest01() {
        openFileDialogUtils.loadPerf(new File(""), new JBPanel(), "");
        Assert.assertTrue(true);
    }

    /**
     * get Instance loadTraceTest
     *
     * @tc.name: loadTraceTest
     * @tc.number: OHOS_JAVA_layout_OpenFileDialogUtils_loadTraceTest_0001
     * @tc.desc: loadTraceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void loadTraceTest01() {
        openFileDialogUtils.loadTrace(new JBPanel(), new File(""), new JBPanel(), true);
        Assert.assertTrue(true);
    }
}
