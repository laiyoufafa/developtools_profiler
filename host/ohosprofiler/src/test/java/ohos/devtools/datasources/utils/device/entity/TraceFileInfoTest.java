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

package ohos.devtools.datasources.utils.device.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Description TraceFileInfoTest
 * @Date 2021/4/3 19:46
 **/
public class TraceFileInfoTest {
    /**
     * functional testing TraceFileInfo initialization configuration
     *
     * @tc.name: TraceFileInfo initialization configuration
     * @tc.number: OHOS_JAVA_device_TraceFileInfo_init_0001
     * @tc.desc: TraceFileInfo initialization configuration
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getTraceFileInfo() {
        TraceFileInfo traceFileInfo = new TraceFileInfo();
        traceFileInfo.setCreateTime(2342L);
        traceFileInfo.setRecordNum(73829L);
        traceFileInfo.setVersion("test");
        traceFileInfo.getCreateTime();
        traceFileInfo.getRecordNum();
        traceFileInfo.getVersion();
        Assert.assertNotNull(traceFileInfo);
    }
}