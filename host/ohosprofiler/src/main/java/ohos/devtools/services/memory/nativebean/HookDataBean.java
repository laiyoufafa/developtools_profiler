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

package ohos.devtools.services.memory.nativebean;

import ohos.devtools.views.layout.chartview.memory.nativehook.NativeHookTreeTableRenderer;

import java.util.Objects;

/**
 * HookDataBean
 *
 * @since: 2021/8/20
 */
public class HookDataBean {
    private String hookMethodName;
    private int hookAllocationCount;
    private int hookDeAllocationCount;
    private long hookAllocationMemorySize;
    private long hookDeAllocationMemorySize;
    private String hookModuleName = "";
    private NativeHookTreeTableRenderer.HookDataBeanEnum beanEnum;
    private int containType = 0; // 0 OK 1 There are keywords 2 children there are keywords 3 there are no keywords

    public String getHookMethodName() {
        return hookMethodName;
    }

    public void setHookMethodName(String hookMethodName) {
        this.hookMethodName = hookMethodName;
    }

    public int getHookAllocationCount() {
        return hookAllocationCount;
    }

    public void setHookAllocationCount(int hookAllocationCount) {
        this.hookAllocationCount = hookAllocationCount;
    }

    public int getHookDeAllocationCount() {
        return hookDeAllocationCount;
    }

    public void setHookDeAllocationCount(int hookDeAllocationCount) {
        this.hookDeAllocationCount = hookDeAllocationCount;
    }

    public long getHookAllocationMemorySize() {
        return hookAllocationMemorySize;
    }

    public void setHookAllocationMemorySize(long hookAllocationMemorySize) {
        this.hookAllocationMemorySize = hookAllocationMemorySize;
    }

    public long getHookDeAllocationMemorySize() {
        return hookDeAllocationMemorySize;
    }

    public void setHookDeAllocationMemorySize(long hookDeAllocationMemorySize) {
        this.hookDeAllocationMemorySize = hookDeAllocationMemorySize;
    }

    public String getHookModuleName() {
        return hookModuleName;
    }

    public void setHookModuleName(String hookModuleName) {
        this.hookModuleName = hookModuleName;
    }

    /**
     * getTotalCount
     *
     * @return int
     */
    public int getTotalCount() {
        return getHookAllocationCount() - getHookDeAllocationCount();
    }

    /**
     * get Rea min Size
     *
     * @return long
     */
    public long getReaminSize() {
        return getHookAllocationMemorySize() - getHookDeAllocationMemorySize();
    }

    public NativeHookTreeTableRenderer.HookDataBeanEnum getBeanEnum() {
        return beanEnum;
    }

    public void setBeanEnum(NativeHookTreeTableRenderer.HookDataBeanEnum beanEnum) {
        this.beanEnum = beanEnum;
    }

    public int getContainType() {
        return containType;
    }

    public void setContainType(int containType) {
        this.containType = containType;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return hookMethodName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hookMethodName, hookAllocationCount, hookDeAllocationCount, hookAllocationMemorySize,
            hookDeAllocationMemorySize, hookModuleName, beanEnum, containType);
    }
}
