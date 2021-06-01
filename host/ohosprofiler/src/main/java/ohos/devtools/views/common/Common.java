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

package ohos.devtools.views.common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Component;

/**
 * 公共方法类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class Common {
    public Common() {
    }

    /**
     * updateNum
     *
     * @param jTabbedPane jTabbedPane
     */
    public void updateNum(JTabbedPane jTabbedPane) {
        // 更新所有的run -- of --
        for (int index = 0; index < jTabbedPane.getTabCount(); index++) {
            Component component = jTabbedPane.getComponentAt(index);
            JPanel jScrollPanelCom = (JPanel) component;
            JPanel jScrollPanelComChart = null;
            Object componentsObj = jScrollPanelCom.getComponents()[0];
            if (componentsObj instanceof JPanel) {
                jScrollPanelComChart = (JPanel) componentsObj;
                // 定位到chart监测页面
                if (jScrollPanelComChart.getComponentCount() == LayoutConstants.DEVICES_Y) {
                    JPanel componentComAll = null;
                    Object compObj = jScrollPanelComChart.getComponents()[0];
                    if (compObj instanceof JPanel) {
                        componentComAll = (JPanel) compObj;
                        if (componentComAll.getComponents()[0] instanceof JLabel) {
                            continue;
                        } else {
                            JPanel componentComAllJpanelJlabel = (JPanel) componentComAll.getComponents()[0];
                            for (Component componentAll : componentComAllJpanelJlabel.getComponents()) {
                                if (componentAll instanceof JLabel) {
                                    JLabel jcb = (JLabel) componentAll;
                                    jcb.setText("Run " + (index + 1) + " of " + jTabbedPane.getTabCount());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
