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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.ability.AbilityActivityInfo;
import ohos.devtools.services.ability.AbilityEventInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AbilityTable
 *
 * @since: 2021/10/22 15:22
 */
public class AbilityTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(AbilityTable.class);
    private static final String ABILITY_DB_NAME = "abilityDb";
    private static final String INSERT_ACTIVITY_SQL = "INSERT OR IGNORE "
        + "INTO "
        + "abilityActivityInfo("
        + "sessionId, "
        + "lifeCycleId, "
        + "timeStamp, "
        + "abilityCardName, "
        + "abilityCardState) "
        + "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_EVENT_SQL = "INSERT OR IGNORE "
        + "INTO "
        + "abilityEventInfo("
        + "session, "
        + "timeStamp, "
        + "eventType, "
        + "key_type, "
        + "is_down) "
        + "VALUES (?, ?, ?, ?, ?)";

    /**
     * AbilityTable Constructor
     */
    public AbilityTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> abilityActivityInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("sessionId INTEGER NOT NULL");
                add("lifeCycleId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("abilityCardName varchar(100) NOT NULL");
                add("abilityCardState integer(50) NOT NULL");
            }
        };

        List<String> abilityActivityIndex = new ArrayList() {
            {
                add("sessionId");
                add("timeStamp");
            }
        };

        List<String> abilityEventInfo = new ArrayList() {
            {
                add("session long NOT NULL");
                add("timeStamp long NOT NULL");
                add("eventType varchar(50) NOT NULL");
                add("key_type INTEGER");
                add("is_down BOOLEAN DEFAULT 'false'");
            }
        };

        List<String> abilityEventIndex = new ArrayList() {
            {
                add("session");
                add("timeStamp");
            }
        };
        createTable(ABILITY_DB_NAME, "abilityActivityInfo", abilityActivityInfo);
        createIndex("abilityActivityInfo", "abilityActivityIndex", abilityActivityIndex);
        createTable(ABILITY_DB_NAME, "abilityEventInfo", abilityEventInfo);
        createIndex("abilityEventInfo", "abilityEventIndex", abilityEventIndex);
    }

    /**
     * insert App activity Info
     *
     * @param abilityActivityInfo abilityActivityInfo
     * @return boolean
     */
    public boolean insertAppActivityInfo(AbilityActivityInfo abilityActivityInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertAppActivityInfo");
        }
        Optional<Connection> option = getConnectByTable("abilityActivityInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement(INSERT_ACTIVITY_SQL);
                pst.setLong(1, abilityActivityInfo.getSessionId());
                pst.setInt(2, abilityActivityInfo.getLifeCycleId());
                pst.setLong(3, abilityActivityInfo.getTimeStamp());
                pst.setString(4, abilityActivityInfo.getAbilityStateName());
                pst.setInt(5, abilityActivityInfo.getAbilityState());
                return pst.executeUpdate() > 0;
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert ability activity data {}", sqlException.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }

    /**
     * insert App Event Info
     *
     * @param abilityEventInfo abilityEventInfo
     * @return boolean
     */
    public boolean insertAppEventInfo(AbilityEventInfo abilityEventInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertAppEventInfo");
        }
        Optional<Connection> option = getConnectByTable("abilityEventInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement(INSERT_EVENT_SQL);
                pst.setLong(1, abilityEventInfo.getSessionId());
                pst.setLong(2, abilityEventInfo.getTimeStamp());
                pst.setString(3, abilityEventInfo.getEventType().toString());
                pst.setInt(4, abilityEventInfo.getKeyType());
                pst.setBoolean(5, abilityEventInfo.isDown());
                return pst.executeUpdate() > 0;
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert ability event data {}", sqlException.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }
}
