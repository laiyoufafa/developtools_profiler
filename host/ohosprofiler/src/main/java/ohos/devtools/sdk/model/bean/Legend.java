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

package ohos.devtools.sdk.model.bean;

import java.awt.Color;

/**
 * legend Color Block In Chart
 *
 * @since 2021/3/1 9:48
 */
public class Legend {
    /**
     * legend name
     */
    private String legendName;

    private Color legendColor;

    private int legendWidth;

    private int legendHeight;

    private int index;

    public String getLegendName() {
        return legendName;
    }

    public void setLegendName(String legendName) {
        this.legendName = legendName;
    }

    public Color getLegendColor() {
        return legendColor;
    }

    public void setLegendColor(Color legendColor) {
        this.legendColor = legendColor;
    }

    public int getLegendWidth() {
        return legendWidth;
    }

    public void setLegendWidth(int legendWidth) {
        this.legendWidth = legendWidth;
    }

    public int getLegendHeight() {
        return legendHeight;
    }

    public void setLegendHeight(int legendHeight) {
        this.legendHeight = legendHeight;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Legend{" + "legendName='" + legendName + '\'' + ", legendColor=" + legendColor + ", legendWidth="
            + legendWidth + ", legendHeight=" + legendHeight + ", index=" + index + '}';
    }
}
