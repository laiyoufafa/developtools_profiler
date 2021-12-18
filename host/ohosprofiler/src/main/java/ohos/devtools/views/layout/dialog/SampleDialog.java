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
import ohos.devtools.views.common.LayoutConstants;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * SampleDialog
 *
 * @since 2021/10/26
 */
public class SampleDialog extends DialogWrapper {
    private String message = "";
    private JFileChooser fileChooser = null;
    private JPanel filePanel = null;

    /**
     * SampleDialogWrapper
     *
     * @param title title
     * @param message message
     */
    public SampleDialog(String title, String message) {
        super(true);
        this.message = message;
        init();
        setTitle(title);
    }

    /**
     * SampleDialogWrapper
     *
     * @param title title
     * @param fileChooser fileChooser
     */
    public SampleDialog(String title, JFileChooser fileChooser) {
        super(true);
        this.fileChooser = fileChooser;
        init();
        setTitle(title);
    }

    /**
     * SampleDialogWrapper
     *
     * @param title title
     * @param filePanel filePanel
     */
    public SampleDialog(String title, JPanel filePanel) {
        super(true);
        this.filePanel = filePanel;
        init();
        setTitle(title);
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
        if (!StringUtils.isEmpty(message)) {
            if (message.contains(System.lineSeparator())) {
                String[] messageArr = message.split("\n\\,");
                String showStr = "<html>";
                for (String str : messageArr) {
                    showStr = showStr + "<p>" + str + "</p>";
                }
                showStr = showStr + "</html>";
                message = showStr;
            }
            JLabel label = new JLabel(message);
            label.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.MONITOR_PANEL_HEIGHT));
            dialogPanel.add(label, BorderLayout.CENTER);
        }
        if (fileChooser != null) {
            dialogPanel.add(fileChooser, BorderLayout.CENTER);
        }
        if (filePanel != null) {
            dialogPanel.add(filePanel, BorderLayout.CENTER);
        }
        return dialogPanel;
    }
}
