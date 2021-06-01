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

package ohos.devtools.services.memory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description MemoryInstanceDetailsInfoTest
 * @Date 2021/4/3 20:10
 **/
public class MemoryInstanceDetailsInfoTest {
    /**
     * memoryInstanceDetailsInfo
     */
    public MemoryInstanceDetailsInfo memoryInstanceDetailsInfo;

    /**
     * functional test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDetailsInfo_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK61P
     */
    @Before
    public void init() {
        memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
    }

    /**
     * functional test
     *
     * @tc.name: test
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDetailsInfo_test_0001
     * @tc.desc: test
     * @tc.type: functional testing
     * @tc.require: AR000FK61P
     */
    @Test
    public void test() {
        memoryInstanceDetailsInfo.setInstanceId(3);
        memoryInstanceDetailsInfo.setClassName("test");
        memoryInstanceDetailsInfo.setFieldName("tste");
        memoryInstanceDetailsInfo.setMethodName("test");
        memoryInstanceDetailsInfo.setId(1);
        memoryInstanceDetailsInfo.setLineNumber(2);
        memoryInstanceDetailsInfo.getInstanceId();
        memoryInstanceDetailsInfo.getClassName();
        memoryInstanceDetailsInfo.getFieldName();
        memoryInstanceDetailsInfo.getMethodName();
        memoryInstanceDetailsInfo.getId();
        memoryInstanceDetailsInfo.getLineNumber();
        memoryInstanceDetailsInfo.toString();
        Assert.assertNotNull(memoryInstanceDetailsInfo);
    }
}