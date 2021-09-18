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

/**
 * Enumeration class of chart type
 *
 * @since 2021/2/1 9:31
 */
public enum ChartType {
    /**
     * Filled poly line chart
     */
    FILLED_LINE,

    /**
     * Poly line chart
     */
    LINE,

    /**
     * Bar chart
     */
    BAR,

    /**
     * Bar chart
     */
    RECT,

    /**
     * unknown type
     */
    UNRECOGNIZED
}
