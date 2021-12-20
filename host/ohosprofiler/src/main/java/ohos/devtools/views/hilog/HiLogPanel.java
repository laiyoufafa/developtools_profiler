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

package ohos.devtools.views.hilog;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBLayeredPane;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.databases.datatable.LogTable;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomTextField;
import ohos.devtools.views.layout.WelcomePanel;
import ohos.devtools.views.layout.utils.EventTrackUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_HILOG;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_HILOG_C;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_HILOG_R;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_SHELL_HILOG;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_HILOG_R;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_SHELL_HI_LOG;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * HiLog Panel
 *
 * @since 2021/09/22 12:25
 */
public class HiLogPanel extends JBLayeredPane implements MouseListener {
    private static final Logger LOGGER = LogManager.getLogger(HiLogPanel.class);

    /**
     * A tag that can only appear once for a hilog panel
     */
    private static boolean isOpen;
    private static final String TAB_STR = "HiLog";
    private static final String TAB_CLOSE_STR = "x";
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String SELECT_DEVICE = "Please select device";
    private static final String LOG_PANEL_DEVICE_REFRESH = "HiLogPanel device refresh";

    /**
     * hilog tab width
     */
    private static final int HILOG_TAB_WIDTH = 60;

    /**
     * hilog tab width
     */
    private static final int HILOG_JPANEL_WIDTH = 80;

    /**
     * button margin top
     */
    private static final int BUTTON_MARGIN_TOP = 11;

    /**
     * wrap margin right
     */
    private static final int WRAP_MARGIN_RIGHT = 95;

    /**
     * end margin right
     */
    private static final int END_MARGIN_RIGHT = 70;

    /**
     * clear margin right
     */
    private static final int CLEAR_MARGIN_RIGHT = 45;

    /**
     * btn width or height
     */
    private static final int BTN_WIDTH_HEIGHT = 20;

    /**
     * font size
     */
    private static final int FONT_SIZE = 14;

    /**
     * log rows
     */
    private static final int LOG_ROWS = 26;

    /**
     * log columns
     */
    private static final int LOG_COLUMNS = 130;

    /**
     * log y
     */
    private static final int LOG_Y = 40;

    /**
     * log type x
     */
    private static final int LOG_TYPE_X = 470;

    /**
     * log margin top
     */
    private static final int LOG_MARGIN_TOP = 100;

    /**
     * log margin right
     */
    private static final int LOG_MARGIN_RIGHT = 30;

    /**
     * close process wait time
     */
    private static final int WAIT_TIME = 800;

    /**
     * insert batch num
     */
    private static final int INSERT_BATCH_NUM = 200;

    /**
     * log interval
     */
    private static final int LOG_INTERVAL = 30;
    private static final int SPLIT_LENGTH = 6;
    private static final int DATE_INDEX = 1;
    private static final int TIME_INDEX = 2;
    private static final int PID_INDEX = 3;
    private static final int TID_INDEX = 4;
    private static final int LOG_TYPE_INDEX = 5;

    /**
     * period
     */
    private static final long PERIOD = 1000L;

    private final Queue<HiLogBean> logDataQueue;
    private boolean rollBottomFlag = true;
    private boolean deviceChangedFlag;
    private JBPanel parentPanel;
    private JBPanel welcomePanel;
    private JBPanel tabPanel;
    private JBPanel tabLeftPanel;
    private JBPanel tabRightPanel;
    private JBLabel tabText;
    private JBLabel tabCloseBtn;
    private JBPanel tabContainer;
    private JComboBox<String> deviceBox;
    private JComboBox<String> logTypeBox;
    private JBLabel wrapBtn;
    private JBLabel endBtn;
    private JBLabel clearLogBtn;
    private StringBuilder wholeBuilder;
    private Vector<String> oldDevice;
    private Vector<String> deviceItems;
    private JTextArea logTextArea;
    private JScrollPane logScroll;
    private Process process;
    private LogTable logTable;

    /**
     * Secondary interface tab container
     */
    private JPanel optionJPanel;

    /**
     * Secondary interface tab content container
     */
    private JPanel optionJPanelContent;

    /**
     * device name drop down box
     */
    private CustomTextField searchTextField;

    /**
     * HiLog Panel
     *
     * @param containerPanel JBPanel
     * @param welcomePanel WelcomePanel
     */
    public HiLogPanel(JBPanel containerPanel, WelcomePanel welcomePanel) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("HiLogPanel");
        }
        EventTrackUtils.getInstance().trackHiLogPage();
        parentPanel = containerPanel;
        this.welcomePanel = welcomePanel;
        logDataQueue = new LinkedBlockingQueue<>();
        initComponents();
        // 设置属性
        setAttributes(containerPanel);
        initTab(containerPanel);
        addEventListener();
        pollingGetDevices();
        isOpen = true;
    }

    private void setAttributes(JBPanel containerPanel) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setAttributes");
        }
        optionJPanelContent.setLayout(null);
        optionJPanelContent.setOpaque(true);
        optionJPanelContent.setBackground(JBColor.background().darker());
        deviceBox.setBounds(LayoutConstants.EIGHT_NUM, LayoutConstants.NUM_2, LayoutConstants.HEIGHT_PRESSE,
            LayoutConstants.DEVICE_PRO_X);
        deviceBox.setName(UtConstant.UT_HILOG_PANEL_DEVICE);
        logTypeBox.setBounds(LOG_TYPE_X, LayoutConstants.NUM_2, LayoutConstants.DEVICE_ADD_WIDTH,
            LayoutConstants.DEVICE_PRO_X);
        searchTextField.setBounds(LayoutConstants.APP_LABEL_Y2, LayoutConstants.NUM_4, LayoutConstants.HEIGHT_PRESSE,
            LayoutConstants.TASK_SCENE_Y);
        wrapBtn.setIcon(AllIcons.Actions.ToggleSoftWrap);
        wrapBtn.setToolTipText("Soft-wrap");
        int width = containerPanel.getWidth();
        wrapBtn.setBounds(width - WRAP_MARGIN_RIGHT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
        endBtn.setIcon(AllIcons.RunConfigurations.Scroll_down);
        endBtn.setToolTipText("Scroll to the end");
        endBtn.setBounds(width - END_MARGIN_RIGHT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
        clearLogBtn.setIcon(AllIcons.Actions.GC);
        clearLogBtn.setToolTipText("Clear Logcat");
        clearLogBtn.setBounds(width - CLEAR_MARGIN_RIGHT, BUTTON_MARGIN_TOP, BTN_WIDTH_HEIGHT, BTN_WIDTH_HEIGHT);
    }

    /**
     * initComponents
     */
    private void initComponents() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initComponents");
        }
        // init tabPanel
        tabPanel = new JBPanel();
        tabLeftPanel = new JBPanel();
        tabLeftPanel.setName(UtConstant.UT_HILOG_PANEL_TITLE_LEFT);
        tabRightPanel = new JBPanel();
        tabRightPanel.setName(UtConstant.UT_HILOG_PANEL_CLOSE_RIGHT);
        tabText = new JBLabel(TAB_STR);
        tabCloseBtn = new JBLabel(TAB_CLOSE_STR);
        // init tab
        tabContainer = new JBPanel(new BorderLayout());
        deviceBox = new JComboBox<String>();
        logTypeBox = new JComboBox<String>();
        logTypeBox.setName(UtConstant.UT_HILOG_PANEL_LOG_TYPE);
        wrapBtn = new JBLabel();
        endBtn = new JBLabel();
        clearLogBtn = new JBLabel();
        wholeBuilder = new StringBuilder();
        oldDevice = new Vector<>();
        deviceItems = new Vector<>();
        optionJPanel = new JPanel(new BorderLayout());
        optionJPanelContent = new JPanel();
        searchTextField = new CustomTextField("press");
        logTable = new LogTable();
        deviceItems.add("Verbose");
        deviceItems.add("Debug");
        deviceItems.add("Info");
        deviceItems.add("Warn");
        deviceItems.add("Error");
        deviceItems.add("Fatal");
        deviceItems.add("Assert");
        logTypeBox.setModel(new DefaultComboBoxModel(deviceItems));
    }

    /**
     * setTabAttributes
     *
     * @param containerPanel containerPanel
     */
    private void initTab(JBPanel containerPanel) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initTab");
        }
        tabPanel.setOpaque(false);
        tabPanel.setPreferredSize(new Dimension(HILOG_JPANEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
        tabLeftPanel.setOpaque(false);
        tabLeftPanel.setLayout(null);
        tabLeftPanel.setPreferredSize(new Dimension(HILOG_TAB_WIDTH, LayoutConstants.JP_LEFT_HEIGHT));
        tabRightPanel.setOpaque(false);
        tabRightPanel.setLayout(new GridLayout());
        tabRightPanel.setPreferredSize(new Dimension(LayoutConstants.JP_RIGHT_WIDTH, LayoutConstants.JP_RIGHT_HEIGHT));
        tabPanel.setLayout(new BorderLayout());
        tabPanel.add(tabLeftPanel, BorderLayout.WEST);
        tabPanel.add(tabRightPanel, BorderLayout.CENTER);
        tabText.setBounds(0, 0, HILOG_TAB_WIDTH, LayoutConstants.JP_SET_HEIGHT);
        tabLeftPanel.add(tabText);
        tabRightPanel.add(tabCloseBtn);
        tabCloseBtn.setHorizontalAlignment(JBLabel.RIGHT);
        tabCloseBtn.setName(UtConstant.UT_HILOG_TAB_CLOSE);
        // Add tab content container to tab container
        optionJPanel.add(optionJPanelContent);
        addComponent();
        tabContainer.add(optionJPanel);
        Constant.jtasksTab.addTab("HilogTab", tabContainer);
        Constant.jtasksTab.setTabComponentAt(Constant.jtasksTab.indexOfComponent(tabContainer), tabPanel);
        containerPanel.setLayout(new BorderLayout());
        Constant.jtasksTab.setBounds(0, 0, containerPanel.getWidth(), containerPanel.getHeight());
        this.add(Constant.jtasksTab);
        containerPanel.add(this);
        double result = Constant.jtasksTab.getTabCount() * LayoutConstants.NUMBER_X;
        if (result > containerPanel.getWidth()) {
            for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                Object tabObj = Constant.jtasksTab.getTabComponentAt(index);
                if (tabObj instanceof JBPanel) {
                    ((JBPanel) tabObj).getComponents()[0].setPreferredSize(new Dimension(
                        (((containerPanel.getWidth() - LayoutConstants.MEMORY_WIDTH) / Constant.jtasksTab.getTabCount())
                            - LayoutConstants.TASK_DEC_NUM) - LayoutConstants.JAVA_HEIGHT,
                        LayoutConstants.JAVA_HEIGHT));
                }
                Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(new Dimension(
                    ((containerPanel.getWidth() - LayoutConstants.MEMORY_WIDTH) / Constant.jtasksTab.getTabCount())
                        - LayoutConstants.TASK_DEC_NUM, LayoutConstants.JAVA_HEIGHT));
            }
        }
    }

    private void addComponent() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addComponent");
        }
        optionJPanelContent.add(deviceBox);
        optionJPanelContent.add(logTypeBox);
        optionJPanelContent.add(searchTextField);
        optionJPanelContent.add(wrapBtn);
        optionJPanelContent.add(endBtn);
        optionJPanelContent.add(clearLogBtn);
        logTextArea = new JTextArea("", LOG_ROWS, LOG_COLUMNS);
        logTextArea.setMargin(new Insets(30, 40, 0, 0));
        logTextArea.setBackground(JBColor.background().darker());
        logTextArea.setFont(new Font("PingFang SC", Font.PLAIN, FONT_SIZE));
        logTextArea.setEditable(false);
        // Place the text field in a scrolling window
        logScroll = new JScrollPane(logTextArea);
        logScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logScroll.setBounds(LayoutConstants.EIGHT_NUM, LOG_Y, parentPanel.getWidth() - LOG_MARGIN_RIGHT,
            parentPanel.getHeight() - LOG_MARGIN_TOP);
        // Add JScrollPane to JPanel container
        optionJPanelContent.add(logScroll);
    }

    /**
     * addEventListener
     */
    private void addEventListener() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addEventListener");
        }
        buttonAddEventListener();
        boxAddListener();
        logScrollAddListener();
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * insertUpdate
             *
             * @param event event
             */
            @Override
            public void insertUpdate(DocumentEvent event) {
                filterLogs();
            }

            /**
             * removeUpdate
             *
             * @param event event
             */
            @Override
            public void removeUpdate(DocumentEvent event) {
                filterLogs();
            }

            /**
             * changedUpdate
             *
             * @param event event
             */
            @Override
            public void changedUpdate(DocumentEvent event) {
            }
        });
    }

    private void logScrollAddListener() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("logScrollAddListener");
        }
        BoundedRangeModel model = logScroll.getVerticalScrollBar().getModel();
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                if (model.getExtent() + model.getValue() == model.getMaximum()) {
                    rollBottomFlag = true;
                } else {
                    rollBottomFlag = false;
                }
            }
        });
    }

    private void boxAddListener() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("boxAddListener");
        }
        ItemListener logTypeListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (arg0.getStateChange() == ItemEvent.SELECTED) {
                    String selectedItem = arg0.getItem().toString();
                    if (deviceItems.contains(selectedItem)) {
                        filterLogs();
                    } else {
                        if (!selectedItem.equals(SELECT_DEVICE)) {
                            try {
                                Thread.sleep(WAIT_TIME);
                            } catch (InterruptedException ioException) {
                                if (ProfilerLogManager.isErrorEnabled()) {
                                    LOGGER.error("interrupted: ", ioException);
                                }
                            }
                            deviceChangedFlag = false;
                            getHilog();
                        }
                    }
                }
                // When switching devices, close the last selected
                if (arg0.getStateChange() == ItemEvent.DESELECTED) {
                    String selectedItem = arg0.getItem().toString();
                    if (!deviceItems.contains(selectedItem) && !selectedItem.equals(SELECT_DEVICE)) {
                        DeviceIPPortInfo desSelectDevice = getSelectDevice(selectedItem);
                        deviceChangedFlag = true;
                        process.destroy();
                        QuartzManager.getInstance().deleteExecutor(desSelectDevice.getDeviceName());
                    }
                }
            }
        };
        deviceBox.addItemListener(logTypeListener);
        logTypeBox.addItemListener(logTypeListener);
    }

    private void buttonAddEventListener() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buttonAddEventListener");
        }
        tabCloseBtn.addMouseListener(this);
        endBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                logTextArea.setFocusable(true);
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                JScrollBar verticalScrollBar = logScroll.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                endBtn.setBorder(BorderFactory.createTitledBorder(""));
                endBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                endBtn.setBorder(null);
                endBtn.setCursor(Cursor.getDefaultCursor());
            }
        });

        wrapBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                logTextArea.setLineWrap(!logTextArea.getLineWrap());
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                wrapBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                wrapBtn.setBorder(BorderFactory.createTitledBorder(""));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                wrapBtn.setCursor(Cursor.getDefaultCursor());
                wrapBtn.setBorder(null);
            }
        });

        clearButtonAddEventListener();
    }

    private void clearButtonAddEventListener() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("clearButtonAddEventListener");
        }
        clearLogBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() {
                        ArrayList clearCmd = getClearCmd();
                        if (clearCmd.isEmpty()) {
                            return "";
                        }
                        HdcWrapper.getInstance().execCmdBy(clearCmd);
                        return SUCCESS;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (InterruptedException | ExecutionException inException) {
                            if (ProfilerLogManager.isErrorEnabled()) {
                                LOGGER.error("interrupted: ", inException);
                            }
                        }
                        logTextArea.setText("");
                        wholeBuilder.delete(0, wholeBuilder.length());
                    }
                }.execute();
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                clearLogBtn.setBorder(BorderFactory.createTitledBorder(""));
                clearLogBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                clearLogBtn.setBorder(null);
                clearLogBtn.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private ArrayList getClearCmd() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getClearCmd");
        }
        DeviceIPPortInfo selectDevice = getSelectDevice(deviceBox.getSelectedItem() + "");
        ArrayList clearLogCmd = new ArrayList<String>();
        if (selectDevice != null) {
            if (IS_SUPPORT_NEW_HDC && selectDevice.getDeviceType() == LEAN_HOS_DEVICE) {
                clearLogCmd = conversionCommand(HDC_STD_HILOG_R, selectDevice.getDeviceID());
            } else if (selectDevice.getDeviceType() == LEAN_HOS_DEVICE) {
                clearLogCmd = conversionCommand(HDC_HILOG_R, selectDevice.getDeviceID());
            } else {
                clearLogCmd = conversionCommand(HDC_HILOG_C, selectDevice.getDeviceID());
            }
        }
        return clearLogCmd;
    }

    private void getHilog() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHilog");
        }
        DeviceIPPortInfo selectDevice = getSelectDevice(deviceBox.getSelectedItem() + "");
        if (selectDevice == null) {
            return;
        }
        wholeBuilder.delete(0, wholeBuilder.length());
        logTextArea.setText("");
        QuartzManager.getInstance().addExecutor(selectDevice.getDeviceName(), new Runnable() {
            @Override
            public void run() {
                ArrayList clearLogCmd = new ArrayList<String>();
                ArrayList hdcCmd = new ArrayList<String>();
                if (IS_SUPPORT_NEW_HDC && selectDevice.getDeviceType() == LEAN_HOS_DEVICE) {
                    clearLogCmd = conversionCommand(HDC_STD_HILOG_R, selectDevice.getDeviceID());
                    hdcCmd = conversionCommand(HDC_STD_SHELL_HI_LOG, selectDevice.getDeviceID());
                } else if (selectDevice.getDeviceType() == LEAN_HOS_DEVICE) {
                    clearLogCmd = conversionCommand(HDC_HILOG_R, selectDevice.getDeviceID());
                    hdcCmd = conversionCommand(HDC_SHELL_HILOG, selectDevice.getDeviceID());
                } else {
                    clearLogCmd = conversionCommand(HDC_HILOG_C, selectDevice.getDeviceID());
                    hdcCmd = conversionCommand(HDC_HILOG, selectDevice.getDeviceID());
                }
                doLineFilter(clearLogCmd, hdcCmd);
            }
        });
        QuartzManager.getInstance().startExecutor(selectDevice.getDeviceName(), 0, PERIOD);
        ExecutorService service = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        service.submit(() -> {
            while (true) {
                if (!isOpen) {
                    logTable.insertLogInfo(logDataQueue);
                    break;
                }
                if (logDataQueue.size() > INSERT_BATCH_NUM) {
                    logTable.insertLogInfo(logDataQueue);
                }
            }
        });
        service.shutdown();
    }

    /**
     * doLineFilter
     *
     * @param clearLogCmd clearLogCmd
     * @param hdcCmd hdcCmd
     */
    private void doLineFilter(ArrayList clearLogCmd, ArrayList hdcCmd) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("doLineFilter");
        }
        InputStream inputStream = null;
        InputStream errorStream = null;
        BufferedReader brInputStream = null;
        BufferedReader brErrorStream = null;
        try {
            HdcWrapper.getInstance().execCmdBy(clearLogCmd);
            process = new ProcessBuilder(hdcCmd).start();
            // Error command result output stream or Get command result output stream
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();
            brInputStream = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            brErrorStream = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                try {
                    Thread.sleep(LOG_INTERVAL);
                } catch (InterruptedException ioException) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("interrupted: " + ioException.getMessage());
                    }
                }
                if (line.contains("error: device") || line.contains("not found") || deviceChangedFlag || !isOpen) {
                    break;
                }
                wholeBuilder.append(line).append(System.lineSeparator());
                addLogDataToQueue(line);
                handleExceedMaxRow();
                String selectedItem = Objects.requireNonNull(logTypeBox.getSelectedItem()).toString();
                String selectType = selectedItem.substring(0, 1);
                HiLogFilter.getInstance().lineFilter(line, searchTextField.getText(), selectType, logTextArea);
                if (rollBottomFlag) {
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }
            }
        } catch (IOException ioException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("ioException: " + ioException.getMessage());
            }
        } finally {
            closeIOStream(brErrorStream, null);
            closeIOStream(brInputStream, null);
            closeIOStream(null, errorStream);
            closeIOStream(null, inputStream);
        }
    }

    private void addLogDataToQueue(String line) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addLogDataToQueue");
        }
        boolean isErrorLog = HiLogFilter.getInstance().isErrorLog(line);
        HiLogBean hiLogBean = new HiLogBean();
        DeviceIPPortInfo selectDevice = getSelectDevice(deviceBox.getSelectedItem() + "");
        if (isErrorLog) {
            hiLogBean.setMessage(line);
        } else {
            String[] lines = line.split(" ");
            int index = 1;
            if (lines.length >= SPLIT_LENGTH) {
                StringBuilder builder = new StringBuilder();
                for (String str : lines) {
                    if (StringUtils.isEmpty(str) && index < SPLIT_LENGTH) {
                        continue;
                    } else {
                        if (index == DATE_INDEX) {
                            hiLogBean.setDate(str);
                        } else if (index == TIME_INDEX) {
                            hiLogBean.setTime(str);
                        } else if (index == PID_INDEX) {
                            hiLogBean.setPid(str);
                        } else if (index == TID_INDEX) {
                            hiLogBean.setTid(str);
                        } else if (index == LOG_TYPE_INDEX) {
                            hiLogBean.setLogType(str);
                        } else {
                            if (index > lines.length - 1) {
                                break;
                            }
                            builder.append(lines[index]).append(" ");
                        }
                        index++;
                    }
                }
                hiLogBean.setMessage(builder.toString().trim());
            }
        }
        hiLogBean.setDeviceName(selectDevice.getDeviceName());
        logDataQueue.offer(hiLogBean);
    }

    /**
     * closeIOStream
     *
     * @param bufferedReader bufferedReader
     * @param inputStream inputStream
     */
    private void closeIOStream(BufferedReader bufferedReader, InputStream inputStream) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("closeIOStream");
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    private void handleExceedMaxRow() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleExceedMaxRow");
        }
        String[] lines = wholeBuilder.toString().split(System.lineSeparator());
        if (lines.length >= HiLogFilter.MAX_ROW_NUM) {
            wholeBuilder.delete(0, lines[0].length() + HiLogFilter.LINE_BREAK_NUM);
        }
    }

    private void filterLogs() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("filterLogs");
        }
        String selectedItem = Objects.requireNonNull(logTypeBox.getSelectedItem()).toString();
        String selectType = selectedItem.substring(0, 1);
        String searchValue = searchTextField.getText();
        HiLogFilter.getInstance().filterLog(logTextArea, selectType, searchValue, wholeBuilder);
    }

    private DeviceIPPortInfo getSelectDevice(String deviceName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getSelectDevice");
        }
        DeviceIPPortInfo selectDevice = null;
        List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getDeviceInfoList();
        for (DeviceIPPortInfo deviceInfo : deviceInfos) {
            if (deviceInfo.getDeviceName().equals(deviceName)) {
                selectDevice = deviceInfo;
            }
        }
        return selectDevice;
    }

    private void pollingGetDevices() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pollingGetDevices");
        }
        QuartzManager.getInstance().addExecutor(LOG_PANEL_DEVICE_REFRESH, new Runnable() {
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getDeviceInfoList();
                if (!deviceInfos.isEmpty()) {
                    Vector<String> items = new Vector<>();
                    items.add(SELECT_DEVICE);
                    deviceInfos.forEach(deviceInfo -> {
                        items.add(deviceInfo.getDeviceName());
                    });
                    if (!oldDevice.equals(items)) {
                        oldDevice = items;
                        deviceBox.setModel(new DefaultComboBoxModel(items));
                    }
                } else {
                    Vector<String> items = new Vector<>();
                    items.add("No connected devices");
                    deviceBox.setModel(new DefaultComboBoxModel(items));
                }
            }
        });
        QuartzManager.getInstance().startExecutor(LOG_PANEL_DEVICE_REFRESH, 0, PERIOD);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("mouseClicked");
        }
        String name = mouseEvent.getComponent().getName();
        if (name.equals(UtConstant.UT_HILOG_TAB_CLOSE)) {
            removeAll();
            Constant.jtasksTab.remove(Constant.jtasksTab.indexOfTabComponent(tabPanel));
            add(Constant.jtasksTab);
            parentPanel.add(this);
            parentPanel.repaint();
            Constant.jtasksTab.updateUI();
            if (Constant.jtasksTab.getTabCount() == 0) {
                removeAll();
                Constant.jtasksTab = null;
                welcomePanel.setVisible(true);
            }
            isOpen = false;
            QuartzManager.getInstance().deleteExecutor(LOG_PANEL_DEVICE_REFRESH);
            if (process != null) {
                process.destroy();
            }
            wholeBuilder.delete(0, wholeBuilder.length());
            DeviceIPPortInfo selectDevice = getSelectDevice(deviceBox.getSelectedItem() + "");
            if (selectDevice == null) {
                return;
            }
            QuartzManager.getInstance().deleteExecutor(selectDevice.getDeviceName());
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    public static boolean isIsOpen() {
        return isOpen;
    }

    public static void setIsOpen(boolean isOpen) {
        HiLogPanel.isOpen = isOpen;
    }
}
