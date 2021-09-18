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

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.transport.grpc.SystemTraceHelper;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.layout.utils.TraceStreamerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_TRACE_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_TRACE_FILE_INFO;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.TRACE_STREAMER_LOAD;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_TRACE_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_TRACE_FILE_INFO;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * CpuItemViewLoadDialog
 */
public class CpuItemViewLoadDialog implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(CpuItemViewLoadDialog.class);

    ExecutorService executorAnalysis = Executors.newSingleThreadExecutor();

    private int bytraceFileSize;
    private int hoursLoading = 0;
    private int minutesLoading = 0;
    private int secondsLoading = 0;
    private int numberOfParsingFile = 1;
    private String sessionId;
    private String fileSuffixTimestamp;
    private boolean typeParam;
    private boolean pullBytraceFileState = false;
    private boolean analysisState = false;

    private Timer timerLoading = null;
    private ProfilerChartsView bottomPanel;
    private DeviceIPPortInfo deviceIPPortInfo;
    private CpuItemDialog cpuItemDialogEvent;
    private AnalysisDialog analysisDialogEvent;

    private JBPanel jPanel = new JBPanel(null);

    private JBLabel statusJLabel = new JBLabel("Status");

    private JBLabel durationJLabel = new JBLabel("Duration");

    private JBLabel recordingJLabel = new JBLabel("Recording");

    private JBLabel timeJLabel = new JBLabel(" 00:00:00");

    private JBLabel typeJLabel = new JBLabel("Type");

    private JBLabel typeValueJLabel = new JBLabel();

    private JButton cancelJButton = new JButton("Cancel");

    private JButton stopJButton = new JButton("Stop");

    private JBLabel statusAnalysisJLabel = new JBLabel("Status");

    private JBLabel durationAnalysisJLabel = new JBLabel("Duration");

    private JBLabel loadingJLabel = new JBLabel("Analysis");

    private JBLabel loadingInitTimeJLabel = new JBLabel(" 00:00:00");

    /**
     * CpuItemViewLoadDialog
     *
     * @param bottomPanelParam
     * @param typeParam
     * @param sessionIdParam
     */
    public CpuItemViewLoadDialog(ProfilerChartsView bottomPanelParam, boolean typeParam, String sessionIdParam) {
        this.bottomPanel = bottomPanelParam;
        this.sessionId = sessionIdParam;
        this.typeParam = typeParam;
        typeValueJLabel.setText(LayoutConstants.TRACE_SYSTEM_CALLS_TIPS);
        cpuItemDialogEvent = new CpuItemDialog("Prompt", jPanel, CpuItemDialog.FileEnum.REAL_TIME_TRACE, timeJLabel);
    }

    /**
     * load
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param sessionIdParam sessionIdParam
     * @param fileSuffixTimestampParam fileSuffixTimestampParam
     */
    public void load(DeviceIPPortInfo deviceIPPortInfoParam, String sessionIdParam, String fileSuffixTimestampParam) {
        deviceIPPortInfo = deviceIPPortInfoParam;
        fileSuffixTimestamp = fileSuffixTimestampParam;
        jPanel.removeAll();
        jPanel.add(statusJLabel);
        jPanel.add(durationJLabel);
        jPanel.add(recordingJLabel);
        jPanel.add(timeJLabel);
        jPanel.add(typeJLabel);
        jPanel.add(typeValueJLabel);
        jPanel.setPreferredSize(new Dimension(300, 150));
        cpuItemDialogEvent.setLabelAttribute(statusJLabel, durationJLabel, recordingJLabel, timeJLabel, typeJLabel);
        typeValueJLabel.setBounds(140, LayoutConstants.CPU_GRAP_TYPE_Y, LayoutConstants.CPU_GRAP_TYPE_WIDTH, 20);
        boolean showAndGet = cpuItemDialogEvent.showAndGet();
        // close timer
        cpuItemDialogEvent.getTimer().stop();
        LOGGER.info(showAndGet);
        if (showAndGet) {
            stopAndDestroySession(deviceIPPortInfoParam, sessionIdParam);
            loading();
        } else {
            cancelActionDestroySession(deviceIPPortInfoParam, sessionIdParam);
        }
    }

    /**
     * stop and destroy session
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param sessionIdParam sessionIdParam
     */
    public void stopAndDestroySession(DeviceIPPortInfo deviceIPPortInfoParam, String sessionIdParam) {
        ExecutorService executorCancel = Executors.newSingleThreadExecutor();
        executorCancel.execute(new Runnable() {
            @Override
            public void run() {
                new SystemTraceHelper().stopSession(deviceIPPortInfoParam, sessionIdParam);
            }
        });
    }

    /**
     * stop and destroy session
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param sessionIdParam sessionIdParam
     */
    public void cancelActionDestroySession(DeviceIPPortInfo deviceIPPortInfoParam, String sessionIdParam) {
        ExecutorService executorCancel = Executors.newSingleThreadExecutor();
        executorCancel.execute(new Runnable() {
            @Override
            public void run() {
                new SystemTraceHelper().cancelActionDestroySession(deviceIPPortInfoParam, sessionIdParam);
            }
        });
    }

    /**
     * loading
     */
    public void loading() {
        pullBytraceFileState = true;
        analysisDialogEvent = new AnalysisDialog(deviceIPPortInfo, jPanel);
        timerLoading = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformed);
        jPanel.setPreferredSize(new Dimension(300, 150));
        cpuItemDialogEvent
            .setLabelAttribute(statusAnalysisJLabel, durationAnalysisJLabel, loadingJLabel, loadingInitTimeJLabel,
                typeJLabel);
        typeValueJLabel.setBounds(140, LayoutConstants.CPU_GRAP_TYPE_Y, LayoutConstants.CPU_GRAP_TYPE_WIDTH, 20);
        jPanel.removeAll();
        jPanel.add(statusAnalysisJLabel);
        jPanel.add(durationAnalysisJLabel);
        jPanel.add(loadingJLabel);
        jPanel.add(loadingInitTimeJLabel);
        jPanel.add(typeJLabel);
        jPanel.add(typeValueJLabel);
        timerLoading.start();
        analysisDialogEvent.show();
    }

    /**
     * actionLoadingPerformed
     *
     * @param actionEvent actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
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
        }
        int num = secondsLoading % 2;
        if (pullBytraceFileState && num == 0 && !analysisState) {
            executorAnalysis.execute(new Runnable() {
                @Override
                public void run() {
                    ArrayList getSimperfOrTraceFileInfoByCmd = null;
                    String filePath = "/data/local/tmp/hiprofiler_data" + fileSuffixTimestamp + ".bytrace";
                    if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                        getSimperfOrTraceFileInfoByCmd =
                            conversionCommand(HDC_STD_GET_TRACE_FILE_INFO, deviceIPPortInfo.getDeviceID(), filePath);
                    } else {
                        getSimperfOrTraceFileInfoByCmd =
                            conversionCommand(HDC_GET_TRACE_FILE_INFO, deviceIPPortInfo.getDeviceID(), filePath);
                    }
                    String fileInfo = HdcWrapper.getInstance().getHdcStringResult(getSimperfOrTraceFileInfoByCmd);
                    if (fileInfo != null && fileInfo.length() > 0 && !analysisState && fileInfo.contains("\t")) {
                        String[] fileInfoArray = fileInfo.split("\t");
                        if (bytraceFileSize != 0 && bytraceFileSize == Integer.valueOf(fileInfoArray[0])
                            && !analysisState) {
                            pullBytraceFileState = false;
                            pullAndAnalysisByTraceFile();
                            executorAnalysis.shutdown();
                        } else {
                            bytraceFileSize = Integer.valueOf(fileInfoArray[0]);
                        }
                    }
                }
            });
        }
        if (analysisState) {
            timerLoading.stop();
            analysisDialogEvent.close(1);
        }
    }

    /**
     * pull and analysis bytrace file
     */
    public void pullAndAnalysisByTraceFile() {
        getByTraceFile();
        new SystemTraceHelper().cancelActionDestroySession(deviceIPPortInfo, sessionId);
        removeOldTraceStreamerLog();
        String dbPath = null;
        try {
            dbPath = analysisTraceFileToDbFile();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        analysisState = true;
        // load AppTracePanel
        String recordTime = timeJLabel.getText();
        long sessionIdTrace = bottomPanel.getSessionId();
        TaskScenePanelChart taskScenePanelChart = bottomPanel.getTaskScenePanelChart();
        taskScenePanelChart.createSessionList(LayoutConstants.TRACE_SYSTEM_CALLS, recordTime, sessionIdTrace, dbPath);
    }

    /**
     * get bytrace file
     */
    public void getByTraceFile() {
        String baseDir = TraceStreamerUtils.getInstance().getBaseDir();
        String filePath = "/data/local/tmp/hiprofiler_data" + fileSuffixTimestamp + ".bytrace";
        ArrayList cmd;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmd = conversionCommand(HDC_STD_GET_TRACE_FILE, deviceIPPortInfo.getDeviceID(), filePath, baseDir);
        } else {
            cmd = conversionCommand(HDC_GET_TRACE_FILE, deviceIPPortInfo.getDeviceID(), filePath, baseDir);
        }
        HdcWrapper.getInstance().execCmdBy(cmd);
        LOGGER.info(cmd);
    }

    /**
     * remove old log
     */
    public void removeOldTraceStreamerLog() {
        String logPath = TraceStreamerUtils.getInstance().getLogPath();
        File logFile = new File(logPath);
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    /**
     * analysis trace file to db file
     *
     * @return String
     * @throws IOException
     */
    public String analysisTraceFileToDbFile() throws IOException {
        String dbPath = TraceStreamerUtils.getInstance().getBaseDir() + "realTimeTrace" + fileSuffixTimestamp + ".db";
        String cmd =
            TraceStreamerUtils.getInstance().getBaseDir() + TraceStreamerUtils.getInstance().getTraceStreamerApp();
        String dir =
            TraceStreamerUtils.getInstance().getBaseDir() + "hiprofiler_data" + fileSuffixTimestamp + ".bytrace";
        ArrayList arrayList = conversionCommand(TRACE_STREAMER_LOAD, cmd, dir, dbPath);
        HdcWrapper.getInstance().getHdcStringResult(arrayList);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String logPath = TraceStreamerUtils.getInstance().getLogPath();
        File logFile = new File(logPath);
        if (logFile.exists() && numberOfParsingFile > 0) {
            RandomAccessFile randomFile = null;
            randomFile = new RandomAccessFile(logFile, "r");
            String tmp = null;
            while ((tmp = randomFile.readLine()) != null) {
                if (Integer.valueOf(tmp.split(":")[1]) != 0) {
                    numberOfParsingFile--;
                    LOGGER.info("File parsing failed last time, try again. value is {}",
                        Integer.valueOf(tmp.split(":")[1]));
                    analysisTraceFileToDbFile();
                }
            }
        }
        return dbPath;
    }
}
