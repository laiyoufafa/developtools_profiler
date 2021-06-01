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

import java.util.HashMap;

/**
 * @Description DataTableHelperTest
 * @Date 2021/04/12 17:33
 **/
public class DataTableHelperTest {
    /**
     * functional testing getTableNameBySql
     *
     * @tc.name: getTableNameBySql
     * @tc.number: OHOS_JAVA_database_DataBaseHelper_getTableNameBySql_0001
     * @tc.desc: getTableNameBySql
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getTableNameBySqlTest() {
        String str = DataTableHelper.getTableNameBySql("(");
        Assert.assertNotNull(str);
    }

    /**
     * functional testing sqlPlaceholder
     *
     * @tc.name: sqlPlaceholder
     * @tc.number: OHOS_JAVA_database_DataTableHelper_sqlPlaceholder_0001
     * @tc.desc: sqlPlaceholder
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void sqlPlaceholderTest() {
        String sqlStr = DataTableHelper.sqlPlaceholder(10);
        Assert.assertNotNull(sqlStr);
    }

    /**
     * functional testing getDeleteCondition
     *
     * @tc.name: getDeleteCondition
     * @tc.number: OHOS_JAVA_database_DataTableHelper_getDeleteCondition_0001
     * @tc.desc: getDeleteCondition
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDeleteConditionTest() {
        String deleteStr = DataTableHelper.getDeleteCondition(new HashMap<>());
        Assert.assertNotNull(deleteStr);
    }

    /**
     * functional testing mapToString
     *
     * @tc.name: mapToString
     * @tc.number: OHOS_JAVA_database_DataTableHelper_mapToString_0001
     * @tc.desc: mapToString
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void mapToStringTest() {
        String mapStr = DataTableHelper.mapToString(new HashMap<>());
        Assert.assertNotNull(mapStr);
    }

}
