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
 * 实例对象业务处理类
 *
 * @version 1.0
 * @date 2021/03/31 19:44
 **/
public class MemoryInstanceManager {
    /**
     * 根据类的Id在数据库中查到相应的instance信息，提供给UI的查询接口
     *
     * @param cId       父Id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return ArrayList<MemoryInstanceInfo>
     */
    public ArrayList<MemoryInstanceInfo> getMemoryInstanceInfos(Integer cId, Long startTime, Long endTime) {
        MemoryInstanceDao memoryInstanceDao = MemoryInstanceDao.getInstance();
        return memoryInstanceDao.getMemoryInstanceInfos(cId, startTime, endTime);
    }

    /**
     * insertMemoryInstanceInfo
     *
     * @param memoryInstanceInfo memoryInstanceInfo
     */
    public void insertMemoryInstanceInfo(MemoryInstanceInfo memoryInstanceInfo) {
        MemoryInstanceDao memoryInstanceDao = MemoryInstanceDao.getInstance();
        memoryInstanceDao.insertMemoryInstanceInfo(memoryInstanceInfo);
    }

    /**
     * 根据类的查instance信息
     *
     * @return ArrayList<MemoryInstanceInfo>
     */
    public ArrayList<MemoryInstanceInfo> getAllMemoryInstanceInfos() {
        MemoryInstanceDao memoryInstanceDao = MemoryInstanceDao.getInstance();
        return memoryInstanceDao.getAllMemoryInstanceInfos();
    }
}
