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

package ohos.devtools.datasources.databases.databasepool;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static ohos.devtools.views.common.LayoutConstants.INDEX_FIVE;
import static ohos.devtools.views.common.LayoutConstants.NEGATIVE_ONE;

/**
 * Database table structure maintenance
 *
 * @since 2021/10/22 17:00
 */
public class DataTableHelper {
    private static final Logger LOGGER = LogManager.getLogger(DataTableHelper.class);

    private DataTableHelper() {
    }

    /**
     * Get the database table name by SQL.
     *
     * @param sql sql
     * @return String
     */
    public static String getTableNameBySql(String sql) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTableNameBySql");
        }
        int tableIndex = sql.indexOf("TABLE");
        if (tableIndex != NEGATIVE_ONE) {
            String tableName = sql.substring(tableIndex + INDEX_FIVE, sql.indexOf("("));
            return tableName.trim();
        }
        return "";
    }

    /**
     * sql Placeholder
     *
     * @param size size
     * @return String
     */
    public static String sqlPlaceholder(int size) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("sqlPlaceholder");
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < size; i++) {
            if (i == (size - 1)) {
                stringBuffer.append("?");
            } else {
                stringBuffer.append("?,");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * Map to String
     *
     * @param map Map<String, Object>
     * @return String
     */
    public static String getDeleteCondition(Map<String, Object> map) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getDeleteCondition");
        }
        Set<String> keySet = map.keySet();
        // Convert set collection to array
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        // Sort the array (ascending order)
        Arrays.sort(keyArray);
        // Because String splicing efficiency will be very low, so switch to String Builder
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            // If the parameter value is empty, it does not participate in the signature.
            // This method trim() removes spaces
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                stringBuffer.append(keyArray[i]);
                if (map.get(keyArray[i]) instanceof String) {
                    stringBuffer.append(" = '").append(String.valueOf(map.get(keyArray[i])).trim()).append("'");
                } else {
                    stringBuffer.append(" = `").append(String.valueOf(map.get(keyArray[i])).trim()).append("`");
                }
            }
            if (i != keyArray.length - 1) {
                stringBuffer.append(" and ");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * Map to String
     *
     * @param map Map<String, Object>
     * @return String
     */
    public static String mapToString(Map<String, Object> map) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("mapToString");
        }
        Set<String> keySet = map.keySet();
        // Convert set collection to array
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        // Sort the array (ascending order)
        Arrays.sort(keyArray);
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            // If the parameter value is empty, it does not participate in the signature.
            // This method trim() removes spaces
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                stringBuffer.append(keyArray[i]).append(" = '").append(String.valueOf(map.get(keyArray[i])).trim())
                    .append("'");
            }
            if (i != keyArray.length - 1) {
                stringBuffer.append("=");
            }
        }
        return stringBuffer.toString();
    }
}
