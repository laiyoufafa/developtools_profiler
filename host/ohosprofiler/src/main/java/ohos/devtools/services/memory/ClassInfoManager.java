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

import java.util.List;

/**
 * 类信息业务处理对象
 *
 * @version 1.0
 * @date 2021/04/05 11:00
 **/
public class ClassInfoManager {
    private final ClassInfoDao classInfoDao = ClassInfoDao.getInstance();

    /**
     * insertClassInfo
     *
     * @param classInfo classInfo
     */
    public void insertClassInfo(ClassInfo classInfo) {
        classInfoDao.insertClassInfo(classInfo);
    }

    /**
     * 获取所有的数据至文件
     *
     * @param sessionId sessionId
     * @return List<ClassInfo>
     */
    public List<ClassInfo> getAllClassInfoData(Long sessionId) {
        return classInfoDao.getAllClassInfoData(sessionId);
    }

    /**
     * 获取所有的数据至文件
     *
     * @param sessionId sessionId
     * @return List<ClassInfo>
     */
    public int getClassIdByClassName(String className) {
        return classInfoDao.getClassIdByClassName(className);
    }
}
