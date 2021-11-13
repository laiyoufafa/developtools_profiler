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

import com.intellij.openapi.util.IconLoader;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.CustomJButton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * SaveTraceDialog
 *
 * @since  2021/10/25
 */
public class SaveTraceDialog {
    /**
     * 全局日志
     */
    private static final Logger LOGGER = LogManager.getLogger(SaveTraceDialog.class);

    /**
     * trace文件保存路径
     */
    private String tracePath = "";

    /**
     * 显示一个自定义的对话框
     *
     * @param btn 对话框的父级组件
     * @param title 对话框的title
     */
    public void showCustomDialog(CustomJButton btn, String title) {
        // 创建一个模态对话框
        JPanel fileJpanel = new JPanel(null);
        fileJpanel.setPreferredSize(new Dimension(LayoutConstants.FOUR_HUNDRED, LayoutConstants.TWO_HUNDRED_SIXTY));
        fileJpanel.setBackground(ColorConstants.HOME_PANE);
        JLabel taskNameLabel = new JLabel("Save as");
        taskNameLabel.setBounds(LayoutConstants.TWENTY, LayoutConstants.TWENTY, LayoutConstants.HUNDRED_FIFTY,
            LayoutConstants.THIRTY);
        JTextField jTextField = new JTextField(LayoutConstants.THIRTY);
        jTextField.setBackground(ColorConstants.SELECT_PANEL);
        jTextField
            .setBounds(LayoutConstants.TWENTY, LayoutConstants.FIFTY, LayoutConstants.SCROPNUM, LayoutConstants.THIRTY);
        JLabel filePathLocation = new JLabel("Save Location");
        filePathLocation.setBounds(LayoutConstants.TWENTY, LayoutConstants.NINETY, LayoutConstants.HUNDRED_FIFTY,
            LayoutConstants.THIRTY);
        JTextArea msgTextArea = new JTextArea(LayoutConstants.TWENTY, LayoutConstants.THIRTY);
        msgTextArea.setEditable(false);
        msgTextArea.setLineWrap(true);
        msgTextArea
            .setBounds(LayoutConstants.TWENTY, LayoutConstants.HUNDRED_TWENTY_TWO, LayoutConstants.SAVE_LOCATION_WIDTH,
                LayoutConstants.THIRTY);
        msgTextArea.setBackground(ColorConstants.SELECT_PANEL);
        if (StringUtils.isNotBlank(tracePath)) {
            msgTextArea.setText(tracePath);
        }
        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.setBounds(LayoutConstants.TWENTY, LayoutConstants.HUNDRED_TWENTY_TWO, LayoutConstants.SCROPNUM,
            LayoutConstants.THIRTY);
        // 获取文件下拉箭头
        JLabel labelSvg = new JLabel();
        labelSvg.setIcon(IconLoader.getIcon(("/images/down.png"), getClass()));
        labelSvg.setBounds(LayoutConstants.THREE_HUNDRED, LayoutConstants.OTHERS_Y, LayoutConstants.JLABEL_SIZE,
            LayoutConstants.THIRTY);
        selectPanel.add(msgTextArea, BorderLayout.WEST);
        selectPanel.add(labelSvg, BorderLayout.CENTER);
        labelSvg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    showFileSaveDialog(fileJpanel, msgTextArea);
                } catch (IOException exception) {
                    LOGGER.error("SaveTraceDialog:{}", exception.getMessage());
                }
            }
        });
        fileJpanel.add(taskNameLabel);
        fileJpanel.add(jTextField);
        fileJpanel.add(filePathLocation);
        fileJpanel.add(selectPanel);
        customDialogWrapper(btn, new SampleDialog(title, fileJpanel), jTextField, msgTextArea);
    }

    /**
     * 创建一个对话框Wrapper
     *
     * @param btn btn
     * @param sampleDialog sampleDialog
     * @param jTextField jTextField
     * @param msgTextArea msgTextArea
     */
    private void customDialogWrapper(CustomJButton btn, SampleDialog sampleDialog, JTextField jTextField,
        JTextArea msgTextArea) {
        boolean flag = sampleDialog.showAndGet();
        if (flag) {
            String fileName = jTextField.getText().trim();
            String filePath = msgTextArea.getText();
            if (StringUtils.isBlank(fileName)) {
                new SampleDialog("Warring", "Please input the file name !").show();
                return;
            }
            if (!fileName.matches("^[A-Za-z0-9]+$")) {
                new SampleDialog("Warring", "The file name can only contain numbers and letters !").show();
                return;
            }
            if (StringUtils.isBlank(filePath)) {
                new SampleDialog("Warring", "Please select the file path !").show();
                return;
            }
            // 查询数据保存到file
            String pathName = filePath + File.separator + fileName + Constant.TRACE_SUFFIX;
            DeviceIPPortInfo deviceIPPortInfo =
                    SessionManager.getInstance().getDeviceInfoBySessionId(btn.getSessionId());
            DeviceProcessInfo deviceProcessInfo = new DeviceProcessInfo();
            deviceProcessInfo.setDeviceName(btn.getDeviceName());
            deviceProcessInfo.setProcessName(btn.getProcessName());
            deviceProcessInfo.setLocalSessionId(btn.getSessionId());
            deviceProcessInfo.setDeviceType(deviceIPPortInfo.getDeviceType().toString());
            boolean saveResult =
                SessionManager.getInstance().saveSessionDataToFile(btn.getSessionId(), deviceProcessInfo, pathName);
            if (saveResult) {
                sampleDialog.close(0);
                new SampleDialog("prompt", "Save Successfully !").show();
            } else {
                sampleDialog.close(0);
                new SampleDialog("prompt", "Save failure !").show();
            }
        }
    }

    /**
     * 选择文件保存路径
     *
     * @param parent parent
     * @param msgTextArea msgTextArea
     * @throws IOException IOException
     */
    private void showFileSaveDialog(Component parent, JTextArea msgTextArea) throws IOException {
        UIManager.put("FileChooser.cancelButtonText", "Cancel");
        // 创建一个默认的文件选取器
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File Path");
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setApproveButtonText("choose");
        fileChooser.setControlButtonsAreShown(false);
        SampleDialog sampleDialog = new SampleDialog("Select File Path", fileChooser);
        boolean flag = sampleDialog.showAndGet();
        if (flag) {
            File file = fileChooser.getCurrentDirectory();
            msgTextArea.setText(file.getCanonicalPath());
            tracePath = file.getCanonicalPath();
        }
    }
}
