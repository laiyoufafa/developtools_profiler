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

package ohos.devtools.views.trace.util;

import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.CpuRateBean;
import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.bean.ProcessData;
import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ProcessMemData;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.bean.WakeupBean;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Database operation class
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public final class Db {
    private static boolean isLocal;
    private static volatile Db db;
    private static String dbName = "trace.db";
    private final String[] units = new String[] {"", "K", "M", "G", "T", "E"};

    /**
     * Gets the value of dbName .
     *
     * @return the value of java.lang.String
     */
    public static String getDbName() {
        return dbName;
    }

    /**
     * Sets the dbName .
     * <p>You can use getDbName() to get the value of dbName</p>
     *
     * @param dbName dbName
     */
    public static void setDbName(final String dbName) {
        Db.dbName = dbName;
    }

    private Db() {
    }

    private LinkedBlockingQueue<Connection> pool = new LinkedBlockingQueue();

    /**
     * Get database connection
     *
     * @return Connection
     */
    public Connection getConn() {
        Connection connection = null;
        try {
            connection = pool.take();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        return connection;
    }

    /**
     * Return the connection to the database connection pool after use
     *
     * @param conn conn
     */
    public void free(final Connection conn) {
        try {
            pool.put(conn);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private Optional<Connection> newConn() {
        URL path = DataUtils.class.getClassLoader().getResource(dbName);
        Connection conn = null;
        try {
            if (isLocal) {
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            } else {
                conn = DriverManager.getConnection("jdbc:sqlite::resource:" + path);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Optional.ofNullable(conn);
    }

    /**
     * Load the database file according to the file location variable
     *
     * @param isLocal isLocal
     */
    public static void load(final boolean isLocal) {
        Db.isLocal = isLocal;
        final int maxConnNum = 10;
        try {
            Class.forName("org.sqlite.JDBC");
            for (Connection connection : db.pool) {
                connection.close();
            }
            db.pool.clear();
            for (int size = 0; size < maxConnNum; size++) {
                db.newConn().ifPresent(c -> {
                    try {
                        db.pool.put(c);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            db.newConn().ifPresent(c -> {
                try {
                    Statement statement = c.createStatement();
                    statement.execute("CREATE VIEW IF NOT EXISTS thread AS SELECT id as utid, * FROM internal_thread;");
                    statement
                        .execute("CREATE VIEW IF NOT EXISTS process AS SELECT id as upid, * FROM internal_process;");
                    statement
                        .execute("CREATE VIEW IF NOT EXISTS sched AS SELECT *, ts + dur as ts_end FROM sched_slice;");
                    statement.execute("CREATE VIEW IF NOT EXISTS instants AS SELECT *, 0.0 as value FROM instant;");
                    statement.close();
                    c.close();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        }
    }

    /**
     * Get the current current db object
     *
     * @return Db
     */
    public static Db getInstance() {
        if (db == null) {
            db = new Db();
        }
        return db;
    }

    /**
     * Read the sql directory under resource
     *
     * @param sqlName sqlName
     * @return String sql
     */
    public static String getSql(String sqlName) {
        String path = "sql/" + sqlName + ".txt";
        try (InputStream STREAM = DataUtils.class.getClassLoader().getResourceAsStream(path)) {
            return IOUtils.toString(STREAM, Charset.forName("UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            exception.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Query the current maximum and minimum time
     *
     * @return long
     */
    public long queryTotalTime() {
        long res = 0;
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(getSql("QueryTotalTime")); // Query data
            while (rs.next()) { // Print out the queried data
                res = rs.getLong("total");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the number of cpu
     *
     * @return List
     */
    public List<Integer> queryCpu() {
        ArrayList<Integer> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs =
                stat.executeQuery("select cpu from cpu_counter_track where name='cpuidle' order by cpu;"); // Query data
            while (rs.next()) { // Print out the queried data
                res.add(rs.getInt("cpu"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the number of cpu according to the cpu frequency
     *
     * @return List Integer
     */
    public List<Integer> queryCpuFreq() {
        ArrayList<Integer> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(
                "select cpu from cpu_counter_track where (name='cpufreq' or name='cpu_frequency') order by cpu;");
            while (rs.next()) { // Print out the queried data
                res.add(rs.getInt("cpu"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the maximum number of cpu
     *
     * @return int
     */
    public int queryCpuMax() {
        String queryCpuSql = "select cpu from sched_slice order by cpu desc limit 1";
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(queryCpuSql); // Query data
            while (rs.next()) { // Print out the queried data
                return rs.getInt("cpu");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Query cpu data list according to cpu
     *
     * @param cpu cpu
     * @return List CpuData
     */
    public List<CpuData> queryCpuData(final int cpu) {
        ArrayList<CpuData> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            String sql = String.format(Locale.ENGLISH, getSql("QueryCpuData"), cpu);
            rs = stat.executeQuery(sql);
            while (rs.next()) { // Print out the queried data
                CpuData cpuData = new CpuData();
                cpuData.setCpu(rs.getInt("cpu"));
                cpuData.setProcessId(rs.getInt("processId"));
                cpuData.setProcessName(rs.getString("processName"));
                cpuData.setProcessCmdLine(rs.getString("processCmdLine"));
                cpuData.setName(rs.getString("name"));
                cpuData.setTid(rs.getInt("tid"));
                cpuData.setId(rs.getInt("id"));
                cpuData.setSchedId(rs.getInt("schedId"));
                cpuData.setType(rs.getString("type"));
                cpuData.setDuration(rs.getLong("dur"));
                cpuData.setStartTime(rs.getLong("startTime"));
                cpuData.setPriority(rs.getInt("priority"));
                cpuData.setEndState(rs.getString("end_state"));
                res.add(cpuData);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the maximum frequency of the cpu
     *
     * @return String
     */
    public Map<String, Object> queryCpuMaxFreq() {
        String queryMaxFreqSql = "select max(value) as maxFreq\n" + "    from counter c\n"
            + "    inner join cpu_counter_track t on c.track_id = t.id\n"
            + "    where (name = 'cpufreq' or name='cpu_frequency');";
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        StringBuilder maxFreq = new StringBuilder();
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("name", "0 Ghz");
        resMap.put("value", 0);
        try {
            int maxIntFreq = 0;
            stat = conn.createStatement();
            rs = stat.executeQuery(queryMaxFreqSql); // Query data
            while (rs.next()) { // Print out the queried data
                maxIntFreq = rs.getInt("maxFreq");
            }
            if (maxIntFreq > 0) {
                double log10 = Math.ceil(Math.log10(maxIntFreq));
                double pow10 = Math.pow(10, log10);
                double afterCeil = Math.ceil(maxIntFreq / (pow10 / 4)) * (pow10 / 4);
                resMap.put("value", afterCeil);
                double unitIndex = Math.floor(log10 / 3);
                maxFreq.append(afterCeil / Math.pow(10, unitIndex * 3));
                maxFreq.append(units[(int) unitIndex + 1]);
            } else {
                maxFreq.append(maxIntFreq);
            }
            maxFreq.append("hz");
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        resMap.put("name", maxFreq.toString());
        return resMap;
    }

    /**
     * Query cpu frequency information
     *
     * @param cpu cpu
     * @return List CpuFreqData
     */
    public List<CpuFreqData> queryCpuFreqData(final int cpu) {
        ArrayList<CpuFreqData> res = new ArrayList<>();
        String queryCpuFreqDataSql =
            "select cpu,value,ts-tb.start_ts as startNS \n" + "    from counter c ,trace_bounds tb\n"
                + "    inner join cpu_counter_track t on c.track_id = t.id\n"
                // The id in cpu_counter_track is invalid, here change t.id to t.cpu
                + "    where (name = 'cpufreq' or name='cpu_frequency') and cpu=" + cpu + "  order by  " + "ts ;";
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(queryCpuFreqDataSql); // Query data
            while (rs.next()) { // Print out the queried data
                CpuFreqData cpuData = new CpuFreqData();
                cpuData.setCpu(rs.getInt("cpu"));
                cpuData.setValue(rs.getLong("value"));
                cpuData.setStartTime(rs.getLong("startNS"));
                res.add(cpuData);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get all process information.
     *
     * @return List Process
     */
    public List<Process> queryProcess() {
        String sql = getSql("QueryProcess");
        ArrayList<Process> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(sql); // Query data
            while (rs.next()) { // Print out the queried data
                Process data = new Process();
                data.setName(rs.getString("processName"));
                data.setPid(rs.getInt("pid"));
                res.add(data);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Obtain process process thread information, group by the above process information
     *
     * @return List ThreadData
     */
    public List<ThreadData> queryProcessThreads() {
        String sql = getSql("QueryProcessThreads");
        ArrayList<ThreadData> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(sql); // Query data
            while (rs.next()) { // Print out the queried data
                ThreadData threadData = new ThreadData();
                threadData.setuPid(rs.getInt("upid"));
                threadData.setuTid(rs.getInt("utid"));
                threadData.setTid(rs.getInt("tid"));
                threadData.setPid(rs.getInt("pid"));
                threadData.setProcessName(rs.getString("processName"));
                threadData.setThreadName(rs.getString("threadName"));
                res.add(threadData);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Drawing process information is related to multiple CPUs.
     *
     * @param id id
     * @return List ProcessData
     */
    public List<ProcessData> queryProcessData(final int id) {
        String sql =
            "select thread_state.*,ts-tb.start_ts as startTime from thread_state,trace_bounds tb where utid in(\n"
                + "select it.id from internal_thread as it\n" + "    left join internal_process ip on it.upid = ip.id\n"
                + "where ip.pid=" + id + ") and cpu is not null order by startTime";
        ArrayList<ProcessData> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(sql); // Query data
            while (rs.next()) { // Print out the queried data
                ProcessData data = new ProcessData();
                data.setId(rs.getInt("id"));
                data.setUtid(rs.getInt("utid"));
                data.setCpu(rs.getInt("cpu"));
                data.setStartTime(rs.getLong("startTime"));
                data.setDuration(rs.getLong("dur"));
                data.setState(rs.getString("state"));
                res.add(data);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Thread information.
     *
     * @param id id
     * @return List ThreadData
     */
    public List<ThreadData> queryThreadData(final int id) {
        String sql = String.format(Locale.ENGLISH, getSql("QueryThreadData"), id);
        ArrayList<ThreadData> res = new ArrayList<>();
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(sql); // Query data
            while (rs.next()) { // Print out the queried data
                ThreadData data = new ThreadData();
                data.setTid(rs.getInt("tid"));
                data.setPid(rs.getInt("pid"));
                data.setCpu(rs.getInt("cpu"));
                data.setStartTime(rs.getLong("startTime"));
                data.setDuration(rs.getLong("dur"));
                data.setProcessName(rs.getString("processName"));
                data.setState(rs.getString("state"));
                res.add(data);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the current ring thread object
     *
     * @param cpuData cpuData
     * @return WakeupBean
     */
    public Optional<WakeupBean> queryWakeupThread(final CpuData cpuData) {
        WakeupBean wb = null;
        String wakeTimeSql = String
            .format(Locale.ENGLISH, getSql("QueryWakeUpThread_WakeTime"), cpuData.getId(), cpuData.getStartTime(),
                cpuData.getId(), cpuData.getStartTime());
        Connection conn = getConn();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(wakeTimeSql);
            while (rs.next()) {
                long wakeupTs = rs.getLong("wakeTs");
                long startTs = rs.getLong("start_ts");
                long preRow = rs.getLong("preRow");
                if (wakeupTs < preRow) {
                    rs.close();
                    statement.close();
                    return Optional.ofNullable(wb);
                }
                String wakeThreadSql = String.format(Locale.ENGLISH, getSql("QueryWakeUpThread_WakeThread"));
                wakeThreadSql = wakeThreadSql.replaceAll("wakeup_ts", wakeupTs + "");
                ResultSet rs1 = statement.executeQuery(wakeThreadSql);
                while (rs1.next()) {
                    wb = new WakeupBean();
                    wb.setWakeupTime(wakeupTs - startTs);
                    wb.setSchedulingLatency(cpuData.getStartTime() - wb.getWakeupTime());
                    wb.setWakeupCpu(rs1.getInt("cpu"));
                    wb.setWakeupThread(rs1.getString("thread"));
                    wb.setWakeupProcess(rs1.getString("process"));
                    wb.setWakeupPid(rs1.getString("pid"));
                    wb.setWakeupTid(rs1.getString("tid"));
                    if (wb.getWakeupProcess() == null) {
                        wb.setWakeupProcess(wb.getWakeupThread());
                    }
                    if (wb.getWakeupPid() == null) {
                        wb.setWakeupPid(wb.getWakeupTid());
                    }
                    wb.setSchedulingDesc(getSql("QueryWakeUpThread_Desc"));
                    rs1.close();
                    rs.close();
                    statement.close();
                    break;
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            free(conn);
        }
        return Optional.ofNullable(wb);
    }

    /**
     * Get cpu utilization
     *
     * @return ArrayList CpuRateBean
     */
    public ArrayList<CpuRateBean> getCpuUtilizationRate() {
        ArrayList<CpuRateBean> list = new ArrayList<>();
        String sql = getSql("GetCpuUtilizationRate");
        Connection conn = getConn();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                CpuRateBean cpuRateBean = new CpuRateBean();
                cpuRateBean.setCpu(rs.getInt("cpu"));
                cpuRateBean.setIndex(rs.getInt("ro"));
                cpuRateBean.setRate(rs.getDouble("rate"));
                list.add(cpuRateBean);
            }
            rs.close();
            st.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            free(conn);
        }
        return list;
    }

    /**
     * Get the memory information of the process
     *
     * @return List ProcessMem
     */
    public List<ProcessMem> getProcessMem() {
        ArrayList<ProcessMem> list = new ArrayList<>();
        String sql = "select\n" + "      process_counter_track.id as trackId,\n"
            + "      process_counter_track.name as trackName,\n" + "      upid,\n" + "      process.pid,\n"
            + "      process.name as processName,\n" + "      process.start_ts as startTs,\n"
            + "      process.end_ts as endTs\n" + "    from process_counter_track\n"
            + "    join process using(upid) order by trackName";
        Connection conn = getConn();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                ProcessMem bean = new ProcessMem();
                bean.setTrackId(rs.getInt("trackId"));
                bean.setTrackName(rs.getString("trackName"));
                bean.setUpid(rs.getInt("upid"));
                bean.setPid(rs.getInt("pid"));
                bean.setProcessName(rs.getString("processName"));
                list.add(bean);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (rs != null) {
                    rs.close();
                }
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return list;
    }

    /**
     * Get memory data information occupied by a process
     *
     * @param trackId trackId
     * @return List list
     */
    public List<ProcessMemData> getProcessMemData(final int trackId) {
        ArrayList<ProcessMemData> list = new ArrayList<>();
        String sql = "select c.*,c.ts-tb.start_ts startTime from counter c,trace_bounds tb where track_id=" + trackId;
        Connection conn = getConn();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                ProcessMemData bean = new ProcessMemData();
                bean.setTrackId(rs.getInt("track_id"));
                bean.setValue(rs.getInt("value"));
                bean.setStartTime(rs.getLong("startTime"));
                bean.setType(rs.getString("type"));
                list.add(bean);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (rs != null) {
                    rs.close();
                }
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return list;
    }

    /**
     * Get method call information according to tid
     *
     * @param tid tid
     * @return ArrayList
     */
    public ArrayList<FunctionBean> getFunDataByTid(final int tid) {
        ArrayList<FunctionBean> list = new ArrayList<>();
        String sql = String.format(Locale.ENGLISH, getSql("GetFunDataByTid"), tid);
        Connection conn = getConn();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                FunctionBean bean = new FunctionBean();
                bean.setTid(rs.getInt("tid"));
                bean.setIsMainThread(rs.getInt("is_main_thread"));
                bean.setTrackId(rs.getInt("track_id"));
                bean.setDepth(rs.getInt("depth"));
                bean.setThreadName(rs.getString("threadName"));
                bean.setFunName(rs.getString("funName"));
                bean.setStartTime(rs.getLong("startTs"));
                bean.setDuration(rs.getLong("dur"));
                list.add(bean);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (rs != null) {
                    rs.close();
                }
                free(conn);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return list;
    }

}
