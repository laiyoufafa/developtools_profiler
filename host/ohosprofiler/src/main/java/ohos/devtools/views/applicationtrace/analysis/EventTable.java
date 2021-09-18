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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.AllData;
import ohos.devtools.views.applicationtrace.DataPanel;
import ohos.devtools.views.applicationtrace.bean.EventBean;
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.applicationtrace.util.TimeUtils;

import javax.annotation.Nullable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * EventTable
 *
 * @date: 2021/5/24 12:22
 */
public class EventTable extends JBPanel {
    private final int RowHeight = 25;
    /**
     * event data source
     */
    public List<EventBean> dataSource = new ArrayList<>();
    private final int RowHeadHeight = 30;
    private List<Col<EventBean>> columnNames = new ArrayList<>();
    private JBScrollPane jScrollPane;
    private EventTableModel tableColumnModel;
    private JBTable jbTable;
    private ITableSizeChangeListener listener;

    /**
     * Constructor
     *
     * @param listener listener
     */
    public EventTable(ITableSizeChangeListener listener) {
        columnNames.add(new Col<>("Start Time", bean -> TimeUtils.getTimeFormatString(bean.getStartTime())));
        columnNames.add(new Col<>("Name", EventBean::getName));
        columnNames.add(new Col<>("Wall Duration", bean -> TimeUtils.getTimeWithUnit(bean.getWallDuration())));
        columnNames.add(new Col<>("Self Time", bean -> TimeUtils.getTimeWithUnit(bean.getSelfTime())));
        columnNames.add(new Col<>("Cpu Duration", bean -> TimeUtils.getTimeWithUnit(bean.getCpuDuration())));
        columnNames.add(new Col<>("Cpu Self Time", bean -> TimeUtils.getTimeWithUnit(bean.getCpuSelfTime())));
        this.listener = listener;
        setLayout(new MigLayout("insets 0", "[grow,fill]", "[grow,fill]"));
        tableColumnModel = new EventTableModel();
        jbTable = new JBTable(tableColumnModel);
        jbTable.setShowGrid(true);
        JTableHeader tableHeader = jbTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), RowHeadHeight));
        tableHeader.setBackground(JBColor.background());
        jbTable.setRowHeight(RowHeight);
        jbTable.setShowGrid(false);
        jbTable.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        jbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jbTable.setBackground(JBColor.background().darker());
        initColumns();
        jScrollPane = new JBScrollPane(jbTable, javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        add(jScrollPane);
    }

    private void initColumns() {
        TableRowSorter<EventTableModel> rowSorter = new TableRowSorter<>(tableColumnModel);
        rowSorter.setComparator(2, getComparator());
        rowSorter.setComparator(3, getComparator());
        rowSorter.setComparator(4, getComparator());
        rowSorter.setComparator(5, getComparator());
        jbTable.setRowSorter(rowSorter);
    }

    private Comparator<String> getComparator() {
        Comparator<String> comparator = (left, right) -> {
            double leftTime = stringToTime(left);
            double rightTime = stringToTime(right);
            return Double.compare(leftTime, rightTime);
        };
        return comparator;
    }

    private double stringToTime(String str) {
        if (str.contains("μs")) {
            return Double.parseDouble(str.replace("μs", "")) * 1000;
        } else if (str.contains("ms")) {
            return Double.parseDouble(str.replace("ms", "")) * 1000000;
        } else if (str.contains("s")) {
            return Double.parseDouble(str.replace("s", "")) * 1000000000;
        } else if (str.contains("m")) {
            return Double.parseDouble(str.replace("m", "")) * 60000000000L;
        } else {
            return 0D;
        }
    }

    private void getAppData(long startNS, long endNS, List<Integer> threadIds) {
        List<Func> funcList = new ArrayList<>();
        threadIds.forEach(threadId -> {
            if (AllData.funcMap.containsKey(threadId)) {
                funcList.addAll(AllData.funcMap.get(threadId).stream().filter(func -> func.getDepth() != -1 && TimeUtils
                    .isRangeCross(startNS, endNS, func.getStartTs(), func.getEndTs())).collect(Collectors.toList()));
            }
        });
        Map<Long, List<Func>> collect = funcList.stream().collect(Collectors.groupingBy(Func::getParentStackId));
        dataSource = funcList.stream().sorted(Comparator.comparingLong(Func::getDur).reversed()).limit(10).map(func -> {
            EventBean eventBean = new EventBean();
            long selfTime;
            long cupSelfTime;
            long duration = func.getDur();
            long cpuDuration = func.getRunning();
            if (collect.containsKey(func.getStackId())) {
                selfTime = func.getDur() - collect.get(func.getStackId()).stream().filter(func1 -> TimeUtils
                    .isRangeCross(func.getStartTs(), func.getEndTs(), func1.getStartTs(), func1.getEndTs()))
                    .mapToLong(Func::getDur).sum();
                cupSelfTime = cpuDuration - collect.get(func.getStackId()).stream().filter(func1 -> TimeUtils
                    .isRangeCross(func.getStartTs(), func.getEndTs(), func1.getStartTs(), func1.getEndTs()))
                    .mapToLong(Func::getRunning).sum();
            } else {
                cupSelfTime = func.getRunning();
                selfTime = func.getDur();
            }
            eventBean.setWallDuration(duration);
            eventBean.setSelfTime(selfTime);
            eventBean.setStartTime(func.getStartTs());
            eventBean.setCpuSelfTime(cupSelfTime);
            eventBean.setCpuDuration(cpuDuration);
            eventBean.setName(func.getFuncName());
            return eventBean;
        }).collect(Collectors.toList());
    }

    /**
     * getData
     *
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     */
    public void getData(long startNS, long endNS, List<Integer> threadIds) {
        if (DataPanel.analysisEnum.equals(AnalysisEnum.APP)) {
            getAppData(startNS, endNS, threadIds);
        }
        if (listener != null) {
            if (dataSource.size() == 10) {
                listener.onTableSizeChange("Longest runing events top (10)");
            } else if (dataSource.size() == 0) {
                listener.onTableSizeChange(null);
            } else {
                listener.onTableSizeChange("Longest runing events (" + dataSource.size() + ")");
            }
        }
        tableColumnModel.fireTableDataChanged();
        jScrollPane
            .setPreferredSize(new Dimension(jScrollPane.getWidth(), dataSource.size() * RowHeight + RowHeadHeight));
    }

    /**
     * freshData
     */
    public void freshData() {
        tableColumnModel.fireTableDataChanged();
        jScrollPane
            .setPreferredSize(new Dimension(jScrollPane.getWidth(), dataSource.size() * RowHeight + RowHeadHeight));
    }

    /**
     * table size change listener
     */
    public interface ITableSizeChangeListener {

        /**
         * Table Size Change Callback
         *
         * @param title table title
         */
        void onTableSizeChange(@Nullable String title);
    }

    /**
     * Col Class
     *
     * @param <T> T type
     */
    public static class Col<T> {
        /**
         * name
         */
        public String name;

        /**
         * callable
         */
        public Function<T, Object> callable;

        /**
         * Constructor
         *
         * @param name name
         * @param callable callable
         */
        public Col(String name, Function<T, Object> callable) {
            this.name = name;
            this.callable = callable;
        }
    }

    /**
     * EventTableModel
     *
     * @date: 2021/5/24 12:22
     */
    public class EventTableModel extends AbstractTableModel {
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
            return columnNames.get(columnIndex).callable.apply(dataSource.get(rowIndex));
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return super.getColumnClass(columnIndex);
        }
    }
}
