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

package ohos.devtools.services.hiperf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

/**
 * PerfDAO Test
 *
 * @since 2021/2/1 9:31
 */
public class PerfDAOTest {
    private PerfDAO per;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_hiperf_PerfDAO_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        per = PerfDAO.getInstance();
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_hiperf_PerfDAO_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        PerfDAO perfDAO = PerfDAO.getInstance();
        Assert.assertNotNull(perfDAO);
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_hiperf_PerfDAO_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        PerfDAO perfDAO = PerfDAO.getInstance();
        PerfDAO perf = PerfDAO.getInstance();
        Assert.assertEquals(perfDAO, perf);
    }

    /**
     * create Table Test
     *
     * @tc.name: createTableTest
     * @tc.number: OHOS_JAVA_Service_hiperf_PerfDAO_createTableTest_0001
     * @tc.desc: create Table Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void createTableTest() {
        per.createTable(null);
        Assert.assertTrue(true);
    }


    /**
     * insert Perf Sample Test
     *
     * @tc.name: insertPerfSampleTest
     * @tc.number: OHOS_JAVA_Service_hiperf_PerfDAO_insertPerfSampleTest_0001
     * @tc.desc: insert Perf Sample Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getConnTest() {
        Connection conn = per.getConn();
        Assert.assertNotNull(conn);
    }
}