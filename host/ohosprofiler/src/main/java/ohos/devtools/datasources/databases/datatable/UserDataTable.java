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

package ohos.devtools.datasources.databases.datatable;

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.UserData;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DiskIo DB and Table Construct
 *
 * @since: 2021/10/22 15:43
 */
public class UserDataTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(UserDataTable.class);
    private static final String DB_NAME = "sdkDb";
    private static final String INSERT_SQL = "INSERT OR IGNORE "
        + "INTO "
        + "sdkDataTable("
        + "session, "
        + "sessionId, "
        + "timeStamp, "
        + "sdkData) "
        + "VALUES (?, ?, ?, ?)";

    /**
     * constructor
     */
    public UserDataTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> sdkInfos = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("sessionId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("sdkData BLOB NOT NULL");
            }
        };
        List<String> sysDiskIoInfoIndex = new ArrayList() {
            {
                add("sessionId");
                add("timeStamp");
            }
        };
        createTable(DB_NAME, "sdkDataTable", sdkInfos);
        createIndex("sdkDataTable", "sdkDataTableIndex", sysDiskIoInfoIndex);
    }

    /**
     * insertSysInfoBatch
     *
     * @param sdkInfoList sdkInfoList
     * @return boolean
     */
    public boolean insertSdkDataInfo(List<UserData> sdkInfoList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertSdkDataInfo");
        }
        Optional<Connection> option = getConnectByTable("sdkDataTable");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement(INSERT_SQL);
                conn.setAutoCommit(false);
                for (UserData sdkInfo : sdkInfoList) {
                    pst.setLong(1, sdkInfo.getLocalSessionId());
                    pst.setInt(2, sdkInfo.getSessionId());
                    pst.setLong(3, sdkInfo.getTimeStamp());
                    pst.setBytes(4, sdkInfo.getBytes());
                    pst.addBatch();
                }
                try {
                    pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.info("insert sdkDataTable {}", exception.getMessage());
                    }
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert sdkDataTable {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }
}
