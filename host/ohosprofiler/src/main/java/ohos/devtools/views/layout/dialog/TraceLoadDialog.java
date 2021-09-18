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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * SystemTuningDialog
 */
public class TraceLoadDialog extends DialogWrapper {
    private static final Logger LOGGER = LogManager.getLogger(TraceLoadDialog.class);
    private static final int MINUTE_TO_S = 60;
    private JPanel filePanel;
    private JBLabel timeJLabel ;
    private Timer timer;

    private int maxDurationParam = 0;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;

    /**
     * get timer
     *
     * @return Timer
     */
    public Timer getTimer() {
        return this.timer;
    }

    /**
     * SystemTunningDialogEvent
     *
     * @param filePanel filePanel
     * @param timeJLabel timeJLabel
     * @param maxDurationParam maxDurationParam
     */
    public TraceLoadDialog(JPanel filePanel,
        JBLabel timeJLabel, int maxDurationParam) {
        super(true);
        this.filePanel = filePanel;
        this.timeJLabel = timeJLabel;
        this.maxDurationParam = maxDurationParam;
        init();
        setTitle("Prompt");
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
        this.offLineTrace();
        JPanel dialogPanel = new JPanel(new BorderLayout());
        if (filePanel != null) {
            dialogPanel.add(filePanel, BorderLayout.CENTER);
        }
        return dialogPanel;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        JPanel dialogSouthPanel = new JPanel(new BorderLayout());
        return dialogSouthPanel;
    }

    /**
     * offLineTrace
     */
    public void offLineTrace() {
        timer = new Timer(LayoutConstants.NUMBER_THREAD, this::actionPerformed);
        timer.start();
    }

    /**
     * actionPerformed
     *
     * @param actionEvent actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        if ((hours * MINUTE_TO_S * MINUTE_TO_S
            + minutes * MINUTE_TO_S + seconds) > maxDurationParam) {
            timer.stop();
            this.doOKAction();
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
     * set Label Attribute
     *
     * @param statusJLabelParam statusJLabelParam
     * @param durationJLabelParam durationJLabelParam
     * @param recordingJLabelParam recordingJLabelParam
     * @param timeJLabelParam timeJLabelParam
     * @param stopJButton stopJButton
     */
    public void setLableAttribute(JLabel statusJLabelParam, JLabel durationJLabelParam, JLabel recordingJLabelParam,
        JLabel timeJLabelParam, JButton stopJButton) {
        statusJLabelParam.setBounds(120, 30, 70, 20);
        recordingJLabelParam.setBounds(190, 30, 70, 20);
        durationJLabelParam.setBounds(120, 60, 70, 20);
        timeJLabelParam.setBounds(190, 60, 70, 20);
        if (stopJButton != null) {
            stopJButton.setBounds(150, 110, 70, 20);
        }
    }
}
