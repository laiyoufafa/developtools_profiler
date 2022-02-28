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

package ohos.devtools.views.common.customcomp;

import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.views.common.LayoutConstants;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Dimension;

/**
 * CustomJButton
 *
 * @since 2021/5/19 16:39
 */
public class CustomJButton extends JButton {
    private SessionInfo sessionInfo;

    private long sessionId;

    private String deviceName;

    private String processName;

    /**
     * CustomJButton
     *
     * @param icon icon
     * @param message message
     */
    public CustomJButton(Icon icon, String message) {
        super(icon);
        this.setPreferredSize(new Dimension(LayoutConstants.BUTTON_SIZE, LayoutConstants.BUTTON_SIZE));
        this.setToolTipText(message);
    }

    /**
     * CustomJButton
     *
     * @param text text
     * @param message message
     */
    public CustomJButton(String text, String message) {
        super(text);
        this.setToolTipText(message);
    }

    /**
     * getSessionInfo
     *
     * @return SessionInfo
     */
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    /**
     * setSessionInfo
     *
     * @param sessionInfo sessionInfo
     */
    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    /**
     * getSessionId
     *
     * @return long
     */
    public long getSessionId() {
        return sessionId;
    }

    /**
     * setSessionId
     *
     * @param sessionId sessionId
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * getDeviceName
     *
     * @return String
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * setDeviceName
     *
     * @param deviceName deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * getProcessName
     *
     * @return String
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * setProcessName
     *
     * @param processName processName
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
