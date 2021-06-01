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
 * Chart事件发布者
 *
 * @ClassName: IChartEventPublisher
 * @since 2021/1/26 19:32
 */
public interface IChartEventPublisher {
    /**
     * 添加监听者
     *
     * @param listener IChartEventListener
     */
    void attach(IChartEventObserver listener);

    /**
     * 移除监听者
     *
     * @param listener IChartEventListener
     */
    void detach(IChartEventObserver listener);

    /**
     * 通知刷新
     *
     * @param start 开始时间
     * @param end   结束时间
     */
    void notifyRefresh(int start, int end);
}
