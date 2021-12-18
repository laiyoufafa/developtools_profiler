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

package ohos.devtools.samplecode.services.samplecode;

import ohos.devtools.samplecode.datasources.databases.datatable.enties.SampleCodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * SampleCode Business processing
 *
 * @since 2021/11/24
 */
public class SampleCodeService {
    private static final Logger LOGGER = LogManager.getLogger(SampleCodeService.class);
    private static final SampleCodeService SAMPLE_CODE_SERVICE = new SampleCodeService();

    private SampleCodeService() {
        super();
    }

    /**
     * SampleCodeService
     *
     * @return MemoryService
     */
    public static SampleCodeService getSampleCodeService() {
        return SampleCodeService.SAMPLE_CODE_SERVICE;
    }

    /**
     * Get all data
     *
     * @param sessionId sessionId
     * @return List <SampleCodeInfo>
     */
    public List<SampleCodeInfo> getAllData(long sessionId) {
        return SampleCodeDao.getInstance().getAllData(sessionId);
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        return SampleCodeDao.getInstance().deleteSessionData(sessionId);
    }
}
