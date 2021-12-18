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
 * Config Struct
 *
 * @since 2021/11/02
 */
public class ConfigInfo {
    private static final ConfigInfo CONFIG_INFO = new ConfigInfo();

    private PerfConfig perfConfig;
    private AppTraceConfig appTraceConfig;
    private NativeHookConfigInfo nativeHookConfigInfo;

    /**
     * single instance
     *
     * @return this
     */
    public static ConfigInfo getInstance() {
        return ConfigInfo.CONFIG_INFO;
    }

    /**
     * get Perf config
     *
     * @param isLeakOhos isLeakOhos
     * @return perf config struct
     */
    public PerfConfig getPerfConfig(boolean isLeakOhos) {
        if (perfConfig == null) {
            restorePerfDefault(isLeakOhos);
        }
        return perfConfig;
    }

    /**
     * set perfConfig
     *
     * @param perfConfig perf config struct
     */
    public void setPerfConfig(PerfConfig perfConfig) {
        this.perfConfig = perfConfig;
    }

    /**
     * init struct
     *
     * @param isLeakOhos isLeakOhos
     * @return new PerfConfig
     */
    public PerfConfig restorePerfDefault(boolean isLeakOhos) {
        PerfConfig config = new PerfConfig();
        if (isLeakOhos) {
            config.setCallStack(PerfConfig.CallStack.DWARF.getIndex());
            config.setOffCpu(true);
        } else {
            config.setCallStack(PerfConfig.CallStack.NONE.getIndex());
            config.setOffCpu(false);
        }
        setPerfConfig(config);
        return perfConfig;
    }

    /**
     * restoreAppTraceDefault
     *
     * @return AppTraceConfig
     */
    public AppTraceConfig restoreAppTraceDefault() {
        setAppTraceConfig(new AppTraceConfig());
        return appTraceConfig;
    }

    /**
     * getAppTraceConfig
     *
     * @return appTraceConfig
     */
    public AppTraceConfig getAppTraceConfig() {
        if (appTraceConfig == null) {
            appTraceConfig = restoreAppTraceDefault();
        }
        return appTraceConfig;
    }

    /**
     * setAppTraceConfig
     *
     * @param appTraceConfig appTraceConfig
     */
    public void setAppTraceConfig(AppTraceConfig appTraceConfig) {
        this.appTraceConfig = appTraceConfig;
    }

    /**
     * restoreNativeHookConfigInfoDefault
     *
     * @return NativeHookConfigInfo
     */
    public NativeHookConfigInfo restoreNativeHookConfigInfoDefault() {
        setNativeHookConfigInfo(new NativeHookConfigInfo());
        return nativeHookConfigInfo;
    }

    /**
     * getAppTraceConfig
     *
     * @return appTraceConfig
     */
    public NativeHookConfigInfo getNativeHookConfigInfo() {
        if (nativeHookConfigInfo == null) {
            nativeHookConfigInfo = restoreNativeHookConfigInfoDefault();
        }
        return nativeHookConfigInfo;
    }

    /**
     * setNativeHookInfoConfig
     *
     * @param nativeHookConfigInfo nativeHookConfigInfo
     */
    public void setNativeHookConfigInfo(NativeHookConfigInfo nativeHookConfigInfo) {
        this.nativeHookConfigInfo = nativeHookConfigInfo;
    }
}
