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

import ohos.devtools.views.common.LayoutConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Description CommonUtilTest
 * @Date 2021/4/9 13:15
 **/
public class CommonUtilTest {
    /**
     * functional testing collectionSize
     *
     * @tc.name: collectionSize
     * @tc.number: OHOS_JAVA_common_CommonUtil_collectionSize_0001
     * @tc.desc: collectionSize
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testcollectionSizeOne() {
        int num = CommonUtil.collectionSize(0);
        Assert.assertEquals(num, LayoutConstants.SIXTEEN);
    }

    /**
     * functional testing collectionSize
     *
     * @tc.name: collectionSize
     * @tc.number: OHOS_JAVA_common_CommonUtil_collectionSize_0002
     * @tc.desc: collectionSize
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testcollectionSizeTwo() {
        int num = CommonUtil.collectionSize(1);
        Assert.assertNotNull(num);
    }

    /**
     * functional testing getRequestId
     *
     * @tc.name: getRequestId
     * @tc.number: OHOS_JAVA_common_CommonUtil_getRequestId_0001
     * @tc.desc: getRequestId
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetRequestId() {
        int num = CommonUtil.getRequestId();
        Assert.assertNotNull(num);
    }

    /**
     * functional testing generateSessionName
     *
     * @tc.name: generateSessionName
     * @tc.number: OHOS_JAVA_common_CommonUtil_generateSessionName_0001
     * @tc.desc: generateSessionName
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgenerateSessionName() {
        String sessionName = CommonUtil.generateSessionName("", 1);
        Assert.assertNotNull(sessionName);
    }

    /**
     * functional testing getLocalSessionId
     *
     * @tc.name: getLocalSessionId
     * @tc.number: OHOS_JAVA_common_CommonUtil_getLocalSessionId_0001
     * @tc.desc: getLocalSessionId
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetLocalSessionId() {
        long num = CommonUtil.getLocalSessionId();
        Assert.assertNotNull(num);
    }
}
