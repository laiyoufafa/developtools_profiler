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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.views.layout.event.SystemTunningByTraceConfigEvent;

/**
 * @Description TaskSystemTunningByTracePanel
 * @Date 2021/4/9 13:15
 **/
public class TaskSystemTunningByTracePanel extends JPanel {
    /**
     * 设备信息集合
     */
    private List<DeviceIPPortInfo> deviceInfos = null;

    /**
     * 设备名称下拉框
     */
    private JComboBox<String> jComboBoxPhone = new JComboBox<String>();

    /**
     * 三级页面事件类
     */
    private SystemTunningByTraceConfigEvent taskSystemTunningByTracePanelEvent = new SystemTunningByTraceConfigEvent();

    /**
     * TaskSystemTunningByTracePanel
     *
     * @param jTaskPanel jTaskPanel
     */
    public TaskSystemTunningByTracePanel(TaskPanel jTaskPanel) {
        // 设置三级界面布局方式为边框布局管理
        this.setLayout(new BorderLayout());
        // 设置top容器，Center容器，South容器的属性
        // Center容器设置
        setjPanelCenter();
        // South容器设置
    }

    /**
     * setjPanelCenter
     */
    public void setjPanelCenter() {
        taskSystemTunningByTracePanelEvent.devicesInfoJComboBoxUpdate(this);
    }

    /**
     * setDeviceInfos
     *
     * @param deviceInfos deviceInfos
     */
    public void setDeviceInfos(List<DeviceIPPortInfo> deviceInfos) {
        this.deviceInfos = deviceInfos;
    }

    /**
     * getJComboBoxPhone
     *
     * @return JComboBox
     */
    public JComboBox getJComboBoxPhone() {
        return jComboBoxPhone;
    }

    /**
     * getDeviceInfos
     *
     * @return List<DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getDeviceInfos() {
        return deviceInfos;
    }

}
