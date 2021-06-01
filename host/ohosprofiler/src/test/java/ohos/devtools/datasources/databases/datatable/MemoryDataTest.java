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

package ohos.devtools.datasources.databases.datatable;

import ohos.devtools.datasources.databases.datatable.enties.MemoryData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description MemoryDataTest
 * @Date 2021/4/23 11:30
 **/
public class MemoryDataTest {
    /**
     * 时间戳
     */
    private long timeStamp = 2006 - 07 - 11;

    /**
     * sessionId数字3243
     */
    private int sessionId = 3243;

    /**
     * localSessionId
     */
    private long localSessionId = 1619147859561L;

    /**
     * memoryData 实例
     */
    private MemoryData memoryData = new MemoryData();

    /**
     * object 数据实例
     */
    private Object object = new Object();

    /**
     * functional testing init
     *
     * @tc.name: initObj
     * @tc.number: OHOS_JAVA_database_MemoryData_initObj_0001
     * @tc.desc: initObj
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Before
    public void initObj() {
        memoryData.setSession(localSessionId);
        memoryData.setSessionId(sessionId);
        memoryData.setTimeStamp(timeStamp);
        memoryData.setData(object);
    }

    /**
     * functional testing getSession
     *
     * @tc.name: getSession
     * @tc.number: OHOS_JAVA_database_MemoryData_getSession_0001
     * @tc.desc: getSession
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getSessionTest() {
        long session = memoryData.getSession();
        Assert.assertEquals(session, 1619147859561L);
    }

    /**
     * functional testing getSessionId
     *
     * @tc.name: getSessionId
     * @tc.number: OHOS_JAVA_database_MemoryData_getSessionId_0001
     * @tc.desc: getSessionId
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getSessionIdTest() {
        int session = memoryData.getSessionId();
        Assert.assertEquals(session, 3243);
    }

    /**
     * functional testing getlocalSessionId
     *
     * @tc.name: getlocalSessionId
     * @tc.number: OHOS_JAVA_database_MemoryData_getlocalSessionId_0001
     * @tc.desc: getlocalSessionId
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getlocalSessionIdTest() {
        long session = memoryData.getLocalSessionId();
        Assert.assertEquals(session, 1619147859561L);
    }

    /**
     * functional testing getTimeStamp
     *
     * @tc.name: getTimeStamp
     * @tc.number: OHOS_JAVA_database_MemoryData_getTimeStamp_0001
     * @tc.desc: getTimeStamp
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getTimeStampTest() {
        long session = memoryData.getTimeStamp();
        Assert.assertEquals(session, 2006 - 07 - 11);
    }

    /**
     * functional testing getData
     *
     * @tc.name: getData
     * @tc.number: OHOS_JAVA_database_MemoryData_getData_0001
     * @tc.desc: getData
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getDataTest() {
        Object obj = memoryData.getData();
        Assert.assertNotNull(obj);
    }

}
