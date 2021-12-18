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

package ohos.devtools.views.trace.component;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBInsets;

import java.awt.BorderLayout;

/**
 * SysAnalystPanel component
 *
 * @since 2021/04/20 12:24
 */
public class SysAnalystPanel extends JBPanel {
    /**
     * splitter
     */
    private static JBSplitter splitter;

    /**
     * bottom tab
     */
    private TabPanel tab;

    private AnalystPanel analystPanel;

    /**
     * constructor
     */
    public SysAnalystPanel() {
        setLayout(new BorderLayout());
        analystPanel = new AnalystPanel();
        tab = new TabPanel();
        tab.setVisible(false);
        tab.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        tab.setTabComponentInsets(JBInsets.create(0, 0));
        analystPanel.setTab(tab);
        splitter = new JBSplitter(true);
        splitter.setFirstComponent(analystPanel);
        splitter.setSecondComponent(tab);
        splitter.setHonorComponentsMinimumSize(false);
        add(splitter, BorderLayout.CENTER);
    }

    /**
     * getAnalystPanel
     *
     * @return AnalystPanel
     */
    public AnalystPanel getAnalystPanel() {
        return analystPanel;
    }

    /**
     * load database
     *
     * @param name db name
     * @param isLocal is local db
     */
    public void load(final String name, final boolean isLocal) {
        analystPanel.load(name, isLocal);
    }

    /**
     * getSplitter
     *
     * @return JBSplitter
     */
    public static JBSplitter getSplitter() {
        return splitter;
    }

    /**
     * setSplitter
     *
     * @param splitter splitter
     */
    public static void setSplitter(JBSplitter splitter) {
        SysAnalystPanel.splitter = splitter;
    }
}
