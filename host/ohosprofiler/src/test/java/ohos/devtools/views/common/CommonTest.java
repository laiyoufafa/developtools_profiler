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

import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * @Description CommonTest
 * @Date 2021/4/5 13:15
 **/
public class CommonTest {
    private Common common;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_Common_init_0001
     * @tc.desc: chart util test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-003
     */
    @Before
    public void init() {
        common = new Common();
    }

    /**
     * update number
     *
     * @tc.name: 主界面-chart页面
     * @tc.number: OHOS_JAVA_views_Common_updateNum_0001
     * @tc.desc: chart页面工具相关功能接口测试
     * @tc.type: 功能测试
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void updateNum() {
        JPanel jPanel7 = new JPanel();
        JPanel jPanel4 = new JPanel();
        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel4);
        jPanel1.add(jPanel7);
        JPanel jPanel5 = new JPanel();
        JPanel jPanel2 = new JPanel();
        jPanel2.add(jPanel5);
        jPanel2.add(jPanel7);
        JPanel jPanel3 = new JPanel();
        JPanel jPanel6 = new JPanel();
        jPanel3.add(jPanel6);
        jPanel3.add(jPanel7);
        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.addTab("test01", jPanel1);
        jTabbedPane.addTab("test02", jPanel2);
        jTabbedPane.addTab("test03", jPanel3);
        common.updateNum(jTabbedPane);
    }
}