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
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.views.layout.TaskPanel;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;

/**
 * AnalysisDialog
 *
 * @since 2021/11/22
 */
public class AnalysisDialog extends DialogWrapper {
    private JPanel filePanel;
    private JBLabel timeJLabel;
    private TaskPanel jTaskPanel;
    private Timer timer;
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * get timer
     *
     * @return Timer
     */
    public Timer getTimer() {
        return this.timer;
    }

    /**
     * AnalysisDialog
     */
    public AnalysisDialog(DeviceIPPortInfo deviceIPPortInfo, JPanel filePanel) {
        super(true);
        this.deviceIPPortInfo = deviceIPPortInfo;
        this.filePanel = filePanel;
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
}
