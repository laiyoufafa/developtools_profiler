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

import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.hoscomp.HosJLabel;
import ohos.devtools.views.layout.event.TaskPanelEvent;
import ohos.devtools.views.trace.component.AnalystPanel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * 二级界面
 *
 * @version 1.0
 * @date 2021/02/27 11:09
 **/
public class TaskPanel extends JLayeredPane {
    /**
     * 全局日志
     */
    private static final Logger LOGGER = LogManager.getLogger(TaskPanel.class);

    /**
     * 二级界面选项卡容器
     */
    private JPanel optionJPanel = new JPanel(new BorderLayout());

    /**
     * 二级界面选项卡内容容器
     */
    private JPanel optionJPanelContent = new JPanel();

    /**
     * 多配置界面选项卡标签
     */
    private JPanel jPanelTabLabel = new JPanel();

    /**
     * 多配置界面选项卡标签左侧
     */
    private JPanel jPanelLeft = new JPanel();

    /**
     * 多配置界面选项卡标签右侧
     */
    private JPanel jPanelRight = new JPanel();

    /**
     * 配置界面选项卡标签命名
     */
    private JLabel jLabelSetting = new JLabel("NewTask-Configure");

    /**
     * 多配置界面选项卡标签关闭按钮
     */
    private JLabel jLabelClose = new JLabel("x");

    private TaskPanelEvent taskPanelEvent = new TaskPanelEvent();

    private JLabel jButtonApplyTun = new JLabel(new ImageIcon(TaskPanel.class.getClassLoader()
        .getResource("images/application_tuning.png")));
    private JLabel jButtonSystemTun =
        new JLabel(new ImageIcon(TaskPanel.class.getClassLoader().getResource("images/system_tuning.png")));
    private JLabel jButtonHadoop = new JLabel(
        new ImageIcon(TaskPanel.class.getClassLoader().getResource("images/distributed_scenario.png")));

    private JLabel jLabelIcon = new JLabel();

    private JLabel jLabelTaskTun = new JLabel("<html>Application tuning<br/><br/>Tune application Launch perfoam"
            + "ace with a 5 second time profile and a thread state trace.</html>");

    private JLabel chooseButton = new JLabel("Choose", JLabel.CENTER);

    private JLabel traceButton = new JLabel("Open Trace File", JLabel.CENTER);

    private JButton jButtonAdd = new JButton("+");

    private Long localSessionId;

    private TransferringWindow jProgressBar = null;

    /**
     * 运行可执行文件将数据源转为db文件的是否成功结果
     */
    private Boolean traceAnalysisResult = true;

    /**
     * Task Panel
     */
    public TaskPanel() {
    }

    /**
     * Task Panel
     *
     * @param taskPanel        task Panel
     * @param taskPanelWelcome task Panel Welcome
     */
    public TaskPanel(JPanel taskPanel, TaskPanelWelcome taskPanelWelcome) {
        // 设置多配置界面 选项卡窗体大小
        this.setPreferredSize(new Dimension(LayoutConstants.OPT_WIDTH, LayoutConstants.OPT_HEIGHT));
        // 设置背景不透明
        this.setOpaque(true);
        this.setBackground(ColorConstants.TOP_COLOR);
        // 将选项卡内容容器添加到选项卡容器
        optionJPanel.add(optionJPanelContent);
        // 设置属性
        setAttributes(taskPanel);
        // 实现可新增多任务功能
        Constant.jtasksTab.addTab("", optionJPanel);
        Constant.jtasksTab.setTabComponentAt(Constant.jtasksTab.indexOfComponent(optionJPanel), jPanelTabLabel);
        taskLabel(jButtonApplyTun, jButtonSystemTun, jButtonHadoop);
        taskPanel.setLayout(new BorderLayout());
        jButtonAdd.setBackground(ColorConstants.HOME_PANE);
        jButtonAdd.setForeground(Color.GRAY);
        Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.DEVICES_HEIGHT);
        jButtonAdd.setFont(font);
        jButtonAdd.setBorderPainted(false);
        jButtonAdd.setBounds(LayoutConstants.NUMBER_X_ADD * Constant.jtasksTab.getTabCount(), LayoutConstants
                .NUMBER_Y, LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
        Constant.jtasksTab.setBounds(0, 0, taskPanel.getWidth(), taskPanel.getHeight());
        this.add(Constant.jtasksTab);
        taskPanel.add(this);
        double result = Constant.jtasksTab.getTabCount() * LayoutConstants.NUMBER_X;
        // 当选项卡标签总长度过长时自动改变每个标签的大小使所有的标签在一行
        if (result > taskPanel.getWidth()) {
            for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                Object tabObj = Constant.jtasksTab.getTabComponentAt(index);
                if (tabObj instanceof JPanel) {
                    ((JPanel) tabObj).getComponents()[0].setPreferredSize(new Dimension(
                            (((taskPanel.getWidth() - LayoutConstants.MEMORY_WIDTH) / Constant.jtasksTab
                                    .getTabCount()) - LayoutConstants.TASK_DEC_NUM) - LayoutConstants.JAVA_HEIGHT,
                            LayoutConstants.JAVA_HEIGHT));
                }
                Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(new Dimension(
                        ((taskPanel.getWidth() - LayoutConstants.MEMORY_WIDTH) /
                                Constant.jtasksTab.getTabCount())
                                - LayoutConstants.TASK_DEC_NUM, LayoutConstants.JAVA_HEIGHT));
                jButtonAdd.setBounds(taskPanel.getWidth() - LayoutConstants.TASK_LABEL_X,
                        LayoutConstants.NUMBER_Y,
                        LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
            }
        }
        optionJPanelContent.add(chooseButton);
        // 关闭选项卡事件
        taskPanelEvent.clickClose(this, taskPanel, taskPanelWelcome);
        // 选择不同场景对应显示
        taskPanelEvent.pplyTunMouseListener(taskPanel, this);
        taskPanelEvent.addSystemMouseListener(taskPanel, this);
        // chooseButton按钮添加点击事件
        taskPanelEvent.applicationTuningClickListener(this);
        // 监听taskPanel大小，将jTabbedPane大小于其联动
        taskPanelEvent.listenerTaskPanel(taskPanel, jButtonAdd);
        // jButtonAdd事件添加配置页面
        // 点击不同场景，出现选择状态
        taskPanelEvent.sigleClick(this);
        // 监听窗体大小改变组件位置
        taskPanelEvent.listenerWindow(taskPanel, this);
    }

    /**
     * 设置属性
     *
     * @param taskPanel taskPanel
     */
    public void setAttributes(JPanel taskPanel) {
        if (taskPanel == null) {
            return;
        }
        optionJPanelContent.setLayout(null);
        // 设置背景不透明
        optionJPanelContent.setOpaque(true);
        optionJPanelContent.setBackground(ColorConstants.SCROLL_PANE);
        jPanelTabLabel.setOpaque(false);
        jPanelTabLabel.setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
        jPanelLeft.setOpaque(false);
        jPanelRight.setOpaque(false);
        jPanelTabLabel.setLayout(new BorderLayout());
        jPanelLeft.setLayout(null);
        jPanelRight.setLayout(new GridLayout());
        jPanelLeft.setPreferredSize(new Dimension(LayoutConstants.JP_LEFT_WIDTH, LayoutConstants.JP_LEFT_HEIGHT));
        jPanelRight.setPreferredSize(new Dimension(LayoutConstants.JP_RIGHT_WIDTH, LayoutConstants.JP_RIGHT_HEIGHT));
        jPanelTabLabel.add(jPanelLeft, BorderLayout.WEST);
        jPanelTabLabel.add(jPanelRight, BorderLayout.CENTER);
        // 给jPanelLeft添加标题
        jLabelSetting.setBounds(0, 0, LayoutConstants.JP_SET_WIDTH, LayoutConstants.JP_SET_HEIGHT);
        jPanelLeft.add(jLabelSetting);
        jPanelRight.add(jLabelClose);
        jLabelClose.setHorizontalAlignment(JLabel.RIGHT);
        jLabelIcon.setIcon(new ImageIcon(TaskPanel.class.getClassLoader()
            .getResource("images/application_tuning.png")));
        jLabelIcon.setBounds(LayoutConstants.DEVICES_X, LayoutConstants.DESCRIPTION_NUMBER, LayoutConstants.HIGTHSCEECS,
                LayoutConstants.HIGTHSCEECS);
        jLabelTaskTun
            .setBounds(LayoutConstants.JAVA_WIDTH, LayoutConstants.DESCRIPTION_NUMBER, LayoutConstants.APP_LABEL_WIDTH,
                LayoutConstants.JLABEL_SIZE);
        Font fontTaskTun = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TUN_LABEL_FONT);
        jLabelTaskTun.setFont(fontTaskTun);
        jLabelTaskTun.setForeground(Color.WHITE);
        Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT);
        setButtonAttributter(font, taskPanel);
        JPanel graphicsJpanel = new GraphicsJpanel(this);
        graphicsJpanel.setBounds(0, LayoutConstants.WIDTHSUPEN, taskPanel.getWidth(), LayoutConstants.JAVA_WIDTH);
        // Monitor window size changes to make graphicsJpanel size adaptive
        taskPanelEvent.listenerGraphicsJpanel(graphicsJpanel, this);
        optionJPanelContent.add(jLabelIcon);
        optionJPanelContent.add(jLabelTaskTun);
        optionJPanelContent.add(graphicsJpanel);
        optionJPanelContent.add(traceButton);
    }

    private void setButtonAttributter(Font font, JPanel taskPanel) {
        chooseButton.setBounds(LayoutConstants.CHOSSE_X + (taskPanel.getWidth() - LayoutConstants.WINDOW_WIDTH),
                LayoutConstants.CHOOSE_Y + (taskPanel.getHeight() - LayoutConstants.WINDOW_HEIGHT),
                LayoutConstants.DEVICES_WIDTH, LayoutConstants.CHOOSE_HEIGHT);
        chooseButton.setOpaque(true);
        chooseButton.setBackground(ColorConstants.CHOOSE_BUTTON);
        chooseButton.setFont(font);
        chooseButton.setForeground(Color.WHITE);
        traceButton.setFont(font);
        traceButton.setOpaque(true);
        traceButton.setBounds(LayoutConstants.TASK_SCENE_X,
                LayoutConstants.CHOOSE_Y + (taskPanel.getHeight() - LayoutConstants.WINDOW_HEIGHT),
                LayoutConstants.CHOOSE_WIDTH, LayoutConstants.CHOOSE_HEIGHT);
        traceButton.setBackground(ColorConstants.CHOOSE_BUTTON);
        traceButton.setForeground(Color.WHITE);
        traceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                showFileOpenDialog(optionJPanelContent);
            }
        });
    }

    /**
     * 打开trace文件
     *
     * @param parent parent
     */
    private void showFileOpenDialog(JPanel parent) {
        JFileChooser fileChooser = new JFileChooser();
        if (StringUtils.isNotBlank(Constant.path)) {
            fileChooser.setCurrentDirectory(new File(Constant.path));
        } else {
            fileChooser.setCurrentDirectory(new File("."));
        }
        UIManager.put("FileChooser.cancelButtonText", "Cancel");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        fileChooser.setDialogTitle("Select a Trace File");
        fileChooser.setApproveButtonText("Open");
        fileChooser.setControlButtonsAreShown(false);
        SampleDialogWrapper sampleDialog = new SampleDialogWrapper("Select a Trace File", fileChooser);
        boolean flag = sampleDialog.showAndGet();
        if (flag) {
            if (fileChooser.getSelectedFile() == null || fileChooser.getSelectedFile().isDirectory()) {
                new SampleDialogWrapper("Prompt", "Please select the trace file !").show();
                return;
            }
            jProgressBar = new TransferringWindow(parent);
            // 移除按钮显示滚动条
            parent.remove(chooseButton);
            parent.remove(traceButton);
            // 监听JProgressBar的大小
            parent.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    super.componentResized(event);
                    jProgressBar.setBounds(LayoutConstants.TEN, parent.getHeight() - LayoutConstants.FORTY,
                            parent.getWidth() - LayoutConstants.TWENTY, LayoutConstants.THIRTY);
                }
            });
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    loadOfflineFile(parent, TaskPanel.this, fileChooser, optionJPanel);
                }
            });
        }
    }

    /**
     * 加载离线文件
     *
     * @param optionJPanelContent optionJPanelContent
     * @param taskPanel           taskPanel
     * @param fileChooser         fileChooser
     * @param optionJPanel        optionJPanel
     */
    private void loadOfflineFile(JPanel optionJPanelContent, TaskPanel taskPanel, JFileChooser fileChooser,
        JPanel optionJPanel) {
        SwingWorker<Optional<DeviceProcessInfo>, Object> task = new SwingWorker<Optional<DeviceProcessInfo>, Object>() {
            /**
             * doInBackground
             *
             * @return Optional<DeviceProcessInfo>
             * @throws Exception Exception
             */
            @Override
            protected Optional<DeviceProcessInfo> doInBackground() throws Exception {
                File file = fileChooser.getSelectedFile();
                Constant.path = fileChooser.getCurrentDirectory().getPath();
                Optional<DeviceProcessInfo> deviceProcessInfo =
                        SessionManager.getInstance().localSessionDataFromFile(jProgressBar, file);
                return deviceProcessInfo;
            }

            /**
             * done
             */
            @Override
            protected void done() {
                Optional<DeviceProcessInfo> deviceProcessInfo = null;
                try {
                    deviceProcessInfo = get();
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
                if (deviceProcessInfo != null && deviceProcessInfo.isPresent()) {
                    DeviceProcessInfo deviceInfo = deviceProcessInfo.get();
                    HosJLabel hosJLabel = new HosJLabel();
                    hosJLabel.setProcessName(deviceInfo.getProcessName());
                    hosJLabel.setSessionId(deviceInfo.getLocalSessionId());
                    hosJLabel.setDeviceName(deviceInfo.getDeviceName());
                    hosJLabel.setDeviceType(true);
                    hosJLabel.setStartTime(deviceInfo.getStartTime());
                    hosJLabel.setEndTime(deviceInfo.getEndTime());
                    List<HosJLabel> hosJLabels = new ArrayList<HosJLabel>();
                    hosJLabels.add(hosJLabel);
                    optionJPanel.removeAll();
                    TaskScenePanelChart taskScenePanelChart = new TaskScenePanelChart(taskPanel, hosJLabels);
                    TaskPanel.this.setLocalSessionId(deviceInfo.getLocalSessionId());
                    optionJPanel.add(taskScenePanelChart);
                } else {
                    loadSystemTurningFile(optionJPanelContent, fileChooser, optionJPanel);
                    return;
                }
            }
        };
        task.execute();
    }

    /**
     * load systrace file
     *
     * @param optionJPanelContent optionJPanelContent
     * @param fileChooser         fileChooser
     * @param optionJPanel        optionJPanel
     */
    private void loadSystemTurningFile(JPanel optionJPanelContent, JFileChooser fileChooser, JPanel optionJPanel) {
        SwingWorker<String, Object> task = new SwingWorker<String, Object>() {
            @Override
            protected String doInBackground() throws Exception {
                File directory = new File("");
                traceAnalysisResult = true;
                String courseFile = directory.getCanonicalPath();
                String pluginPath = SessionManager.getInstance().getPluginPath();
                String logPath = pluginPath + "trace_streamer/trace_streamer.log";
                File logFile = new File(logPath);
                if (logFile.exists()) {
                    logFile.delete();
                }
                // 将数据源转为db文件
                String baseDir = pluginPath + "trace_streamer\\";
                String dbPath = baseDir + "systrace.db";
                File file = fileChooser.getSelectedFile();
                String[] cmd = {baseDir + "trace_streamer.exe", file.getPath(), "-e", dbPath};
                HdcWrapper.getInstance().getHdcStringArrayResult(cmd);
                // 获取.log日志(存放在根目录)
                randomFile(logFile);
                jProgressBar.setValue(LayoutConstants.FIFTY);
                return dbPath;
            }

            /**
             * done
             */
            @Override
            protected void done() {
                if (!traceAnalysisResult) {
                    optionJPanelContent.remove(jProgressBar);
                    optionJPanelContent.add(chooseButton);
                    optionJPanelContent.add(traceButton);
                    optionJPanelContent.repaint();
                    new SampleDialogWrapper("Warring",
                            "The system cannot parse the file properly. Please import the legal file.").show();
                }
                try {
                    if (traceAnalysisResult) {
                        String dbPath = get();
                        optionJPanel.removeAll();
                        AnalystPanel component = new AnalystPanel();
                        component.load(dbPath, true);
                        jProgressBar.setValue(LayoutConstants.HUNDRED);
                        optionJPanel.add(component);
                    }
                } catch (Exception exception) {
                    LOGGER.error("loadSystemTurningFile exception:{}", exception.getMessage());
                }
            }
        };
        task.execute();
    }

    /**
     * random File
     *
     * @param logFile log File
     * @throws IOException IOException
     */
    private void randomFile(File logFile) throws IOException {
        RandomAccessFile randomFile = null;
        try {
            if (logFile.exists()) {
                randomFile = new RandomAccessFile(logFile, "r");
                String tmp = null;
                while ((tmp = randomFile.readLine()) != null) {
                    if (Integer.valueOf(tmp.split(":")[1]) != 0) {
                        traceAnalysisResult = false;
                    }
                }
            }
        } catch (FileNotFoundException fileNotFoundException) {
            LOGGER.error("randomFile exception:{}", fileNotFoundException.getMessage());
        } catch (IOException iOException) {
            LOGGER.error("randomFile exception:{}", iOException.getMessage());
        } finally {
            if (randomFile != null) {
                randomFile.close();
            }
        }
    }

    /**
     * 新建描述图标
     *
     * @param jButtonApplyTun  jButtonApplyTun
     * @param jButtonSystemTun jButtonSystemTun
     * @param jButtonHadoop    jButtonHadoop
     */
    public void taskLabel(JLabel jButtonApplyTun, JLabel jButtonSystemTun, JLabel jButtonHadoop) {
        if (jButtonApplyTun == null || jButtonSystemTun == null || jButtonHadoop == null) {
            return;
        }
        // 新建描述图标
        JLabel jLabelRealTimeTaskZhu = new JLabel("Task scene");
        jLabelRealTimeTaskZhu.setForeground(Color.white);
        JLabel jLabelRealTimeTaskCi = new JLabel("Choose the most suitable scene.");
        Font fontZhu = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TASK_FONT);
        Font fontCi = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.APP_BUT_FONT);
        jLabelRealTimeTaskZhu.setFont(fontZhu);
        jLabelRealTimeTaskCi.setFont(fontCi);
        jLabelRealTimeTaskCi.setForeground(ColorConstants.BORDER_COLOR);
        jLabelRealTimeTaskZhu
                .setBounds(LayoutConstants.TASK_SCENE_X, LayoutConstants.TASK_SCENE_Y, LayoutConstants.TASK_WIDTH,
                        LayoutConstants.TASK_HEIGHT);
        jLabelRealTimeTaskCi
                .setBounds(LayoutConstants.CH_TASK_X, LayoutConstants.CH_TASK_Y, LayoutConstants.CH_TASK_WIDTH,
                        LayoutConstants.CH_TASK_HEIGHT);
        jButtonApplyTun
                .setBounds(LayoutConstants.APP_BUT_X, LayoutConstants.APP_BUT_Y, LayoutConstants.TWO_HUNDRED_EIGHTEEN,
                        LayoutConstants.HUNDRED_FIFTY_FIVE);
        jButtonApplyTun.setText("Application tuning");
        Font fontAll = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.APP_BUT_FONT);
        jButtonApplyTun.setFont(fontAll);
        jButtonApplyTun.setOpaque(true);
        jButtonApplyTun.setVerticalTextPosition(JLabel.BOTTOM);
        jButtonApplyTun.setHorizontalTextPosition(JLabel.CENTER);
        jButtonApplyTun.setBackground(ColorConstants.APPLYTUN_COLOR);
        jButtonSystemTun.setBounds(LayoutConstants.LABEL_NAME_WIDTH, LayoutConstants.SYS_BUT_Y,
                LayoutConstants.TWO_HUNDRED_EIGHTEEN, LayoutConstants.HUNDRED_FIFTY_FIVE);
        jButtonSystemTun.setText("System tuning");
        jButtonSystemTun.setFont(fontAll);
        jButtonSystemTun.setOpaque(true);
        jButtonSystemTun.setVerticalTextPosition(JLabel.BOTTOM);
        jButtonSystemTun.setHorizontalTextPosition(JLabel.CENTER);
        jButtonSystemTun.setBackground(ColorConstants.SYSTEM_COLOR);
        jButtonHadoop
                .setBounds(LayoutConstants.EMB_BUT_X, LayoutConstants.EMB_BUT_Y, LayoutConstants.TWO_HUNDRED_EIGHTEEN,
                        LayoutConstants.HUNDRED_FIFTY_FIVE);
        jButtonHadoop.setText("Embrace scene");
        jButtonHadoop.setFont(fontAll);
        jButtonHadoop.setOpaque(true);
        jButtonHadoop.setVerticalTextPosition(JLabel.BOTTOM);
        jButtonHadoop.setHorizontalTextPosition(JLabel.CENTER);
        jButtonHadoop.setBackground(ColorConstants.SYSTEM_COLOR);
        optionJPanelContent.add(jLabelRealTimeTaskZhu);
        optionJPanelContent.add(jLabelRealTimeTaskCi);
        optionJPanelContent.add(jButtonApplyTun);
        optionJPanelContent.add(jButtonSystemTun);
    }

    public JLabel getJLabelClose() {
        return jLabelClose;
    }

    public JPanel getJPanelTabLabel() {
        return jPanelTabLabel;
    }

    public JLabel getJLabelTaskTun() {
        return jLabelTaskTun;
    }

    public JLabel getChooseButton() {
        return chooseButton;
    }

    public JLabel getJButtonApplyTun() {
        return jButtonApplyTun;
    }

    public JLabel getJButtonSystemTun() {
        return jButtonSystemTun;
    }

    public JLabel getJButtonHadoop() {
        return jButtonHadoop;
    }

    public JPanel getOptionJPanel() {
        return optionJPanel;
    }

    public JPanel getOptionJPanelContent() {
        return optionJPanelContent;
    }

    public JLabel getJLabelSetting() {
        return jLabelSetting;
    }

    public JPanel getJPanelRight() {
        return jPanelRight;
    }

    public JPanel getJPanelLeft() {
        return jPanelLeft;
    }

    public JButton getJButtonAdd() {
        return jButtonAdd;
    }

    public void setjButtonAdd(JButton jButtonAdd) {
        this.jButtonAdd = jButtonAdd;
    }

    public JLabel getjLabelIcon() {
        return jLabelIcon;
    }

    public JLabel getTraceButton() {
        return traceButton;
    }

    public void setLocalSessionId(Long localSessionId) {
        this.localSessionId = localSessionId;
    }

    public Long getLocalSessionId() {
        return localSessionId;
    }
}
