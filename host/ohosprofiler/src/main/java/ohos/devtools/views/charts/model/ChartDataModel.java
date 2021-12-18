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

package ohos.devtools.views.charts.model;

import java.awt.Color;

/**
 * Chart data model
 *
 * @since 2021/3/10
 */
public class ChartDataModel {
    private int index;

    private String name;

    private double doubleValue;

    private Color color;

    private int value;

    private String unit;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double cpuPercent) {
        this.doubleValue = cpuPercent;
    }

    public String getUnit() {
        return unit == null ? "" : unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "ChartDataModel{" + "index=" + index + ", name='" + name + '\'' + ", doubleValue=" + doubleValue
            + ", color=" + color + ", value=" + value + '}';
    }
}
