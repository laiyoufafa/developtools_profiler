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

package ohos.devtools.services.diskio;

import ohos.devtools.datasources.databases.datatable.DiskIoTable;
import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * DiskIo Dao Test
 *
 * @since 2021/2/1 9:31
 */
public class UserDataDaoTest {
    private DiskIoDao diskIoDao;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        diskIoDao = DiskIoDao.getInstance();
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        DiskIoDao dao = DiskIoDao.getInstance();
        Assert.assertNotNull(dao);
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        DiskIoDao dao = DiskIoDao.getInstance();
        DiskIoDao diskIo = DiskIoDao.getInstance();
        Assert.assertEquals(dao, diskIo);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_getAllDataTest_0001
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getAllDataTest01() {
        List<DiskIOData> allData = diskIoDao.getAllData(1L);
        Assert.assertNotNull(allData);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_getAllDataTest_0002
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getAllDataTest02() {
        List<DiskIOData> allData = diskIoDao.getAllData(1L);
        List<DiskIOData> list = diskIoDao.getAllData(1L);
        Assert.assertEquals(allData, list);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_getDataTest_0001
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getDataTest01() {
        LinkedHashMap<Integer, List<ChartDataModel>> data = diskIoDao.getData(1L, 0, 2, 1L, true);
        Assert.assertNotNull(data);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_getDataTest_0002
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getDataTest02() {
        LinkedHashMap<Integer, List<ChartDataModel>> data = diskIoDao.getData(1L, 0, 2, 1L, true);
        LinkedHashMap<Integer, List<ChartDataModel>> map = diskIoDao.getData(1L, 0, 2, 1L, true);
        Assert.assertEquals(data, map);
    }

    /**
     * delete Session Data Test
     *
     * @tc.name: deleteSessionDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDao_deleteSessionDataTest_0001
     * @tc.desc: delete Session Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void deleteSessionDataTest() {
        DiskIoTable diskIoTable = new DiskIoTable();
        boolean res = diskIoDao.deleteSessionData(1L);
        Assert.assertFalse(res);
    }
}