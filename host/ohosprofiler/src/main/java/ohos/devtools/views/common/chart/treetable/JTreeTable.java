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

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static ohos.devtools.views.common.LayoutConstants.NEGATIVE_ONE;

/**
 * JTreeTable
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class JTreeTable extends JTable {
    private static final Logger LOGGER = LogManager.getLogger(JTreeTable.class);

    /**
     * 数字220
     */
    public static final int NUM_220 = 220;

    /**
     * TreeTableCellRenderer对象
     */
    public TreeTableCellRenderer treeTableCellRenderer;

    /**
     * TreeTableModel类
     */
    public TreeTableModel treeTableModel;

    /**
     * getTreeTableModel
     *
     * @return TreeTableModel
     */
    public TreeTableModel getTreeTableModel() {
        return treeTableModel;
    }

    /**
     * setTreeTableModel
     *
     * @param treeTableModel treeTableModel
     */
    public void setTreeTableModel(TreeTableModel treeTableModel) {
        this.treeTableModel = treeTableModel;
        if (treeTableCellRenderer == null) {
            treeTableCellRenderer = new TreeTableCellRenderer(treeTableModel);
        } else {
            treeTableCellRenderer.setModel(treeTableModel);
            treeTableCellRenderer.setShowsRootHandles(true);
        }
        TreeTableModelAdapter model = new TreeTableModelAdapter(treeTableModel, treeTableCellRenderer);
        super.setModel(model);
        treeTableCellRenderer.setRowHeight(getRowHeight());
        setDefaultRenderer(TreeTableModel.class, treeTableCellRenderer);
        TableRowSorter<TableModel> sorter = new TableRowSorter(this.getModel());
        sorter.setComparator(0, DataNodeCompares.classNameString);
        sorter.addRowSorterListener(event -> {
            try {
                if (!sorter.getSortKeys().isEmpty()) {
                    List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
                    AgentDataModel treeTableModels = null;
                    TreeTableModel treeTModel = JTreeTable.this.treeTableModel;
                    if (treeTModel instanceof AgentDataModel) {
                        treeTableModels = (AgentDataModel) treeTModel;
                    }
                    DataNode rootNode = treeTableModels.getDataNode();
                    ArrayList<DataNode> datNode = rootNode.getChildren();
                    RowSorter.SortKey key = keys.get(0);
                    String sortOrder = key.getSortOrder().name();
                    Comparator comparator = new DataNodeCompares().chooseCompare(key.getColumn(), sortOrder);
                    if (comparator != null) {
                        Collections.sort(datNode, comparator);
                    }
                    rootNode.setChildren(datNode);
                    AgentDataModel agentDataModel = new AgentDataModel(rootNode);
                    JTreeTable.this.treeTableModel = agentDataModel;
                    treeTableCellRenderer = new TreeTableCellRenderer(agentDataModel);
                    treeTableCellRenderer.setRowHeight(getRowHeight());
                    setDefaultRenderer(TreeTableModel.class, treeTableCellRenderer);
                }
            } catch (IndexOutOfBoundsException | ClassCastException | UnsupportedOperationException
                    | IllegalArgumentException exception) {
                LOGGER.error("reflush Exception {}", exception.getMessage());
            }
        });
        this.setRowSorter(sorter);
    }

    /**
     * JTreeTable
     *
     * @param treeTableModel treeTableModel
     */
    public JTreeTable(TreeTableModel treeTableModel) {
        super();
        this.treeTableModel = treeTableModel;
        treeTableCellRenderer = new TreeTableCellRenderer(treeTableModel);
        super.setModel(new TreeTableModelAdapter(treeTableModel, treeTableCellRenderer));
        treeTableCellRenderer.setSelectionModel(new DefaultTreeSelectionModel() {
            {
                setSelectionModel(listSelectionModel);
            }
        });
        treeTableCellRenderer.setRowHeight(getRowHeight());
        setDefaultRenderer(TreeTableModel.class, treeTableCellRenderer);
        setShowGrid(false);
        getColumnModel().getColumn(0).setPreferredWidth(NUM_220);
        setIntercellSpacing(new Dimension(0, 0));
    }

    /**
     * TreeTableCellRenderer
     */
    public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
        /**
         * visibleRow
         */
        protected int visibleRow;

        /**
         * TreeTableCellRenderer
         *
         * @param model model
         */
        public TreeTableCellRenderer(TreeModel model) {
            super(model);
            this.setShowsRootHandles(true);
        }

        /**
         * setBounds
         *
         * @param pointX pointX
         * @param pointY pointY
         * @param width  width
         * @param height height
         */
        @Override
        public void setBounds(int pointX, int pointY, int width, int height) {
            super.setBounds(pointX, 0, width, JTreeTable.this.getHeight());
        }

        /**
         * paint
         *
         * @param graphics graphics
         */
        @Override
        public void paint(Graphics graphics) {
            graphics.translate(0, -visibleRow * getRowHeight());
            super.paint(graphics);
        }

        /**
         * 获取表单元格渲染器组件
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
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            visibleRow = row;
            return this;
        }

        /**
         * getNextMatch
         *
         * @param prefix      prefix
         * @param startingRow startingRow
         * @param bias        bias
         * @return TreePath
         */
        @Override
        public synchronized TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
            String newPrefix = prefix;
            int max = getRowCount();
            int row = 0;
            if (newPrefix == null) {
                return null;
            }
            if (startingRow < 0 || (startingRow + 1) == getRowCount()) {
                row = 0;
            } else {
                row = startingRow + 1;
            }

            if (startingRow >= max) {
                return null;
            }
            newPrefix = newPrefix.toUpperCase(Locale.ENGLISH);
            TreePath andExpand = null;
            if (startingRow < 0) {
                andExpand = findAndExpand(newPrefix, row, bias, NEGATIVE_ONE);
            } else {
                andExpand = findAndExpand(newPrefix, row, bias, getSelectedRow());
            }
            if (andExpand == null && startingRow >= 0) {
                TreePath path = getPathForRow(startingRow);
                String text =
                    convertRowToText(path.getLastPathComponent(), isRowSelected(startingRow), isExpanded(startingRow),
                        true, startingRow, false);
                if (text.toUpperCase(Locale.ENGLISH).indexOf(newPrefix) > NEGATIVE_ONE) {
                    return path;
                } else {
                    return null;
                }
            } else {
                return andExpand;
            }
        }

        /**
         * 查找并扩展
         *
         * @param prefix             prefix
         * @param row                row
         * @param bias               bias
         * @param currentSelectedRow currentSelectedRow
         * @return TreePath
         */
        public TreePath findAndExpand(String prefix, int row, Position.Bias bias, int currentSelectedRow) {
            TreePath treePath = getPathForRow(row);
            String text =
                convertRowToText(treePath
                    .getLastPathComponent(), isRowSelected(row), isExpanded(row), true, row, false);
            if (text.toUpperCase(Locale.ENGLISH).indexOf(prefix) > NEGATIVE_ONE) {
                return treePath;
            } else {
                DataNode node = null;
                Object objComponent = treePath.getLastPathComponent();
                if (objComponent instanceof DataNode) {
                    node = (DataNode) objComponent;
                }
                if (node != null && node.getChildren() != null && node.getChildren().size() > 0 && !isExpanded(row)) {
                    // expand current row
                    expandRow(row);
                    TreePath pathTem = findAndExpand(prefix, row + 1, bias, currentSelectedRow);
                    if (pathTem == null) {
                        collapseRow(row);
                    } else {
                        if (!isSubPath(treePath, pathTem)) {
                            collapseRow(row);
                        }
                    }
                    return pathTem;
                } else {
                    if (getRowCount() == row + 1) {
                        if (currentSelectedRow <= 0) {
                            return null;
                        } else {
                            return findAndExpand(prefix, 0, bias, currentSelectedRow);
                        }
                    }
                    if (currentSelectedRow == row + 1) {
                        return null;
                    }
                    return findAndExpand(prefix, row + 1, bias, currentSelectedRow);
                }
            }
        }

        /**
         * isSubPath
         *
         * @param parentPath parentPath
         * @param childPath  childPath
         * @return boolean
         */
        public boolean isSubPath(TreePath parentPath, TreePath childPath) {
            TreePath parent = childPath;
            if (parentPath.getPathCount() >= childPath.getPathCount()) {
                return false;
            }
            for (int index = 0; index < childPath.getPathCount() - parentPath.getPathCount(); index++) {
                parent = childPath.getParentPath();
            }
            if (parent == parentPath) {
                return true;
            }
            return false;
        }

        /**
         * convertValueToText
         *
         * @param value    value
         * @param selected selected
         * @param expanded expanded
         * @param leaf     leaf
         * @param row      row
         * @param hasFocus hasFocus
         * @return String
         */
        @Override
        public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
            if (value != null && value instanceof DataNode) {
                String strValue = ((DataNode) value).toString();
                if (strValue != null) {
                    return strValue;
                }
            }
            return "";
        }

        /**
         * convertRowToText
         *
         * @param value    value
         * @param selected selected
         * @param expanded expanded
         * @param leaf     leaf
         * @param row      row
         * @param hasFocus hasFocus
         * @return String
         */
        public String convertRowToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
            if (value != null && value instanceof DataNode) {
                String strValue = ((DataNode) value).toStr();
                if (strValue != null) {
                    return strValue;
                }
            }
            return "";
        }
    }
}
