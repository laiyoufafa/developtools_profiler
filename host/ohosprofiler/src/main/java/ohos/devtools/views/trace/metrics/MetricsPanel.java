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
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.layout.SystemPanel;

import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.trace.component.SysAnalystPanel;
import ohos.devtools.views.trace.metrics.strategy.MetricsContext;
import ohos.devtools.views.trace.util.Db;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.awt.Image.SCALE_DEFAULT;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.LOADING_SIZE;

/**
 * Metrics Panel
 */
public class MetricsPanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(MetricsPanel.class);

    /**
     * COLUMNS
     */
    private static final int COLUMNS = 28;

    /**
     * rows
     */
    private static final int ROWS = 20;

    /**
     * button x
     */
    private static final int BUTTON_X = 30;

    /**
     * previous Button width
     */
    private static final int PREVIOUS_BUTTON_WIDTH = 200;

    /**
     * previous Button height
     */
    private static final int PREVIOUS_BUTTON_HEIGHT = 45;

    /**
     * run Button height
     */
    private static final int RUN_BUTTON_HEIGHT = 40;

    /**
     * run Button WIDTH
     */
    private static final int RUN_BUTTON_WIDTH = 100;

    /**
     * run Button margin left
     */
    private static final int RUN_BUTTON_MARGIN_LEFT = 15;

    /**
     * bottom x
     */
    private static final int BOTTOM_X = 35;

    /**
     * bottom y
     */
    private static final int BOTTOM_Y = 155;

    /**
     * bottom JPanel margin right
     */
    private static final int BOTTOM_PANEL_MARGIN_RIGHT = 80;

    /**
     * top JPanel height
     */
    private static final int TOP_PANEL_HEIGHT = 50;

    /**
     * center JPanel height
     */
    private static final int CENTER_PANEL_HEIGHT = 80;

    /**
     * center JPanel X
     */
    private static final int CENTER_PANEL_X = 35;

    /**
     * center JPanel Y
     */
    private static final int CENTER_PANEL_Y = 70;

    /**
     * font size
     */
    private static final int FONT_SIZE = 14;

    /**
     * label height
     */
    private static final int LABEL_HEIGHT = 20;

    /**
     * label width
     */
    private static final int LABEL_WIDTH = 300;

    /**
     * select y
     */
    private static final int SELECT_Y = 35;

    /**
     * quarter
     */
    private static final int QUARTER = 4;

    /**
     * half
     */
    private static final int HALF = 2;

    /**
     * other height
     */
    private static final int OTHER_HEIGHT = 100;
    private JBLabel previousButton;
    private JBPanel optionJPanel;
    private JBPanel topJPanel;
    private JBPanel centerJPanel;
    private JBLabel inputLabel;
    private SysAnalystPanel analystPanel;
    private JComboBox<String> metricSelect;
    private JButton metricsButton;
    private JBPanel bottomJPanel;
    private JButton runButton;
    private JTextArea resultTextArea;
    private JScrollPane resultScroll;

    /**
     * System Tuning Panel
     */
    public MetricsPanel(JBPanel optionJPanel, SysAnalystPanel analystPanel, JButton metricsButton) {
        this.optionJPanel = optionJPanel;
        this.analystPanel = analystPanel;
        this.metricsButton = metricsButton;
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
        metricSelect = new JComboBox<>();
        bottomJPanel = new JBPanel();
        runButton = new JButton();
        resultTextArea = new JTextArea(ROWS, COLUMNS);
    }

    private void setAttributes() {
        metricsButton.setIcon(IconLoader.getIcon("/images/overhead.png", getClass()));
        this.setLayout(null);
        this.setOpaque(true);
        topJPanel.setLayout(null);
        centerJPanel.setLayout(null);
        bottomJPanel.setLayout(null);
        previousButton.setText("Metrics");
        Font font = new Font("PingFang SC", Font.PLAIN, FONT_SIZE);
        previousButton.setFont(font);
        previousButton.setIcon(AllIcons.Actions.Play_back);
        previousButton.setBounds(BUTTON_X, 0, PREVIOUS_BUTTON_WIDTH, PREVIOUS_BUTTON_HEIGHT);
        topJPanel.setBounds(0, 0, optionJPanel.getWidth(), TOP_PANEL_HEIGHT);
        topJPanel.setBackground(JBColor.background().darker());
        topJPanel.add(previousButton);
        inputLabel.setFont(font);
        inputLabel.setText("Select a metric");
        inputLabel.setBounds(0, 0, LABEL_WIDTH, LABEL_HEIGHT);
        metricSelect.setBounds(0, SELECT_Y, optionJPanel.getWidth() / QUARTER, RUN_BUTTON_HEIGHT);
        metricSelect.setName(UtConstant.UT_METRICS_PANEL_SELECT);
        addSelectItem();
        runButton.setFont(font);
        runButton.setText("Run");
        runButton.setName(UtConstant.UT_SYSTEM_TUNING_METRICS_RUN);
        runButton.setBounds(optionJPanel.getWidth() / QUARTER + RUN_BUTTON_MARGIN_LEFT, SELECT_Y, RUN_BUTTON_WIDTH,
            RUN_BUTTON_HEIGHT);
        centerJPanel.setBounds(CENTER_PANEL_X, CENTER_PANEL_Y, optionJPanel.getWidth(), CENTER_PANEL_HEIGHT);
        centerJPanel.setBackground(JBColor.background().darker());
        resultTextArea.setFont(font);
        resultTextArea.setText("");
        resultTextArea.setEditable(false);
        resultTextArea.setBackground(JBColor.background().darker());
        resultScroll = new JScrollPane(resultTextArea);
        resultScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        int bottomHeight = optionJPanel.getHeight() - (TOP_PANEL_HEIGHT + CENTER_PANEL_HEIGHT + OTHER_HEIGHT);
        resultScroll.setBounds(0, 0, optionJPanel.getWidth() - BOTTOM_PANEL_MARGIN_RIGHT, bottomHeight);
        bottomJPanel
            .setBounds(BOTTOM_X, BOTTOM_Y, optionJPanel.getWidth() - BOTTOM_PANEL_MARGIN_RIGHT, bottomHeight);
        this.setPreferredSize(new Dimension(optionJPanel.getWidth(), optionJPanel.getHeight() / HALF));
    }

    private void addSelectItem() {
        MetricsSql[] values = MetricsSql.values();
        for (MetricsSql sql : values) {
            metricSelect.addItem(sql.getName());
        }
        metricSelect.setSelectedItem(MetricsSql.TRACE_TASK_NAMES.getName());
    }

    private void addComponent() {
        centerJPanel.add(inputLabel);
        centerJPanel.add(metricSelect);
        centerJPanel.add(runButton);
        bottomJPanel.add(resultScroll);
        this.add(centerJPanel);
        this.add(topJPanel);
        this.add(bottomJPanel);
    }

    private void componentAddListener() {
        previousButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                Component[] components = optionJPanel.getComponents();
                for (Component item : components) {
                    if (!(item instanceof SystemPanel)) {
                        optionJPanel.remove(item);
                        break;
                    }
                }
                metricsButton.setIcon(IconLoader.getIcon("/images/overhead_normal.png", getClass()));
                optionJPanel.add(analystPanel);
            }
        });
        runButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                resultTextArea.setText("");
                String selectStr = Objects.requireNonNull(metricSelect.getSelectedItem()).toString();
                JBLabel loadingLabel = new JBLabel();
                showLoadingLabel(loadingLabel);
                MetricsContext context = new MetricsContext();
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() {
                        // get query Result
                        return context.getQueryResult(MetricsSql.valueOf(selectStr.toUpperCase(Locale.ENGLISH)));
                    }

                    @Override
                    protected void done() {
                        try {
                            String result = get();
                            resultTextArea.remove(loadingLabel);
                            resultTextArea.repaint();
                            showText(result);
                        } catch (InterruptedException | ExecutionException exception) {
                            LOGGER.error(exception.getMessage());
                        }
                    }
                }.execute();
            }
        });
    }

    private void showLoadingLabel(JBLabel loadingLabel) {
        URL url = TaskScenePanelChart.class.getClassLoader().getResource("/images/loading.gif");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            icon.setImage(icon.getImage().getScaledInstance(LOADING_SIZE, LOADING_SIZE, SCALE_DEFAULT));
            loadingLabel.setIcon(icon);
        }
        loadingLabel
            .setBounds(bottomJPanel.getWidth() / HALF, bottomJPanel.getHeight() / HALF, LOADING_SIZE, LOADING_SIZE);
        resultTextArea.add(loadingLabel);
        resultTextArea.revalidate();
        resultScroll.revalidate();
        bottomJPanel.revalidate();
        optionJPanel.revalidate();
    }

    private void showText(String result) {
        if (!StringUtils.isEmpty(result)) {
            SwingUtilities.invokeLater(new Runnable() {
                /**
                 * run
                 */
                public void run() {
                    resultTextArea.setText(result);
                }
            });
        }
    }
}
