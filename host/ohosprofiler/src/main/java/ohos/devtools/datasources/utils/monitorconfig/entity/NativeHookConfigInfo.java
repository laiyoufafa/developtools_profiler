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
package ohos.devtools.datasources.utils.monitorconfig.entity;

/**
 * NativeHook Config  Struct.
 *
 * @since 2021/11/17
 */
public class NativeHookConfigInfo {
    private final String DEFAULT_MEMORY_BUFFER = "4:MB";
    private final String DEFAULT_FILTER_SIZE = "0:MB";
    private final int UNWIND_SIZE = 10;

    private String sharedMemorySize = DEFAULT_MEMORY_BUFFER;
    private String filterSize = DEFAULT_FILTER_SIZE;
    private int unwind = UNWIND_SIZE;


    /**
     * getSharedMemorySize
     *
     * @return String
     */
    public String getSharedMemorySize() {
        return sharedMemorySize;
    }

    /**
     * getSharedMemorySizeValue
     *
     * @return int
     */
    public int getSharedMemorySizeValue() {
        return convertToValue(sharedMemorySize);
    }

    /**
     * setSharedMemorySize
     *
     * @param sharedMemorySize sharedMemorySize
     */
    public void setSharedMemorySize(String sharedMemorySize) {
        this.sharedMemorySize = sharedMemorySize;
    }

    /**
     * getFilterSize
     *
     * @return String
     */
    public String getFilterSize() {
        return filterSize;
    }


    /**
     * getFilterSizeValue
     *
     * @return int
     */
    public int getFilterSizeValue() {
        return convertToValue(filterSize);
    }

    /**
     * setFilterSize
     *
     * @param filterSize filterSize
     */
    public void setFilterSize(String filterSize) {
        this.filterSize = filterSize;
    }

    /**
     * getUnwind
     *
     * @return int
     */
    public int getUnwind() {
        return unwind;
    }

    /**
     * setUnwind
     *
     * @param unwind unwind
     */
    public void setUnwind(int unwind) {
        this.unwind = unwind;
    }

    private int convertToValue(String size) {
        String[] split = size.split(":");
        int value;
        String unit = split[1];
        switch (unit) {
            case "MB":
                value = Integer.parseInt(split[0]) * 1024 * 1024;
                break;
            case "KB":
                value = Integer.parseInt(split[0]) * 1024;
                break;
            default:
                value = 0;
        }
        return value / 4096;
    }
}
