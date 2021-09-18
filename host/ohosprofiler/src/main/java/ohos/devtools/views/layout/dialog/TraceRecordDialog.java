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

package ohos.devtools.views.layout.dialog;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.transport.grpc.SystemTraceHelper;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.SystemPanel;
import ohos.devtools.views.layout.TaskPanel;
import ohos.devtools.views.layout.utils.TraceStreamerUtils;
import ohos.devtools.views.trace.component.AnalystPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_TRACE_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_TRACE_FILE_INFO;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.TRACE_STREAMER_LOAD;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_TRACE_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_TRACE_FILE_INFO;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * TraceRecordDialog
 */
public class TraceRecordDialog {
    private static final Logger LOGGER = LogManager.getLogger(TraceRecordDialog.class);
    private int bytraceFileSize;
    private Timer timerLoading;
    private DeviceIPPortInfo deviceIPPortInfo;
    private String sessionId;
    private String fileExtension;
    private Boolean analysisState = false;
    private Boolean pullBytraceFileState = false;
    private Boolean chooseMode;
    private JBLabel statusAnalysisJLabel = new JBLabel("Status");
    private JBLabel durationAnalysisJLabel = new JBLabel("Duration");
    private JBLabel loadingJLabel = new JBLabel("Loading");
    private JBLabel loadingInitTimeJLabel = new JBLabel(" 00:00:00");
    private int hoursLoading = 0;
    private int minutesLoading = 0;
    private int secondsLoading = 0;
    private TaskPanel taskPanel = null;
    private JPanel countPanel = new JPanel(null);
    private JBLabel statusJLabel = new JBLabel("Status");
    private JBLabel durationJLabel = new JBLabel("Duration");
    private JBLabel recordingJLabel = new JBLabel("Recording");
    private JBLabel timeJLabel = new JBLabel();
    private JButton stopJButton = new JButton("Stop");
    private TraceLoadDialog traceLoadDialog;
    private AnalysisDialog analysisDialogEvent;

    /**
     * load
     *
     * @param taskPanel taskPanel
     * @param maxDurationParam maxDurationParam
     * @param sessionIdParam sessionIdParam
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param chooseMode chooseMode
     */
    public void load(TaskPanel taskPanel, int maxDurationParam, String sessionIdParam,
        DeviceIPPortInfo deviceIPPortInfoParam, boolean chooseMode) {
        this.taskPanel = taskPanel;
        this.sessionId = sessionIdParam;
        this.deviceIPPortInfo = deviceIPPortInfoParam;
        this.chooseMode = chooseMode;
        timeJLabel.setText(" 00:00:00");
        this.fileExtension = chooseMode ? ".bytrace" : ".htrace";
        traceLoadDialog = new TraceLoadDialog(countPanel, timeJLabel, maxDurationParam);
        stopJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                traceLoadDialog.getTimer().stop();
                traceLoadDialog.doCancelAction();
                new SystemTraceHelper().cancelActionDestroySession(deviceIPPortInfo, sessionId);
            }
        });
        countPanel.setPreferredSize(new Dimension(300, 150));
        statusJLabel.setForeground(Color.white);
        durationJLabel.setForeground(Color.white);
        recordingJLabel.setForeground(Color.white);
        timeJLabel.setForeground(Color.white);
        traceLoadDialog.setLableAttribute(statusJLabel, durationJLabel, recordingJLabel, timeJLabel, stopJButton);
        countPanel.add(statusJLabel);
        countPanel.add(durationJLabel);
        countPanel.add(recordingJLabel);
        countPanel.add(timeJLabel);
        countPanel.add(stopJButton);
        traceLoadDialog.show();
        int exitCode = traceLoadDialog.getExitCode();
        traceLoadDialog.getTimer().stop();
        if (exitCode == 0) {
            this.loading();
            if (chooseMode) {
                ExecutorService executorCancel = Executors.newSingleThreadExecutor();
                executorCancel.execute(new Runnable() {
                    @Override
                    public void run() {
                        new SystemTraceHelper().stopSession(deviceIPPortInfo, sessionId);
                    }
                });
            }
        } else {
            if (chooseMode) {
                new SystemTraceHelper().cancelActionDestroySession(deviceIPPortInfo, sessionId);
            }
        }
    }

    /**
     * loading
     */
    public void loading() {
        pullBytraceFileState = true;
        analysisDialogEvent = new AnalysisDialog(deviceIPPortInfo, countPanel);
        timerLoading = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformedLoading);
        countPanel.setPreferredSize(new Dimension(300, 150));
        statusAnalysisJLabel.setForeground(Color.white);
        durationAnalysisJLabel.setForeground(Color.white);
        loadingJLabel.setForeground(Color.white);
        loadingInitTimeJLabel.setForeground(Color.white);
        traceLoadDialog
            .setLableAttribute(statusAnalysisJLabel, durationAnalysisJLabel, loadingJLabel, loadingInitTimeJLabel,
                null);
        countPanel.removeAll();
        countPanel.add(statusAnalysisJLabel);
        countPanel.add(durationAnalysisJLabel);
        countPanel.add(loadingJLabel);
        countPanel.add(loadingInitTimeJLabel);
        countPanel.repaint();
        timerLoading.start();
        analysisDialogEvent.show();
    }

    /**
     * actionLoadingPerformed
     *
     * @param actionEvent actionEvent
     */
    public void actionPerformedLoading(ActionEvent actionEvent) {
        if (secondsLoading <= LayoutConstants.NUMBER_SECONDS) {
            loadingInitTimeJLabel.setText(" " + String.format(Locale.ENGLISH, "%02d", hoursLoading) + ":" + String
                .format(Locale.ENGLISH, "%02d", minutesLoading) + ":" + String
                .format(Locale.ENGLISH, "%02d", secondsLoading));
            secondsLoading++;
            if (secondsLoading > LayoutConstants.NUMBER_SECONDS) {
                secondsLoading = 0;
                minutesLoading++;
                if (minutesLoading > LayoutConstants.NUMBER_SECONDS) {
                    minutesLoading = 0;
                    hoursLoading++;
                }
            }
            int num = secondsLoading % 2;
            if (pullBytraceFileState && num == 0) {
                String filePath = "/data/local/tmp/hiprofiler_data" + fileExtension;
                ArrayList<String> getBytraceFileInfoCmd;
                if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
                    getBytraceFileInfoCmd = conversionCommand(HDC_STD_GET_TRACE_FILE_INFO, deviceIPPortInfo.getDeviceID(), filePath);
                } else {
                    getBytraceFileInfoCmd = conversionCommand(HDC_GET_TRACE_FILE_INFO, deviceIPPortInfo.getDeviceID(), filePath);
                }
                String bytraceFileInfo = HdcWrapper.getInstance().getHdcStringResult(getBytraceFileInfoCmd);
                if (bytraceFileInfo != null && bytraceFileInfo.length() > 0) {
                    String[] bytraceFileInfoArray = bytraceFileInfo.split("\t");
                    LOGGER.info("trace file size: {}", bytraceFileInfoArray[0]);
                    if (bytraceFileSize != 0 && bytraceFileSize == Integer.valueOf(bytraceFileInfoArray[0])) {
                        pullBytraceFileState = false;
                        pullAndAnalysisBytraceFile();
                    } else {
                        bytraceFileSize = Integer.valueOf(bytraceFileInfoArray[0]);
                    }
                }
            }
        }
        if (analysisState) {
            timerLoading.stop();
            analysisDialogEvent.close(1);
        }
    }

    /**
     * pull and analysis bytrace file
     */
    public void pullAndAnalysisBytraceFile() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                new SwingWorker<String, Object>() {
                    @Override
                    protected String doInBackground() {
                        getBytraceFile();
                        String baseDir = TraceStreamerUtils.getInstance().getBaseDir();
                        String dir = baseDir + "hiprofiler_data" + fileExtension;
                        String dbPath = TraceStreamerUtils.getInstance().getDbPath();
                        ArrayList arrayList = conversionCommand(TRACE_STREAMER_LOAD,
                            baseDir + TraceStreamerUtils.getInstance().getTraceStreamerApp(), dir, dbPath);
                        HdcWrapper.getInstance().getHdcStringResult(arrayList);
                        return TraceStreamerUtils.getInstance().getDbPath();
                    }

                    @Override
                    protected void done() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    analysisState = true;
                                    if (chooseMode) {
                                        new SystemTraceHelper().cancelActionDestroySession(deviceIPPortInfo, sessionId);
                                    }
                                    JBPanel tabContainer = taskPanel.getTabContainer();
                                    tabContainer.removeAll();
                                    AnalystPanel component = new AnalystPanel();
                                    String dbPath = get();
                                    component.load(dbPath, true);
                                    tabContainer.setBackground(JBColor.background());
                                    SystemPanel systemTuningPanel = new SystemPanel(tabContainer, component);
                                    tabContainer.add(systemTuningPanel, BorderLayout.NORTH);
                                    tabContainer.add(component, BorderLayout.CENTER);
                                    taskPanel.getTabContainer().add(component, BorderLayout.CENTER);
                                    taskPanel.repaint();
                                } catch (InterruptedException | ExecutionException exception) {
                                    LOGGER.error(" ExecutionException ", exception);
                                }
                            }
                        });
                    }
                }.execute();
            }
        });
    }

    /**
     * get bytrace file
     */
    public void getBytraceFile() {
        String filePath = "/data/local/tmp/hiprofiler_data" + fileExtension;
        ArrayList cmd;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            cmd = conversionCommand(HDC_STD_GET_TRACE_FILE, deviceIPPortInfo.getDeviceID(), filePath,
                TraceStreamerUtils.getInstance().getBaseDir());
        } else {
            cmd = conversionCommand(HDC_GET_TRACE_FILE, deviceIPPortInfo.getDeviceID(), filePath,
                TraceStreamerUtils.getInstance().getBaseDir());
        }
        HdcWrapper.getInstance().execCmdBy(cmd);
        LOGGER.info(cmd);
    }
}
