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

package ohos.devtools.views.layout;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomSearchComBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultComboBoxModel;
import java.awt.Font;
import java.util.Vector;

/**
 * DistributedDeviceProcessPanel
 *
 * @since 2021/10/25
 */
public class DistributedDeviceProcessPanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(DistributedDeviceProcessPanel.class);
    private ComboBox<DeviceIPPortInfo> deviceComboBox;
    private Vector<DeviceIPPortInfo> vector;
    private CustomSearchComBox customSearchComBox;

    /**
     * DistributedDeviceProcessPanel
     *
     * @param deviceId deviceId
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param processInfo processInfo
     */
    public DistributedDeviceProcessPanel(int deviceId, DeviceIPPortInfo deviceIPPortInfo, ProcessInfo processInfo) {
        this.setLayout(new MigLayout("insets 5 5 0 0"));
        this.setOpaque(false);
        JBPanel deviceTitlePanel = initDeviceTitlePanel(deviceId);
        this.add(deviceTitlePanel, "wrap, span");
        ComboBox<String> deviceConnectTypeComboBox = new ComboBox<String>();
        deviceConnectTypeComboBox.addItem(LayoutConstants.USB);
        deviceComboBox = new ComboBox<>();
        deviceComboBox.addItem(deviceIPPortInfo);
        this.add(deviceConnectTypeComboBox, "width 30%");
        this.add(deviceComboBox, "wrap, width 68%");
        JBLabel applicationDesLabel = new JBLabel("Application");
        this.add(applicationDesLabel, "wrap, span 2");
        customSearchComBox = new CustomSearchComBox(deviceId, false);
        String processName = processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")";
        customSearchComBox.getSelectedProcessTextFiled().setText(processName);
        this.add(customSearchComBox, "wrap, span 2, width 99%");
    }

    /**
     * DistributedDeviceProcessPanel
     *
     * @param deviceId deviceId
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    public DistributedDeviceProcessPanel(int deviceId, DeviceIPPortInfo deviceIPPortInfo) {
        this.setLayout(new MigLayout("insets 5 5 0 0", "[][]", "[][][][]"));
        this.setOpaque(false);
        JBPanel deviceTitlePanel = initDeviceTitlePanel(deviceId);
        this.add(deviceTitlePanel, "wrap, span");
        ComboBox<String> deviceConnectTypeComboBox = new ComboBox<String>();
        deviceConnectTypeComboBox.addItem(LayoutConstants.USB);
        deviceConnectTypeComboBox.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_CONNECT_TYPE);
        deviceComboBox = new ComboBox<>();
        deviceComboBox.addItem(deviceIPPortInfo);
        deviceComboBox.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_DEVICE_NAME);
        this.add(deviceConnectTypeComboBox, "width 30%");
        this.add(deviceComboBox, "wrap, width 68%");
        JBLabel applicationDesLabel = new JBLabel("Application");
        this.add(applicationDesLabel, "wrap, span 2");
        customSearchComBox = new CustomSearchComBox(deviceId, false);
        this.add(customSearchComBox, "wrap, span 2, width 99%");
    }

    /**
     * DistributedDeviceProcessPanel
     *
     * @param deviceId deviceId
     * @param isDistributedPanel isDistributedPanel
     */
    public DistributedDeviceProcessPanel(int deviceId, boolean isDistributedPanel) {
        this.setLayout(new MigLayout("insets 5 5 0 0"));
        this.setOpaque(false);
        JBPanel deviceTitlePanel = initDeviceTitlePanel(deviceId);
        this.add(deviceTitlePanel, "wrap, span");
        ComboBox<String> deviceConnectTypeComboBox = new ComboBox<String>();
        deviceConnectTypeComboBox.addItem(LayoutConstants.USB);
        deviceConnectTypeComboBox.setName(UtConstant.UT_DEVICE_GPU_CONNECT_TYPE);
        deviceComboBox = new ComboBox<>();
        deviceComboBox.setName(UtConstant.UT_DEVICE_GPU_DEVICE_NAME);
        this.add(deviceConnectTypeComboBox, "width 30%");
        this.add(deviceComboBox, "wrap, width 68%");
        JBLabel applicationDesLabel = new JBLabel("Application");
        this.add(applicationDesLabel, "wrap, span 2");
        customSearchComBox = new CustomSearchComBox(deviceId, isDistributedPanel);
        this.add(customSearchComBox, "wrap, span 2, width 99%");
    }

    /**
     * initDeviceTitlePanel
     *
     * @param index index
     * @return JBPanel
     */
    private JBPanel initDeviceTitlePanel(int index) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initDeviceTitlePanel");
        }
        JBPanel firstDeviceTitlePanel = new JBPanel(new MigLayout("insets 0 0 5 0", "[]15[]push", "20[fill,fill]"));
        firstDeviceTitlePanel.setOpaque(false);
        JBLabel firstDeviceTitle = new JBLabel("Device 0" + index);
        firstDeviceTitle.setFont(new Font("Helvetica", Font.BOLD, 16));
        firstDeviceTitle.setForeground(JBColor.foreground().brighter());
        JBLabel deviceLabel = new JBLabel("Device");
        deviceLabel.setFont(new Font("PingFang SC", Font.PLAIN, 14));
        firstDeviceTitlePanel.add(firstDeviceTitle, "wrap");
        firstDeviceTitlePanel.add(deviceLabel);
        return firstDeviceTitlePanel;
    }

    /**
     * getSearchComBox
     *
     * @return CustomSearchComBox
     */
    public CustomSearchComBox getSearchComBox() {
        return customSearchComBox;
    }

    /**
     * getDeviceComboBox
     *
     * @return ComboBox
     */
    public ComboBox<DeviceIPPortInfo> getDeviceComboBox() {
        return deviceComboBox;
    }

    /**
     * refreshDeviceItem
     *
     * @param vector vector
     */
    public void refreshDeviceItem(Vector<DeviceIPPortInfo> vector) {
        deviceComboBox.setModel(new DefaultComboBoxModel(vector));
        this.vector = vector;
    }

    /**
     * getVector
     *
     * @return Vector
     */
    public Vector<DeviceIPPortInfo> getVector() {
        if (vector == null) {
            return new Vector<>();
        }
        return vector;
    }
}