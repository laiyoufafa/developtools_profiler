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

package ohos.devtools.services.diskio;

import ohos.devtools.datasources.databases.datatable.DiskIoTable;
import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
import ohos.devtools.datasources.utils.common.util.Validate;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.pluginconfig.DiskIoConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * DiskIo Validate
 *
 * @since: 2021/9/20
 */
public class DiskIoValidate extends Validate {
    private boolean registerDisk;
    private DiskIoTable diskIoTable;
    private List<DiskIOData> sysDiskIoInfoList;

    public DiskIoValidate() {
        diskIoTable = new DiskIoTable();
        sysDiskIoInfoList = new ArrayList();
    }

    @Override
    public <T> boolean validate(T obj) {
        if (!registerDisk && obj instanceof DiskIOData) {
            DiskIOData sysDiskIoInfo = (DiskIOData) obj;
            PlugManager.getInstance()
                .addPluginStartSuccess(sysDiskIoInfo.getLocalSessionId(), new DiskIoConfig().createConfig());
            registerDisk = true;
        }
        return obj instanceof DiskIOData;
    }

    @Override
    public <T> void addToList(T obj) {
        DiskIOData sysDiskIoInfo = (DiskIOData) obj;
        sysDiskIoInfoList.add(sysDiskIoInfo);
    }

    @Override
    public void batchInsertToDb() {
        diskIoTable.insertSysDiskIoInfo(sysDiskIoInfoList);
        sysDiskIoInfoList.clear();
    }

}
