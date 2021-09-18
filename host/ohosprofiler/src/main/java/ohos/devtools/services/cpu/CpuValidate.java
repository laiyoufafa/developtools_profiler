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

package ohos.devtools.services.cpu;

import ohos.devtools.datasources.databases.datatable.CpuTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.datasources.utils.common.util.Validate;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.pluginconfig.CpuConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Cpu Validate
 */
public class CpuValidate extends Validate {
    private boolean registerCpu;
    private CpuTable cpuTable = new CpuTable();
    private List<ProcessCpuData> cpuList = new ArrayList<>();

    /**
     * CpuValidate
     *
     */
    public CpuValidate() {
    }

    @Override
    public <T> boolean validate(T obj) {
        if (obj instanceof ProcessCpuData && !registerCpu) {
            ProcessCpuData processCpuData = (ProcessCpuData) obj;
            PlugManager.getInstance()
                .addPluginStartSuccess(processCpuData.getLocalSessionId(), new CpuConfig().createConfig());
            registerCpu = true;
        }
        return obj instanceof ProcessCpuData;
    }

    @Override
    public <T> void addToList(T obj) {
        ProcessCpuData processCpuData = (ProcessCpuData) obj;
        cpuList.add(processCpuData);
    }

    @Override
    public void batchInsertToDb() {
        cpuTable.insertProcessCpuInfo(cpuList);
        cpuList.clear();
    }
}
