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
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.datasources.utils.trace.service.TraceManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.SystemTunningProbesCheckbox;
import ohos.devtools.views.layout.event.SystemTunningDialogEvent;
import ohos.devtools.views.trace.component.AnalystPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description TaskSystemLoad
 * @Date 2021/4/10 15:16
 **/
public class SystemTunningLoadDialog implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(SystemTunningLoadDialog.class);
    private Date executeArrivalToTime = null;
    private int bytraceFileSize;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private int maxDurationParam = 0;
    private String sessionId;
    private DeviceIPPortInfo deviceIPPortInfo;
    private int differentRequests = 0;
    private Boolean pullBytraceFileState = false;
    private Boolean analysisState = false;
    /**
     * loading
     */
    private int hoursLoading = 0;
    private int minutesLoading = 0;
    private int secondsLoading = 0;

    private TaskPanel jTaskPanel = null;

    private JPanel jPanel = new JPanel(null);

    private JLabel statusJLabel = new JLabel("Status");

    private JLabel durationJLabel = new JLabel("Duration");

    private JLabel recordingJLabel = new JLabel("Recording");

    private JLabel timeJLabel = new JLabel();

    private JButton stopJButton = new JButton("Stop");

    private SystemTunningDialogEvent systemTunningDialogEvent;

    private Timer timer = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformed);

    private Timer timerLoading = null;

    private JLabel statusAnalysisJLabel = new JLabel("Status");

    private JLabel durationAnalysisJLabel = new JLabel("Duration");

    private JLabel loadingJLabel = new JLabel("Loading");

    private JLabel loadingInitTimeJLabel = new JLabel(" 00:00:00");

    /**
     * load
     *
     * @param jTaskPanel            jTaskPanel
     * @param differentRequestsPram differentRequestsPram
     * @param maxDurationParam      maxDurationParam
     * @param sessionIdParam        sessionIdParam
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     */
    public void load(TaskPanel jTaskPanel, int differentRequestsPram, int maxDurationParam, String sessionIdParam,
        DeviceIPPortInfo deviceIPPortInfoParam) {
        // 当前时间大于大于采集的时间、小于采集加解析的时间
        if (timer.isRepeats()) {
            timer.stop();
        }
        timer = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformed);
        systemTunningDialogEvent = new SystemTunningDialogEvent("Prompt", jPanel);
        stopJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                timer.stop();
                systemTunningDialogEvent.close(1);
                new TraceManager().stopAndDestroySession(deviceIPPortInfo, sessionId);
            }
        });
        this.jTaskPanel = jTaskPanel;
        this.maxDurationParam = maxDurationParam;
        this.sessionId = sessionIdParam;
        this.deviceIPPortInfo = deviceIPPortInfoParam;
        this.differentRequests = differentRequestsPram;
        jPanel.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_LOADING_WIDTH, LayoutConstants.SYSTEM_TUNNING_LOADING_HEIGHT));
        statusJLabel.setForeground(Color.white);
        durationJLabel.setForeground(Color.white);
        recordingJLabel.setForeground(Color.white);
        timeJLabel.setForeground(Color.white);
        this.setLableAttribute(statusJLabel, durationJLabel, recordingJLabel, timeJLabel);
        timeJLabel.setText(" 00:00:00");
        jPanel.add(statusJLabel);
        jPanel.add(durationJLabel);
        jPanel.add(recordingJLabel);
        jPanel.add(timeJLabel);
        jPanel.add(stopJButton);
        timer.start();
        systemTunningDialogEvent.show();
    }

    /**
     * actionPerformed
     *
     * @param actionEvent actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        if ((hours * SystemTunningProbesCheckbox.MINUTE_TO_S * SystemTunningProbesCheckbox.MINUTE_TO_S
            + minutes * SystemTunningProbesCheckbox.MINUTE_TO_S + seconds) > maxDurationParam) {
            timer.stop();
            // 加载展示页面
            this.loading();
        }
        if (seconds <= LayoutConstants.NUMBER_SECONDS) {
            timeJLabel.setText(" " + String.format(Locale.ENGLISH, "%02d", hours) + ":" + String
                .format(Locale.ENGLISH, "%02d", minutes) + ":" + String.format(Locale.ENGLISH, "%02d", seconds));
            seconds++;
            if (seconds > LayoutConstants.NUMBER_SECONDS) {
                seconds = 0;
                minutes++;
                if (minutes > LayoutConstants.NUMBER_SECONDS) {
                    minutes = 0;
                    hours++;
                }
            }
        }
    }

    /**
     * loading
     */
    public void loading() {
        pullBytraceFileState = true;
        timerLoading = new Timer(LayoutConstants.NUMBER_THREAD, this::actionLoadingPerformed);
        jPanel.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_LOADING_WIDTH, LayoutConstants.SYSTEM_TUNNING_LOADING_HEIGHT));
        statusAnalysisJLabel.setForeground(Color.white);
        durationAnalysisJLabel.setForeground(Color.white);
        loadingJLabel.setForeground(Color.white);
        loadingInitTimeJLabel.setForeground(Color.white);
        this.setLableAttribute(statusAnalysisJLabel, durationAnalysisJLabel, loadingJLabel, loadingInitTimeJLabel);
        jPanel.removeAll();
        jPanel.add(statusAnalysisJLabel);
        jPanel.add(durationAnalysisJLabel);
        jPanel.add(loadingJLabel);
        jPanel.add(loadingInitTimeJLabel);
        timerLoading.start();
        systemTunningDialogEvent.repaint();
    }


    /**
     * actionLoadingPerformed
     *
     * @param actionEvent actionEvent
     */
    public void actionLoadingPerformed(ActionEvent actionEvent) {
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
                String getBytraceFileInfoCmd = "hdc shell du /data/local/tmp/hiprofiler_data.bytrace ";
                String bytraceFileInfo = HdcWrapper.getInstance().getHdcStringResult(getBytraceFileInfoCmd);
                if (bytraceFileInfo != null && bytraceFileInfo.length() > 0) {
                    String[] bytraceFileInfoArray = bytraceFileInfo.split("\t");
                    if (bytraceFileSize != 0 && bytraceFileSize == Integer.valueOf(bytraceFileInfoArray[0])) {
                        pullBytraceFileState = false;
                        pullAndAnalysisBytraceFile();
                    }else {
                        bytraceFileSize = Integer.valueOf(bytraceFileInfoArray[0]);
                    }
                }
            }
        }
        if (analysisState) {
            timerLoading.stop();
            systemTunningDialogEvent.close(1);
        }
    }

    /**
     * pull and analysis bytrace file
     *
     */
    public void pullAndAnalysisBytraceFile() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                getBytraceFile();
                String baseDir = SessionManager.getInstance().getPluginPath() + "trace_streamer\\";
                String dbPath = baseDir + "systrace.db";
                File file = new File(baseDir);
                String cmd = baseDir + "trace_streamer.exe";
                if (differentRequests == 0) {
                    cmd = cmd + " " + baseDir + "hiprofiler_data.bytrace";
                }
                if (differentRequests == 1) {
                    cmd = cmd + " " + baseDir + "hiprofiler_data.ptrace";
                }
                cmd = cmd + " -e " + dbPath;
                LOGGER.info("cmd: {}", cmd);
                HdcWrapper.getInstance().getHdcStringResult(cmd);
                jTaskPanel.getOptionJPanel().removeAll();
                AnalystPanel component = new AnalystPanel();
                component.load(dbPath, true);
                jTaskPanel.getOptionJPanel().add(component);
                jTaskPanel.repaint();
                analysisState = true;
            }
        });
    }

    /**
     * get bytrace file
     *
     */
    public void getBytraceFile() {
        String pluginPath = SessionManager.getInstance().getPluginPath() + "trace_streamer\\";
        LOGGER.info("start >>> hdc file recv /data/local/tmp/hiprofiler_data.bytrace");
        String cmd = null;
        if (differentRequests == 0) {
            cmd = "hdc file recv /data/local/tmp/hiprofiler_data.bytrace " + pluginPath;
        }
        if (differentRequests == 1) {
            cmd = "hdc file recv /data/local/tmp/hiprofiler_data.ptrace " + pluginPath;
        }
        HdcWrapper.getInstance().execCmdBy(cmd, LayoutConstants.TEN);

        // 抓取数据结束，获取ptrace数据到本地过后，stopSession、destroySession
        new TraceManager().stopAndDestroySession(deviceIPPortInfo, sessionId);
        LOGGER.info("end >>> hdc file recv /data/local/tmp/hiprofiler_data.ptrace");
    }

    /**
     * set Label Attribute
     *
     * @param statusJLabelParam statusJLabelParam
     * @param durationJLabelParam durationJLabelParam
     * @param recordingJLabelParam recordingJLabelParam
     * @param timeJLabelParam timeJLabelParam
     */
    public void setLableAttribute(JLabel statusJLabelParam, JLabel durationJLabelParam,
        JLabel recordingJLabelParam, JLabel timeJLabelParam) {
        statusJLabelParam.setBounds(LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_X,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INIT_INITLINE_ONE_Y,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_HEIGHT);
        recordingJLabelParam.setBounds(LayoutConstants.SYSTEM_TUNNING_LOADING_INIT_LINE_TWO_X,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INIT_INITLINE_ONE_Y,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_HEIGHT);
        durationJLabelParam.setBounds(LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_X,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INIT_INITLINE_TWO_Y,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_HEIGHT);
        timeJLabelParam.setBounds(LayoutConstants.SYSTEM_TUNNING_LOADING_INIT_LINE_TWO_X,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INIT_INITLINE_TWO_Y,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_HEIGHT);
        stopJButton
            .setBounds(LayoutConstants.SYSTEM_TUNNING_LOADING_HEIGHT, LayoutConstants.SYSTEM_TUNNING_LOADING_BUTTON_Y,
                LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_WIDTH,
                LayoutConstants.SYSTEM_TUNNING_LOADING_INITLINE_ONE_HEIGHT);
    }
}
