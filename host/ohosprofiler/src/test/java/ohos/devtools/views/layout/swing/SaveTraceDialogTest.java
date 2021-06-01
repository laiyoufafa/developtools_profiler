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
import ohos.devtools.views.common.hoscomp.HosJButton;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import javax.swing.JPanel;
import java.awt.Dimension;

/**
 * Save Trace Dialog Test
 *
 * @version 1.0
 * @date 2021/4/2 13:00
 **/
public class SaveTraceDialogTest {
    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SaveTraceDialog_getSaveTraceDialog_0001
     * @tc.desc: chart trace test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getSaveTraceDialog() {
        try {
            JPanel fileJpanel = new JPanel(null);
            fileJpanel.setPreferredSize(new Dimension(LayoutConstants.FOUR_HUNDRED, LayoutConstants.TWO_HUNDRED_SIXTY));
            fileJpanel.setBackground(ColorConstants.HOME_PANE);
            SampleDialogWrapper mock = PowerMockito.mock(SampleDialogWrapper.class);
            PowerMockito.whenNew(SampleDialogWrapper.class).withArguments("Save The Task", fileJpanel).thenReturn(mock);
            SaveTraceDialog saveTraceDialog = new SaveTraceDialog();
            saveTraceDialog.showCustomDialog(new HosJButton("dd", "ddd"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}