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

/**
 * Profiler的监控项
 *
 * @since 2021/11/22
 */
public class ProfilerMonitorItem {
    private int index;
    private final String name;
    private final Class<? extends MonitorItemView> clazz;

    /**
     * ProfilerMonitorItem
     *
     * @param index index
     * @param name name
     * @param clazz clazz
     */
    public ProfilerMonitorItem(int index, String name, Class<? extends MonitorItemView> clazz) {
        this.index = index;
        this.name = name;
        this.clazz = clazz;
    }

    /**
     * getName
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * getClazz
     *
     * @return Class <MonitorItemView>
     */
    public Class<? extends MonitorItemView> getClazz() {
        return clazz;
    }

    /**
     * getIndex
     *
     * @return int index
     */
    public int getIndex() {
        return index;
    }
}
