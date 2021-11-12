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
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.SwingWorker;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_STOP_NATIVE_HOOK;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;

/**
 * record native dialog
 *
 * @since : 2021/10/25
 */
public class NativeRecordDialog {
    private static final Logger LOGGER = LogManager.getLogger(NativeRecordDialog.class);

    /**
     * record native dialog width
     */
    private static final int DIALOG_WIDTH = 428;

    /**
     * record native dialog height
     */
    private static final int DIALOG_HEIGHT = 222;

    private final ProfilerChartsView bottomPanel;

    private final CustomDialog sampleDialog;

    private final JBPanel jPanel = new JBPanel(null);

    private final JBLabel status = new JBLabel("Status");
    private final JBLabel duration = new JBLabel("Duration");
    private final JBLabel statusValue = new JBLabel("Record native allocations");

    private final JBLabel timeJLabel = new JBLabel();

    private final JButton buttonCancel = new JButton("Cancel");

    private final JButton buttonStop = new JButton("Stop");

    private long sessionId = 1L;

    private boolean recordStatus = true;

    /**
     * HosRecordDialog
     *
     * @param bottomJPanel bottomJPanel
     * @param id Native Hook Session Id
     */
    public NativeRecordDialog(ProfilerChartsView bottomJPanel, long id) {
        this.sessionId = id;
        this.bottomPanel = bottomJPanel;
        sampleDialog = new CustomDialog("Record native allocations", jPanel);
        addButtonListeners();
        jPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        // set the style of jPanel and button
        setFontAndBounds();
        // Add components
        timeJLabel.setText("00:00.000");
        jPanel.add(buttonCancel);
        jPanel.add(buttonStop);
        jPanel.add(status);
        jPanel.add(duration);
        jPanel.add(statusValue);
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                Date date = new Date();
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                long cur;
                long start = System.currentTimeMillis();
                while (recordStatus) {
                    cur = System.currentTimeMillis() - start;
                    if (cur >= LayoutConstants.ONE_HOURS_MILLISECONDS) {
                        recordStatus = false;
                    }
                    date.setTime(cur);
                    printWriter.format("%1$tM:%1$tS.%tL", date);
                    printWriter.flush();
                    StringBuffer stringBuffer = stringWriter.getBuffer();
                    timeJLabel.setText(stringBuffer.toString());
                    stringBuffer.setLength(0);
                    try {
                        TimeUnit.MILLISECONDS.sleep(6L);
                    } catch (InterruptedException exception) {
                        LOGGER.error("Thread sleep error: {}", exception.getMessage());
                    }
                }
                return timeJLabel;
            }

            @Override
            protected void done() {
                sampleDialog.close(1);
            }
        }.execute();
        jPanel.add(timeJLabel);
        sampleDialog.setResizable(true);
        sampleDialog.show();
    }

    /**
     * Set the style of J Panel and Button
     */
    private void setFontAndBounds() {
        Font fontUpText = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.FONT_SIZE);
        status.setFont(fontUpText);
        duration.setFont(fontUpText);
        statusValue.setFont(fontUpText);
        timeJLabel.setFont(fontUpText);

        status.setBounds(LayoutConstants.SIXTY, LayoutConstants.NUM_50, LayoutConstants.RECORD_LEFT_PANEL_WIDTH,
            LayoutConstants.NUM_20);
        duration.setBounds(LayoutConstants.SIXTY, LayoutConstants.RECORD_LEFT_POINT_NUMBER,
            LayoutConstants.RECORD_LEFT_PANEL_WIDTH, LayoutConstants.NUM_20);
        statusValue.setBounds(LayoutConstants.RECORD_UP_POINT_NUMBER, LayoutConstants.NUM_50,
            LayoutConstants.RECORD_RIGHT_PANEL_WIDTH, LayoutConstants.NUM_20);
        timeJLabel.setBounds(LayoutConstants.RECORD_UP_POINT_NUMBER, LayoutConstants.RECORD_LEFT_POINT_NUMBER,
            LayoutConstants.RECORD_RIGHT_PANEL_WIDTH, LayoutConstants.NUM_20);

        Font fontUpButton = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.NUM_20);
        buttonCancel.setFont(fontUpButton);
        buttonStop.setFont(fontUpButton);

        buttonCancel.setBounds(LayoutConstants.NUM_110, LayoutConstants.NUM_142, LayoutConstants.NUM_96,
            LayoutConstants.NUM_40);
        buttonStop.setBounds(LayoutConstants.NUM_222, LayoutConstants.NUM_142, LayoutConstants.NUM_96,
            LayoutConstants.NUM_40);
    }

    private void addButtonListeners() {
        buttonStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                int endTime = bottomPanel.getTimeline().getEndTime();
                Date date = new Date();
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                date.setTime(endTime);
                printWriter.format("%1$tM:%1$tS.%tL", date);
                printWriter.flush();
                recordStatus = false;
                sampleDialog.close(1);
                SessionInfo sessionInfo = SessionManager.getInstance().getSessionInfo(bottomPanel.getSessionId());
                if (Objects.isNull(sessionId)) {
                    return;
                }
                DeviceIPPortInfo deviceIPPortInfo = sessionInfo.getDeviceIPPortInfo();
                ArrayList cmd = conversionCommand(HDC_STD_STOP_NATIVE_HOOK, deviceIPPortInfo.getDeviceID(),
                        String.valueOf(sessionInfo.getPid()));
                HdcWrapper.getInstance().execCmdBy(cmd);
                String timeText = stringWriter.toString();
                bottomPanel.getTaskScenePanelChart()
                    .createSessionList(LayoutConstants.NATIVE_HOOK_RECORDING, timeText, sessionId, null);
            }
        });

        buttonCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                recordStatus = false;
                sampleDialog.close(1);
            }
        });
    }

}
