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

package ohos.devtools.datasources.transport.hdc;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ohos.devtools.datasources.utils.common.Constant.TIME_OUT;

/**
 * Interact with commands on the device side
 *
 * @since 2021/5/19 16:39
 */
public class HdcWrapper {
    private static final Logger LOGGER = LogManager.getLogger(HdcWrapper.class);
    private static final HdcWrapper INSTANCE = new HdcWrapper();

    private HdcWrapper() {
    }

    /**
     * Get an instance
     *
     * @return HdcWrapper
     */
    public static HdcWrapper getInstance() {
        return INSTANCE;
    }

    /**
     * generate Cmd Head
     *
     * @param isLeakOhos judge use hdc or hdc_std
     * @param deviceId select device id
     * @return ArrayList <String>
     */
    public ArrayList<String> generateDeviceCmdHead(boolean isLeakOhos, String deviceId) {
        ArrayList<String> headCommand = new ArrayList<>();
        String pluginPath;
        if (isLeakOhos) {
            pluginPath = SessionManager.getInstance().getHdcStdPath();
        } else {
            pluginPath = SessionManager.getInstance().getHdcPath();
        }
        headCommand.add(pluginPath);
        headCommand.add("-t");
        headCommand.add(deviceId);
        headCommand.add("shell");
        return headCommand;
    }

    /**
     * Get hdc string result
     *
     * @param hdcCmd hdc command
     * @return String
     */
    public String getHdcStringResult(ArrayList<String> hdcCmd) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHdcStringResult");
        }
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            Process process = new ProcessBuilder(hdcCmd).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInputStream = null;
            BufferedReader brErrorStream = null;
            try {
                inputStream = process.getInputStream();
                errorStream = process.getErrorStream();
                brInputStream = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("gbk")));
                brErrorStream = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    cmdStrResult.append(line).append(System.getProperty("line.separator"));
                }
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("ioException error: " + ioException);
                }
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
                if (Objects.nonNull(process)) {
                    process.destroy();
                }
            }
        } catch (IOException ioException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("ioException error: " + ioException);
            }
        }
        return cmdStrResult.toString();
    }

    /**
     * Get hdc string result
     *
     * @param hdcCmd hdc command
     * @param timeout timeout
     * @return String
     */
    public String getHdcStringResult(ArrayList<String> hdcCmd, long timeout) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHdcStringResult");
        }
        long time = timeout;
        if (timeout <= 0) {
            time = 5;
        }
        ExecResult execResult = new CmdExecutors().executeCommand(hdcCmd, time);
        StringBuilder cmdStrResult = new StringBuilder();
        int exitCode = execResult.getExitCode();
        if (exitCode == 0) {
            ArrayList<String> executeOut = execResult.getExecuteOut();
            executeOut.forEach(line -> {
                cmdStrResult.append(line).append(System.getProperty("line.separator"));
            });
        }
        if (exitCode == -2) {
            return TIME_OUT;
        }
        return cmdStrResult.toString();
    }

    /**
     * Get hdc string result
     *
     * @param hdcCmd hdc command
     * @return String
     */
    public String getHdcResult(ArrayList<String> hdcCmd) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHdcStringResult");
        }
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            Process process = new ProcessBuilder(hdcCmd).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInputStream = null;
            BufferedReader brErrorStream = null;
            try {
                inputStream = process.getInputStream();
                errorStream = process.getErrorStream();
                brInputStream = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("gbk")));
                brErrorStream = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    cmdStrResult.append(line);
                }
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("ioException error: " + ioException);
                }
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
                if (Objects.nonNull(process)) {
                    process.destroy();
                }
            }
        } catch (IOException ioException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("ioException error: " + ioException);
            }
        }
        return cmdStrResult.toString();
    }

    /**
     * execCmdBy
     *
     * @param hdcCmd hdc command
     * @return String
     */
    public String execCmdBy(ArrayList<String> hdcCmd) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("execCmdBy");
        }
        Process process;
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            process = new ProcessBuilder(hdcCmd).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInputStream = null;
            BufferedReader brErrorStream = null;
            try {
                inputStream = process.getInputStream();
                errorStream = process.getErrorStream();
                brInputStream = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("gbk")));
                brErrorStream = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    cmdStrResult.append(line);
                    if ("StartDaemonSuccess".equals(line)) {
                        break;
                    }
                }
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("ioException error: " + ioException);
                }
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
                if (Objects.nonNull(process)) {
                    process.destroy();
                }
            }
        } catch (IOException ioException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("ioException error: " + ioException);
            }
        }
        return cmdStrResult.toString();
    }

    /**
     * execCmdBy
     *
     * @param hdcCmd hdc command
     * @param timeout timeout
     * @return String
     */
    public String execCmdBy(ArrayList<String> hdcCmd, long timeout) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("execCmdBy");
        }
        if (hdcCmd.isEmpty()) {
            return "";
        }
        StringBuilder cmdStrResult = new StringBuilder();
        ExecResult execResult = new CmdExecutors().executeCommand(hdcCmd, timeout);
        int exitCode = execResult.getExitCode();
        if (exitCode == 0) {
            ArrayList<String> executeOut = execResult.getExecuteOut();
            for (String excuteResult : executeOut) {
                if (StringUtils.isNotBlank(excuteResult)) {
                    cmdStrResult.append(excuteResult);
                }
            }
        }
        return cmdStrResult.toString();
    }

    /**
     * execCmd
     *
     * @param hdcCmd hdcCmd
     * @return List <String>
     */
    public List<String> execCmd(ArrayList<String> hdcCmd) {
        List<String> result = new ArrayList<>();
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("execCmdBy");
        }
        Process process;
        String line;
        try {
            process = new ProcessBuilder(hdcCmd).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInputStream = null;
            BufferedReader brErrorStream = null;
            try {
                inputStream = process.getInputStream();
                errorStream = process.getErrorStream();
                brInputStream = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("gbk")));
                brErrorStream = new BufferedReader(new InputStreamReader(errorStream));
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    if (ProfilerLogManager.isInfoEnabled()) {
                        LOGGER.info("cmd result line {}", line);
                    }
                    result.add(line);
                    if ("StartDaemonSuccess".equals(line)) {
                        break;
                    }
                }
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("ioException error: " + ioException);
                }
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
                if (Objects.nonNull(process)) {
                    process.destroy();
                }
            }
        } catch (IOException ioException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("ioException error: " + ioException);
            }
        }
        return result;
    }

    /**
     * getCliResult
     *
     * @param hdcStr hdc String
     * @return ArrayList <ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getCliResult(ArrayList<String> hdcStr) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getCliResult");
        }
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        ExecResult execResult = new CmdExecutors().executeCommand(hdcStr, 8);
        int exitCode = execResult.getExitCode();
        if (exitCode == 0) {
            ArrayList<String> executeOut = execResult.getExecuteOut();
            for (String excuteResult : executeOut) {
                if (StringUtils.isNotBlank(excuteResult)) {
                    ArrayList<String> list = new ArrayList<>();
                    String[] newLine = excuteResult.split(":");
                    for (String str : newLine) {
                        String value = str.trim();
                        if (!"".equals(value)) {
                            list.add(value);
                        }
                    }
                    result.add(list);
                }
            }
        }
        if (exitCode == -2) {
            ArrayList<String> list = new ArrayList<>();
            list.add(TIME_OUT);
            result.add(list);
        }
        return result;
    }

    /**
     * Get result list
     *
     * @param hdcStr hdc String
     * @return Map<String, String>
     */
    public Map<String, String> getCmdResultMap(ArrayList<String> hdcStr) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getCmdResultMap");
        }
        Map<String, String> resultMap = new HashMap<>();
        ExecResult execResult = new CmdExecutors().executeCommandByLine(hdcStr, 5);
        int exitCode = execResult.getExitCode();
        if (exitCode == 0) {
            ArrayList<String> executeOut = execResult.getExecuteOut();
            for (String excuteResult : executeOut) {
                if (StringUtils.isNotBlank(excuteResult)) {
                    String[] strings = excuteResult.trim().split("  ");
                    resultMap.put(strings[1], strings[0]);
                }
            }
        }
        return resultMap;
    }

    /**
     * Get result list
     *
     * @param hdcStr hdc String
     * @return ArrayList <ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getListResult(ArrayList<String> hdcStr) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getListResult");
        }
        ArrayList<ArrayList<String>> devices = new ArrayList<>();
        ExecResult execResult = new CmdExecutors().executeCommand(hdcStr, 8);
        int exitCode = execResult.getExitCode();
        if (exitCode == 0) {
            ArrayList<String> executeOut = execResult.getExecuteOut();
            for (String excuteResult : executeOut) {
                if (StringUtils.isNotBlank(excuteResult)) {
                    ArrayList<String> list = new ArrayList<>();
                    String[] newLine = excuteResult.split(" ");
                    for (String str : newLine) {
                        String value = str.trim();
                        if (!"".equals(value)) {
                            list.add(value);
                        }
                    }
                    devices.add(list);
                }
            }
        }
        if (exitCode == -2) {
            ArrayList<String> list = new ArrayList<>();
            list.add(TIME_OUT);
            devices.add(list);
        }
        return devices;
    }

    /**
     * Get result list
     *
     * @param hdcStr hdc String
     * @return ArrayList <ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getListHdcStdResult(ArrayList<String> hdcStr) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getListHdcStdResult");
        }
        String temp;
        ArrayList<ArrayList<String>> devices = new ArrayList<>();
        ExecResult execResult = new CmdExecutors().executeCommand(hdcStr, 8);
        int exitCode = execResult.getExitCode();
        if (exitCode == 0) {
            ArrayList<String> executeOut = execResult.getExecuteOut();
            for (String excuteResult : executeOut) {
                if (StringUtils.isNotBlank(excuteResult)) {
                    ArrayList<String> list = new ArrayList<>();
                    String[] newLine = excuteResult.split("\t");
                    for (String str : newLine) {
                        if (StringUtils.isNotBlank(str)) {
                            String value = str.trim();
                            list.add(value);
                        }
                    }
                    devices.add(list);
                }
            }
        }
        if (exitCode == -2) {
            ArrayList<String> list = new ArrayList<>();
            list.add(TIME_OUT);
            devices.add(list);
        }
        return devices;
    }

    /**
     * conversionCommand
     *
     * @param list list
     * @param args args
     * @return ArrayList
     */
    public static ArrayList<String> conversionCommand(ArrayList<String> list, String... args) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("conversionCommand");
        }
        int index = 0;
        ArrayList<String> cmdlist = new ArrayList<>();
        for (String string : list) {
            if (StringUtils.equals(string, "%s")) {
                cmdlist.add(args[index]);
                index = index + 1;
                continue;
            }

            if (string.contains("%s") && !StringUtils.equals(string, "%s")) {
                if (args.length <= index) {
                    cmdlist.add(string);
                    continue;
                } else {
                    String replace = string.replace("%s", args[index]);
                    cmdlist.add(replace);
                }
                index = index + 1;
                continue;
            }
            cmdlist.add(string);
        }
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("conversionCommand cmd is {}", cmdlist);
        }
        return cmdlist;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("close");
                }
                closeable.close();
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("IOException ", exception);
                }
            }
        }
    }
}
