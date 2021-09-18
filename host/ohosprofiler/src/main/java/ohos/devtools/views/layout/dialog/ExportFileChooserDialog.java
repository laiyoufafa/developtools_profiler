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
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomJButton;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.io.File;

/**
 * ExportFileChooserDialog
 */
public class ExportFileChooserDialog extends DialogWrapper {
    private TextFieldWithBrowseButton textFieldWithBrowseButton;

    private JTextField jNameTextField;

    private String exportFileName;

    private String exportFilePath;

    private String fileType;

    private JComboBox<String> fileTypeBox = new JComboBox<String>();

    /**
     * constructor
     *
     * @param title title
     * @param fileType fileType
     */
    public ExportFileChooserDialog(String title, String fileType) {
        super(true);
        init();
        setTitle(title);
        this.fileType = fileType;
        fileTypeBox.addItem(fileType);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBPanel panel = new JBPanel(null);
        JLabel taskNameLabel = new JLabel("File Name");
        taskNameLabel.setBounds(LayoutConstants.MARGIN_LEFT, LayoutConstants.TWENTY, LayoutConstants.HUNDRED_FIFTY,
            LayoutConstants.THIRTY);
        panel.add(taskNameLabel);
        jNameTextField = new JTextField(LayoutConstants.THIRTY);
        jNameTextField.setBounds(LayoutConstants.MARGIN_LEFT, LayoutConstants.FIFTY, LayoutConstants.SCROPNUM,
            LayoutConstants.THIRTY);
        jNameTextField.setName(UtConstant.UT_EXPORT_FILE_FILE_NAME);
        panel.add(jNameTextField);
        JLabel filePathLocation = new JLabel("File Location");
        filePathLocation.setBounds(LayoutConstants.MARGIN_LEFT, LayoutConstants.NINETY, LayoutConstants.HUNDRED_FIFTY,
            LayoutConstants.THIRTY);
        panel.add(filePathLocation);
        textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, true, true, true, true, true);
        TextBrowseFolderListener listener = new TextBrowseFolderListener(chooserDescriptor);
        textFieldWithBrowseButton.addBrowseFolderListener(listener);
        textFieldWithBrowseButton.setText(System.getProperty("user.dir"));
        textFieldWithBrowseButton
            .setBounds(LayoutConstants.MARGIN_LEFT, LayoutConstants.FILE_SELECT_Y, LayoutConstants.SCROPNUM,
                LayoutConstants.THIRTY);
        panel.add(textFieldWithBrowseButton);
        JLabel fileTypeLabel = new JLabel("File Type");
        fileTypeLabel.setBounds(LayoutConstants.MARGIN_LEFT, LayoutConstants.FILE_TYPE_Y, LayoutConstants.HUNDRED_FIFTY,
            LayoutConstants.THIRTY);
        panel.add(fileTypeLabel);
        fileTypeBox.setBounds(LayoutConstants.MARGIN_LEFT, LayoutConstants.FILE_TYPE_BOX_Y, LayoutConstants.SCROPNUM,
            LayoutConstants.THIRTY);
        panel.add(fileTypeBox);
        panel.setPreferredSize(new Dimension(LayoutConstants.DIALOG_WIDTH, LayoutConstants.DIALOG_HEIGHT));
        return panel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        exportFilePath = textFieldWithBrowseButton.getText();
        exportFileName = jNameTextField.getText().trim();
        if (StringUtils.isBlank(exportFileName)) {
            return new ValidationInfo("Please input the file name !", jNameTextField);
        }
        if (!exportFileName.matches("^[A-Za-z0-9]+$")) {
            return new ValidationInfo("The file name can only contain numbers and letters !", jNameTextField);
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(exportFilePath);
        if (virtualFile == null || exportFilePath.isEmpty()) {
            return new ValidationInfo("Illegal path", textFieldWithBrowseButton);
        }
        return null;
    }

    /**
     *  save as trace file
     *
     * @param jButton jButton
     */
    public void saveDataToFile(CustomJButton jButton) {
        // 查询数据保存到file
        String pathName = exportFilePath + File.separator + exportFileName + Constant.TRACE_SUFFIX;
        DeviceProcessInfo deviceProcessInfo = new DeviceProcessInfo();
        deviceProcessInfo.setDeviceName(jButton.getDeviceName());
        deviceProcessInfo.setProcessName(jButton.getProcessName());
        deviceProcessInfo.setLocalSessionId(jButton.getSessionId());
        boolean saveResult =
            SessionManager.getInstance().saveSessionDataToFile(jButton.getSessionId(), deviceProcessInfo, pathName);
        if (saveResult) {
            new SampleDialog("prompt", "Save Successfully !").show();
        } else {
            new SampleDialog("prompt", "Save failure !").show();
        }
    }

    /**
     * get ExportFileName
     *
     * @return String FileName
     */
    public String getExportFileName() {
        return exportFileName;
    }

    /**
     * get exportFilePath
     *
     * @return String FilePath
     */
    public String getExportFilePath() {
        return exportFilePath;
    }

    public String getFileType() {
        return fileType;
    }
}
