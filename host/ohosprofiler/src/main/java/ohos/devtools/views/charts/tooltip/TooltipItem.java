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

package ohos.devtools.views.charts.tooltip;

import java.awt.Color;

/**
 * Tooltip中的要显示的图例
 *
 * @since 2021/1/19 21:35
 */
public class TooltipItem {
    /**
     * Tooltip中图例色块的颜色
     */
    private Color color;

    /**
     * 文本
     */
    private String text;

    private String name;

    /**
     * 构造函数
     *
     * @param color Tooltip中图例色块的颜色
     * @param text tips文本
     */
    public TooltipItem(Color color, String text) {
        this.color = color;
        this.text = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
