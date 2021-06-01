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

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.layout.swing.SystemTunningConfigPanel;
import ohos.devtools.views.layout.swing.TaskSystemTunningByTracePanel;

import javax.swing.DefaultComboBoxModel;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import static ohos.devtools.views.common.Constant.DEVICEREFRESH;

/**
 * SystemTunningByTraceConfigEvent
 *
 * @version 1.0
 * @date 2021/04/10 15:16
 **/
public class SystemTunningByTraceConfigEvent implements ClipboardOwner {
    private Vector<String> oldDevice = new Vector<>();

    DeviceIPPortInfo deviceIPPortInfo = null;

    /**
     * requestByTrace
     */
    public void requestByTrace() {
    }

    /**
     * devicesInfoJComboBoxUpdate
     *
     * @param taskSystemTunningByTracePanel taskSystemTunningByTracePanel
     */
    public void devicesInfoJComboBoxUpdate(TaskSystemTunningByTracePanel taskSystemTunningByTracePanel) {
        QuartzManager.getInstance().addExecutor(DEVICEREFRESH, new Runnable() {
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getAllDeviceIPPortInfos();
                taskSystemTunningByTracePanel.setDeviceInfos(deviceInfos);
                Vector<String> items = new Vector<>();
                deviceInfos.forEach(deviceInfo -> {
                    items.add(deviceInfo.getDeviceName());
                });
                if (!oldDevice.equals(items)) {
                    oldDevice = items;
                    taskSystemTunningByTracePanel.getJComboBoxPhone().setModel(new DefaultComboBoxModel(items));
                }
            }
        });
        QuartzManager.getInstance().startExecutor(DEVICEREFRESH, 0, 1000);
    }

    /**
     * itemStateChanged
     *
     * @param taskSystemTunningPanel taskSystemTunningPanel
     */
    public void itemStateChanged(SystemTunningConfigPanel taskSystemTunningPanel) {
        taskSystemTunningPanel.getJComboBoxPhone().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent exception) {
                // 获取选中项，设置对象信息用于查询对应的进程信息
                for (DeviceIPPortInfo deviceInfo : taskSystemTunningPanel.getDeviceInfos()) {
                    if (deviceInfo.getDeviceName()
                        .equals(taskSystemTunningPanel.getJComboBoxPhone().getSelectedItem())) {
                        deviceIPPortInfo = deviceInfo;
                    }
                }
            }
        });
        if (taskSystemTunningPanel.getDeviceInfos() != null && deviceIPPortInfo == null) {
            deviceIPPortInfo = taskSystemTunningPanel.getDeviceInfos().get(0);
        }
    }

    /**
     * lostOwnership
     *
     * @param clipboard clipboard
     * @param contents  contents
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
