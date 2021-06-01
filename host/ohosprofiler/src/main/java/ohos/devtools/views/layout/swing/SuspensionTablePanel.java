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

package ohos.devtools.views.layout.swing;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import ohos.devtools.services.memory.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.MemoryInstanceDetailsManager;
import ohos.devtools.services.memory.MemoryInstanceInfo;
import ohos.devtools.services.memory.MemoryInstanceManager;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.event.TaskScenePanelChartEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * 悬浮table 展示
 *
 * @version 1.0
 * @date 2021/3/12 18:44
 **/
public class SuspensionTablePanel extends TaskScenePanelChartEvent {
    private boolean flag = false;

    private JTable jTable;
    private JPanel jp = new JPanel(new BorderLayout());

    /**
     * 设置悬浮table
     *
     * @param jpanelSupenn Supen jpanel
     * @param cId          cId
     * @param sessionId    sessionId
     * @param className    Name class
     * @return JPanel
     */
    public JPanel createSuspensionTable(JPanel jpanelSupenn, Integer cId, long sessionId, String className) {
        jpanelSupenn.setLayout(new BorderLayout());
        Vector<String> columnNames = new Vector<>();
        columnNames.add("instance");
        columnNames.add("allocTime");
        columnNames.add("DealloTime");
        columnNames.add("ID");
        columnNames.add("InstanceId");
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        initData(model, cId, className, sessionId);
        JBTable table = new JBTable(model);
        setExtracted(model, table);
        JBScrollPane jScrollPane = new JBScrollPane(table);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel jpTop = getTopJPanel();
        JLabel jb =
            new JLabel(new ImageIcon(SuspensionTablePanel.class.getClassLoader().getResource("images/close.png")));
        jb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                jpanelSupenn.setVisible(false);
            }
        });

        JPanel jPanel = new JPanel(new BorderLayout());
        jpTop.add(jb, BorderLayout.EAST);
        jPanel.add(jpTop, BorderLayout.NORTH);

        JLayeredPane jLayeredPane = new JLayeredPane();

        jPanel.add(jScrollPane, BorderLayout.CENTER);
        jPanel.setBounds(0, 0, LayoutConstants.THREE_HUNDRED, LayoutConstants.THREE_HUNDRED);
        jLayeredPane.add(jPanel, JLayeredPane.PALETTE_LAYER);
        jpanelSupenn.removeAll();
        jpanelSupenn.add(jLayeredPane);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                flag = true;
                if (mouseEvent.getClickCount() == 1) {
                    getJTable(table, jpanelSupenn, jLayeredPane);
                }
            }
        });
        return jpanelSupenn;
    }

    /**
     * get JTable
     *
     * @param table table
     * @param jPanelSuspension jPanelSuspension
     * @param jLayeredPane jLayeredPane
     */
    private void getJTable(JBTable table, JPanel jPanelSuspension, JLayeredPane jLayeredPane) {
        int selectedRow = table.getSelectedRow();
        RowSorter<TableModel> rowSorter = null;
        Object sorterObj = table.getRowSorter();
        if (sorterObj instanceof RowSorter) {
            rowSorter = (RowSorter<TableModel>) sorterObj;
            DefaultTableModel detailModel = null;
            Object modelObj = rowSorter.getModel();
            if (modelObj instanceof DefaultTableModel) {
                detailModel = (DefaultTableModel) modelObj;
                Vector<Vector> detailModeldatas = detailModel.getDataVector();
                Vector detailModeldata = detailModeldatas.get(selectedRow);
                Integer instanceId = 0;
                Object dataObj = detailModeldata.get(4);
                if (dataObj instanceof Integer) {
                    instanceId = (Integer) dataObj;
                    jTable = createChildTable(instanceId, flag, jPanelSuspension, jLayeredPane);
                }
            }
        }
    }

    private void setExtracted(DefaultTableModel model, JBTable table) {
        TableColumnModel tcm = table.getColumnModel();
        table.removeColumn(tcm.getColumn(4));
        table.removeColumn(tcm.getColumn(3));
        table.getTableHeader().setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jp.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        table.setShowHorizontalLines(false);
        table.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);
    }

    private JPanel getTopJPanel() {
        JLabel label = new JLabel("instanceView");
        label.setOpaque(true);
        label.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        label.setPreferredSize(new Dimension(LayoutConstants.TASK_DEC_X, LayoutConstants.THIRTY));
        JPanel jpTop = new JPanel(new BorderLayout());
        jpTop.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpTop.add(label, BorderLayout.WEST);
        return jpTop;
    }

    /**
     * 初始化table 表格
     *
     * @param model     model
     * @param cId       cId
     * @param className className
     * @param sessionId long
     */
    public void initData(DefaultTableModel model, Integer cId, String className, long sessionId) {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        ChartStandard standard = ProfilerChartsView.sessionMap.get(sessionId).getObserver().getStandard();
        long firstTime = standard.getFirstTimestamp();
        ChartDataRange selectedRange = standard.getSelectedRange();
        long startTime = selectedRange.getStartTime() + firstTime;
        long endTime = selectedRange.getEndTime() + firstTime;
        List<MemoryInstanceInfo> memoryInstanceInfos =
            memoryInstanceManager.getMemoryInstanceInfos(cId, startTime, endTime);
        memoryInstanceInfos.forEach(memoryInstanceInfo -> {
            long deallocTime = memoryInstanceInfo.getDeallocTime();
            long alloc = TimeUnit.MILLISECONDS.toMicros(memoryInstanceInfo.getAllocTime() - firstTime);
            String allocTime = getSemiSimplifiedClockString(alloc);
            String dellTime = "-- : -- : --";
            if (deallocTime != 0) {
                long delloc = TimeUnit.MILLISECONDS.toMicros(memoryInstanceInfo.getDeallocTime() - firstTime);
                dellTime = getSemiSimplifiedClockString(delloc);
            }
            Integer id = memoryInstanceInfo.getId();
            Integer instanceId = memoryInstanceInfo.getInstanceId();
            Vector<Object> rowData = new Vector<>();
            rowData.add(className);
            rowData.add(allocTime);
            rowData.add(dellTime);
            rowData.add(id);
            rowData.add(instanceId);
            model.addRow(rowData);
        });
    }

    /**
     * Return a formatted time String in the form of "hh:mm:ss.sss".
     * Default format for Range description.
     *
     * @param micro micro
     * @return String
     */
    public String getFullClockString(long micro) {
        long micros = Math.max(0, micro);
        long milli = TimeUnit.MICROSECONDS.toMillis(micros) % TimeUnit.SECONDS.toMillis(1);
        long sec = TimeUnit.MICROSECONDS.toSeconds(micros) % TimeUnit.MINUTES.toSeconds(1);
        long min = TimeUnit.MICROSECONDS.toMinutes(micros) % TimeUnit.HOURS.toMinutes(1);
        long hour = TimeUnit.MICROSECONDS.toHours(micros);
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d.%03d", hour, min, sec, milli);
    }

    /**
     * Return a formatted time String in the form of "hh:mm:ss.sss"".
     * Hide hours value if both hours and minutes value are zero.
     * Default format for Tooltips.
     *
     * @param micro micro
     * @return String
     */
    public String getSemiSimplifiedClockString(long micro) {
        long micros = Math.max(0, micro);
        String result = getFullClockString(micros);
        return result.startsWith("00:00:") ? result.substring(3) : result;
    }

    /**
     * 根据筛选填充table 表格
     *
     * @param model model
     */
    public void selectData(DefaultTableModel model) {
        // 获取初始化数据，根目录
        Vector<Object> rowDat = new Vector<>();
        rowDat.add("John");
        model.addRow(rowDat);
    }

    /**
     * select Data
     *
     * @return JPanel
     */
    public JPanel selectData() {
        JPanel jpanel = new JPanel(new BorderLayout());
        JTextField jTextField = new JTextField();
        jTextField.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        Label lb = new Label("Table");
        lb.setPreferredSize(new Dimension(LayoutConstants.FORTY, LayoutConstants.TWENTY));
        jpanel.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpanel.add(jTextField, BorderLayout.EAST);
        jpanel.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpanel.add(lb, BorderLayout.WEST);
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent exception) {
            }

            @Override
            public void removeUpdate(DocumentEvent exception) {
            }

            @Override
            public void changedUpdate(DocumentEvent exception) {
            }
        });
        return jpanel;
    }

    /**
     * 层级展示table 表格
     *
     * @param model model
     * @param id id
     * @param sp sp
     */
    public void insertData(DefaultTableModel model, String id, String sp) {
        // 根据父亲id,获取子项
        String space = sp + "    ";
        Vector<Object> rowDate = new Vector<>();
        rowDate.add(space + "John");
        rowDate.add(LayoutConstants.EIGHTY);
        rowDate.add(LayoutConstants.SEVENTY);
        rowDate.add(LayoutConstants.SIXTY);
        rowDate.add(LayoutConstants.TWO_HUNDRED_TEN);
        rowDate.add(Math.random());
        rowDate.add(id);
        rowDate.add("n");
        model.addRow(rowDate);
    }

    /**
     * 子悬浮table
     *
     * @param instanceId   instanceId
     * @param flag         flag
     * @param jpanelSupenn jpanelSupenn
     * @param jLayeredPane jLayeredPane
     * @return JTable
     */
    public JTable createChildTable(Integer instanceId, boolean flag, JPanel jpanelSupenn, JLayeredPane jLayeredPane) {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("");
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        JTable table = new JTable(model);
        table.getTableHeader().setBackground(ColorConstants.TRACE_TABLE_COLOR);
        table.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        MemoryInstanceDetailsManager detailsManager = new MemoryInstanceDetailsManager();
        ArrayList<MemoryInstanceDetailsInfo> detailsInfos = detailsManager.getMemoryInstanceDetailsInfos(instanceId);
        detailsInfos.forEach(detailsInfo -> {
            Vector<String> rowData = new Vector<>();
            rowData.add(
                detailsInfo.getClassName() + "::" + detailsInfo.getMethodName() + " " + detailsInfo.getLineNumber()
                    + " " + detailsInfo.getFieldName());
            model.addRow(rowData);
        });
        table.setShowHorizontalLines(false);
        JScrollPane jScrollPane = new JScrollPane(table);
        jScrollPane.getViewport().setOpaque(true);
        jScrollPane.getViewport().setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        JLabel label = new JLabel("Allocation Call Stack ");
        label.setPreferredSize(new Dimension(LayoutConstants.TASK_DEC_X, LayoutConstants.THIRTY));
        label.setOpaque(true);
        label.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        JPanel jpTop = new JPanel(new BorderLayout());
        jpTop.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpTop.add(label, BorderLayout.WEST);
        JLabel jb =
            new JLabel(new ImageIcon(SuspensionTablePanel.class.getClassLoader().getResource("images/close.png")));
        jb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                jLayeredPane.remove(jp);
            }
        });
        jpTop.add(jb, BorderLayout.EAST);
        jp.removeAll();
        jp.add(jpTop, BorderLayout.NORTH);
        jp.add(jScrollPane, BorderLayout.CENTER);
        jp.setBounds(0, LayoutConstants.THREE_HUNDRED, LayoutConstants.THREE_HUNDRED,
            LayoutConstants.THREE_HUNDRED_TWENTY);
        jLayeredPane.add(jp, JLayeredPane.DRAG_LAYER);
        jLayeredPane.revalidate();
        return table;
    }
}
