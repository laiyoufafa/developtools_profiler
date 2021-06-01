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
import java.util.List;

/**
 * Instance对应的调用栈信息处理对象
 *
 * @version 1.0
 * @date 2021/03/31 21:07
 **/
public class MemoryInstanceDetailsManager {
    private final MemoryInstanceDetailsDao memoryInstanceDetailsDao = MemoryInstanceDetailsDao.getInstance();

    /**
     * 根据instanceId在数据库中查到instance对应的调用信息，提供给UI的查询接口
     *
     * @param instanceId 实例Id
     * @return ArrayList<MemoryInstanceDetailsInfo>
     */
    public ArrayList<MemoryInstanceDetailsInfo> getMemoryInstanceDetailsInfos(Integer instanceId) {
        return memoryInstanceDetailsDao.getMemoryInstanceDetails(instanceId);
    }

    /**
     * insertMemoryInstanceDetailsInfo
     *
     * @param memoryInstanceDetailsInfo memoryInstanceDetailsInfo
     */
    public void insertMemoryInstanceDetailsInfo(MemoryInstanceDetailsInfo memoryInstanceDetailsInfo) {
        memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(memoryInstanceDetailsInfo);
    }

    /**
     * 根据instanceId在数据库中查到instance对应的调用信息
     *
     * @return ArrayList<MemoryInstanceDetailsInfo>
     */
    public List<MemoryInstanceDetailsInfo> getAllMemoryInstanceDetails() {
        return memoryInstanceDetailsDao.getAllMemoryInstanceDetails();
    }

}
