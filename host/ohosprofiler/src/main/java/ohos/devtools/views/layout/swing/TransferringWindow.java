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

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @Description TransferringWindow
 * @Date 2021/3/25 15:58
 **/
public class TransferringWindow extends JProgressBar {
    /**
     * TransferringWindow
     *
     * @param owner owner
     */
    public TransferringWindow(JPanel owner) {
        this.setBounds(LayoutConstants.TEN, owner.getHeight() - LayoutConstants.FORTY,
            owner.getWidth() - LayoutConstants.TWENTY, LayoutConstants.THIRTY);
        this.setMinimum(0);
        this.setMaximum(LayoutConstants.HUNDRED);
        this.setValue(0);
        this.setStringPainted(true);
        this.setForeground(ColorConstants.CYAN);
        this.setBackground(ColorConstants.BLACK);
        this.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
            }
        });
        owner.add(this);
    }
}
