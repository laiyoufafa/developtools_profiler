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

package ohos.devtools.datasources.utils.plugin.entity;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * enum Analysis Type
 *
 * @since 2021/08/06 15:22
 */
public enum AnalysisType {
    APPLICATION_TYPE, SYSTEM_TYPE, DISTRIBUTED_TYPE, GPU_CONFIG_TYPE;

    /**
     * AnalysisType
     */
    AnalysisType() {
        if (ProfilerLogManager.isDebugEnabled()) {
            Logger LOGGER = LogManager.getLogger(AnalysisType.class);
            LOGGER.debug("AnalysisType init ");
        }
    }
}
