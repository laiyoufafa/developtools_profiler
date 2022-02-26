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

import com.intellij.ui.components.JBLabel;

/**
 * HosJLabel
 *
 * @since 2021/5/19 16:39
 */
public class CustomJLabel extends JBLabel {
    private long sessionId;
    private String deviceName;
    private String processName;
    private String message;
    private String connectType;
    private boolean isOnline = true;
    private long startTime;
    private long endTime;
    private CustomJLabel left;
    private String fileType;
    private String cardName;

    /**
     * CustomJLabel
     */
    public CustomJLabel() {
        super("", null, LEADING);
    }

    /**
     * CustomJLabel
     *
     * @param text text
     */
    public CustomJLabel(String text) {
        super(text);
    }

    /**
     * isOnline
     *
     * @return boolean
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * setOnline
     *
     * @param deviceType deviceType
     */
    public void setOnline(boolean deviceType) {
        this.isOnline = deviceType;
    }

    /**
     * getStartTime
     *
     * @return long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * setStartTime
     *
     * @param startTime startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * getEndTime
     *
     * @return long
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * setEndTime
     *
     * @param endTime endTime
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * getMessage
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * setMessage
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
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

    /**
     * getConnectType
     *
     * @return String
     */
    public String getConnectType() {
        return connectType;
    }

    /**
     * setConnectType
     *
     * @param connectType connectType
     */
    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }

    /**
     * getLeft
     *
     * @return CustomJLabel
     */
    public CustomJLabel getLeft() {
        return left;
    }

    /**
     * setLeft
     *
     * @param left left
     */
    public void setLeft(CustomJLabel left) {
        this.left = left;
    }

    /**
     * getFileType
     *
     * @return String
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * setFileType
     *
     * @param fileType fileType
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * getCardName
     *
     * @return cardName
     */
    public String getCardName() {
        return cardName;
    }

    /**
     * setCardName
     *
     * @param cardName cardName
     */
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    /**
     * toString
     *
     * @return String
     */
    @Override
    public String toString() {
        return "CustomJLabel{" + "sessionId=" + sessionId + ", deviceName='" + deviceName + '\'' + ", processName='"
            + processName + '\'' + ", message='" + message + '\'' + ", connectType='" + connectType + '\''
            + ", isOnline=" + isOnline + ", startTime=" + startTime + ", endTime=" + endTime + ", left=" + left
            + ", fileType='" + fileType + '\'' + ", cardName='" + cardName + '\'' + '}';
    }
}
