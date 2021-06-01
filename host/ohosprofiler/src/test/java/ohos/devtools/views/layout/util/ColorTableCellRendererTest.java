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

package ohos.devtools.views.layout.util;

import com.intellij.ui.table.JBTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.util.Vector;

/**
 * @Description FileUtil test
 * @Date 2021/4/2 11:25
 **/
public class ColorTableCellRendererTest {
    private ColorTableCellRenderer colorTableCellRenderer;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ColorTableCellRenderer_getColorTableCellRenderer_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void getColorTableCellRenderer() {
        colorTableCellRenderer = new ColorTableCellRenderer();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ColorTableCellRenderer_getTableCellRendererComponent_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getTableCellRendererComponent() {
        JBTable jbTable = new JBTable();
        Vector vector = new Vector(3);
        vector.add(0, "周杰伦");
        vector.add(1, "蔡依林");
        vector.add(2, "费玉清");
        Object obj = jbTable.getModel();
        DefaultTableModel tableModel = null;
        String[] columnNames = {"列名1", "列名2", "列名3"};
        if (obj instanceof DefaultTableModel) {
            tableModel = (DefaultTableModel)obj;
            tableModel.setColumnIdentifiers(columnNames);
            tableModel.addRow(vector);
            tableModel.setValueAt("Edfs", 0, 0);
            tableModel.setValueAt("Dewr", 0, 1);
            tableModel.setValueAt("Iewer", 0, 2);
            jbTable.setModel(tableModel);
        }
        String str = "sjkdlf";
        Component tableCellRendererComponent =
            colorTableCellRenderer.getTableCellRendererComponent(jbTable, str, true, true, 0, 0);
        Component tableCellRendererComponent1 =
            colorTableCellRenderer.getTableCellRendererComponent(jbTable, str, true, true, 0, 1);
        Component tableCellRendererComponent2 =
            colorTableCellRenderer.getTableCellRendererComponent(jbTable, str, true, true, 0, 2);
        Assert.assertNotNull(tableCellRendererComponent);
        Assert.assertNotNull(tableCellRendererComponent1);
        Assert.assertNotNull(tableCellRendererComponent2);
    }

}