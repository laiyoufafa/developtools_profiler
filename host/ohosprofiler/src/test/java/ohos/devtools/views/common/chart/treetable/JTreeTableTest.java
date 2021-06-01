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

import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;
import java.util.Locale;

/**
 * @Description JTreeTable test
 * @Date 2021/4/5 13:15
 **/
public class JTreeTableTest {
    /**
     * JTreeTable
     */
    private JTreeTable jTreeTable;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_JTreeTable_init_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Before
    public void init() {
        jTreeTable = new JTreeTable(new AgentDataModel(new DataNode()));
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_JTreeTable_getTreeTableModel_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void getTreeTableModel() {
        TreeTableModel treeTableModel = jTreeTable.getTreeTableModel();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_JTreeTable_setTreeTableModel_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void setTreeTableModel() {
        TreeTableModel treeTableModel = jTreeTable.getTreeTableModel();
        jTreeTable.setTreeTableModel(treeTableModel);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_JTreeTable_convertRowToText_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void convertRowToText() {
        Locale defaultLocale = JTreeTable.TreeTableCellRenderer.getDefaultLocale();
        JTreeTable.TreeTableCellRenderer.setDefaultLocale(defaultLocale);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_JTreeTable_setBounds_0001
     * @tc.desc: chart of Memory functional test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void setBounds() {
        boolean lightweightComponent = JTreeTable.TreeTableCellRenderer.isLightweightComponent(new JPanel());
    }
}