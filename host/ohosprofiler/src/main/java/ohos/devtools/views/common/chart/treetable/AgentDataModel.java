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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import static ohos.devtools.views.common.LayoutConstants.NEGATIVE_ONE;
import static ohos.devtools.views.common.LayoutConstants.NUM_0;
import static ohos.devtools.views.common.LayoutConstants.NUM_1;
import static ohos.devtools.views.common.LayoutConstants.NUM_2;
import static ohos.devtools.views.common.LayoutConstants.NUM_3;
import static ohos.devtools.views.common.LayoutConstants.NUM_4;

/**
 * agentDataModel class.
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class AgentDataModel implements TreeTableModel {
    private static final Logger LOGGER = LogManager.getLogger(AgentDataModel.class);

    /**
     * Names of the columns.
     */
    protected static String[] cNames = {"Class Name", "Allocations", "Deallocations", "Total Count", "Shallow Size"};

    /**
     * cTypes Types of the columns.
     */
    protected static Class[] cTypes = {TreeTableModel.class, Integer.class, Integer.class, Integer.class, Long.class};

    /**
     * listenerList
     */
    protected EventListenerList listenerList = new EventListenerList();

    private DataNode dataNode;

    /**
     * FileSystemModel
     *
     * @param dataNode dataNode
     */
    public AgentDataModel(DataNode dataNode) {
        this.dataNode = dataNode;
    }

    /**
     * getDataNode
     *
     * @return DataNode
     */
    public DataNode getDataNode() {
        return dataNode;
    }

    /**
     * getClassObject
     *
     * @param node node
     * @return DataNode
     */
    protected DataNode getClassObject(Object node) {
        return (DataNode) node;
    }

    /**
     * getChildren
     *
     * @param node node
     * @return Object[]
     */
    protected Object[] getChildren(Object node) {
        DataNode digitalNode = null;
        if (node instanceof DataNode) {
            digitalNode = (DataNode) node;
        }
        if (digitalNode == null) {
            return new DataNode[0];
        }
        if (digitalNode.getChildren() != null) {
            return digitalNode.getChildren().toArray();
        } else {
            return new DataNode[0];
        }
    }

    /**
     * 获取子节点数量
     *
     * @param node node
     * @return int
     */
    public int getChildCount(Object node) {
        Object[] children = getChildren(node);
        return (children == null) ? 0 : children.length;
    }

    /**
     * getRootNode
     *
     * @return dataNode dataNode
     */
    @Override
    public Object getRoot() {
        return dataNode;
    }

    /**
     * 获取子节点
     *
     * @param node   node
     * @param number number
     * @return Object
     */
    public Object getChild(Object node, int number) {
        return getChildren(node)[number];
    }

    /**
     * isLeaf
     *
     * @param node node
     * @return boolean
     */
    public boolean isLeaf(Object node) {
        return getChildCount(node) <= 0;
    }

    /**
     * valueForPathChanged Not implemented
     *
     * @param path path
     * @param newValue newValue
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    /**
     * 获取子节点序列
     *
     * @param parent parent
     * @param child  child
     * @return int
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        for (int index = 0; index < getChildCount(parent); index++) {
            if (getChild(parent, index).equals(child)) {
                return index;
            }
        }
        return NEGATIVE_ONE;
    }

    /**
     * addTreeModelListener
     *
     * @param listener listener
     */
    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listenerList.add(TreeModelListener.class, listener);
    }

    /**
     * removeTreeModelListener
     *
     * @param listener listener
     */
    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listenerList.remove(TreeModelListener.class, listener);
    }

    /**
     * getColumnCount
     *
     * @return int
     */
    public int getColumnCount() {
        return cNames.length;
    }

    /**
     * 获取列名
     *
     * @param column column
     * @return String
     */
    public String getColumnName(int column) {
        return cNames[column];
    }

    /**
     * getColumnClass
     *
     * @param column column
     * @return Class
     */
    public Class getColumnClass(int column) {
        return cTypes[column];
    }

    /**
     * 获取数据
     *
     * @param node   node
     * @param column column
     * @return Object
     */
    public Object getValueAt(Object node, int column) {
        Object object = null;
        DataNode digitalNode = getClassObject(node);
        try {
            switch (column) {
                case NUM_0:
                    object = digitalNode.getClassName();
                    break;
                case NUM_1:
                    object = digitalNode.getAllocations();
                    break;
                case NUM_2:
                    object = digitalNode.getDeallocations();
                    break;
                case NUM_3:
                    object = digitalNode.getTotalCount();
                    break;
                case NUM_4:
                    object = digitalNode.getShallowSize();
                    break;
                default:
                    break;
            }
        } catch (SecurityException exception) {
            LOGGER.error("SecurityException error: {}", exception.getMessage());
        }
        return object;
    }
}
