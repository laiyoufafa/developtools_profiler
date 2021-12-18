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

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.net.URL;

import static java.awt.Image.SCALE_DEFAULT;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.LOADING_SIZE;

/**
 * Loading Panel
 *
 * @since 2021/11/22
 */
public class LoadingPanel extends JBPanel {
    /**
     * Constructor
     */
    public LoadingPanel() {
        this.setLayout(new BorderLayout());
        JBLabel loadingLabel;
        URL url = getClass().getClassLoader().getResource("images/loading.gif");
        if (url == null) {
            loadingLabel = new JBLabel("Loading...");
        } else {
            ImageIcon icon = new ImageIcon(url);
            icon.setImage(icon.getImage().getScaledInstance(LOADING_SIZE, LOADING_SIZE, SCALE_DEFAULT));
            loadingLabel = new JBLabel(icon, JBLabel.CENTER);
        }
        this.add(loadingLabel, BorderLayout.CENTER);
    }
}
