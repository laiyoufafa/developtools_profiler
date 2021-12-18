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

package ohos.devtools.services.hiperf;

import ohos.devtools.datasources.transport.grpc.service.HiperfReport;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Trace File Parse
 *
 * @since 2021/8/20 18:00
 */
public class HiperfParse extends ParsePerf {
    /**
     * Log
     */
    private static final Logger LOGGER = LogManager.getLogger(HiperfParse.class);

    /**
     * File Head Sign
     */
    private static final String HEAD = "HIPERF_PB_";

    /**
     * File Struct List
     */
    private final Map<Integer, HiperfReport.SymbolTableFile> mFiles = new HashMap<>();

    /**
     * Thread Struct List
     */
    private final Map<Integer, HiperfReport.VirtualThreadInfo> mThreads = new HashMap<>();

    /**
     * Sample Struct List
     */
    private final List<HiperfReport.CallStackSample> mSamples = new ArrayList<>();

    /**
     * Parse trace File
     *
     * @param trace trace file
     * @throws IOException io
     */
    public void parseFile(File trace) throws IOException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("parseFile");
        }
        if (trace == null || !trace.isFile()) {
            return;
        }
        ByteBuffer buffer = byteBufferFromFile(trace);
        verifyHead(buffer, HEAD);
        // myTraceVersion
        buffer.getShort();
        int recordSize = buffer.getInt();
        long sampleCount = 0L;
        while (recordSize != 0) {
            byte[] recordBytes = new byte[recordSize];
            buffer.get(recordBytes);
            HiperfReport.HiperfRecord record = HiperfReport.HiperfRecord.parseFrom(recordBytes);

            switch (record.getRecordTypeCase()) {
                case FILE:
                    HiperfReport.SymbolTableFile file = record.getFile();
                    mFiles.put(file.getId(), file);
                    break;
                case STATISTIC:
                    HiperfReport.SampleStatistic situation = record.getStatistic();
                    sampleCount = situation.getCount();
                    break;
                case SAMPLE:
                    HiperfReport.CallStackSample sample = record.getSample();
                    mSamples.add(sample);
                    break;
                case THREAD:
                    HiperfReport.VirtualThreadInfo thread = record.getThread();
                    mThreads.put(thread.getTid(), thread);
                    break;
                default:
                    LOGGER.info("Lost Case");
            }
            recordSize = buffer.getInt();
        }

        if (mSamples.size() != sampleCount) {
            throw new IllegalStateException("Samples count doesn't match the number of samples read.");
        }
    }

    /**
     * insertPerfSample
     */
    public void insertSample() {
        Connection conn = PerfDAO.getInstance().getConn();
        try {
            PreparedStatement callChainPst = conn.prepareStatement(
                    "insert into "
                            + "perf_callchain("
                            + "sample_id, "
                            + "callchain_id, "
                            + "vaddr_in_file, "
                            + "file_id, "
                            + "symbol_id) "
                            + "values(?,?,?,?,?)");
            conn.setAutoCommit(false);

            PreparedStatement samplePst = conn.prepareStatement(
                    "insert into "
                            + "perf_sample("
                            + "sample_id, "
                            + "timestamp, "
                            + "thread_id, "
                            + "event_count, "
                            + "event_type_id) "
                            + "values(?,?,?,?,?)");
            conn.setAutoCommit(false);

            // sample
            getSamplePst(samplePst, callChainPst);

            // files
            PreparedStatement filePst = getFilePst(conn);
            // thread
            PreparedStatement threadPst = getThreadPst(conn);
            try {
                samplePst.executeBatch();
                callChainPst.executeBatch();
                filePst.executeBatch();
                threadPst.executeBatch();
                conn.commit();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            } finally {
                if (samplePst != null) {
                    samplePst.close();
                }
                conn.close();
            }
        } catch (SQLException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
        }
    }


    private PreparedStatement getThreadPst(Connection conn) throws SQLException {
        PreparedStatement threadPst = conn.prepareStatement(
                "insert into "
                        + "perf_thread("
                        + "thread_id, "
                        + "process_id, "
                        + "thread_name) "
                        + "values(?,?,?)");
        conn.setAutoCommit(false);
        mThreads.values().forEach(thread -> {
            try {
                threadPst.setInt(1, thread.getTid());
                threadPst.setInt(2, thread.getPid());
                threadPst.setString(3, thread.getName());
                threadPst.addBatch();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            }
        });
        return threadPst;
    }

    private PreparedStatement getFilePst(Connection conn) throws SQLException {
        PreparedStatement filePst = conn.prepareStatement(
                "insert into "
                        + "perf_files("
                        + "file_id, "
                        + "symbol, "
                        + "path) "
                        + "values(?,?,?)");
        conn.setAutoCommit(false);
        mFiles.values().forEach(perffile -> {
            perffile.getFunctionNameList().forEach(function -> {
                try {
                    filePst.setInt(1, perffile.getId());
                    filePst.setString(2, function);
                    filePst.setString(3, perffile.getPath());
                    filePst.addBatch();
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error(exception.getMessage());
                    }
                }
            });
        });
        return filePst;
    }

    private void getSamplePst(PreparedStatement samplePst, PreparedStatement callChainPst) {
        for (int sampleId = 0; sampleId < mSamples.size(); sampleId++) {
            HiperfReport.CallStackSample sample = mSamples.get(sampleId);
            try {
                samplePst.setLong(1, sampleId);
                samplePst.setLong(2, sample.getTime());
                samplePst.setInt(3, sample.getTid());
                samplePst.setLong(4, sample.getEventCount());
                samplePst.setInt(5, sample.getConfigNameId());
                samplePst.addBatch();
                for (int callChainId = 0; callChainId < sample.getCallStackFrameList().size(); callChainId++) {
                    HiperfReport.CallStackSample.CallStackFrame callChain =
                            sample.getCallStackFrameList().get(callChainId);
                    callChainPst.setLong(1, sampleId);
                    callChainPst.setLong(2, callChainId);
                    callChainPst.setLong(3, callChain.getSymbolsVaddr());
                    callChainPst.setLong(4, callChain.getSymbolsFileId());
                    callChainPst.setInt(5, callChain.getFunctionNameId());
                    callChainPst.addBatch();
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }
    }
}
