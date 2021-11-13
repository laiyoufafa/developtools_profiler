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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Disk Io Service Test
 *
 * @since 2021/2/1 9:31
 */
public class DiskIoServiceTest {
    private DiskIoService diskIoService;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoService_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        diskIoService = DiskIoService.getInstance();
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoService_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        DiskIoService diskIo = DiskIoService.getInstance();
        Assert.assertNotNull(diskIo);
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoService_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        DiskIoService diskIo = DiskIoService.getInstance();
        DiskIoService disk = DiskIoService.getInstance();
        Assert.assertEquals(disk, diskIo);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoService_getAllDataTest_0001
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getAllDataTest01() {
        List<DiskIOData> allData = diskIoService.getAllData(1L);
        Assert.assertNotNull(allData);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoService_getAllDataTest_0002
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getAllDataTest02() {
        List<DiskIOData> allData = diskIoService.getAllData(1L);
        List<DiskIOData> list = diskIoService.getAllData(1L);
        Assert.assertEquals(allData, list);
    }

    /**
     * delete Session Data Test
     *
     * @tc.name: deleteSessionDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoService_deleteSessionDataTest_0001
     * @tc.desc: delete Session Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void deleteSessionDataTest() {
        DiskIoTable diskIoTable = new DiskIoTable();
        boolean res = diskIoService.deleteSessionData(1L);
        Assert.assertFalse(res);
    }
}