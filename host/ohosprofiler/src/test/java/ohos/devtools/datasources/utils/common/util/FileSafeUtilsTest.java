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

package ohos.devtools.datasources.utils.common.util;

import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * File Safe Utils Test
 */
public class FileSafeUtilsTest {
    private String filepath;
    private String sha;
    private File file;

    /**
     * functional testing setFile
     *
     * @tc.name: setFile
     * @tc.number: OHOS_JAVA_common_FileSafeUtils_setFile_0001
     * @tc.desc: setFile
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Before
    public void setFile() {
        filepath = SessionManager.getInstance().getPluginPath() + "libmemdataplugin.z.so";
        sha = "SHA-256";
        file = new File(filepath);
    }

    /**
     * functional testing getFileSha
     *
     * @tc.name: getFileSha
     * @tc.number: OHOS_JAVA_common_FileSafeUtils_getFileSha_0001
     * @tc.desc: getFileSha
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getFileSha01() {
        String fileSha = FileSafeUtils.getFileSha(file, sha);
        Assert.assertNotNull(fileSha);
    }

    /**
     * functional testing getFileSha
     *
     * @tc.name: getFileSha
     * @tc.number: OHOS_JAVA_common_FileSafeUtils_getFileSha_0002
     * @tc.desc: getFileSha
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getFileSha02() {
        String fileSha = FileSafeUtils.getFileSha(file, null);
        Assert.assertNotNull(fileSha);
    }

    /**
     * functional testing getFileSha
     *
     * @tc.name: getFileSha
     * @tc.number: OHOS_JAVA_common_FileSafeUtils_getFileSha_0003
     * @tc.desc: getFileSha
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getFileSha03() {
        String fileSha = FileSafeUtils.getFileSha(null, sha);
        Assert.assertNotNull(fileSha);
    }

    /**
     * functional testing getFileSha
     *
     * @tc.name: getFileSha
     * @tc.number: OHOS_JAVA_common_FileSafeUtils_getFileSha_0004
     * @tc.desc: getFileSha
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getFileSha04() {
        String fileSha01 = FileSafeUtils.getFileSha(file, sha);
        String fileSha02 = FileSafeUtils.getFileSha(file, sha);
        Assert.assertEquals(fileSha01, fileSha02);
    }
}
