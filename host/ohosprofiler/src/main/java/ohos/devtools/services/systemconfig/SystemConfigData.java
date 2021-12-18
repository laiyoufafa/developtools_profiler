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

package ohos.devtools.services.systemconfig;

import java.util.ArrayList;

/**
 * SystemConfigData
 *
 * @since 2021/11/25
 */
public class SystemConfigData {
    private ArrayList<String> eventsList;
    private ArrayList<ArrayList<String>> hTraceEventsList;
    private int maxDuration;
    private int inMemoryValue;
    private boolean memoryInfo;
    private boolean vmemoryInfo;
    private String hilogLevel;

    public ArrayList<String> getEventsList() {
        return eventsList;
    }

    public void setEventsList(ArrayList<String> eventsList) {
        this.eventsList = eventsList;
    }

    public ArrayList<ArrayList<String>> gethTraceEventsList() {
        return hTraceEventsList;
    }

    public void sethTraceEventsList(ArrayList<ArrayList<String>> hTraceEventsList) {
        this.hTraceEventsList = hTraceEventsList;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public int getInMemoryValue() {
        return inMemoryValue;
    }

    public void setInMemoryValue(int inMemoryValue) {
        this.inMemoryValue = inMemoryValue;
    }

    public boolean isMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(boolean memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public boolean isVmemoryInfo() {
        return vmemoryInfo;
    }

    public void setVmemoryInfo(boolean vmemoryInfo) {
        this.vmemoryInfo = vmemoryInfo;
    }

    public String getHilogLevel() {
        return hilogLevel;
    }

    public void setHilogLevel(String hilogLevel) {
        this.hilogLevel = hilogLevel;
    }
}
