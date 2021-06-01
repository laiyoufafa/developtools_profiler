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

package ohos.devtools.services.memory;

import java.util.ArrayList;

/**
 * agent数据处理对象
 *
 * @version 1.0
 * @date 2021/03/30 10:11
 **/
public class MemoryHeapManager {
    private final MemoryHeapDao memoryHeapDao = MemoryHeapDao.getInstance();

    /**
     * 获取框选时间段内的数据，提供给UI的堆信息查询接口
     *
     * @param sessionId sessionId
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return ArrayList<MemoryHeapInfo>
     * @date 2021/03/30 20:25
     */
    public ArrayList<MemoryHeapInfo> getMemoryHeapInfos(Long sessionId, Long startTime, Long endTime) {
        return memoryHeapDao.getMemoryHeapInfos(sessionId, startTime, endTime);
    }

    /**
     * 获取MemoryHeapInfos的数据
     *
     * @param sessionId sessionId
     * @return ArrayList<MemoryHeapInfo>
     */
    public ArrayList<MemoryHeapInfo> getAllMemoryHeapInfos(Long sessionId) {
        return memoryHeapDao.getAllMemoryHeapInfos(sessionId);
    }

}
