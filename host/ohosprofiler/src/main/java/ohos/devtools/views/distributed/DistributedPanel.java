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

package ohos.devtools.views.distributed;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.distributed.bean.DistributedParams;

import java.awt.BorderLayout;

/**
 * DistributedPanel
 *
 * @since 2021/7/6 15:36
 */
public class DistributedPanel extends JBPanel {
    /**
     * slitter
     */
    private static JBSplitter splitter;

    /**
     * bottom tab
     */
    private static DistributedDataPane tab;

    private DistributedChartPanel chartPanel;

    /**
     * constructor
     */
    public DistributedPanel() {
        setLayout(new BorderLayout());
        chartPanel = new DistributedChartPanel();
        splitter = new JBSplitter(true);
        add(splitter, BorderLayout.CENTER);
    }

    /**
     * load by paramstest
     *
     * @param params params
     */
    public void load(DistributedParams params) {
        chartPanel.load(params);
        tab = new DistributedDataPane(it -> {
            tab.setVisible(false);
            splitter.setProportion(1.0f);
        }, rectangle -> {
            rectangle.getWidth();
        });
        splitter.setFirstComponent(chartPanel);
        splitter.setSecondComponent(tab);
        splitter.setHonorComponentsMinimumSize(false);
        splitter.setProportion(1.0f);
    }

    public static JBSplitter getSplitter() {
        return splitter;
    }

    public static DistributedDataPane getTab() {
        return tab;
    }
}
