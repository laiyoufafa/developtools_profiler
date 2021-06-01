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

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.Common;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.layout.swing.HomeWindow;
import ohos.devtools.views.layout.swing.TaskPanel;
import org.apache.logging.log4j.Level;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 一级界面事件处理对象
 *
 * @version 1.0
 * @date 2021/03/01 15:20
 **/
public class HomeWindowEvent {
    // 初始化公共方法类
    private Common common = new Common();

    /**
     * clickAddTask
     *
     * @param homeWindow homeWindow
     */
    public void clickAddTask(HomeWindow homeWindow) {
        homeWindow.getJNewRealTimeTaskJMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                homeWindow.getTaskPanel().removeAll();
                if (Constant.jtasksTab == null || (Constant.jtasksTab != null
                    && Constant.jtasksTab.getTabCount() == 0)) {
                    Constant.jtasksTab = new JTabbedPane();
                }
                new TaskPanel(homeWindow.getTaskPanel(), homeWindow.getTaskPanelWelcome());
                // 更新所有的run -- of --
                common.updateNum(Constant.jtasksTab);
                homeWindow.setVisible(true);
            }
        });
    }

    /**
     * clickUpdateLogLevel
     *
     * @param homeWindow homeWindow
     */
    public void clickUpdateLogLevel(HomeWindow homeWindow) {
        JMenuItem switchLog = HomeWindow.getJLogSwitch();
        switchLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent exception) {
                Level logLevel = ProfilerLogManager.getSingleton().getNowLogLevel();
                if (Level.ERROR.equals(logLevel)) {
                    ProfilerLogManager.getSingleton().updateLogLevel(Level.DEBUG);
                    switchLog.setIcon(new ImageIcon(
                        TaskScenePanelChartEvent.class.getClassLoader()
                            .getResource("images/selected.png")));
                } else {
                    ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
                    switchLog.setIcon(null);
                }
            }
        });
    }
}
