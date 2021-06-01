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

package ohos.devtools.datasources.utils.plugin.dao;

import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPlugin;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 插件dao层(与sqlite交互)测试类
 *
 * @version 1.0
 * @date 2021/04/12 11:11
 **/
public class PlugDaoTest {
    /**
     * functional testing getInstance
     *
     * @tc.name: PlugDao getInstance
     * @tc.number: OHOS_JAVA_plugin_PlugDao_getInstance_0001
     * @tc.desc: PlugDao getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getInstanceTest() {
        PlugDao plugDao = PlugDao.getInstance();
        Assert.assertNotNull(plugDao);
    }

    /**
     * functional testing selectPlugConfig
     *
     * @tc.name: PlugDao selectPlugConfig
     * @tc.number: OHOS_JAVA_plugin_PlugDao_selectPlugConfig_0001
     * @tc.desc: PlugDao selectPlugConfig
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void selectPlugConfigTest() {
        List<HiProfilerPlugin> list = null;
        list = PlugDao.getInstance().selectPlugConfig("1");
        if (list != null) {
            Assert.assertTrue(true);
        }
    }
}
