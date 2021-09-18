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

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassInfo Dao Test
 */
public class ClassInfoDaoTest {
    private ClassInfoDao classInfoDao;
    private ClassInfo classInfo;

    /**
     * functional testing init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Before
    public void init() {
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        classInfoDao = ClassInfoDao.getInstance();
        classInfoDao.createClassInfo();
        classInfo = new ClassInfo();
        classInfo.setClassName("className");
        classInfo.setcId(1);
        classInfo.setId(1);
        classInfoDao.insertClassInfo(classInfo);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testGetInstance01() {
        ClassInfoDao instance = ClassInfoDao.getInstance();
        Assert.assertNotNull(instance);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_getInstance_0002
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testGetInstance02() {
        ClassInfoDao instance = ClassInfoDao.getInstance();
        ClassInfoDao classInfoDaoInstance = ClassInfoDao.getInstance();
        Assert.assertEquals(instance, classInfoDaoInstance);
    }

    /**
     * functional testing insertClassInfo
     *
     * @tc.name: insertClassInfo
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_insertClassInfo_0001
     * @tc.desc: insertClassInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testinsertClassInfo() {
        classInfoDao.insertClassInfo(classInfo);
    }

    /**
     * functional testing insertClassInfos
     *
     * @tc.name: insertClassInfos
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_insertClassInfos_0001
     * @tc.desc: insertClassInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testinsertClassInfos() {
        List<ClassInfo> list = new ArrayList<>();
        list.add(classInfo);
        classInfoDao.insertClassInfos(list);
    }

    /**
     * functional testing getAllClassInfoData
     *
     * @tc.name: getAllClassInfoData
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_getAllClassInfoData_0001
     * @tc.desc: getAllClassInfoData
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetAllClassInfoData01() {
        List<ClassInfo> list = new ArrayList<>();
        list = classInfoDao.getAllClassInfoData(1L);
        Assert.assertNotNull(list);
    }

    /**
     * functional testing getAllClassInfoData
     *
     * @tc.name: getAllClassInfoData
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_getAllClassInfoData_0002
     * @tc.desc: getAllClassInfoData
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetAllClassInfoData02() {
        List<ClassInfo> list = classInfoDao.getAllClassInfoData(1L);
        List<ClassInfo> classInfoList = classInfoDao.getAllClassInfoData(10L);
        Assert.assertNotEquals(list, classInfoList);
    }

    /**
     * functional testing getClassIdByClassName
     *
     * @tc.name: getClassIdByClassName
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_getClassIdByClassName_0001
     * @tc.desc: getClassIdByClassName
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetClassIdByClassName01() {
        int num = classInfoDao.getClassIdByClassName("className");
        Assert.assertNotNull(num);
    }

    /**
     * functional testing getClassIdByClassName
     *
     * @tc.name: getClassIdByClassName
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_getClassIdByClassName_0002
     * @tc.desc: getClassIdByClassName
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetClassIdByClassName02() {
        int num = classInfoDao.getClassIdByClassName("className");
        int className = classInfoDao.getClassIdByClassName("className");
        Assert.assertEquals(num, className);
    }

    /**
     * functional testing deleteSessionData
     *
     * @tc.name: deleteSessionData
     * @tc.number: OHOS_JAVA_memory_ClassInfoDao_deleteSessionData_0001
     * @tc.desc: deleteSessionData
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetdeleteSessionData() {
        classInfoDao.deleteSessionData(1L);
    }
}
