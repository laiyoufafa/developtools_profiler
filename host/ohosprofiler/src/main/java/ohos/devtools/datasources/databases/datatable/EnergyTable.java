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
import ohos.devtools.datasources.databases.datatable.enties.EnergyData;
import ohos.devtools.datasources.databases.datatable.enties.EnergyLocationInfo;
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
 * EnergyTable
 *
 * @since: 2021/6/20 13:22
 */
public class EnergyTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(EnergyTable.class);
    private static final String ENERGY_DB_NAME = "energyDb";
    private static final int INDEX_ONE = 1;
    private static final int INDEX_TWO = 2;
    private static final int INDEX_THREE = 3;
    private static final int INDEX_FOUR = 4;
    private static final int INDEX_FIVE = 5;
    private static final int INDEX_SIX = 6;
    private static final int INDEX_SEVEN = 7;
    private static final int INDEX_EIGHT = 8;
    private static final int INDEX_NINE = 9;
    private static final int INDEX_TEN = 10;
    private static final int INDEX_ELEVEN = 11;
    private static final int INDEX_TWELVE = 12;
    private static final int INDEX_THIRTEEN = 13;
    private static final int INDEX_FOURTEEN = 14;
    private static final int INDEX_FIFTEEN = 15;
    private static final int INDEX_SISTEEN = 16;
    private static final int INDEX_SEVENTEEN = 17;
    private static final int INDEX_EIGHTTEEN = 18;
    private static final int INDEX_NINETEEN = 19;
    private static final int INDEX_TWENTY = 20;
    private static final int INDEX_TWENTY_ONE = 21;
    private static final int INDEX_TWENTY_TWO = 22;
    private static final int INDEX_TWENTY_THREE = 23;
    private static final int INDEX_TWENTY_FOUR = 24;
    private static final int INDEX_TWENTY_FIVE = 25;

    /**
     * constructor
     */
    public EnergyTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> processEnergyEventInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("sessionId LONG NOT NULL");
                add("eventId LONG not null");
                add("systemEvent varchar(100) NOT NULL");
                add("description varchar(100) NOT NULL");
                add("callStack varchar(200) NOT NULL");
                add("endCallStack varchar(200)");
                add("startTimeStamp Long NOT NULL");
                add("endTimeStamp Long");
                add("triggertimeNs Long");
                add("provider varchar(100)");
                add("priority varchar(100)");
                add("minInterval Long");
                add("FestInterval Long");
                add("startType varchar(100)");
                add("endType varchar(100)");
                add("workNetworkType varchar(100)");
                add("workCharging varchar(100)");
                add("workStorage varchar(100)");
                add("workDeepIdle varchar(100)");
                add("workBattery varchar(100)");
                add("workPersisted varchar(100)");
                add("workRepeatCounter varchar(100)");
                add("workRepeatCycleTime varchar(100)");
                add("workDelay varchar(100)");
                add("workResult varchar(100)");
                add("eventType varchar(100)");
            }
        };
        List<String> processEnergyEventeInfoIndex = new ArrayList() {
            {
                add("id");
                add("eventId");
                add("sessionId");
            }
        };
        createTable(ENERGY_DB_NAME, "processEnergyEventInfo", processEnergyEventInfo);
        createIndex("processEnergyEventInfo", "processEnergyEventeInfoIndex", processEnergyEventeInfoIndex);
        initializeExtracted();
    }

    /**
     * initializeExtracted
     */
    private void initializeExtracted() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initializeExtracted");
        }
        List<String> processEnergyLocationInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("sessionId LONG NOT NULL");
                add("eventId LONG not null");
                add("timestamp Long NOT NULL");
                add("priority varchar(100) NOT NULL");
                add("energyUsage int NOT NULL");
            }
        };
        List<String> processEnergyLocationIndex = new ArrayList() {
            {
                add("id");
                add("eventId");
                add("sessionId");
                add("timeStamp");
            }
        };
        createTable(ENERGY_DB_NAME, "processEnergyLocationInfo", processEnergyLocationInfo);
        createIndex("processEnergyLocationInfo", "processEnergyLocationIndex", processEnergyLocationIndex);
    }

    /**
     * insertProcessCpuInfo
     *
     * @param energyDataList energyDataList
     * @return boolean
     */
    public boolean insertEnergyAgentInfo(List<EnergyData> energyDataList) {
        return insertEnergyEventBatch(energyDataList);
    }

    private boolean insertEnergyEventBatch(List<EnergyData> energyDataList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertEnergyEventBatch");
        }
        Optional<Connection> option = getConnectByTable("processEnergyEventInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            try {
                PreparedStatement pst = conn.prepareStatement(getInsertEnergyEventSql());
                conn.setAutoCommit(false);
                energyDataList.forEach(energyData -> {
                    try {
                        setPreparedStatementData(pst, energyData);
                    } catch (SQLException sqlException) {
                        if (ProfilerLogManager.isErrorEnabled()) {
                            LOGGER.info("insert EnergySample data {}", sqlException.getMessage());
                        }
                    }
                });
                try {
                    int[] results = pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException throwAbles) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.info("insert EnergySample data {}", throwAbles.getMessage());
                    }
                } finally {
                    if (pst != null) {
                        pst.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert EnergySample data {}", exception.getMessage());
                }
            }
        }
        return false;
    }

    private String getInsertEnergyEventSql() {
        return "INSERT OR IGNORE "
            + "INTO "
            + "processEnergyEventInfo("
            + "sessionId, "
            + "eventId, "
            + "systemEvent, "
            + "description, "
            + "callStack, "
            + "startTimeStamp, "
            + "endTimeStamp, "
            + "triggertimeNs, "
            + "provider, "
            + "priority, "
            + "minInterval, "
            + "FestInterval, "
            + "endCallStack, "
            + "workNetworkType, "
            + "workCharging, "
            + "workStorage, "
            + "workDeepIdle, "
            + "workBattery, "
            + "workPersisted, "
            + "workRepeatCounter, "
            + "workRepeatCycleTime, "
            + "workDelay, "
            + "startType, "
            + "endType, "
            + "eventType) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * setPreparedStatementData
     *
     * @param pst pst
     * @param energyData energyData
     * @throws SQLException SQLException
     */
    private void setPreparedStatementData(PreparedStatement pst, EnergyData energyData) throws SQLException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setPreparedStatementData");
        }
        pst.setLong(INDEX_ONE, energyData.getSessionId());
        pst.setLong(INDEX_TWO, energyData.getEventId());
        pst.setString(INDEX_THREE, energyData.getSystemEvent());
        pst.setString(INDEX_FOUR, energyData.getDescription());
        pst.setString(INDEX_FIVE, energyData.getCallStack());
        pst.setLong(INDEX_SIX, energyData.getStartTimeStamp());
        pst.setLong(INDEX_SEVEN, energyData.getEndTimeStamp() == null ? 0 : energyData.getEndTimeStamp());
        pst.setLong(INDEX_EIGHT, energyData.getTriggerTimeNs());
        pst.setString(INDEX_NINE, energyData.getProvider());
        pst.setString(INDEX_TEN, energyData.getPriority());
        pst.setLong(INDEX_ELEVEN, energyData.getMinInterval());
        pst.setLong(INDEX_TWELVE, energyData.getFestInterval());
        pst.setString(INDEX_THIRTEEN, energyData.getEndCallStack());
        pst.setString(INDEX_FOURTEEN, energyData.getWorkNetworkType());
        pst.setString(INDEX_FIFTEEN, energyData.getWorkCharging());
        pst.setString(INDEX_SISTEEN, energyData.getWorkStorage());
        pst.setString(INDEX_SEVENTEEN, energyData.getWorkDeepIdle());
        pst.setString(INDEX_EIGHTTEEN, energyData.getWorkBattery());
        pst.setString(INDEX_NINETEEN, energyData.getWorkPersisted());
        pst.setString(INDEX_TWENTY, energyData.getWorkRepeatCounter());
        pst.setString(INDEX_TWENTY_ONE, energyData.getWorkRepeatCycleTime());
        pst.setString(INDEX_TWENTY_TWO, energyData.getWorkDelay());
        pst.setString(INDEX_TWENTY_THREE, energyData.getStartType());
        pst.setString(INDEX_TWENTY_FOUR, energyData.getEndType());
        pst.setString(INDEX_TWENTY_FIVE, energyData.getEventType());
        pst.addBatch();
    }

    /**
     * insertProcessCpuInfo
     *
     * @param energyLocationDataList energyLocationDataList
     * @return boolean
     */
    public boolean insertEnergyLocationInfo(List<EnergyLocationInfo> energyLocationDataList) {
        return insertEnergyLocationBatch(energyLocationDataList);
    }

    private boolean insertEnergyLocationBatch(List<EnergyLocationInfo> energyLocationDataList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertEnergyLocationBatch");
        }
        Optional<Connection> option = getConnectByTable("processEnergyLocationInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            try {
                PreparedStatement pst = conn.prepareStatement(getInsertEnergyLocationSql());
                conn.setAutoCommit(false);
                energyLocationDataList.forEach(energyData -> {
                    try {
                        pst.setLong(INDEX_ONE, energyData.getSessionId());
                        pst.setLong(INDEX_TWO, energyData.getEventId());
                        pst.setLong(INDEX_THREE, energyData.getTimestamp());
                        pst.setInt(INDEX_FOUR, energyData.getEnergyUsage());
                        pst.setString(INDEX_FIVE, energyData.getPriority());
                        pst.addBatch();
                    } catch (SQLException sqlException) {
                        LOGGER.info("insert EnergyLocationSample data {}", sqlException.getMessage());
                    }
                });
                try {
                    int[] results = pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException throwAbles) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.info("insert EnergyLocationSample data {}", throwAbles.getMessage());
                    }
                } finally {
                    if (pst != null) {
                        pst.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert EnergyLocationSample data {}", exception.getMessage());
                }
            }
        }
        return false;
    }

    private String getInsertEnergyLocationSql() {
        return "INSERT OR IGNORE "
            + "INTO "
            + "processEnergyLocationInfo("
            + "sessionId, "
            + "eventId, "
            + "timestamp, "
            + "energyUsage, "
            + "priority) "
            + "VALUES (?, ?, ?, ?, ?)";
    }
}
