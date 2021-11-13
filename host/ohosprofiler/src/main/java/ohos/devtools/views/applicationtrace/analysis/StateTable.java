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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.AllData;
import ohos.devtools.views.applicationtrace.bean.Thread;
import ohos.devtools.views.applicationtrace.bean.ThreadStateBean;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.EventPanel;
import ohos.devtools.views.trace.TracePanel;
import ohos.devtools.views.trace.util.ComparatorUtils;
import org.apache.commons.collections.map.HashedMap;

import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StateTable
 *
 */
public class StateTable extends EventPanel {
    private static final Map<String, String> STATUS_MAP = new HashedMap() {{
        put("D", "Waiting");
        put("S", "Sleeping");
        put("R", "Runnable");
        put("Running", "Running");
        put("R+", "Runnable");
        put("DK", "Waiting");
        put("W", "Runnable");
        put("X", "Dead");
        put("Z", "Exit (Zombie)");
        put("K", "Wake Kill");
        put("P", "Parked");
        put("N", "No Load");
    }};

    /**
     * table data source
     */
    public List<ThreadStateBean> dataSource = new ArrayList<>();

    private final int RowHeight = 25;
    private final int RowHeadHeight = 30;
    private JBScrollPane jScrollPane;
    private StateTableModel tableColumnModel;
    private JBTable jbTable;
    private TableRowSorter<StateTableModel> rowSorter;
    private List<EventTable.Col<ThreadStateBean>> columnNames = new ArrayList<>() {{
        add(new EventTable.Col<>("Thread State", ThreadStateBean::getState));
        add(new EventTable.Col<>("Duration", ThreadStateBean::getDuration));
        add(new EventTable.Col<>("%", ThreadStateBean::getPercent));
        add(new EventTable.Col<>("Occurrences", ThreadStateBean::getOccurrences));
    }};

    /**
     * structure function
     */
    public StateTable() {
        setLayout(new MigLayout("insets 0", "[grow,fill]", "[grow,fill]"));
        tableColumnModel = new StateTableModel();
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
        rowSorter = new TableRowSorter<>(tableColumnModel);
        for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
            if (i == 1) {
                // set duration sorter
                rowSorter.setComparator(i, (str1, str2) -> {
                    // change duration String to time ns
                    long time1 = TimeUtils.getNSByTimeString(String.valueOf(str1));
                    long time2 = TimeUtils.getNSByTimeString(String.valueOf(str2));
                    if (time1 < time2) {
                        return -1;
                    } else if (time1 == time2) {
                        return 0;
                    } else {
                        return 1;
                    }
                });
            } else {
                rowSorter.setComparator(i, ComparatorUtils.generateComparator(""));
            }
        }
        jbTable.setRowSorter(rowSorter); // add row sorter
        jScrollPane = new JBScrollPane(jbTable, javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        add(jScrollPane);
    }

    private void getAppData(long startNS, long endNS, List<Integer> threadIds) {
        List<Thread> threads = new ArrayList<>();
        dataSource.clear();
        threadIds.forEach(threadId -> {
            if (AllData.threadMap.containsKey(threadId)) {
                threads.addAll(AllData.threadMap.get(threadId).stream().filter(thread -> TimeUtils
                        .isRangeCross(startNS, endNS, thread.getStartTime(),
                                thread.getStartTime() + thread.getDuration()))
                    .collect(Collectors.toList()));
            }
        });
        Map<String, List<Thread>> collect = threads.stream().filter(filter -> STATUS_MAP.get(filter.getState()) != null)
            .collect(Collectors.groupingBy(thread -> STATUS_MAP.get(thread.getState())));
        long totalDur = endNS - startNS;
        collect.forEach((key, value) -> {
            ThreadStateBean bean = new ThreadStateBean();
            long sum = value.stream().mapToLong(thread -> TimeUtils
                .getNanoIntersection(startNS, endNS, thread.getStartTime(),
                    thread.getStartTime() + thread.getDuration())).sum();
            bean.setState(key);
            bean.setOccurrences(Integer.toString(value.size()));
            bean.setDuration(TimeUtils.getTimeWithUnit(sum));
            bean.setPercent(String.format(Locale.ENGLISH, "%.2f", sum * 1.0 / totalDur * 100));
            dataSource.add(bean);
        });
        tableColumnModel.fireTableDataChanged();
        jScrollPane
            .setPreferredSize(new Dimension(jScrollPane.getWidth(), dataSource.size() * RowHeight + RowHeadHeight));
    }

    private void getData(long startNS, long endNS, List<Integer> threadIds) {
        getAppData(startNS, endNS, threadIds);
        tableColumnModel.fireTableDataChanged();
        jScrollPane
            .setPreferredSize(new Dimension(jScrollPane.getWidth(), dataSource.size() * RowHeight + RowHeadHeight));
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
        getData(startNS, endNS, threadIds);
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
        if (TracePanel.rangeStartNS == null && TracePanel.rangeEndNS == null) {
            getData(startNS, endNS, TracePanel.currentSelectThreadIds);
        }
    }

    /**
     * class of state table model
     */
    public class StateTableModel extends AbstractTableModel {
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
    }

}
