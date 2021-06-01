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

import ohos.devtools.datasources.utils.monitorconfig.service.MonitorConfigManager;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.swing.SampleDialogWrapper;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @Description HosDialog test
 * @Date 2021/4/3 20:29
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest({SampleDialogWrapper.class, HosDialog.class, LogManager.class})
@PowerMockIgnore({"javax.swing.*"})
public class HosDialogTest {
    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_HosDialog_getSessionInfo_0001
     * @tc.desc: chart HosDialog test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     * @throws Exception throw Exception
     */
    @Test
    public void getSessionInfo() throws Exception {
        SampleDialogWrapper mock = PowerMockito.mock(SampleDialogWrapper.class);
        PowerMockito.mockStatic(LogManager.class);
        PowerMockito.whenNew(SampleDialogWrapper.class).withAnyArguments().thenReturn(mock);
        Mockito.when(mock.showAndGet()).thenReturn(true);
        HashMap<String, LinkedList<String>> stringLinkedListHashMap = new HashMap<>();
        LinkedList<String> strings = new LinkedList<>();
        strings.add("java");
        strings.add("native");
        stringLinkedListHashMap.put("Memory", strings);
        MonitorConfigManager.dataMap.put(23478L, stringLinkedListHashMap);
        HosDialog hosDialog = new HosDialog(23478L, new ProfilerChartsView(23478L, false, new TaskScenePanelChart()));
        HosDialog hosDialog1 = new HosDialog(23478L, new ProfilerChartsView(23478L, true, new TaskScenePanelChart()));
        hosDialog.getSessionInfo();
    }
}