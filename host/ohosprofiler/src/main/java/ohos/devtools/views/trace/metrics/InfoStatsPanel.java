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

package ohos.devtools.views.trace.metrics;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.metrics.bean.Metadata;
import ohos.devtools.views.trace.metrics.bean.Stats;
import ohos.devtools.views.trace.util.Db;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Info Stats Panel
 */
public class InfoStatsPanel extends JBPanel {
    /**
     * margin bottom
     */
    private static final int MARGIN_BOTTOM = 150;

    /**
     * margin right
     */
    private static final int MARGIN_RIGHT = 100;

    /**
     * font size
     */
    private static final int FONT_SIZE = 16;

    /**
     * half
     */
    private static final int HALF = 2;

    private JBLabel previousButton;
    private JBPanel optionJPanel;
    private JBPanel topJPanel;
    private AnalystPanel analystPanel;
    private JButton infoButton;
    private JBPanel centerJPanel;
    private JBLabel centerLabel;
    private JScrollPane centerScrollPane;
    private JBPanel bottomJPanel;
    private JBLabel bottomLabel;
    private JScrollPane bottomScrollPane;

    /**
     * Info Stats Panel
     */
    public InfoStatsPanel(JBPanel optionJPanel, AnalystPanel analystPanel, JButton infoButton) {
        this.optionJPanel = optionJPanel;
        this.analystPanel = analystPanel;
        this.infoButton = infoButton;
        initComponents();
        // 设置属性
        setAttributes();
        addComponent();
        componentAddListener();
        MetricsDb.setDbName(Db.getDbName());
        MetricsDb.load(true);
        getMetadata();
        getStats();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        previousButton = new JBLabel();
        topJPanel = new JBPanel();
        centerJPanel = new JBPanel();
        centerLabel = new JBLabel();
        centerScrollPane = new JScrollPane();
        bottomJPanel = new JBPanel();
        bottomLabel = new JBLabel();
        bottomScrollPane = new JScrollPane();
    }

    private void setAttributes() {
        infoButton.setIcon(IconLoader.getIcon("/images/notificationInfo.png", getClass()));
        MigLayout layout = new MigLayout();
        this.setLayout(layout);
        this.setOpaque(true);
        MigLayout topLayout = new MigLayout();
        topJPanel.setLayout(topLayout);
        MigLayout centerLayout = new MigLayout();
        centerJPanel.setLayout(centerLayout);
        MigLayout bottomLayout = new MigLayout();
        bottomJPanel.setLayout(bottomLayout);
        previousButton.setText("Info and stats");
        Font font = new Font("PingFang SC", Font.PLAIN, FONT_SIZE);
        previousButton.setFont(font);
        previousButton.setIcon(AllIcons.Actions.Play_back);
        topJPanel.setBackground(ColorConstants.ABILITY_COLOR);
        topJPanel.add(previousButton, "gapleft 10");
        centerJPanel.setBackground(ColorConstants.ABILITY_COLOR);
        this.setPreferredSize(new Dimension(optionJPanel.getWidth(), optionJPanel.getHeight() / HALF));
        centerLabel.setFont(font);
        centerLabel.setText("System info and metadata");
        bottomJPanel.setBackground(ColorConstants.ABILITY_COLOR);
        bottomLabel.setFont(font);
        bottomLabel.setText("Debugging stats");
    }

    private void addComponent() {
        this.add(topJPanel, "width " + optionJPanel.getWidth() + ",height 42,wrap");
        centerJPanel.add(centerLabel, "gapleft 12,width 500,height 30,wrap");
        this.add(centerJPanel, "width " + (optionJPanel.getWidth() - MARGIN_RIGHT) + ",wrap,height "
            + (optionJPanel.getHeight() - MARGIN_BOTTOM) / HALF);
        bottomJPanel.add(bottomLabel, "gapleft 12,width 500,height 30,wrap");
        this.add(bottomJPanel, "width " + (optionJPanel.getWidth() - MARGIN_RIGHT) + ",height "
            + (optionJPanel.getHeight() - MARGIN_BOTTOM) / HALF);
    }

    private void componentAddListener() {
        previousButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                Component[] components = optionJPanel.getComponents();
                for (Component item : components) {
                    if (item instanceof InfoStatsPanel) {
                        optionJPanel.remove(item);
                    }
                }
                infoButton.setIcon(IconLoader.getIcon("/images/notificationInfo_normal.png", getClass()));
                optionJPanel.add(analystPanel);
            }
        });
    }

    private void getStats() {
        JTable table;
        DefaultTableModel tableModel;
        String[] columnNames = {"Name", "Value", "Type"};
        List<Stats> stats = new ArrayList<>() {
        };
        MetricsDb.getInstance().query(MetricsSql.TRACE_STATS, stats);
        String[][] tableValues = new String[stats.size()][columnNames.length];
        int rows = 0;
        for (Stats item : stats) {
            int columns = 0;
            tableValues[rows][columns++] = item.getName() + "_" + item.getType();
            tableValues[rows][columns++] = item.getCount() + "";
            tableValues[rows][columns++] = item.getSeverity() + "(" + item.getSource() + ")";
            rows++;
        }
        tableModel = new DefaultTableModel(tableValues, columnNames);
        table = new JTable(tableModel);
        addTableListener(table);
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setHorizontalAlignment(JLabel.LEFT);
        table.getTableHeader().setDefaultRenderer(hr);
        bottomScrollPane.setViewportView(table);
        table.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));
        bottomJPanel.add(bottomScrollPane, "gapleft 12,width " + (optionJPanel.getWidth() - MARGIN_RIGHT) + ",height "
            + (optionJPanel.getHeight() - MARGIN_BOTTOM) / HALF);
    }

    /**
     * addTableListener
     *
     * @param table table
     */
    private void addTableListener(JTable table) {
        table.addMouseMotionListener(new MouseAdapter() {
            /**
             * mouseMoved
             *
             * @param event event
             */
            public void mouseMoved(MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int col = table.columnAtPoint(event.getPoint());
                if (row > -1 && col > -1) {
                    Object value = table.getValueAt(row, col);
                    if (value != null && !"".equals(value)) {
                        table.setToolTipText(value.toString());
                    } else {
                        table.setToolTipText(null);
                    }
                }
            }
        });
    }

    private void getMetadata() {
        JTable table;
        DefaultTableModel tableModel;
        String[] columnNames = {"Name", "Value"};
        List<Metadata> list = new ArrayList<>() {
        };
        MetricsDb.getInstance().query(MetricsSql.TRACE_METADATA, list);
        String[][] tableValues = new String[list.size()][columnNames.length];
        int rows = 0;
        for (Metadata item : list) {
            int columns = 0;
            tableValues[rows][columns++] = item.getName();
            tableValues[rows][columns++] = item.getValue();
            rows++;
        }
        tableModel = new DefaultTableModel(tableValues, columnNames);
        table = new JTable(tableModel);
        addTableListener(table);
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setHorizontalAlignment(JLabel.LEFT);
        table.getTableHeader().setDefaultRenderer(hr);
        centerScrollPane.setViewportView(table);
        table.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));
        centerJPanel.add(centerScrollPane, "gapleft 12,width " + (optionJPanel.getWidth() - MARGIN_RIGHT) + ",height "
            + (optionJPanel.getHeight() - MARGIN_BOTTOM) / HALF);
    }
}
