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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import ohos.devtools.views.common.LayoutConstants;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * CpuItemDialog
 */
public class CpuItemDialog extends DialogWrapper {
    private JPanel filePanel;
    private JBLabel timeLabel;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private Timer timer = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformed);
    private FileEnum fileType;

    /**
     * CpuItemDialog
     */
    public CpuItemDialog(String title, JPanel filePanel, FileEnum fileType, JBLabel timeJLabel) {
        super(true);
        this.filePanel = filePanel;
        this.fileType = fileType;
        this.timeLabel = timeJLabel;
        init();
        setTitle(title);
        setResizable(false);
    }

    /**
     * createCenterPanel
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (fileType.equals(FileEnum.REAL_TIME_TRACE)) {
            this.loadRealTimeTrace();
        }
        JPanel dialogPanel = new JPanel(new BorderLayout());
        if (filePanel != null) {
            dialogPanel.add(filePanel, BorderLayout.CENTER);
        }
        return dialogPanel;
    }

    /**
     * loadRealTimeTrace
     */
    public void loadRealTimeTrace() {
        timeLabel.setText(" 00:00:00");
        filePanel.add(timeLabel);
        timer = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformed);
        timer.start();
    }

    /**
     * actionPerformed
     *
     * @param actionEvent actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        if (seconds <= LayoutConstants.NUMBER_SECONDS) {
            timeLabel.setText(" " + String.format(Locale.ENGLISH, "%02d", hours) + ":" +
                    String.format(Locale.ENGLISH, "%02d", minutes) + ":" +
                    String.format(Locale.ENGLISH, "%02d", seconds));
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
     * set Label Attribute
     *
     * @param statusJLabelParam statusJLabelParam
     * @param durationJLabelParam durationJLabelParam
     * @param recordingJLabelParam recordingJLabelParam
     * @param timeJLabelParam timeJLabelParam
     * @param typeJLabelParam typeJLabelParam
     * @param typeValueJLabelParam typeValueJLabelParam
     */
    public void setLabelAttribute(JLabel statusJLabelParam, JLabel durationJLabelParam, JLabel recordingJLabelParam,
        JLabel timeJLabelParam, JLabel typeJLabelParam) {
        statusJLabelParam.setBounds(70, 30, 70, 20);
        recordingJLabelParam.setBounds(140, 30, 70, 20);
        durationJLabelParam.setBounds(70, 60, 70, 20);
        timeJLabelParam.setBounds(140, 60, 70, 20);
        typeJLabelParam.setBounds(70, LayoutConstants.CPU_GRAP_TYPE_Y, 70, 20);
    }

    /**
     * FileEnum
     */
    public enum FileEnum {
        OFF_LINE_TRACE("offLineTrace"), REAL_TIME_TRACE("realTimeTrace");
        private String fileType;

        FileEnum(String fileType) {
            this.fileType = fileType;
        }

        public String getFileType() {
            return fileType;
        }
    }

    public Timer getTimer() {
        return this.timer;
    }
}
