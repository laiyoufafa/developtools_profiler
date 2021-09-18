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

package ohos.devtools.views.layout.chartview.event;

/**
 * Publisher of chart event
 */
public interface IChartEventPublisher {
    /**
     * Add observer
     *
     * @param observer Observer of chart refreshes event
     */
    void attach(IChartEventObserver observer);

    /**
     * Remove observer
     *
     * @param observer Observer of chart refreshes event
     */
    void detach(IChartEventObserver observer);

    /**
     * notify to refresh
     *
     * @param start Start time of chart
     * @param end   End time of chart
     */
    void notifyRefresh(int start, int end);
}
