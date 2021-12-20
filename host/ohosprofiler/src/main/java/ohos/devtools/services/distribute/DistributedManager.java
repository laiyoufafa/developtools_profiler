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

package ohos.devtools.services.distribute;

import ohos.devtools.datasources.transport.grpc.SystemTraceHelper;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.distributed.bean.DistributedParams;
import ohos.devtools.views.layout.utils.TraceStreamerUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_TIME;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_HAS_TRACE_FILE_INFO;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_PULL_TRACE_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.TRACE_STREAMER_LOAD;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_TIME;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_HAS_TRACE_FILE_INFO;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PULL_TRACE_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * DistributedManager
 *
 * @since 2021/9/20
 */
public class DistributedManager {
    private static final Logger LOGGER = LogManager.getLogger(DistributedManager.class);
    private static final String FULL_PERFETTO_STR =
        "power;ability;ace;app;audio;binder_lock;bionic;camera;database;distributeddatamgr;gfx;graphic;hal;"
            + "i2c;idle;input;mdfs;memreclaim;network;nnapi;notification;ohos;res;rro;rs;sm;ss;vibrator;video;view;"
            + "webview;wm;zaudio;zcamera;zimage;zmedia";
    private static final String LEAN_PERFETTO_STR =
        "ability;ace;app;distributeddatamgr;freq;graphic;idle;irq;mdfs;mmc;notification;ohos;pagecache;sync;"
            + "workq;zaudio;zcamera;zimage;zmedia";
    private static final int NUMBER_OF_TIMES = 10;

    private final DistributeDevice firstDevice;
    private final DistributeDevice secondDevice;
    private final int maxDurationParam = 10;
    private int firstFileSize;
    private int secondFileSize;
    private long firstStartTime;
    private long secondStartTime;
    private boolean isTest = false;
    private final ExecutorService executorService =
        new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    /**
     * DistributedManager
     *
     * @param firstDevice firstDevice
     * @param secondDevice secondDevice
     */
    public DistributedManager(DistributeDevice firstDevice, DistributeDevice secondDevice) {
        this.firstDevice = firstDevice;
        this.secondDevice = secondDevice;
    }

    /**
     * startCollecting
     *
     * @return boolean
     */
    public boolean startCollecting() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startCollecting");
        }
        Future<Optional<TimeCalibrationBean>> firstDeviceTimeBeanOpt =
            executorService.submit(new TimeCalibrationDevice(firstDevice));
        Future<Optional<TimeCalibrationBean>> secondDeviceTimeBeanOpt =
            executorService.submit(new TimeCalibrationDevice(secondDevice));
        try {
            Optional<TimeCalibrationBean> firstDeviceCalibrationBean = firstDeviceTimeBeanOpt.get();
            Optional<TimeCalibrationBean> secondDeviceCalibrationBean = secondDeviceTimeBeanOpt.get();

            if (firstDeviceCalibrationBean.isPresent() && secondDeviceCalibrationBean.isPresent()) {
                TimeCalibrationBean firstDeviceCalibration = firstDeviceCalibrationBean.get();
                TimeCalibrationBean secondDeviceCalibration = secondDeviceCalibrationBean.get();
                long realPc1Time = firstDeviceCalibration.getPcTime() + (firstDeviceCalibration.getUseTime() / 2);
                long realPc2Time = secondDeviceCalibration.getPcTime() + (secondDeviceCalibration.getUseTime() / 2);
                long timeDifference = realPc1Time - realPc2Time;
                if (timeDifference == 0) {
                    // 时间一致不需要同步
                    firstStartTime = firstDeviceCalibration.getDeviceTime();
                    secondStartTime = secondDeviceCalibration.getDeviceTime();
                } else if (timeDifference < 0) {
                    firstStartTime = firstDeviceCalibration.getDeviceTime();
                    secondStartTime = secondDeviceCalibration.getDeviceTime() + timeDifference;
                } else {
                    firstStartTime = firstDeviceCalibration.getDeviceTime() + timeDifference;
                    secondStartTime = secondDeviceCalibration.getDeviceTime();
                }
            }
            Future<String> firstResult = executorService.submit(new CollectingCallBack(firstDevice));
            Future<String> secondResult = executorService.submit(new CollectingCallBack(secondDevice));
            String firstString = firstResult.get();
            String secondString = secondResult.get();
            if (StringUtils.equals(firstString, "0") || StringUtils.equals(secondString, "0")) {
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("startSession failed");
                }
                return false;
            } else {
                firstDevice.setSessionId(firstString);
                secondDevice.setSessionId(secondString);
                return true;
            }
        } catch (InterruptedException | ExecutionException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("start Collect Failed ", exception);
            }
            return false;
        }
    }

    class TimeCalibrationDevice implements Callable<Optional<TimeCalibrationBean>> {
        private final DistributeDevice distributeDevice;

        /**
         * TimeCalibrationDevice
         *
         * @param distributeDevice distributeDevice
         */
        public TimeCalibrationDevice(DistributeDevice distributeDevice) {
            this.distributeDevice = distributeDevice;
        }

        @Override
        public Optional<TimeCalibrationBean> call() throws Exception {
            return calibrationEquipmentTime(distributeDevice);
        }

        private Optional<TimeCalibrationBean> calibrationEquipmentTime(DistributeDevice distributeDevice) {
            List<TimeCalibrationBean> timeBeans = new ArrayList<>();
            for (int count = 0; count < NUMBER_OF_TIMES; count++) {
                TimeCalibrationBean timeCalibrationBean = getDeviceTime(distributeDevice);
                timeBeans.add(timeCalibrationBean);
            }
            LOGGER.info("timeBeans {}", timeBeans);
            return timeBeans.stream().sorted(Comparator.comparingLong(TimeCalibrationBean::getUseTime)).findFirst();
        }
    }

    /**
     * hasFile
     *
     * @return boolean
     */
    public boolean hasFile() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("hasFile");
        }
        DeviceIPPortInfo firstInfo = firstDevice.getDeviceIPPortInfo();
        DeviceIPPortInfo secondInfo = secondDevice.getDeviceIPPortInfo();
        ArrayList<String> firstFormat;
        if (IS_SUPPORT_NEW_HDC && firstInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            firstFormat =
                conversionCommand(HDC_STD_HAS_TRACE_FILE_INFO, firstInfo.getDeviceID(), getTraceFilePath(firstDevice));
        } else {
            firstFormat =
                conversionCommand(HDC_HAS_TRACE_FILE_INFO, firstInfo.getDeviceID(), getTraceFilePath(firstDevice));
        }
        String firstResult = HdcWrapper.getInstance().execCmdBy(firstFormat);
        ArrayList<String> secondFrom;
        if (IS_SUPPORT_NEW_HDC && secondInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            secondFrom = conversionCommand(HDC_STD_HAS_TRACE_FILE_INFO, secondInfo.getDeviceID(),
                getTraceFilePath(secondDevice));
        } else {
            secondFrom =
                conversionCommand(HDC_HAS_TRACE_FILE_INFO, secondInfo.getDeviceID(), getTraceFilePath(secondDevice));
        }
        String secondResult = HdcWrapper.getInstance().execCmdBy(secondFrom);
        if (!firstFormat.isEmpty() && !secondFrom.isEmpty()) {
            String[] firstFileArray = firstResult.split("\t");
            String[] secondFileArray = secondResult.split("\t");
            if (firstFileSize != 0 && firstFileSize == Integer.valueOf(firstFileArray[0]) && secondFileSize != 0
                && secondFileSize == Integer.valueOf(secondFileArray[0])) {
                return true;
            } else {
                firstFileSize = Integer.valueOf(firstFileArray[0]);
                secondFileSize = Integer.valueOf(secondFileArray[0]);
                return false;
            }
        }
        return false;
    }

    /**
     * pullTraceFile
     *
     * @return boolean
     */
    public boolean pullTraceFile() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pullTraceFile");
        }
        DeviceIPPortInfo firstInfo = firstDevice.getDeviceIPPortInfo();
        DeviceIPPortInfo secondInfo = secondDevice.getDeviceIPPortInfo();
        String secondTraceFilePath;
        String firstTraceFilePath;
        if (isTest) {
            firstTraceFilePath = getTestTraceFilePath(firstDevice);
            secondTraceFilePath = getTestTraceFilePath(secondDevice);
        } else {
            firstTraceFilePath = getTraceFilePath(firstDevice);
            secondTraceFilePath = getTraceFilePath(secondDevice);
        }
        ArrayList pullFirstTraceFile;
        if (IS_SUPPORT_NEW_HDC && firstInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            pullFirstTraceFile = conversionCommand(HDC_STD_PULL_TRACE_FILE, firstInfo.getDeviceID(), firstTraceFilePath,
                TraceStreamerUtils.getInstance().getCreateFileDir());
        } else {
            pullFirstTraceFile = conversionCommand(HDC_PULL_TRACE_FILE, firstInfo.getDeviceID(), firstTraceFilePath,
                TraceStreamerUtils.getInstance().getCreateFileDir());
        }
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pullFirstTraceFile {}", pullFirstTraceFile);
        }
        HdcWrapper.getInstance().getHdcStringResult(pullFirstTraceFile);
        ArrayList pullSecondTraceFile;
        if (IS_SUPPORT_NEW_HDC && secondInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            pullSecondTraceFile =
                conversionCommand(HDC_STD_PULL_TRACE_FILE, secondInfo.getDeviceID(), secondTraceFilePath,
                    TraceStreamerUtils.getInstance().getCreateFileDir());
        } else {
            pullSecondTraceFile = conversionCommand(HDC_PULL_TRACE_FILE, secondInfo.getDeviceID(), secondTraceFilePath,
                TraceStreamerUtils.getInstance().getCreateFileDir());
        }
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pullSecondTraceFile {}", pullSecondTraceFile);
        }
        HdcWrapper.getInstance().getHdcStringResult(pullSecondTraceFile);
        return true;
    }

    /**
     * analysisFileTraceFile
     *
     * @return boolean
     */
    public boolean analysisFileTraceFile() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("analysisFileTraceFile");
        }
        String firstTraceFile;
        String secondTraceFile;
        if (isTest) {
            firstTraceFile = getTestTraceFile(firstDevice);
            secondTraceFile = getTestTraceFile(secondDevice);
        } else {
            firstTraceFile = getTraceFile(firstDevice);
            secondTraceFile = getTraceFile(secondDevice);
        }
        String baseDir = TraceStreamerUtils.getInstance().getBaseDir();
        String fileDir = TraceStreamerUtils.getInstance().getCreateFileDir();
        String traceStreamerApp = TraceStreamerUtils.getInstance().getTraceStreamerApp();
        String dbPathFirst = TraceStreamerUtils.getInstance().getDbPath(getTraceDBName(firstDevice));
        ArrayList firstList =
            conversionCommand(TRACE_STREAMER_LOAD, baseDir + traceStreamerApp, fileDir + firstTraceFile, dbPathFirst);
        HdcWrapper.getInstance().getHdcStringResult(firstList);
        String dbPathSecond = TraceStreamerUtils.getInstance().getDbPath(getTraceDBName(secondDevice));
        ArrayList secondList =
            conversionCommand(TRACE_STREAMER_LOAD, baseDir + traceStreamerApp, fileDir + secondTraceFile, dbPathSecond);
        HdcWrapper.getInstance().getHdcStringResult(secondList);
        return true;
    }

    /**
     * getDistributedParams
     *
     * @return DistributedParams
     */
    public DistributedParams getDistributedParams() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getDistributedParams");
        }
        DeviceIPPortInfo deviceIPPortInfo = firstDevice.getDeviceIPPortInfo();
        DeviceIPPortInfo secondDeviceIPPortInfo = secondDevice.getDeviceIPPortInfo();
        String processName = firstDevice.getProcessName();
        String secondName = secondDevice.getProcessName();
        String pid = processName.substring(processName.lastIndexOf("(") + 1, processName.lastIndexOf(")"));
        String secondPid = secondName.substring(secondName.lastIndexOf("(") + 1, secondName.lastIndexOf(")"));
        if (isTest) {
            return new DistributedParams.Builder().setDeviceNameA(deviceIPPortInfo.getDeviceName())
                .setDeviceNameB(secondDeviceIPPortInfo.getDeviceName())
                .setPathA(TraceStreamerUtils.getInstance().getDbPath(getTraceDBName(firstDevice)))
                .setPathB(TraceStreamerUtils.getInstance().getDbPath(getTraceDBName(secondDevice)))
                .setPkgNameA(processName).setPkgNameB(secondName).setProcessIdA(27521).setProcessIdB(1155).build();
        }
        return new DistributedParams.Builder().setDeviceNameA(deviceIPPortInfo.getDeviceName())
            .setDeviceNameB(secondDeviceIPPortInfo.getDeviceName())
            .setPathA(TraceStreamerUtils.getInstance().getDbPath(getTraceDBName(firstDevice)))
            .setPathB(TraceStreamerUtils.getInstance().getDbPath(getTraceDBName(secondDevice))).setPkgNameA(processName)
            .setPkgNameB(secondName).setProcessIdA(Integer.parseInt(pid)).setProcessIdB(Integer.parseInt(secondPid))
            .setOffsetA(firstStartTime).setOffsetB(secondStartTime).build();
    }

    private String getTraceFilePath(DistributeDevice distributeDevice) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTraceFilePath");
        }
        return "/data/local/tmp/" + getTraceFile(distributeDevice);
    }

    private String getTestTraceFilePath(DistributeDevice distributeDevice) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTestTraceFilePath");
        }
        return "/data/local/tmp/" + getTestTraceFile(distributeDevice);
    }

    private String getTraceFile(DistributeDevice distributeDevice) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTraceFile");
        }
        DeviceIPPortInfo deviceIPPortInfo = distributeDevice.getDeviceIPPortInfo();
        String processName = distributeDevice.getProcessName();
        String pid = processName.substring(processName.lastIndexOf("(") + 1, processName.lastIndexOf(")"));
        return deviceIPPortInfo.getDeviceName() + pid + ".bytrace";
    }

    private String getTestTraceFile(DistributeDevice distributeDevice) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTestTraceFile");
        }
        if (distributeDevice.equals(firstDevice)) {
            return "fbs_dev_1.trace";
        } else {
            return "fbs_dev_2.trace";
        }
    }

    private String getTraceDBName(DistributeDevice distributeDevice) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTraceDBName");
        }
        DeviceIPPortInfo deviceIPPortInfo = distributeDevice.getDeviceIPPortInfo();
        String processName = distributeDevice.getProcessName();
        String pid = processName.substring(processName.lastIndexOf("(") + 1, processName.lastIndexOf(")"));
        return deviceIPPortInfo.getDeviceName() + pid + ".db";
    }

    /**
     * cancelCollection
     */
    public void cancelCollection() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("cancelCollection");
        }
        SystemTraceHelper systemTraceHelper = SystemTraceHelper.getSingleton();
        systemTraceHelper.stopSession(firstDevice.getDeviceIPPortInfo(), firstDevice.getSessionId());
        systemTraceHelper.cancelActionDestroySession(firstDevice.getDeviceIPPortInfo(), firstDevice.getSessionId());
        systemTraceHelper.stopSession(secondDevice.getDeviceIPPortInfo(), secondDevice.getSessionId());
        systemTraceHelper.cancelActionDestroySession(secondDevice.getDeviceIPPortInfo(), secondDevice.getSessionId());
    }

    /**
     * stopCollection
     */
    public void stopCollection() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopCollection");
        }
        SystemTraceHelper systemTraceHelper = SystemTraceHelper.getSingleton();
        systemTraceHelper.stopSession(firstDevice.getDeviceIPPortInfo(), firstDevice.getSessionId());
        systemTraceHelper.cancelActionDestroySession(firstDevice.getDeviceIPPortInfo(), firstDevice.getSessionId());
        systemTraceHelper.stopSession(secondDevice.getDeviceIPPortInfo(), secondDevice.getSessionId());
        systemTraceHelper.cancelActionDestroySession(secondDevice.getDeviceIPPortInfo(), secondDevice.getSessionId());

    }

    /**
     * getMaxDurationParam
     *
     * @return int
     */
    public int getMaxDurationParam() {
        return maxDurationParam;
    }

    /**
     * CollectingCallBack
     */
    class CollectingCallBack implements Callable<String> {
        private final DistributeDevice distributeDevice;

        /**
         * CollectingCallBack
         *
         * @param distributeDevice distributeDevice
         */
        public CollectingCallBack(DistributeDevice distributeDevice) {
            this.distributeDevice = distributeDevice;
        }

        @Override
        public String call() throws Exception {
            String outPutPath = getTraceFilePath(distributeDevice);
            String perfettoString;
            if (distributeDevice.getDeviceIPPortInfo().getDeviceType() == DeviceType.FULL_HOS_DEVICE) {
                perfettoString = FULL_PERFETTO_STR;
            } else {
                perfettoString = LEAN_PERFETTO_STR;
            }
            return SystemTraceHelper.getSingleton()
                .createSessionByTraceRequest(distributeDevice.getDeviceIPPortInfo(), perfettoString, maxDurationParam,
                    10, outPutPath, false);
        }
    }

    private TimeCalibrationBean getDeviceTime(DistributeDevice distributeDevice) {
        ArrayList<String> cmd;
        if (IS_SUPPORT_NEW_HDC && distributeDevice.getDeviceIPPortInfo().getDeviceType() == LEAN_HOS_DEVICE) {
            cmd = conversionCommand(HDC_STD_GET_TIME, distributeDevice.getDeviceIPPortInfo().getDeviceID());
        } else {
            cmd = conversionCommand(HDC_GET_TIME, distributeDevice.getDeviceIPPortInfo().getDeviceID());
        }
        long pcTime = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        String deviceTime = HdcWrapper.getInstance().execCmdBy(cmd);
        long endTime = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        TimeCalibrationBean timeCalibrationBean = new TimeCalibrationBean();
        timeCalibrationBean.setPcTime(pcTime);
        timeCalibrationBean.setEndTime(endTime);
        timeCalibrationBean.setDeviceTime(Long.parseLong(deviceTime));
        return timeCalibrationBean;
    }

    class TimeCalibrationBean {
        private long pcTime;
        private long endTime;
        private long deviceTime;

        /**
         * getPcTime
         *
         * @return long
         */
        public long getPcTime() {
            return pcTime;
        }

        /**
         * setPcTime
         *
         * @param pcTime pcTime
         */
        public void setPcTime(long pcTime) {
            this.pcTime = pcTime;
        }

        /**
         * getEndTime
         *
         * @return long
         */
        public long getEndTime() {
            return endTime;
        }

        /**
         * setEndTime
         *
         * @param endTime endTime
         */
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        /**
         * getDeviceTime
         *
         * @return long
         */
        public long getDeviceTime() {
            return deviceTime;
        }

        /**
         * setDeviceTime
         *
         * @param deviceTime deviceTime
         */
        public void setDeviceTime(long deviceTime) {
            this.deviceTime = deviceTime;
        }

        /**
         * getUseTime
         *
         * @return long
         */
        public long getUseTime() {
            return endTime - pcTime;
        }

        @Override
        public String toString() {
            return "TimeCalibrationBean{" + "pcTime=" + pcTime + ", endTime=" + endTime + ", deviceTime=" + deviceTime
                + ", useTime=" + (endTime - pcTime) + '}';
        }
    }

    /**
     * Result
     */
    class Result {
        private String sessionId;
        private String StartTime;

        /**
         * Result
         *
         * @param sessionId sessionId
         * @param startTime startTime
         */
        public Result(String sessionId, String startTime) {
            this.sessionId = sessionId;
            StartTime = startTime;
        }

        /**
         * getSessionId
         *
         * @return String
         */
        public String getSessionId() {
            return sessionId;
        }

        /**
         * getStartTime
         *
         * @return String
         */
        public String getStartTime() {
            return StartTime;
        }
    }

}
