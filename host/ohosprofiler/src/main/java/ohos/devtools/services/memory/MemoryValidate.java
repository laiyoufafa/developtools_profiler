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

import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.utils.common.util.Validate;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.pluginconfig.MemoryConfig;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.agentdao.MemoryUpdateInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Validate
 */
public class MemoryValidate extends Validate {
    private boolean registerMemory;
    private List<ProcessMemInfo> processMemInfoList;
    private List<ClassInfo> classInfos;
    private List<MemoryHeapInfo> memoryHeapInfos;
    private List<MemoryUpdateInfo> instanceInfos;
    private List<MemoryInstanceDetailsInfo> detailsInfos;
    private MemoryTable memoTable;
    private ClassInfoDao classInfoDao;
    private MemoryHeapDao memoryHeapDao;
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;

    public MemoryValidate() {
        processMemInfoList = new ArrayList();
        classInfos = new ArrayList<>();
        memoryHeapInfos = new ArrayList<>();
        instanceInfos = new ArrayList<>();
        detailsInfos = new ArrayList<>();
        memoTable = new MemoryTable();
        memoryHeapDao = new MemoryHeapDao();
        memoryInstanceDao = new MemoryInstanceDao();
        classInfoDao = new ClassInfoDao();
        memoryInstanceDetailsDao = new MemoryInstanceDetailsDao();
    }

    @Override
    public <T> boolean validate(T data) {
        if (data instanceof ClassInfo) {
            return true;
        } else if (data instanceof MemoryHeapInfo) {
            return true;
        } else if (data instanceof MemoryInstanceDetailsInfo) {
            return true;
        } else if (data instanceof MemoryUpdateInfo) {
            return true;
        } else if (data instanceof ProcessMemInfo) {
            ProcessMemInfo processMem = (ProcessMemInfo) data;
            if (!registerMemory) {
                PlugManager.getInstance()
                    .addPluginStartSuccess(processMem.getLocalSessionId(), new MemoryConfig().createConfig());
                registerMemory = true;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public <T> void addToList(T data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            classInfos.add(classInfo);
        } else if (data instanceof MemoryHeapInfo) {
            MemoryHeapInfo memoryHeapInfo = (MemoryHeapInfo) data;
            memoryHeapInfos.add(memoryHeapInfo);
        } else if (data instanceof MemoryInstanceDetailsInfo) {
            MemoryInstanceDetailsInfo detailsInfo = (MemoryInstanceDetailsInfo) data;
            detailsInfos.add(detailsInfo);
        } else if (data instanceof MemoryUpdateInfo) {
            MemoryUpdateInfo memoryInstanceInfo = (MemoryUpdateInfo) data;
            instanceInfos.add(memoryInstanceInfo);
        } else if (data instanceof ProcessMemInfo) {
            ProcessMemInfo processMem = (ProcessMemInfo) data;
            processMemInfoList.add(processMem);
        } else {
            return;
        }
    }

    @Override
    public void batchInsertToDb() {
        classInfoDao.insertClassInfos(classInfos);
        memoryHeapDao.insertMemoryHeapInfos(memoryHeapInfos);
        memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(detailsInfos);
        memoryInstanceDao.insertMemoryInstanceInfos(instanceInfos);
        memoTable.insertProcessMemInfo(processMemInfoList);
        classInfos.clear();
        memoryHeapInfos.clear();
        detailsInfos.clear();
        instanceInfos.clear();
        processMemInfoList.clear();
    }

    /**
     * getMenInfoSize
     *
     * @return int
     */
    public int getMenInfoSize() {
        return processMemInfoList.size();
    }

}
