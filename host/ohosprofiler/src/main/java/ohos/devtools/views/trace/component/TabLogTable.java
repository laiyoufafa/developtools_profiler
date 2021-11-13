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

package ohos.devtools.views.trace.component;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.JViewport;

import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.bean.TraceLog;
import ohos.devtools.views.trace.util.Db;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * TabLogTable
 *
 * @since 2021/04/20 12:12
 */
public class TabLogTable extends JBPanel {
    private float[] columnWidthPercentage = {0.15f, 0.05f, 0.2f, 0.6f};
    private TableModel model = new TableModel();
    private JBTable table = new JBTable(model);
    private JBLabel label = new JBLabel();
    private int count = -1;
    private int pageSize = 20;
    private int page = 1;
    private boolean loading = false;
    private boolean complete = false;
    private long endTime;
    private long startTime;

    /**
     * TabLogTable
     */
    public TabLogTable() {
        setLayout(new MigLayout("inset 8"));
        JBScrollPane scrollPane = new JBScrollPane(table);
        table.setShowGrid(false);
        table.setRowHeight(40);
        add(label, "pushx,growx,wrap");
        add(scrollPane, "push,grow");
        resizeColumns();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                resizeColumns();
            }
        });
        JViewport viewport = scrollPane.getViewport();
        scrollPane.getViewport().addChangeListener(event -> {
            Rectangle viewRect = viewport.getViewRect();
            int first = table.rowAtPoint(new Point(0, viewRect.y));
            if (first == -1) {
                return; // Table is empty
            }
            int last = table.rowAtPoint(new Point(0, viewRect.y + viewRect.height - 1));
            if (last == -1) {
                last = model.getRowCount() - 1; // Handle empty space below last row
            }
            label.setText("Logs rows [" + first + "," + last + "] / " + count + "");
            if (last + 1 == page * pageSize) {
                page++;
                if (!loading && !complete) {
                    query(page, pageSize);
                }
            }
        });
    }

    private void resizeColumns() {
        int tableWidth = table.getWidth();
        TableColumn column;
        TableColumnModel jTableColumnModel = table.getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++) {
            column = jTableColumnModel.getColumn(i);
            int pWidth = Math.round(columnWidthPercentage[i] * tableWidth);
            column.setPreferredWidth(pWidth);
        }
    }

    /**
     * query log
     *
     * @param clean clean
     */
    public void query(boolean clean) {
        loading = true;
        page = 1;
        pageSize = 20;
        List<TraceLog> traceLogs = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.SYS_QUERY_LOGS, traceLogs, startTime, endTime, pageSize, (page - 1) * pageSize);
        if (clean) {
            model.dataSource.clear();
        }
        model.dataSource.addAll(traceLogs);
        model.fireTableDataChanged();
        count = Db.getInstance().queryCount(Sql.SYS_QUERY_LOGS_COUNT, startTime, endTime);
        label.setText("Logs rows [0,20] / " + count + "");
        loading = false;
    }

    private void query(Integer page, Integer pageSize) {
        loading = true;
        this.page = page;
        this.pageSize = pageSize;
        List<TraceLog> traceLogs = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.SYS_QUERY_LOGS, traceLogs, startTime, endTime, pageSize, (page - 1) * pageSize);
        if (traceLogs.size() < pageSize) {
            complete = true;
        }
        model.dataSource.addAll(traceLogs);
        model.fireTableDataChanged();
        loading = false;
    }

    /**
     * time range change
     *
     * @param sn sn
     * @param en en
     */
    public void rangeChange(long sn, long en) {
        this.startTime = sn;
        this.endTime = en;
        query(true);
    }

    private class TableModel extends AbstractTableModel {
        /**
         * dataSource list
         */
        public final List<TraceLog> dataSource = new ArrayList<>();
        private List<Column> columnNames = new ArrayList<>();

        /**
         * TableModel
         */
        public TableModel() {
            columnNames.add(new Column("time", (item) -> item.getStartTime()));
            columnNames.add(new Column("level", (item) -> item.getLevel()));
            columnNames.add(new Column("tag", (item) -> item.getTag()));
            columnNames.add(new Column("context", (item) -> item.getContext()));
        }

        @Override
        public int getRowCount() {
            return dataSource.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames.get(column).name;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnNames.get(columnIndex).callable.map(dataSource.get(rowIndex));
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == columnNames.size() - 1) {
                return true;
            } else {
                return false;
            }
        }
    }

    private class Column {

        /**
         * column name
         */
        private String name;
        private Process callable;

        /**
         * Column
         *
         * @param name name
         * @param callable callable
         */
        public Column(String name, Process callable) {
            this.name = name;
            this.callable = callable;
        }
    }

    /**
     * Process
     */
    private interface Process {
        /**
         * map process
         *
         * @param traceLog traceLog
         * @return Object
         */
        Object map(TraceLog traceLog);
    }

}
