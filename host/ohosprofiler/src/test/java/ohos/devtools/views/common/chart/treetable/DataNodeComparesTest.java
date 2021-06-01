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

package ohos.devtools.views.common.chart.treetable;

import ohos.devtools.views.common.LayoutConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * DataNodeCompares sorting test
 *
 * @Description DataNodeCompares sorting test
 * @Date 2021/4/5 13:15
 **/
public class DataNodeComparesTest {
    private DataNodeCompares dataNodeCompares;
    private Comparator comparator;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_init_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Before
    public void init() {
        dataNodeCompares = new DataNodeCompares();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_init_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @After
    public void after() {
        DataNode dataNode1 = new DataNode();
        DataNode dataNode2 = new DataNode();
        DataNode dataNode3 = new DataNode();
        DataNode dataNode4 = new DataNode();
        dataNode1.addChildren(dataNode2);
        dataNode3.addChildren(dataNode4);
        dataNode1.addChildren(dataNode3);
        ArrayList<DataNode> dataNodes = new ArrayList<>();
        dataNodes.add(dataNode1);
        Collections.sort(dataNodes, comparator);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare01() {
        comparator = dataNodeCompares.chooseCompare(0, LayoutConstants.ASC);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0002
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare02() {
        comparator = dataNodeCompares.chooseCompare(1, LayoutConstants.ASC);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0003
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare03() {
        comparator = dataNodeCompares.chooseCompare(2, LayoutConstants.ASC);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0004
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare04() {
        comparator = dataNodeCompares.chooseCompare(3, LayoutConstants.ASC);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0005
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare05() {
        comparator = dataNodeCompares.chooseCompare(4, LayoutConstants.ASC);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0006
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare06() {
        comparator = dataNodeCompares.chooseCompare(0, "test");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0007
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare07() {
        comparator = dataNodeCompares.chooseCompare(1, "test");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0008
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare08() {
        comparator = dataNodeCompares.chooseCompare(2, "test");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0009
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare09() {
        comparator = dataNodeCompares.chooseCompare(3, "test");
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DataNodeCompares_chooseCompare_0010
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void chooseCompare10() {
        comparator = dataNodeCompares.chooseCompare(4, "test");
    }
}