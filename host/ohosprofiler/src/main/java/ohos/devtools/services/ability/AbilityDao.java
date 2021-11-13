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

package ohos.devtools.services.ability;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.layout.chartview.ability.AbilityCardInfo;
import ohos.devtools.views.layout.chartview.ability.AbilityCardStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_ABILITY_CARD_INFO;
import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_ABILITY_END_TIME;
import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_ABILITY_EVENT_INFO;
import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_ABILITY_LIFE_TIME;
import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_ALL_ABILITY_CARD_INFO;
import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_EVENT_AFTER_TAIL;
import static ohos.devtools.services.ability.AbilityDao.AbilitySelectStatements.SELECT_EVENT_BEFORE_HEAD;

/**
 * AbilityDao
 *
 * @since: 2021/9/20 18:00
 */
public class AbilityDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(AbilityDao.class);
    private static final String ABILITY_ACTIVITY_TABLE = "abilityActivityInfo";
    private static final String ABILITY_EVENT_TABLE = "abilityEventInfo";
    private static final String TIME_STAMP = "timeStamp";
    private static final String LIFE_CYCLE_ID = "lifeCycleId";
    private static final String ABILITY_CARD_STATE = "abilityCardState";
    private static final String ABILITY_CARD_NAME = "abilityCardName";
    private static final String EVENT_TYPE = "eventType";
    private static final String KEY_TYPE = "key_type";
    private static final String IS_DOWN = "is_down";
    private static final String DELETE_ACTIVITY_TABLE = "DELETE FROM abilityActivityInfo WHERE sessionId = ";
    private static final String DELETE_EVENT_TABLE = "DELETE FROM abilityEventInfo WHERE session = ";
    private static volatile AbilityDao singleton;

    /**
     * AbilitySelectStatements
     */
    public enum AbilitySelectStatements {
        SELECT_ALL_ABILITY_CARD_INFO(
            "SELECT "
                + "timeStamp, "
                + "abilityCardName, "
                + "lifeCycleId, "
                + "abilityCardState "
                + "from "
                + "abilityActivityInfo "
                + "where "
                + "sessionId = "
                + "?"),
        SELECT_ABILITY_CARD_INFO(
            "SELECT "
                + "lifeCycleId, "
                + "timeStamp, "
                + "abilityCardName, "
                + "abilityCardState "
                + "from "
                + "abilityActivityInfo "
                + "where "
                + "sessionId = "
                + "? " + "and "
                + "timeStamp > "
                + "? "
                + "and "
                + "timeStamp < "
                + "? "
                + "ORDER BY "
                + "timeStamp "),
        SELECT_ABILITY_LIFE_TIME(
            "SELECT "
                + "timeStamp, "
                + "abilityCardState "
                + "from "
                + "abilityActivityInfo "
                + "where "
                + "sessionId = "
                + "? "
                + "and "
                + "lifeCycleId = "
                + "?"),
        SELECT_ABILITY_END_TIME(
            "SELECT"
                + " isnull(sum(timeStamp), 0) "
                + "from "
                + "abilityActivityInfo "
                + "where "
                + "sessionId = "
                + "? "
                + "and "
                + "lifeCycleId = "
                + "? "
                + "and "
                + "abilityCardState = "
                + "6"),

        DELETE_ABILITY_CARD_INFO("delete "
            + "from "
            + "abilityActivityInfo "
            + "where "
            + "sessionId = "
            + "?"),
        SELECT_ABILITY_EVENT_INFO(
            "SELECT "
                + "eventType, "
                + "key_type, "
                + "is_down, "
                + "timeStamp "
                + "from "
                + "abilityEventInfo "
                + "where "
                + "session = "
                + "? "
                + "and "
                + "timeStamp > "
                + "? "
                + "and "
                + "timeStamp < "
                + "? "
                + "ORDER BY "
                + "timeStamp "),
        SELECT_EVENT_BEFORE_HEAD(
            "SELECT "
                + "eventType, "
                + "key_type, "
                + "is_down, "
                + "timeStamp "
                + "from "
                + "abilityEventInfo "
                + "where "
                + "session ="
                + " ? "
                + "and "
                + "timeStamp < "
                + "? "
                + "order by "
                + "timeStamp "
                + "desc "
                + "limit 1"),
        SELECT_EVENT_AFTER_TAIL(
            "SELECT "
                + "eventType, "
                + "key_type, "
                + "is_down, "
                + "timeStamp "
                + "from "
                + "abilityEventInfo "
                + "where "
                + "session ="
                + " ? "
                + "and "
                + "timeStamp > "
                + "? "
                + "order by "
                + "timeStamp "
                + "asc "
                + "limit 1");
        private final String sqlStatement;

        AbilitySelectStatements(String sqlStatement) {
            this.sqlStatement = sqlStatement;
        }

        /**
         * sql Statement
         *
         * @return String
         */
        public String getStatement() {
            return sqlStatement;
        }
    }

    /**
     * Get an instance
     *
     * @return AbilityDao
     */
    public static AbilityDao getInstance() {
        if (singleton == null) {
            synchronized (AbilityDao.class) {
                if (singleton == null) {
                    singleton = new AbilityDao();
                }
            }
        }
        return singleton;
    }

    /**
     * getAllData
     *
     * @param sessionId sessionId
     * @return List <AbilityActivityInfo>
     */
    public List<AbilityActivityInfo> getAllData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllData");
        }
        Optional<Connection> connectByTable = getConnectByTable("abilityActivityInfo");
        List<AbilityActivityInfo> result = new ArrayList<>();
        connectByTable.ifPresent(connection -> {
            ResultSet rs = null;
            PreparedStatement pst = null;
            try {
                pst = connection.prepareStatement(SELECT_ALL_ABILITY_CARD_INFO.getStatement());
                if (pst != null) {
                    pst.setLong(1, sessionId);
                    rs = pst.executeQuery();
                    while (rs.next()) {
                        long timeStamp = rs.getLong(TIME_STAMP);
                        int lifeCycleId = rs.getInt(LIFE_CYCLE_ID);
                        String abilityCardName = rs.getString(ABILITY_CARD_NAME);
                        int abilityCardState = rs.getInt(ABILITY_CARD_STATE);
                        AbilityActivityInfo abilityActivityInfo = new AbilityActivityInfo();
                        abilityActivityInfo.setSessionId(sessionId);
                        abilityActivityInfo.setLifeCycleId(lifeCycleId);
                        abilityActivityInfo.setTimeStamp(timeStamp);
                        abilityActivityInfo.setAbilityStateName(abilityCardName);
                        abilityActivityInfo.setAbilityState(abilityCardState);
                        result.add(abilityActivityInfo);
                    }
                }
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("AbilityDao getAllData SQLException", throwAbles);
                }
            } finally {
                close(pst, rs, connection);
            }
        });
        return result;
    }

    /**
     * Ability get Activity Data
     *
     * @param sessionId sessionId
     * @param min min
     * @param max max
     * @param startTimeStamp startTimeStamp
     * @return LinkedHashMap
     */
    public List<AbilityCardInfo> getActivityData(long sessionId, int min, int max, long startTimeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getActivityData");
        }
        Optional<Connection> connectByTable = getConnectByTable("abilityActivityInfo");
        List<AbilityCardInfo> result = new ArrayList<>();
        connectByTable.ifPresent(connection -> {
            ResultSet rs = null;
            PreparedStatement pst = null;
            long startTime = startTimeStamp + min;
            try {
                pst = connection.prepareStatement(SELECT_ABILITY_CARD_INFO.getStatement());
                pst.setLong(1, sessionId);
                pst.setLong(2, startTime);
                long endTime = startTimeStamp + max;
                pst.setLong(3, endTime);
                rs = pst.executeQuery();
                if (rs != null) {
                    analyseAbilityData(sessionId, result, rs, startTime);
                }
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("AbilityDao getActivityData SQLException", throwAbles);
                }
            } finally {
                close(pst, rs, connection);
            }
        });
        return result;
    }

    /**
     * get Analyse Ability Data
     *
     * @param sessionId sessionId
     * @param result result
     * @param rs resultSet
     * @param startTime startTime
     * @throws SQLException SQLException
     */
    private void analyseAbilityData(long sessionId, List<AbilityCardInfo> result, ResultSet rs, long startTime)
        throws SQLException {
        boolean isFirstData = false;
        while (rs.next()) {
            int lifeCycleId = rs.getInt(LIFE_CYCLE_ID);
            long timeStamp = rs.getLong(TIME_STAMP);
            String abilityCardName = rs.getString(ABILITY_CARD_NAME);
            int abilityCardState = rs.getInt(ABILITY_CARD_STATE);
            if (abilityCardState == 6) {
                if (!isFirstData) {
                    AbilityCardInfo abilityCardInfo =
                        getAbilityCardInfo(startTime, timeStamp, abilityCardName, AbilityCardStatus.ACTIVE);
                    result.add(abilityCardInfo);
                    isFirstData = true;
                }
            } else {
                long lifeEndTime = getLifeEndTime(sessionId, lifeCycleId);
                AbilityCardStatus status = AbilityCardStatus.INITIAL;
                if (lifeEndTime == 0) {
                    status = AbilityCardStatus.ACTIVE;
                }
                AbilityCardInfo abilityCardInfo =
                    getAbilityCardInfo(timeStamp, lifeEndTime, abilityCardName, status);
                result.add(abilityCardInfo);
            }
        }
    }

    /**
     * get Event Data
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param endTime endTime
     * @param startTimeStamp startTimeStamp
     * @return LinkedHashMap
     */
    public List<AbilityEventInfo> getEventData(long sessionId, int startTime, int endTime, long startTimeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getEventData");
        }
        Optional<Connection> connectByTable = getConnectByTable("abilityEventInfo");
        List<AbilityEventInfo> result = new ArrayList<>();
        connectByTable.ifPresent(connection -> {
            ResultSet rs = null;
            PreparedStatement pst = null;
            try {
                if (startTime > 0) {
                    result.addAll(getEventAssistData(sessionId, startTime, startTimeStamp, true));
                }
                pst = connection.prepareStatement(SELECT_ABILITY_EVENT_INFO.getStatement());
                pst.setLong(1, sessionId);
                pst.setLong(2, startTimeStamp + startTime);
                pst.setLong(3, startTimeStamp + endTime);
                rs = pst.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        String eventType = rs.getString(EVENT_TYPE);
                        int keyType = rs.getInt(KEY_TYPE);
                        boolean isDown = rs.getBoolean(IS_DOWN);
                        long timeStamp = rs.getLong(TIME_STAMP);
                        AbilityEventInfo abilityEventInfo = new AbilityEventInfo();
                        abilityEventInfo.setSessionId(sessionId);
                        abilityEventInfo.setEventType(Enum.valueOf(EventType.class, eventType));
                        abilityEventInfo.setKeyType(keyType);
                        abilityEventInfo.setDown(isDown);
                        abilityEventInfo.setTimeStamp(timeStamp);
                        result.add(abilityEventInfo);
                    }
                }
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("AbilityDao getEventData SQLException", throwAbles);
                }
            } finally {
                close(pst, rs, connection);
            }
        });
        result.addAll(getEventAssistData(sessionId, endTime, startTimeStamp, false));
        return result;
    }

    /**
     * get EventAssist Data
     *
     * @param sessionId sessionId
     * @param time time
     * @param startTimeStamp startTimeStamp
     * @param isBeforeData isBeforeData
     * @return List <AbilityEventInfo>
     */
    private List<AbilityEventInfo> getEventAssistData(long sessionId, int time, long startTimeStamp,
        boolean isBeforeData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getEventAssistData");
        }
        Optional<Connection> connectByTable = getConnectByTable("abilityEventInfo");
        List<AbilityEventInfo> result = new ArrayList<>();
        connectByTable.ifPresent(connection -> {
            ResultSet rs = null;
            PreparedStatement pst = null;
            try {
                pst = isBeforeData ? connection.prepareStatement(SELECT_EVENT_BEFORE_HEAD.getStatement())
                    : connection.prepareStatement(SELECT_EVENT_AFTER_TAIL.getStatement());
                pst.setLong(1, sessionId);
                pst.setLong(2, time + startTimeStamp);
                rs = pst.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        String eventType = rs.getString(EVENT_TYPE);
                        int keyType = rs.getInt(KEY_TYPE);
                        boolean isDown = rs.getBoolean(IS_DOWN);
                        long timeStamp = rs.getLong(TIME_STAMP);
                        AbilityEventInfo abilityEventInfo = new AbilityEventInfo();
                        abilityEventInfo.setSessionId(sessionId);
                        abilityEventInfo.setEventType(Enum.valueOf(EventType.class, eventType));
                        abilityEventInfo.setKeyType(keyType);
                        abilityEventInfo.setDown(isDown);
                        abilityEventInfo.setTimeStamp(timeStamp);
                        result.add(abilityEventInfo);
                    }
                }
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("AbilityDao getEventAssistData SQLException ", throwAbles);
                }
            } finally {
                close(pst, rs, connection);
            }
        });
        return result;
    }

    /**
     * get abilityCardInfo
     *
     * @param startTime startTime
     * @param timeStamp timeStamp
     * @param abilityCardName abilityCardName
     * @param status status
     * @return AbilityCardInfo
     */
    private AbilityCardInfo getAbilityCardInfo(long startTime, long timeStamp, String abilityCardName,
        AbilityCardStatus status) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAbilityCardInfo");
        }
        AbilityCardInfo abilityCardInfo = new AbilityCardInfo();
        abilityCardInfo.setApplicationName(abilityCardName);
        abilityCardInfo.setStartTime(startTime);
        abilityCardInfo.setEndTime(timeStamp);
        abilityCardInfo.setAbilityCardStatus(status);
        return abilityCardInfo;
    }

    /**
     * getLifeCycleData
     *
     * @param sessionId sessionId
     * @param lifeCycleId lifeCycleId
     * @return LinkedHashMap
     */
    public List<AbilityActivityInfo> getLifeCycleData(long sessionId, int lifeCycleId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLifeCycleData");
        }
        Optional<Connection> connectByTable = getConnectByTable("abilityActivityInfo");
        List<AbilityActivityInfo> result = new ArrayList<>();
        connectByTable.ifPresent(connection -> {
            ResultSet rs = null;
            PreparedStatement pst = null;
            try {
                pst = connection.prepareStatement(SELECT_ABILITY_LIFE_TIME.getStatement());
                pst.setLong(1, sessionId);
                pst.setInt(2, lifeCycleId);
                rs = pst.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        long timeStamp = rs.getLong(TIME_STAMP);
                        int abilityCardState = rs.getInt(ABILITY_CARD_STATE);
                        AbilityActivityInfo abilityActivityInfo = new AbilityActivityInfo();
                        abilityActivityInfo.setTimeStamp(timeStamp);
                        abilityActivityInfo.setAbilityState(abilityCardState);
                        result.add(abilityActivityInfo);
                    }
                }
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("getLifeCycleData SQLException", throwAbles);
                }
            } finally {
                close(pst, rs, connection);
            }
        });
        return result;
    }

    /**
     * getLifeEndTime
     *
     * @param sessionId sessionId
     * @param lifeCycleId lifeCycleId
     * @return LinkedHashMap
     */
    public Long getLifeEndTime(long sessionId, int lifeCycleId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLifeEndTime");
        }
        Optional<Connection> connectByTable = getConnectByTable("abilityActivityInfo");
        AtomicLong result = new AtomicLong();
        connectByTable.ifPresent(connection -> {
            ResultSet rs = null;
            PreparedStatement pst = null;
            try {
                pst = connection.prepareStatement(SELECT_ABILITY_END_TIME.getStatement());
                pst.setLong(1, sessionId);
                pst.setInt(2, lifeCycleId);
                rs = pst.executeQuery();
                if (rs != null) {
                    result.set(rs.getLong(TIME_STAMP));
                }
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("getLifeEndTime SQLException", throwAbles);
                }
            } finally {
                close(pst, rs, connection);
            }
        });
        return result.get();
    }

    /**
     * delete AbilityData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteAbilityData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteAbilityData");
        }
        return deleteEventData(sessionId) && deleteActivityData(sessionId);
    }

    /**
     * delete ActivityData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteActivityData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteActivityData");
        }
        Optional<Connection> abilityActivityInfo = DataBaseApi.getInstance().getConnectByTable("abilityActivityInfo");
        Connection connection = null;
        if (abilityActivityInfo.isPresent()) {
            try {
                connection = abilityActivityInfo.get();
                return execute(connection, DELETE_ACTIVITY_TABLE + sessionId);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("delete Activity Data error: ", exception);
                    }
                }
            }
        } else {
            return false;
        }
    }

    /**
     * delete EventData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteEventData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteEventData");
        }
        Optional<Connection> abilityEventInfo = DataBaseApi.getInstance().getConnectByTable("abilityEventInfo");
        Connection connection = null;
        if (abilityEventInfo.isPresent()) {
            try {
                connection = abilityEventInfo.get();
                return execute(connection, DELETE_EVENT_TABLE + sessionId);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("delete Event Data error: ", exception);
                    }
                }
            }
        } else {
            return false;
        }
    }
}
