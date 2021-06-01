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

package ohos.devtools.datasources.utils.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Description ConstantTest
 * @Date 2021/4/3 20:47
 **/
public class ConstantTest {
    /**
     * functional testing init
     *
     * @tc.name: constant test
     * @tc.number: OHOS_JAVA_utils_Constant_0001
     * @tc.desc: constant test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void test01() {
        Long abnormal = Constant.ABNORMAL;
        int normalStatus = Constant.NORMAL_STATUS;
        int fileImportScene = Constant.FILE_IMPORT_SCENE;
        String memoryPlug = Constant.MEMORY_PLUG;
        String cpuPlugName = Constant.CPU_PLUG_NAME;
        String destPath = Constant.DEST_PATH;
        String deviceSataStatPushed = Constant.DEVICE_SATA_STAT_PUSHED;
        String deviceStatClosed = Constant.DEVICE_STAT_CLOSED;
        String deviceStatError = Constant.DEVICE_STAT_ERROR;
        String deviceStatFail = Constant.DEVICE_STAT_FAIL;
        String deviceStatNotFound = Constant.DEVICE_STAT_NOT_FOUND;
        String deviceStatOffline = Constant.DEVICE_STAT_OFFLINE;
        String deviceStatOnline = Constant.DEVICE_STAT_ONLINE;
        String deviceStstUnauthorized = Constant.DEVICE_STST_UNAUTHORIZED;
        String fileName = Constant.FILE_NAME;
        String fileSuffix = Constant.FILE_SUFFIX;
        String targetPlugPath = Constant.TARGET_PLUG_PATH;
        String sourceFilepath = Constant.SOURCE_FILEPATH;
        int realtimeScene = Constant.REALTIME_SCENE;
        int radix = Constant.RADIX;
        Assert.assertNotNull(radix);
        Assert.assertNotNull(realtimeScene);
        Assert.assertNotNull(sourceFilepath);
        Assert.assertNotNull(targetPlugPath);
        Assert.assertNotNull(fileSuffix);
        Assert.assertNotNull(fileName);
        Assert.assertNotNull(deviceStstUnauthorized);
        Assert.assertNotNull(deviceStatOnline);
        Assert.assertNotNull(deviceStatOffline);
        Assert.assertNotNull(deviceStatNotFound);
        Assert.assertNotNull(deviceStatFail);
        Assert.assertNotNull(deviceStatError);
        Assert.assertNotNull(deviceStatClosed);
        Assert.assertNotNull(deviceSataStatPushed);
        Assert.assertNotNull(destPath);
        Assert.assertNotNull(cpuPlugName);
        Assert.assertNotNull(abnormal);
        Assert.assertNotNull(normalStatus);
        Assert.assertNotNull(fileImportScene);
        Assert.assertNotNull(memoryPlug);
    }
}