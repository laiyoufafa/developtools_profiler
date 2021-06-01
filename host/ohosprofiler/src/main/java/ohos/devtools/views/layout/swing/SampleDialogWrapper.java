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

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import org.apache.commons.lang3.StringUtils;
import com.intellij.openapi.ui.DialogWrapper;

import ohos.devtools.views.common.LayoutConstants;

/**
 * SampleDialogWrapper
 *
 * @version 1.0
 * @date 2021/4/1 15:01
 **/
public class SampleDialogWrapper extends DialogWrapper {
    private String message = "";
    private JFileChooser fileChooser = null;
    private JPanel fileJpanel = null;

    /**
     * SampleDialogWrapper
     *
     * @param title   title
     * @param message message
     */
    public SampleDialogWrapper(String title, String message) {
        super(true);
        this.message = message;
        init();
        setTitle(title);
    }

    /**
     * SampleDialogWrapper
     *
     * @param title       title
     * @param fileChooser fileChooser
     */
    public SampleDialogWrapper(String title, JFileChooser fileChooser) {
        super(true);
        this.fileChooser = fileChooser;
        init();
        setTitle(title);
    }

    /**
     * SampleDialogWrapper
     *
     * @param title      title
     * @param fileJpanel fileJpanel
     */
    public SampleDialogWrapper(String title, JPanel fileJpanel) {
        super(true);
        this.fileJpanel = fileJpanel;
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
            JLabel label = new JLabel(message);
            label.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.MONITOR_PANEL_HEIGHT));
            dialogPanel.add(label, BorderLayout.CENTER);
        }
        if (fileChooser != null) {
            dialogPanel.add(fileChooser, BorderLayout.CENTER);
        }
        if (fileJpanel != null) {
            dialogPanel.add(fileJpanel, BorderLayout.CENTER);
        }
        return dialogPanel;
    }
}
