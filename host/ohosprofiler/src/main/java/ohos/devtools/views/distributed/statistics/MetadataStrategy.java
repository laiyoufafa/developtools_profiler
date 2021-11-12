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

package ohos.devtools.views.distributed.statistics;

import ohos.devtools.views.distributed.statistics.bean.Metadata;
import ohos.devtools.views.distributed.util.DistributedDB;
import ohos.devtools.views.trace.Sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata Strategy
 *
 * @since 2021/5/19 16:39
 */
public class MetadataStrategy implements Strategy {
    /**
     * get Query Metadata Result
     *
     * @param device device
     * @return String String
     */
    @Override
    public String getQueryResult(String device) {
        List<Metadata> list = new ArrayList<>() {
        };
        if (device.equalsIgnoreCase("A")) {
            DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_TRACE_METADATA, list);
        } else {
            DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_TRACE_METADATA, list);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("trace_metadata: {").append(System.lineSeparator());
        for (Metadata item : list) {
            String regex = "^-?\\d+$";
            String value = "";
            if (item.getValue().matches(regex)) {
                value = item.getValue();
            } else {
                value = "\"" + item.getValue().replaceAll("\r|\n", "") + "\"";
            }
            builder.append("  ").append(item.getName()).append(":").append(" ").append(value)
                .append(System.lineSeparator());
        }
        builder.append("}");
        return builder.toString();
    }
}
