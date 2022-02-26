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
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.component.SysAnalystPanel;
import ohos.devtools.views.trace.util.Db;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Query Sql Panel
 *
 * @since 2021/5/19 16:39
 */
public class QuerySqlPanel extends JBPanel {
    /**
     * COLUMNS
     */
    private static final int COLUMNS = 28;

    /**
     * button x
     */
    private static final int BUTTON_X = 30;

    /**
     * panel margin x
     */
    private static final int PANEL_MARGIN_X = 35;

    /**
     * margin x
     */
    private static final int MARGIN_X = 50;

    /**
     * previous Button width
     */
    private static final int PREVIOUS_BUTTON_WIDTH = 200;

    /**
     * previous Button height
     */
    private static final int PREVIOUS_BUTTON_HEIGHT = 45;

    /**
     * font size
     */
    private static final int FONT_SIZE = 14;

    /**
     * top JPanel height
     */
    private static final int TOP_HEIGHT = 50;

    /**
     * input y
     */
    private static final int INPUT_Y = 10;

    /**
     * label height
     */
    private static final int LABEL_HEIGHT = 20;

    /**
     * label width
     */
    private static final int LABEL_WIDTH = 500;

    /**
     * bottom JPanel height
     */
    private static final int BOTTOM_HEIGHT = 115;

    /**
     * bottom JPanel y
     */
    private static final int BOTTOM_Y = 185;

    /**
     * center JPanel y
     */
    private static final int CENTER_Y = 80;

    /**
     * center JPanel height
     */
    private static final int CENTER_HEIGHT = 80;

    /**
     * text filed y
     */
    private static final int TEXT_FIELD_Y = 35;

    /**
     * text filed height
     */
    private static final int TEXT_FIELD_HEIGHT = 40;

    /**
     * half
     */
    private static final int HALF = 2;

    private JBLabel previousButton;
    private JBPanel optionJPanel;
    private JBPanel topJPanel;
    private JBPanel centerJPanel;
    private JBLabel inputLabel;
    private SysAnalystPanel analystPanel;
    private JTextField textField;
    private JButton queryButton;
    private JBPanel bottomJPanel;
    private JBLabel bottomLabel;
    private JBLabel errorLabel;
    private JScrollPane scrollPane;
    private List<String> notSupportList;

    /**
     * System Tuning Panel
     *
     * @param optionJPanel optionJPanel
     * @param analystPanel analystPanel
     * @param queryButton queryButton
     */
    public QuerySqlPanel(JBPanel optionJPanel, SysAnalystPanel analystPanel, JButton queryButton) {
        this.optionJPanel = optionJPanel;
        this.analystPanel = analystPanel;
        this.queryButton = queryButton;
        initComponents();
        // 设置属性
        setAttributes();
        addComponent();
        componentAddListener();
        MetricsDb.setDbName(Db.getDbName());
        MetricsDb.load(true);
    }

    /**
     * initComponents
     */
    private void initComponents() {
        previousButton = new JBLabel();
        topJPanel = new JBPanel();
        centerJPanel = new JBPanel();
        inputLabel = new JBLabel();
        textField = new JTextField(COLUMNS);
        bottomJPanel = new JBPanel();
        bottomLabel = new JBLabel();
        errorLabel = new JBLabel();
        scrollPane = new JScrollPane();
        notSupportList = new ArrayList<>();
    }

    private void setAttributes() {
        queryButton.setIcon(IconLoader.getIcon("/images/preview.png", getClass()));
        this.setLayout(null);
        this.setOpaque(true);
        topJPanel.setLayout(null);
        centerJPanel.setLayout(null);
        MigLayout layout = new MigLayout();
        bottomJPanel.setLayout(layout);
        previousButton.setText("Query (SQL)");
        Font font = new Font("PingFang SC", Font.PLAIN, FONT_SIZE);
        previousButton.setFont(font);
        previousButton.setIcon(AllIcons.Actions.Play_back);
        previousButton.setBounds(BUTTON_X, 0, PREVIOUS_BUTTON_WIDTH, PREVIOUS_BUTTON_HEIGHT);
        topJPanel.setBounds(0, 0, optionJPanel.getWidth(), TOP_HEIGHT);
        topJPanel.setBackground(JBColor.background().darker());
        topJPanel.add(previousButton);
        inputLabel.setFont(font);
        inputLabel.setText("Enter query and press cmd/ctrl + Enter");
        inputLabel.setBounds(MARGIN_X, INPUT_Y, LABEL_WIDTH, LABEL_HEIGHT);
        textField.setBounds(MARGIN_X, TEXT_FIELD_Y, optionJPanel.getWidth() - 100, TEXT_FIELD_HEIGHT);
        centerJPanel.setBounds(PANEL_MARGIN_X, CENTER_Y, optionJPanel.getWidth(), CENTER_HEIGHT);
        centerJPanel.setBackground(JBColor.background().darker());
        bottomLabel.setFont(font);
        bottomLabel.setText("");
        errorLabel.setFont(font);
        Color errorColor = new JBColor(new Color(0xFFFF9201, true), new Color(255, 146, 1));
        errorLabel.setForeground(errorColor);
        errorLabel.setText("");
        bottomJPanel.setBounds(PANEL_MARGIN_X, BOTTOM_Y, optionJPanel.getWidth(), BOTTOM_HEIGHT);
        bottomJPanel.setBackground(JBColor.background().darker());
        this.setPreferredSize(new Dimension(optionJPanel.getWidth(), optionJPanel.getHeight() / HALF));
    }

    private void addComponent() {
        centerJPanel.add(inputLabel);
        centerJPanel.add(textField);
        this.add(centerJPanel);
        this.add(topJPanel);
        this.add(bottomJPanel);
        notSupportList.add("insert");
        notSupportList.add("delete");
        notSupportList.add("update");
        notSupportList.add("drop");
        notSupportList.add("alter");
        notSupportList.add("truncate");
    }

    private void componentAddListener() {
        previousButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                Component[] components = optionJPanel.getComponents();
                for (Component item : components) {
                    if (item instanceof QuerySqlPanel) {
                        optionJPanel.remove(item);
                    }
                }
                queryButton.setIcon(IconLoader.getIcon("/images/preview_normal.png", getClass()));
                optionJPanel.add(analystPanel);
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.VK_ENTER) && (event.isControlDown())) {
                    bottomJPanel.removeAll();
                    long start = System.currentTimeMillis();
                    for (String item : notSupportList) {
                        if (textField.getText().contains(item)) {
                            errorLabel.setText("Statement contains a change action keyword,The change operation is not "
                                + "supported.");
                            bottomJPanel.add(errorLabel, "gapleft 6,width 500,height 25");
                            bottomJPanel.revalidate();
                            bottomJPanel.repaint();
                            return;
                        }
                    }
                    List<Map<String, String>> list = MetricsDb.getInstance().queryBySql(textField.getText());
                    long spendTime = System.currentTimeMillis() - start;
                    bottomLabel.setText("Query result - " + spendTime + " ms  " + textField.getText());
                    bottomJPanel.add(bottomLabel, "gapleft 6,width 500,height 25,wrap");
                    if (list.size() == 1 && list.get(0).containsKey("error")) {
                        errorLabel.setText(list.get(0).get("error"));
                        bottomJPanel.add(errorLabel, "gapleft 6,width 500,height 25");
                    } else {
                        keyPressHandle(list);
                    }
                    bottomJPanel.revalidate();
                    bottomJPanel.repaint();
                }
            }
        });
    }

    private void keyPressHandle(List<Map<String, String>> list) {
        if (list.size() <= 0) {
            return;
        }
        JTable table;
        DefaultTableModel tableModel;
        Map<String, String> columnMap = list.get(0);
        String[] columnNames = new String[columnMap.size()];
        Set<String> keys = columnMap.keySet();
        int index = 0;
        for (String key : keys) {
            columnNames[index++] = key;
        }
        String[][] tableValues = new String[list.size()][columnMap.size()];
        int rows = 0;
        for (Map<String, String> item : list) {
            Set<Map.Entry<String, String>> entries = item.entrySet();
            int columns = 0;
            for (Map.Entry<String, String> entry : entries) {
                tableValues[rows][columns++] = entry.getValue();
            }
            rows++;
        }
        tableModel = new DefaultTableModel(tableValues, columnNames);
        table = new JTable(tableModel);
        table.addMouseMotionListener(new MouseAdapter() {
            /**
             * Mouse move Handle
             *
             * @param event mouse event
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
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setHorizontalAlignment(JLabel.LEFT);
        table.getTableHeader().setDefaultRenderer(hr);
        scrollPane.setViewportView(table);
        table.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));
        bottomJPanel.add(scrollPane, "gapleft 6,width " + (optionJPanel.getWidth() - 100) + ",wrap");
        bottomJPanel.setBounds(PANEL_MARGIN_X, BOTTOM_Y, optionJPanel.getWidth() - 80, optionJPanel.getHeight() - 180);
    }
}
