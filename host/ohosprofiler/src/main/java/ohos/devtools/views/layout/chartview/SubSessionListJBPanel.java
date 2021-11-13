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

package ohos.devtools.views.layout.chartview;

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.common.customcomp.CustomJLabel;

/**
 * SubSessionListJBPanel
 */
public class SubSessionListJBPanel extends JBPanel {
    private String startTime;

    private CustomJLabel hosJLabel;

    private CustomJLabel timeJLabel;

    private String panelName;

    private String dbPath;

    public CustomJLabel getHosJLabel() {
        return hosJLabel;
    }

    public void setHosJLabel(CustomJLabel hosJLabel) {
        this.hosJLabel = hosJLabel;
    }

    public CustomJLabel getTimeJLabel() {
        return timeJLabel;
    }

    public void setTimeJLabel(CustomJLabel timeJLabel) {
        this.timeJLabel = timeJLabel;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPanelName() {
        return panelName;
    }

    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }
}
