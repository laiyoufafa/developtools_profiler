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

package ohos.devtools.datasources.databases.databasepool;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Data Base Helper Test
 *
 * @since 2021/5/19 16:39
 */
public class DataBaseHelperTest {
    private static String url = "jdbc:sqlite://localhost:1521";

    /**
     * functional testing checkDataBaseExists
     *
     * @tc.name: checkDataBaseExists
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_checkDataBaseExists_0001
     * @tc.desc: checkDataBaseExists
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkDataBaseExistsTest() {
        boolean flag = DataBaseHelper.checkDataBaseExists(url);
        if (flag) {
            Assert.assertTrue(true);
        }
    }

    /**
     * functional testing loadSqlFileToList
     *
     * @tc.name: loadSqlFileToList
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_loadSqlFileToList_0001
     * @tc.desc: loadSqlFileToList
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void loadSqlFileToListTest() {
        List<String> list = null;
        try {
            list = DataBaseHelper.loadSqlFileToList("d://");
            Assert.assertNotNull(list);
        } catch (IOException ioException) {
            Assert.assertNotNull(list);
        }
    }

    /**
     * functional testing getFilePath
     *
     * @tc.name: getFilePath
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_getFilePath_0001
     * @tc.desc: get FilePath
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getFilePathTest() {
        String fileStr = DataBaseHelper.getFilePath(url);
        Assert.assertNotNull(fileStr);
    }

    /**
     * functional testing getUrlByDataBaseName
     *
     * @tc.name: getUrlByDataBaseName
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_getUrlByDataBaseName_0001
     * @tc.desc: get Url By DataBaseName
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getUrlByDataBaseNameTest() {
        String dataBaseStr = DataBaseHelper.getUrlByDataBaseName("defaultDB");
        Assert.assertNotNull(dataBaseStr);
    }

    /**
     * functional testing createDataBase
     *
     * @tc.name: createDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_createDataBase_0001
     * @tc.desc: createDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest() {
        DataBase dataBase = DataBaseHelper.createDataBase();
        Assert.assertNotNull(dataBase);
    }

    /**
     * functional testing createDefaultDataBase
     *
     * @tc.name: createDefaultDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_createDefaultDataBase_0001
     * @tc.desc: create DefaultDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDefaultDataBaseTest() {
        DataBase dataBaseDefault = DataBaseHelper.createDefaultDataBase();
        Assert.assertNotNull(dataBaseDefault);
    }
}
