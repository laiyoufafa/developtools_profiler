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

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * ColorTableCellRenderer
 *
 * @version 1.0
 * @date 2021/3/4 10:55
 **/
public class ColorTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * getTableCellRendererComponent
     *
     * @param table      table
     * @param value      value
     * @param isSelected isSelected
     * @param hasFocus   hasFocus
     * @param row        row
     * @param column     column
     * @return Component
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Object selectedValue = table.getModel().getValueAt(row, column);
        if ("E".equals(selectedValue.toString())) {
            comp.setForeground(Color.RED);
        } else if ("D".equals(selectedValue.toString())) {
            comp.setForeground(Color.blue);
        } else if ("I".equals(selectedValue.toString())) {
            comp.setForeground(Color.GREEN);
        } else if ("V".equals(selectedValue.toString())) {
            comp.setForeground(Color.WHITE);
        } else if ("W".equals(selectedValue.toString())) {
            comp.setForeground(Color.orange);
        } else {
            return comp;
        }
        return comp;
    }
}
