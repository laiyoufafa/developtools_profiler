/*
* Copyright (C) 2022 Huawei Device Co., Ltd.
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

/**
 * Provide interfaces related to debugger access and obtaining CPU, 
 * memory and other virtual machine information during runtime for JS programs
 *
 * @syscap SystemCapability.HiviewDFX.HiProfiler.HiDebug
 * @import import napi_hidebug from '@ohos.napi_hidebug'
 * @since 8
 */
declare namespace hidebug {
    /**
     * Get total native heap memory size
     * @since 8
     */
    function getNativeHeapSize() : bigint;

    /**
     * Get Native heap memory allocation size
     * @since 8
     */
    function getNativeHeapAllocatedSize() : bigint;

    /**
     * Get Native heap memory free size
     * @since 8
     */
    function getNativeHeapFreeSize() : bigint;


    /**
     * Get application process proportional set size memory information
     * @since 8
     */
    function getPss() : bigint;

    /**
     * Get process private dirty memory size
     * @since 8
     */
    function getSharedDirty() : bigint;

    /**
     * Start CPU Profiling
     * @since 8
     */
    function startProfiling(fileName : string) : void;

    /**
     * Stop CPU Profiling
     * @since 8
     */
    function stopProfiling() : void;

    /**
     * Dump JS Virtual Machine Heap Snapshot
     * @since 8
     */
    function dumpHeapData(fileName : string) : void;
}
export default hidebug;