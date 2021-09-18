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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * ImportFileChooserDialog
 */
public class ImportFileChooserDialog extends DialogWrapper {
    private TextFieldWithBrowseButton textFieldWithBrowseButton;
    private String importFilePath;

    public String getImportFilePath() {
        return importFilePath;
    }

    /**
     * constructor
     *
     * @param title title
     */
    public ImportFileChooserDialog(String title) {
        super(true);
        init();
        setTitle(title);
        setResizable(false);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBPanel dialogPanel = new JBPanel(null);
        JLabel filePathLocation = new JLabel("File Location:");
        filePathLocation.setBounds(15, 15, 350, 30);
        dialogPanel.add(filePathLocation);
        textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, true, true, true, true, true);
        chooserDescriptor.setHideIgnored(false);
        TextBrowseFolderListener listener = new TextBrowseFolderListener(chooserDescriptor);
        textFieldWithBrowseButton.addBrowseFolderListener(listener);
        textFieldWithBrowseButton.setText("");
        textFieldWithBrowseButton.setBounds(15, 60, 350, 30);
        dialogPanel.add(textFieldWithBrowseButton);
        dialogPanel.setPreferredSize(new Dimension(380, 120));
        return dialogPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        importFilePath = textFieldWithBrowseButton.getText();
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(importFilePath);
        if (virtualFile == null || importFilePath.isEmpty()) {
            return new ValidationInfo("The path is empty!", textFieldWithBrowseButton);
        }
        if (virtualFile.isDirectory()) {
            return new ValidationInfo("Please choose the file!", textFieldWithBrowseButton);
        }
        return null;
    }
}
