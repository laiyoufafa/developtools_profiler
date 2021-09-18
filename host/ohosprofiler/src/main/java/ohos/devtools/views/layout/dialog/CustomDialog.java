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

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Custom dialog
 */
public class CustomDialog extends DialogWrapper {
    private final JPanel fileJPanel;

    /**
     * CustomDialog
     *
     * @param title title
     * @param fileJPanel fileJPanel
     */
    public CustomDialog(String title, JPanel fileJPanel) {
        super(true);
        this.fileJPanel = fileJPanel;
        init();
        setTitle(title);
    }

    /**
     * createCenterPanel
     *
     * @return JComponent
     */
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        if (fileJPanel != null) {
            dialogPanel.add(fileJPanel, BorderLayout.CENTER);
        }
        return dialogPanel;
    }

    /**
     * createSouthPanel
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        return new JPanel(new BorderLayout());
    }
}
