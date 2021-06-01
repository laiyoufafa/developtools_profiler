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

package ohos.devtools.views.common.hoscomp;

import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.views.common.LayoutConstants;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Dimension;

/**
 * @Description HosJButton
 * @Date 2021/3/10 20:50
 **/
public class HosJButton extends JButton {
    private SessionInfo sessionInfo;

    private long sessionId;

    private String deviceName;

    private String processName;

    public HosJButton(Icon icon, String message) {
        super(icon);
        this.setPreferredSize(new Dimension(LayoutConstants.BUTTON_SIZE, LayoutConstants.BUTTON_SIZE));
        this.setToolTipText(message);
    }

    public HosJButton(String text, String message) {
        super(text);
        this.setToolTipText(message);
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
