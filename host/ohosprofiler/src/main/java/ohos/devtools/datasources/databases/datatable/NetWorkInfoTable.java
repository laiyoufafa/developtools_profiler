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
import ohos.devtools.datasources.transport.grpc.service.AgentPluginNetworkData;
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
 * NetWorkInfoTable
 *
 * @since: 2021/10/22 15:22
 */
public class NetWorkInfoTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(NetWorkInfoTable.class);
    private static final String DB_NAME = "NetWorkInfo";

    /**
     * constructor
     */
    public NetWorkInfoTable() {
        initialize();
    }

    /**
     * initialize
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> httpRequestHead = new ArrayList() {
            {
                add("connectId long not null");
                add("sessionId long NOT NULL");
                add("timeStamp long NOT NULL");
                add("url varchar(100) NOT NULL");
                add("method varchar(100) NOT NULL");
                add("fields varchar(100) NOT NULL");
                add("trace varchar(100) NOT NULL");
            }
        };
        List<String> httpResponseHead = new ArrayList() {
            {
                add("connectId long not null");
                add("sessionId long NOT NULL");
                add("timeStamp long NOT NULL");
                add("status varchar(100) NOT NULL");
                add("fields varchar(100) NOT NULL");
            }
        };

        List<String> httpBody = new ArrayList() {
            {
                add("connectId long not null");
                add("sessionId long NOT NULL");
                add("timeStamp long NOT NULL");
                add("payload_id varchar(100)");
                add("payload_size int");
                add("type int NOT NULL");
                add("payload_fields BLOB");
            }
        };

        List<String> httpThreads = new ArrayList() {
            {
                add("connectId long not null");
                add("sessionId long NOT NULL");
                add("timeStamp long NOT NULL");
                add("thread_id long");
                add("thread_name varchar(100)");
            }
        };

        createTable(DB_NAME, "httpRequestHead", httpRequestHead);
        createTable(DB_NAME, "httpResponseHead", httpResponseHead);
        createTable(DB_NAME, "httpBody", httpBody);
        createTable(DB_NAME, "httpThreads", httpThreads);
    }

    /**
     * insertHttpRequestHead
     *
     * @param httpRequestHeadList httpRequestHeadList
     * @param sessionId sessionId
     * @param connectId connectId
     * @param timeStamp timeStamp
     * @return boolean
     */
    public boolean insertHttpRequestHead(AgentPluginNetworkData.HttpRequestHead httpRequestHeadList, long sessionId,
        long connectId, long timeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertHttpRequestHead");
        }
        Optional<Connection> option = getConnectByTable("httpRequestHead");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("INSERT INTO "
                    + "httpRequestHead("
                    + "connectId, "
                    + "sessionId, "
                    + "timeStamp, "
                    + "url, "
                    + "method, "
                    + "fields, "
                    + "trace) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                pst.setLong(1, connectId);
                pst.setLong(2, sessionId);
                pst.setLong(3, timeStamp);
                pst.setString(4, httpRequestHeadList.getUrl());
                pst.setString(5, httpRequestHeadList.getMethod());
                pst.setString(6, httpRequestHeadList.getFields());
                pst.setString(7, httpRequestHeadList.getTrace());
                pst.executeUpdate();
                return pst.executeUpdate() > 0;
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert insertHttpRequestHead {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }

    /**
     * insertHttpResponseHead
     *
     * @param responseHead responseHead
     * @param sessionId sessionId
     * @param connectId connectId
     * @param timeStamp timeStamp
     * @return boolean
     */
    public boolean insertHttpResponseHead(AgentPluginNetworkData.HttpResponseHead responseHead, long sessionId,
        long connectId, long timeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertHttpResponseHead");
        }
        Optional<Connection> option = getConnectByTable("httpResponseHead");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("INSERT INTO "
                    + "httpResponseHead("
                    + "connectId, "
                    + "sessionId, "
                    + "timeStamp, "
                    + "status, "
                    + "fields) "
                    + "VALUES (?, ?, ?, ?, ?)");
                pst.setLong(1, connectId);
                pst.setLong(2, sessionId);
                pst.setLong(3, timeStamp);
                pst.setString(4, "200");
                pst.setString(5, responseHead.getFields());
                return pst.executeUpdate() > 0;
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert insertHttpResponseHead {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }

    /**
     * insertHttpBody
     *
     * @param httpBody httpBody
     * @param sessionId sessionId
     * @param connectId connectId
     * @param timeStamp timeStamp
     * @param type type
     * @return boolean
     */
    public boolean insertHttpBody(AgentPluginNetworkData.HttpBody httpBody, long sessionId, long connectId,
        long timeStamp, int type) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertHttpBody");
        }
        Optional<Connection> option = getConnectByTable("httpBody");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("INSERT INTO "
                    + "httpBody("
                    + "connectId, "
                    + "sessionId, "
                    + "timeStamp, "
                    + "payload_id, "
                    + "payload_size, "
                    + "type, "
                    + "payload_fields) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                pst.setLong(1, connectId);
                pst.setLong(2, sessionId);
                pst.setLong(3, timeStamp);
                pst.setString(4, httpBody.getPayloadId());
                pst.setLong(5, httpBody.getPayloadSize());
                pst.setLong(6, type);
                pst.setBytes(7, httpBody.getPayloadFields().toByteArray());
                return pst.executeUpdate() > 0;
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert httpBody {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }

    /**
     * insertHttpThreads
     *
     * @param agentThread agentThreads
     * @param sessionId sessionId
     * @param connectId connectId
     * @param timeStamp timeStamp
     * @return boolean
     */
    public boolean insertHttpThreads(AgentPluginNetworkData.AgentThread agentThread, long sessionId, long connectId,
        long timeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertHttpThreads");
        }
        Optional<Connection> option = getConnectByTable("httpThreads");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("INSERT INTO "
                    + "httpThreads("
                    + "connectId, "
                    + "sessionId, "
                    + "timeStamp, "
                    + "thread_id, "
                    + "thread_name) "
                    + "VALUES (?, ?, ?, ?, ?)");
                pst.setLong(1, connectId);
                pst.setLong(2, sessionId);
                pst.setLong(3, timeStamp);
                pst.setLong(4, agentThread.getId());
                pst.setString(5, agentThread.getName());
                return pst.executeUpdate() > 0;
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert AppInfo {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }
}
