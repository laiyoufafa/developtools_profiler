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

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 自定义选项卡
 * @Date 2021/3/29 10:36
 **/
public class HosJTabbedPane extends JPanel {
    // tab选项卡容器
    private JPanel tabJPanel = new JPanel(null);

    // 当前选中的tab。
    private JPanel tab;

    // 所有tab的集合
    private List<JPanel> tabLists;

    // content选项卡对应内容的容器
    private JPanel tabContentJPanel;

    // 所有内容面板的集合
    private Map<Integer, JPanel> tabContentLists = new HashMap<>();
    private int count = 0;

    /**
     * HosJTabbedPane
     *
     * @param tab              tab
     * @param tabContentJPanel tabContentJPanel
     */
    public HosJTabbedPane(JPanel tab, JPanel tabContentJPanel) {
        this.tab = tab;
        this.tab.setPreferredSize(new Dimension(100, 20));
        this.tabJPanel.setBackground(Color.lightGray);
        this.tabJPanel.add(tab);
        this.tabJPanel.setBackground(new Color(41, 41, 48));
        this.tab.setBackground(new Color(13, 14, 19));
        this.tabContentJPanel = tabContentJPanel;
        this.tabContentJPanel.setBackground(new Color(13, 14, 19));
        this.tabContentLists.put(count, tabContentJPanel);
        this.setLayout(new BorderLayout());
        this.add(this.tabJPanel, BorderLayout.NORTH);
        this.add(this.tabContentJPanel, BorderLayout.CENTER);
        this.tab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
            }
        });
    }

    /**
     * hosAdd
     *
     * @param tab              tab
     * @param tabContentJPanel tabContentJPanel
     */
    public void hosAdd(JPanel tab, JPanel tabContentJPanel) {
        this.count++;
        this.tab = tab;
        this.tab.setBackground(new Color(13, 14, 19));
        this.tab.setPreferredSize(new Dimension(100, 20));
        this.tabJPanel.add(tab);
        this.tabContentJPanel = tabContentJPanel;
        this.tabContentJPanel.setBackground(new Color(13, 14, 19));
        this.tabContentLists.put(this.count, this.tabContentJPanel);
    }

    public JPanel getTabJPanel() {
        return tabJPanel;
    }

    public void setTabJPanel(JPanel tabJPanel) {
        this.tabJPanel = tabJPanel;
    }

    public JPanel getTab() {
        return tab;
    }

    public void setTab(JPanel tab) {
        this.tab = tab;
    }

    public List<JPanel> getTabLists() {
        return tabLists;
    }

    public void setTabLists(List<JPanel> tabLists) {
        this.tabLists = tabLists;
    }

    public JPanel getTabContentJPanel() {
        return tabContentJPanel;
    }

    public void setTabContentJPanel(JPanel tabContentJPanel) {
        this.tabContentJPanel = tabContentJPanel;
    }

    public Map<Integer, JPanel> getTabContentLists() {
        return tabContentLists;
    }

    public void setTabContentLists(Map<Integer, JPanel> tabContentLists) {
        this.tabContentLists = tabContentLists;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

class HosTab {
    private Integer id;
    private JPanel tab;

    public HosTab(Integer id, JPanel tab) {
        this.id = id;
        this.tab = tab;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public JPanel getTab() {
        return tab;
    }

    public void setTab(JPanel tab) {
        this.tab = tab;
    }
}