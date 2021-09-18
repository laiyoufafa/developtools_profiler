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

package ohos.devtools.views.layout;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.CustomTextField;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.metrics.InfoStatsPanel;
import ohos.devtools.views.trace.metrics.MetricsPanel;
import ohos.devtools.views.trace.metrics.QuerySqlPanel;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * SystemTuningPanel
 */
public class SystemPanel extends JBPanel {
    /**
     * QUERY BUTTON WIDTH
     */
    private static final int QUERY_BUTTON_WIDTH = 110;

    /**
     * INFO BUTTON WIDTH
     */
    private static final int INFO_BUTTON_WIDTH = 126;

    /**
     * BUTTON HEIGHT
     */
    private static final int BUTTON_HEIGHT = 30;

    /**
     * button margin top
     */
    private static final int BUTTON_MARGIN_TOP = 11;

    /**
     * save button margin right
     */
    private static final int SAVE_MARGIN_LEFT = 375;

    /**
     * gc margin left
     */
    private static final int GC_MARGIN_LEFT = 400;

    /**
     * btn width or height
     */
    private static final int BTN_WIDTH_HEIGHT = 20;

    /**
     * INFO X
     */
    private static final int INFO_X = 236;

    /**
     * search margin right
     */
    private static final int SEARCH_MARGIN_RIGHT = 530;

    /**
     * down margin right
     */
    private static final int DOWN_MARGIN_RIGHT = 65;

    /**
     * left margin right
     */
    private static final int LEFT_MARGIN_RIGHT = 40;

    /**
     * top panel height
     */
    private static final int TOP_PANEL_HEIGHT = 34;

    /**
     * margin x
     */
    private static final int MARGIN_Y = 2;

    /**
     * margin x
     */
    private static final int SEARCH_MARGIN_Y = 4;

    /**
     * METRICS X
     */
    private static final int METRICS_X = 126;

    /**
     * margin
     */
    private static final int MARGIN = 20;

    private JButton queryButton;
    private JButton metricsButton;
    private JButton infoButton;
    private JBLabel saveBtn;
    private JBLabel gcBtn;
    private JBLabel downBtn;
    private JBLabel leftBtn;
    private JBPanel optionJPanel;
    private AnalystPanel analystPanel;


    /**
     * device name drop down box
     */
    private CustomTextField searchTextField;

    /**
     * System Tuning Panel
     */
    public SystemPanel(JBPanel optionJPanel, AnalystPanel analystPanel) {
        this.optionJPanel = optionJPanel;
        this.analystPanel = analystPanel;
        this.setPreferredSize(new Dimension(optionJPanel.getWidth() - MARGIN, TOP_PANEL_HEIGHT));
        initComponents();
        // 设置属性
        setAttributes();
        addComponent();
        componentAddListener();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        queryButton = new JButton();
        queryButton.setText("Query(SQL)");
        queryButton.setIcon(IconLoader.getIcon("/images/preview_normal.png", getClass()));
        metricsButton = new JButton();
        metricsButton.setIcon(IconLoader.getIcon("/images/overhead_normal.png", getClass()));
        metricsButton.setText("Metrics");
        infoButton = new JButton();
        infoButton.setIcon(IconLoader.getIcon("/images/notificationInfo_normal.png", getClass()));
        infoButton.setText("Info and stats");
        saveBtn = new JBLabel();
        saveBtn.setIcon(IconLoader.getIcon("/images/menu-saveAll_grey.png", getClass()));
        saveBtn.setToolTipText("Save");
        gcBtn = new JBLabel();
        gcBtn.setIcon(IconLoader.getIcon("/images/gc_grey.png", getClass()));
        gcBtn.setToolTipText("Delete");
        downBtn = new JBLabel();
        downBtn.setIcon(IconLoader.getIcon("/images/previewDetailsVertically_grey.png", getClass()));
        downBtn.setToolTipText("Expand page down");
        leftBtn = new JBLabel();
        leftBtn.setIcon(IconLoader.getIcon("/images/previewDetails_grey.png", getClass()));
        leftBtn.setToolTipText("Expand page left");
        searchTextField = new CustomTextField("press");
    }

    private void setAttributes() {
        this.setLayout(null);
        this.setOpaque(true);
        this.setBackground(JBColor.background().darker());
        queryButton.setBounds(LayoutConstants.EIGHT_NUM, MARGIN_Y, QUERY_BUTTON_WIDTH, BUTTON_HEIGHT);
        metricsButton.setBounds(METRICS_X, MARGIN_Y, LayoutConstants.DEVICE_ADD_WIDTH, BUTTON_HEIGHT);
        infoButton.setBounds(INFO_X, MARGIN_Y, INFO_BUTTON_WIDTH, BUTTON_HEIGHT);
        saveBtn.setBounds(SAVE_MARGIN_LEFT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
        gcBtn.setBounds(GC_MARGIN_LEFT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
        int panelWidth = optionJPanel.getWidth();
        searchTextField.setBounds(panelWidth - SEARCH_MARGIN_RIGHT, SEARCH_MARGIN_Y, LayoutConstants.HEIGHT_PRESSE,
            LayoutConstants.TASK_SCENE_Y);
        downBtn.setBounds(panelWidth - DOWN_MARGIN_RIGHT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
        leftBtn.setBounds(panelWidth - LEFT_MARGIN_RIGHT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
    }

    private void addComponent() {
        this.add(queryButton);
        this.add(metricsButton);
        this.add(infoButton);
    }

    private void componentAddListener() {
        queryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                QuerySqlPanel querySqlPanel = new QuerySqlPanel(optionJPanel, analystPanel, queryButton);
                metricsButton.setIcon(IconLoader.getIcon("/images/overhead_normal.png", getClass()));
                infoButton.setIcon(IconLoader.getIcon("/images/notificationInfo_normal.png", getClass()));
                removeCenterComponent();
                optionJPanel.add(querySqlPanel, BorderLayout.CENTER);
                optionJPanel.revalidate();
            }
        });

        metricsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                MetricsPanel metricsPanel = new MetricsPanel(optionJPanel, analystPanel, metricsButton);
                queryButton.setIcon(IconLoader.getIcon("/images/preview_normal.png", getClass()));
                infoButton.setIcon(IconLoader.getIcon("/images/notificationInfo_normal.png", getClass()));
                removeCenterComponent();
                optionJPanel.add(metricsPanel, BorderLayout.CENTER);
                optionJPanel.revalidate();
            }
        });

        infoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                InfoStatsPanel metricsPanel = new InfoStatsPanel(optionJPanel, analystPanel, infoButton);
                queryButton.setIcon(IconLoader.getIcon("/images/preview_normal.png", getClass()));
                metricsButton.setIcon(IconLoader.getIcon("/images/overhead_normal.png", getClass()));
                removeCenterComponent();
                optionJPanel.add(metricsPanel, BorderLayout.CENTER);
                optionJPanel.revalidate();
            }
        });
    }

    private void removeCenterComponent() {
        Component[] components = optionJPanel.getComponents();
        for (Component item : components) {
            if (!(item instanceof SystemPanel)) {
                optionJPanel.remove(item);
                break;
            }
        }
    }
}
