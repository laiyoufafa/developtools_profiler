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

package ohos.devtools.views.layout.utils;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.hiperf.HiperfParse;
import ohos.devtools.services.hiperf.ParsePerf;
import ohos.devtools.services.hiperf.PerfDAO;
import ohos.devtools.views.applicationtrace.AppTracePanel;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.common.customcomp.CustomProgressBar;
import ohos.devtools.views.layout.SystemPanel;
import ohos.devtools.views.layout.TaskPanel;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.layout.dialog.ImportFileChooserDialog;
import ohos.devtools.views.layout.dialog.SampleDialog;
import ohos.devtools.views.perftrace.PerfTracePanel;
import ohos.devtools.views.trace.component.AnalystPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.TRACE_STREAMER_LOAD;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.common.Constant.DEVICE_STAT_OFFLINE;

/**
 * OpenFileDialogUtil
 *
 * @since : 2021/10/25
 */
public class OpenFileDialogUtils {
    private static OpenFileDialogUtils instance = new OpenFileDialogUtils();
    private static TaskPanel taskPanel;
    private static CustomProgressBar progressBar;
    private static final Logger LOGGER = LogManager.getLogger(OpenFileDialogUtils.class);
    private static final String HI_PERF_TYPE_VALUE = "HIPERF";
    private static final String BYTRACE_TYPE_VALUE = "TRACE";
    private static final String BYTRACE_TYPE_FLAG_VALUE = "tracer";
    private static final String HTRACE_TYPE_VALUE = "OHOSPROF";
    private static final String TRACE_FILE = "TraceFileInfo";
    private Boolean traceAnalysisResult = true;
    private String path = "";

    private OpenFileDialogUtils() {
    }

    /**
     * Get the current OpenFileDialogUtils object
     *
     * @return Db
     */
    public static OpenFileDialogUtils getInstance() {
        if (instance == null) {
            instance = new OpenFileDialogUtils();
        }
        return instance;
    }

    /**
     * showFileOpenDialog
     *
     * @param tabItem tabItem
     * @param contentPanel contentPanel
     */
    public void showFileOpenDialog(JBPanel tabItem, TaskPanel contentPanel) {
        ImportFileChooserDialog fileChooserDialogWrapper = new ImportFileChooserDialog("Open File");
        boolean showAndGet = fileChooserDialogWrapper.showAndGet();
        if (showAndGet) {
            taskPanel = contentPanel;
            progressBar = new CustomProgressBar(tabItem);
            tabItem.remove(taskPanel.getBtnPanel());
            // JProgressBar Listener
            tabItem.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    super.componentResized(event);
                    progressBar.setBounds(LayoutConstants.TEN, tabItem.getHeight() - LayoutConstants.FORTY,
                        tabItem.getWidth() - LayoutConstants.TWENTY, LayoutConstants.THIRTY);
                }
            });
            BufferedReader bufferedReader = null;
            try {
                String fileName = fileChooserDialogWrapper.getImportFilePath();
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "GBK"));
                String readLineStr = null;
                while ((readLineStr = bufferedReader.readLine()) != null) {
                    break;
                }
                File selectedFile = new File(fileName);
                if (readLineStr == null) {
                    showWarningDialog(tabItem);
                    return;
                }
                loadOfflineFiles(tabItem, readLineStr, selectedFile);
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                LOGGER.error(unsupportedEncodingException.getMessage());
            } catch (FileNotFoundException fileNotFoundException) {
                LOGGER.error(fileNotFoundException.getMessage());
            } catch (IOException ioException) {
                LOGGER.error(ioException.getMessage());
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ioException) {
                        LOGGER.error(ioException.getMessage());
                    }
                }
            }
        } else {
            tabItem.requestFocus(false);
        }
    }

    /**
     * load offline files
     *
     * @param tabItem tabItem
     * @param readLineStr readLineStr
     * @param selectedFile selectedFile
     */
    private void loadOfflineFiles(JBPanel tabItem, String readLineStr, File selectedFile) {
        if (readLineStr.contains(HI_PERF_TYPE_VALUE)) {
            loadPerf(selectedFile, taskPanel.getTabContainer(), readLineStr);
        } else if (readLineStr.contains(BYTRACE_TYPE_VALUE) || readLineStr.contains(HTRACE_TYPE_VALUE)
                || readLineStr.contains(BYTRACE_TYPE_FLAG_VALUE)) {
            loadOfflineFile(tabItem, taskPanel, selectedFile, taskPanel.getTabContainer());
        } else if (readLineStr.contains(TRACE_FILE)) {
            loadOfflineFile(tabItem, taskPanel, selectedFile, taskPanel.getTabContainer());
        } else if (readLineStr.contains("malloc") || readLineStr.contains("free")) {
            loadOfflineFileNativeHook(selectedFile, taskPanel, taskPanel.getTabContainer());
        } else {
            showWarningDialog(tabItem);
        }
    }

    /**
     * loadOfflineFileNativeHook
     *
     * @param selectedFile selectedFile
     * @param taskPanel taskPanel
     * @param optionJPanel optionJPanel
     */
    public void loadOfflineFileNativeHook(File selectedFile, TaskPanel taskPanel, JBPanel optionJPanel) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String time = formatter.format(currentTime);
        String absolutePath = null;
        try {
            absolutePath = selectedFile.getCanonicalPath();
        } catch (IOException ioException) {
            LOGGER.error(ioException.getMessage());
        }
        long sessionId = CommonUtil.getLocalSessionId();
        CustomJLabel hosJLabel = new CustomJLabel();
        hosJLabel.setProcessName(time + ";" + selectedFile.getName() + ";" + "NativeHook");
        hosJLabel.setDeviceName("");
        hosJLabel.setOnline(false);
        hosJLabel.setConnectType(DEVICE_STAT_OFFLINE);
        hosJLabel.setSessionId(sessionId);
        hosJLabel.setFileType("nativehook");
        hosJLabel.setMessage(absolutePath);
        ArrayList<CustomJLabel> hosJLabels = new ArrayList<CustomJLabel>();
        hosJLabels.add(hosJLabel);
        optionJPanel.removeAll();
        TaskScenePanelChart taskScenePanelChart = new TaskScenePanelChart(taskPanel, hosJLabels);
        taskPanel.getTabContainer().setBackground(JBColor.background());
        taskPanel.setLocalSessionId(sessionId);
        optionJPanel.add(taskScenePanelChart);
    }

    /**
     * loadOfflineFile
     *
     * @param optionJPanelContent optionJPanelContent
     * @param taskPanel taskPanel
     * @param selectedFile selectedFile
     * @param optionJPanel optionJPanel
     */
    private void loadOfflineFile(JBPanel optionJPanelContent, TaskPanel taskPanel, File selectedFile,
        JBPanel optionJPanel) {
        SwingWorker<Optional<DeviceProcessInfo>, Object> task = new SwingWorker<Optional<DeviceProcessInfo>, Object>() {
            /**
             * doInBackground
             *
             * @return Optional<DeviceProcessInfo>
             */
            @Override
            protected Optional<DeviceProcessInfo> doInBackground() {
                path = selectedFile.getPath();
                Optional<DeviceProcessInfo> deviceProcessInfo =
                    SessionManager.getInstance().localSessionDataFromFile(progressBar, selectedFile);
                return deviceProcessInfo;
            }

            /**
             * done
             */
            @Override
            protected void done() {
                try {
                    Optional<DeviceProcessInfo> deviceProcessInfo = get();
                    if (deviceProcessInfo != null && deviceProcessInfo.isPresent()) {
                        DeviceProcessInfo deviceInfo = deviceProcessInfo.get();
                        CustomJLabel hosJLabel = new CustomJLabel();
                        hosJLabel.setProcessName(deviceInfo.getProcessName());
                        hosJLabel.setSessionId(deviceInfo.getLocalSessionId());
                        hosJLabel.setDeviceName(deviceInfo.getDeviceName());
                        hosJLabel.setOnline(false);
                        hosJLabel.setFileType(UtConstant.FILE_TYPE_TRACE);
                        hosJLabel.setStartTime(deviceInfo.getStartTime());
                        hosJLabel.setEndTime(deviceInfo.getEndTime());
                        List<CustomJLabel> hosJLabels = new ArrayList<CustomJLabel>();
                        hosJLabels.add(hosJLabel);
                        optionJPanel.removeAll();
                        TaskScenePanelChart taskScenePanelChart = new TaskScenePanelChart(taskPanel, hosJLabels);
                        taskPanel.getTabContainer().setBackground(JBColor.background());
                        taskPanel.setLocalSessionId(deviceInfo.getLocalSessionId());
                        optionJPanel.add(taskScenePanelChart);
                    } else {
                        ChooseTraceTypeDialogWrapper chooseTraceTypeDialogWrapper =
                            new ChooseTraceTypeDialogWrapper(optionJPanelContent, selectedFile, optionJPanel);
                        boolean result = chooseTraceTypeDialogWrapper.showAndGet();
                        if (!result) {
                            optionJPanelContent.remove(progressBar);
                            optionJPanelContent.add(taskPanel.getBtnPanel());
                            optionJPanelContent.repaint();
                        }
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        };
        task.execute();
    }

    /**
     * loadHiPerf
     *  @param selectedFile selectedFile
     * @param optionJPanel optionJPanel
     * @param readLineStr readLineStr
     */
    public void loadPerf(File selectedFile, JBPanel optionJPanel, String readLineStr) {
        SwingWorker<String, Object> task = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                progressBar.setValue(30);
                ParsePerf traceParser = new HiperfParse();
                try {
                    traceParser.parseFile(selectedFile);
                    PerfDAO.getInstance().createTable(null);
                    traceParser.insertSample();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                progressBar.setValue(LayoutConstants.HUNDRED);
                return SessionManager.getInstance().tempPath() + "perf.db";
            }

            @Override
            protected void done() {
                try {
                    String dbPath = get();
                    if (dbPath != null) {
                        optionJPanel.removeAll();
                        taskPanel.getTabContainer().setBackground(JBColor.background());
                        PerfTracePanel component = new PerfTracePanel();
                        component.load(dbPath, null, true);
                        progressBar.setValue(LayoutConstants.HUNDRED);
                        optionJPanel.add(component);
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        };
        task.execute();
    }

    /**
     * loadTrace
     *
     * @param optionJPanelContent optionJPanelContent
     * @param selectedFile selectedFile
     * @param optionJPanel optionJPanel
     * @param isAppTrace isAppTrace
     */
    public void loadTrace(JBPanel optionJPanelContent, File selectedFile, JBPanel optionJPanel, boolean isAppTrace) {
        SwingWorker<String, Object> task = new SwingWorker<String, Object>() {
            @Override
            protected String doInBackground() throws Exception {
                traceAnalysisResult = true;
                String logPath = TraceStreamerUtils.getInstance().getLogPath();
                File logFile = new File(logPath);
                if (logFile.exists()) {
                    logFile.delete();
                }
                String baseDir = TraceStreamerUtils.getInstance().getBaseDir();
                String dbPath = TraceStreamerUtils.getInstance().getDbPath();
                HdcWrapper.getInstance().getHdcStringResult(conversionCommand(TRACE_STREAMER_LOAD,
                    baseDir + TraceStreamerUtils.getInstance().getTraceStreamerApp(), selectedFile.getPath(), dbPath));
                randomFile(logFile);
                progressBar.setValue(LayoutConstants.FIFTY);
                return dbPath;
            }

            /**
             * done
             */
            @Override
            protected void done() {
                if (!traceAnalysisResult) {
                    optionJPanelContent.remove(progressBar);
                    optionJPanelContent.add(taskPanel.getBtnPanel());
                    optionJPanelContent.repaint();
                    new SampleDialog("Warring",
                        "The system cannot parse the file properly. Please import the legal file.").show();
                }
                try {
                    if (traceAnalysisResult) {
                        String dbPath = get();
                        addOptionJPanel(dbPath, optionJPanel, isAppTrace);
                    }
                } catch (InterruptedException interruptedException) {
                    LOGGER.error(interruptedException.getMessage());
                } catch (ExecutionException executionException) {
                    LOGGER.error(executionException.getMessage());
                }
            }
        };
        task.execute();
    }

    /**
     * addOptionJPanel
     *
     * @param dbPath dbPath
     * @param optionJPanel optionJPanel
     * @param isAppTrace isAppTrace
     */
    private void addOptionJPanel(String dbPath, JBPanel optionJPanel, boolean isAppTrace) {
        optionJPanel.removeAll();
        if (isAppTrace) {
            AppTracePanel component = new AppTracePanel();
            component.load(dbPath, null, null, true);
            taskPanel.getTabContainer().setBackground(JBColor.background());
            progressBar.setValue(LayoutConstants.HUNDRED);
            optionJPanel.add(component);
        } else {
            AnalystPanel component = new AnalystPanel();
            component.load(dbPath, true);
            taskPanel.getTabContainer().setBackground(JBColor.background());
            progressBar.setValue(LayoutConstants.HUNDRED);
            SystemPanel systemTuningPanel = new SystemPanel(optionJPanel, component);
            optionJPanel.add(systemTuningPanel, BorderLayout.NORTH);
            optionJPanel.add(component, BorderLayout.CENTER);
        }
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

    private void showWarningDialog(JBPanel tabItem) {
        tabItem.remove(progressBar);
        tabItem.add(taskPanel.getBtnPanel());
        tabItem.repaint();
        new SampleDialog("prompt", "Please select the right file!").show();
    }

    /**
     * ChooseTraceTypeDialogWrapper
     */
    private class ChooseTraceTypeDialogWrapper extends DialogWrapper {
        private static final String DIALOG_TITLE = "Please Choose the type";
        private static final String SYSTEM_TYPE = "1.System Trace";
        private static final String APPLICATION_TYPE = "2.Application Trace";
        private JBPanel optionJPanelContent;
        private JBPanel optionJPanel;
        private File selectedFile;
        private JBList<String> typeList;
        private DefaultListModel<String> listModel;

        /**
         * ChooseTraceTypeDialogWrapper
         *
         * @param optionJPanelContent optionJPanelContent
         * @param selectedFile selectedFile
         * @param optionJPanel optionJPanel
         */
        ChooseTraceTypeDialogWrapper(JBPanel optionJPanelContent, File selectedFile, JBPanel optionJPanel) {
            super(true);
            this.optionJPanelContent = optionJPanelContent;
            this.selectedFile = selectedFile;
            this.optionJPanel = optionJPanel;
            init();
            setTitle(DIALOG_TITLE);
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            listModel = new DefaultListModel<>();
            listModel.add(0, SYSTEM_TYPE);
            listModel.add(1, APPLICATION_TYPE);
            typeList = new JBList<>(listModel);
            ColoredListCellRenderer<String> listCellRenderer = new ColoredListCellRenderer<String>() {
                @Override
                protected void customizeCellRenderer(@NotNull JList<? extends String> jList, String value, int index,
                    boolean selected, boolean hasFocus) {
                    append(value);
                }
            };
            typeList.setCellRenderer(listCellRenderer);
            typeList.setSelectedIndex(0);
            return typeList;
        }

        @Nullable
        @Override
        protected ValidationInfo doValidate() {
            String value = typeList.getSelectedValue();
            if (value != null) {
                if (value.equals(SYSTEM_TYPE)) {
                    loadTrace(optionJPanelContent, selectedFile, optionJPanel, false);
                }
                if (value.equals(APPLICATION_TYPE)) {
                    loadTrace(optionJPanelContent, selectedFile, optionJPanel, true);
                }
            }
            return null;
        }
    }
}
