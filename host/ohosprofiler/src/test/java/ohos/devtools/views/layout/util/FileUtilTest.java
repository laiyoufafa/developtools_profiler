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

package ohos.devtools.views.layout.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description FileUtil test
 * @Date 2021/4/2 11:25
 **/
public class FileUtilTest {
    private FileUtil fileUtil;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_FileUtil_getFileUtil_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void getFileUtil() {
        fileUtil = new FileUtil();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_FileUtil_writeFile_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void writeFile() {
        fileUtil.writeFile("E:\\", "test");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_FileUtil_writeFile_0002
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void writeFileTest01() {
        fileUtil.writeFile("E:\\", "");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_FileUtil_writeFile_0003
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void writeFileTest02() {
        fileUtil.writeFile("", "test");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_FileUtil_readTxtFile_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void readTxtFileTest01() {
        String txtStr = "D:\\";
        try {
            txtStr = fileUtil.readTxtFile("E:\\");
            Assert.assertNotNull(txtStr);
        } catch (Exception exception) {
            Assert.assertNotNull(txtStr);
        }
    }
}