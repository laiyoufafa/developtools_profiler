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

package ohos.devtools.views.layout.swing;

import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.event.TaskPanelWelcomeEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * 二级欢迎界面
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class TaskPanelWelcome extends JPanel {
    /**
     * 新建实时任务按钮
     */
    private static JLabel jNewButton = new JLabel("+   New Task", JLabel.CENTER);

    /**
     * 新建按钮区域背景面板
     */
    private static JPanel colorJPanel = new JPanel(null);

    /**
     * 欢迎页面面板
     */
    private static JPanel welcomeJPanel = new JPanel(new BorderLayout());

    /**
     * 欢迎语
     */
    private static JLabel jLabelWelcome = new JLabel("<html><br/><p style=\"font-size:18px;font-weight:700;"
        + "text-align:center;color:white;\">Welcome HosProfiler</p><br/><p style=\"font-size:10px;text-align:center;"
        + "color:#757784;\">Click New Sessions Bu!on to process or load a capture.</p></html>", JLabel.CENTER);

    private TaskPanelWelcomeEvent taskPanelWelcomeEvent = new TaskPanelWelcomeEvent();

    public TaskPanelWelcome() {
        // 添加新建按钮背景面板和欢迎页面面板
        this.setLayout(new BorderLayout());
        this.add(colorJPanel, BorderLayout.NORTH);
        this.add(welcomeJPanel, BorderLayout.CENTER);
        // 设置新建按钮背景面板大小和颜色
        colorJPanel.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.DEVICE_WIDTH));
        colorJPanel.setBackground(ColorConstants.TOP_COLOR);
        // 将新建按钮添加进背景面板和欢迎图片标语添加进欢迎面板
        jNewButton.setOpaque(true);
        jNewButton.setBounds(LayoutConstants.MEMORY_X_TESTING, LayoutConstants.MEMORY_Y, LayoutConstants.JP_LEFT_WIDTH,
            LayoutConstants.CON_BOX_Y);
        Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT);
        jNewButton.setFont(font);
        jNewButton.setBackground(ColorConstants.CENTER_COLOR);
        jNewButton.setForeground(Color.white);
        colorJPanel.add(jNewButton);
        jLabelWelcome.setIcon(new ImageIcon(TaskPanelWelcome.class.getClassLoader().getResource("images/pic.png")));
        jLabelWelcome.setVerticalTextPosition(JLabel.BOTTOM);
        jLabelWelcome.setHorizontalTextPosition(JLabel.CENTER);
        welcomeJPanel.setBackground(ColorConstants.CENTER_COLOR);
        welcomeJPanel.add(jLabelWelcome);
        // +号新建实时任务按钮事件
        taskPanelWelcomeEvent.clickAddTask(this);
    }

    /**
     * 获取JNewButton按钮
     *
     * @return JButton
     */
    public JLabel getJNewButton() {
        return jNewButton;
    }
}
