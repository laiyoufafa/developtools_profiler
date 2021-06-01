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

import ohos.devtools.views.common.Common;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.layout.swing.TaskPanel;
import ohos.devtools.views.layout.swing.TaskPanelWelcome;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 二级欢迎界面事件处理对象
 *
 * @version 1.0
 * @date 2021/03/01 15:20
 **/
public class TaskPanelWelcomeEvent {
    // 初始化公共方法类
    private Common common = new Common();

    /**
     * 给新增实时任务按钮添加事件
     *
     * @param taskPanelWelcome taskPanelWelcome
     * @date 2021/03/01 15:20
     */
    public void clickAddTask(TaskPanelWelcome taskPanelWelcome) {
        taskPanelWelcome.getJNewButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                // 获取Jframe和taskPanel二级界面容器
                JPanel taskPanel = null;
                Object obj = taskPanelWelcome.getParent();
                if (obj instanceof JPanel) {
                    taskPanel = (JPanel) obj;
                    taskPanel.remove(taskPanelWelcome);
                    if (Constant.jtasksTab == null || (Constant.jtasksTab != null
                        && Constant.jtasksTab.getTabCount() == 0)) {
                        Constant.jtasksTab = new JTabbedPane();
                    }
                    new TaskPanel(taskPanel, taskPanelWelcome);
                    // 更新所有的run -- of --
                    common.updateNum(Constant.jtasksTab);
                    taskPanel.updateUI();
                    taskPanel.repaint();
                }
            }

        });
    }
}
