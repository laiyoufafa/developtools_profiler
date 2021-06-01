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

import javax.swing.tree.TreeModel;

/**
 * TreeTableModel类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public interface TreeTableModel extends TreeModel {
    /**
     * Returns the number ofs available column.
     *
     * @return int
     */
    int getColumnCount();

    /**
     * getColumnName Returns the name for column number <code>column</code>.
     *
     * @param column column
     * @return String
     */
    String getColumnName(int column);

    /**
     * getColumnClass Returns the type for column number <code>column</code>.
     *
     * @param column column
     * @return Class
     */
    Class getColumnClass(int column);

    /**
     * Returns the value to be displayed for node <code>node</code>, at column number <code>column</code>.
     *
     * @param node   node
     * @param column column
     * @return Object
     */
    Object getValueAt(Object node, int column);
}
