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
import ohos.devtools.datasources.databases.datatable.enties.NetWorkInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ohos.devtools.views.common.LayoutConstants.INDEX_FIVE;
import static ohos.devtools.views.common.LayoutConstants.INDEX_FOUR;
import static ohos.devtools.views.common.LayoutConstants.INDEX_ONE;
import static ohos.devtools.views.common.LayoutConstants.INDEX_THREE;
import static ohos.devtools.views.common.LayoutConstants.TWO;

/**
 * NetworkTable DB and Table Construct
 *
 * @since: 2021/10/22 15:43
 */
public class NetworkTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(NetworkTable.class);
    private static final String NETWORK_DB = "networkDB";
    private static final String INSERT_SQL = "INSERT OR IGNORE "
        + "INTO "
        + "networkInfo("
        + "session, "
        + "sessionId, "
        + "timeStamp, "
        + "send_speed, "
        + "receive_speed, "
        + "energy_network) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * constructor
     */
    public NetworkTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> networkInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("sessionId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("send_speed BigDecimal NOT NULL");
                add("receive_speed BigDecimal NOT NULL");
                add("energy_network INTEGER NOT NULL");
            }
        };
        List<String> networkIndex = new ArrayList() {
            {
                add("session");
                add("timeStamp");
            }
        };
        createTable(NETWORK_DB, "networkInfo", networkInfo);
        createIndex("networkInfo", "networkInfoIndex", networkIndex);
    }

    /**
     * insertNetworkInfoList
     *
     * @param netWorkInfoList netWorkInfoList
     * @return boolean
     */
    public boolean insertNetworkInfoList(List<NetWorkInfo> netWorkInfoList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertNetworkInfoList");
        }
        Optional<Connection> option = getConnectByTable("networkInfo");
        if (option.isPresent()) {
            try (Connection conn = option.get();
                PreparedStatement pst = conn.prepareStatement(INSERT_SQL)) {
                conn.setAutoCommit(false);
                netWorkInfoList.forEach(netWorkInfo -> {
                    try {
                        pst.setLong(1, netWorkInfo.getLocalSessionId());
                        pst.setInt(2, netWorkInfo.getSessionId());
                        pst.setLong(3, netWorkInfo.getTimeStamp());
                        pst.setBigDecimal(4, netWorkInfo.getSendSpeed());
                        pst.setBigDecimal(5, netWorkInfo.getReceiveSpeed());
                        pst.setInt(6, netWorkInfo.getEnergyNetworkData());
                        pst.addBatch();
                    } catch (SQLException sqlException) {
                        LOGGER.info("insert networkInfo {}", sqlException.getMessage());
                    }
                });
                try {
                    pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.info("insert networkInfo {}", exception.getMessage());
                    }
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert networkInfo {}", exception.getMessage());
                }
            }
        }
        return false;
    }
}
