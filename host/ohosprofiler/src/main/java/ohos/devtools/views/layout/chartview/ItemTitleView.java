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

package ohos.devtools.views.layout.chartview;

import javax.swing.JLabel;
import java.awt.Dimension;

import static ohos.devtools.views.common.ViewConstants.LABEL_DEFAULT_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.LABEL_DEFAULT_WIDTH;

/**
 * 指标项标题面板
 *
 * @see "包含名称（Memory，CPU等）"
 * @since 2021/2/8 9:39
 */
public class ItemTitleView extends AbsItemTitleView {
    /**
     * 构造函数
     *
     * @param bottomPanel 最底层面板
     * @param name        指标项名称
     */
    public ItemTitleView(ProfilerChartsView bottomPanel, String name) {
        super(bottomPanel, name);
        initTitle();
    }

    private void initTitle() {
        JLabel title = new JLabel(this.getName());
        title.setPreferredSize(new Dimension(LABEL_DEFAULT_WIDTH, LABEL_DEFAULT_HEIGHT));
        this.add(title);
    }

}
