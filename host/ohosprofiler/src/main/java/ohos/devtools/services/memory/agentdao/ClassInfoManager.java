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

package ohos.devtools.services.memory.agentdao;

import ohos.devtools.services.memory.agentbean.ClassInfo;

import java.util.List;

/**
 * ClassInfo Manager
 */
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
     * Get all data to file
     *
     * @param sessionId sessionId
     * @return List <ClassInfo>
     */
    public List<ClassInfo> getAllClassInfoData(Long sessionId) {
        return classInfoDao.getAllClassInfoData(sessionId);
    }

    /**
     * get ClassId By ClassName
     *
     * @param className className
     * @return int
     */
    public int getClassIdByClassName(String className) {
        return classInfoDao.getClassIdByClassName(className);
    }
}
