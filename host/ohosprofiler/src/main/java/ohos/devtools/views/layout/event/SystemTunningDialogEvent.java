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

package ohos.devtools.views.layout.event;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.DialogWrapperDialog;

/**
 * SystemTunningDialog 加载框
 *
 * @version 1.0
 * @date 2021/04/14 20:13
 **/
public class SystemTunningDialogEvent extends DialogWrapper {
    private JPanel fileJpanel = null;

    /**
     * SystemTunningDialogEvent
     *
     * @param title      title
     * @param fileJpanel fileJpanel
     */
    public SystemTunningDialogEvent(String title, JPanel fileJpanel) {
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
        if (fileJpanel != null) {
            dialogPanel.add(fileJpanel, BorderLayout.CENTER);
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
     * closeWindow
     *
     * @param window    window
     * @param modalOnly modalOnly
     */
    public static void closeWindow(Window window, boolean modalOnly) {
        if (modalOnly && window instanceof Frame) {
            return;
        }
        if (window instanceof DialogWrapperDialog) {
            ((DialogWrapperDialog) window).getDialogWrapper().doCancelAction();
            return;
        }
        window.setVisible(false);
        window.dispose();
    }
}
