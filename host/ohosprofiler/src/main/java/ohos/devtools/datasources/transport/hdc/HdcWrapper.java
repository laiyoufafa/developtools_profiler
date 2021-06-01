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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Interact with commands on the device side
 *
 * @version 1.0
 * @date 2021/02/01 10:47
 **/
public class HdcWrapper {
    // Log
    private static final Logger LOGGER = LogManager.getLogger(HdcWrapper.class);

    /**
     * Singleton Hdc command parsing object
     */
    private static volatile HdcWrapper hdcWrapper;

    /**
     * Get an instance
     *
     * @return HdcWrapper
     */
    public static HdcWrapper getInstance() {
        if (hdcWrapper == null) {
            synchronized (HdcWrapper.class) {
                if (hdcWrapper == null) {
                    hdcWrapper = new HdcWrapper();
                }
            }
        }
        return hdcWrapper;
    }

    private HdcWrapper() {
    }

    /**
     * Get hdc string result
     *
     * @param hdcCmd hdc command
     * @return String
     */
    public String getHdcStringResult(String hdcCmd) {
        String line = "";
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            // Excuting an order
            Process process = Runtime.getRuntime().exec(hdcCmd);
            // Error command result output stream
            // Get command result output stream
            try (InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                BufferedReader brInputStream = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName("gbk")));
                BufferedReader brErrorStream = new BufferedReader(new InputStreamReader(errorStream))) {
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    cmdStrResult.append(line).append(System.getProperty("line.separator"));
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException.getMessage());
        }
        return cmdStrResult.toString();
    }

    /**
     * getHdcStringArrayResult
     *
     * @param hdcCmd hdc command
     * @return String
     */
    public String getHdcStringArrayResult(String[] hdcCmd) {
        String line = "";
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            // 执行命令
            Process process = Runtime.getRuntime().exec(hdcCmd);
            // 错误命令结果输出流
            // 获取命令结果输出流
            try (InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                BufferedReader brInputStream = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName("gbk")));
                BufferedReader brErrorStream = new BufferedReader(new InputStreamReader(errorStream))) {
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    cmdStrResult.append(line).append(System.getProperty("line.separator"));
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException.getMessage());
        }
        return cmdStrResult.toString();
    }

    /**
     * execCmdBy
     *
     * @param hdcCmd hdc command
     * @return String
     */
    public String execCmdBy(String hdcCmd) {
        // Excuting an order
        Process process = null;
        String line = "";
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(hdcCmd);
            // Get command and error command result output stream
            try (InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                BufferedReader brInputStream = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName("gbk")));
                BufferedReader brErrorStream = new BufferedReader(new InputStreamReader(errorStream))) {
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    if ("StartDaemonSuccess".equals(line)) {
                        break;
                    }
                }
                LOGGER.info("cmd result ok{}", cmdStrResult);
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException.getMessage());
        }
        return cmdStrResult.toString();
    }

    /**
     * execCmdBy
     *
     * @param hdcCmd  hdc command
     * @param timeout timeout
     * @return String
     */
    public String execCmdBy(String hdcCmd, long timeout) {
        // Excuting an order
        Process process = null;
        String line = "";
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(hdcCmd);
            process.waitFor(timeout, TimeUnit.MILLISECONDS);
            // Get command and error command result output stream
            try (InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                BufferedReader brInputStream = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName("gbk")));
                BufferedReader brErrorStream = new BufferedReader(new InputStreamReader(errorStream))) {
                LOGGER.info("cmd result ok {}", cmdStrResult);
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException.getMessage());
            }
        } catch (IOException | InterruptedException ioException) {
            LOGGER.error("ioException error: " + ioException.getMessage());
        }
        return cmdStrResult.toString();
    }

    /**
     * getCliResult
     *
     * @param hdcStr hdc String
     * @return ArrayList<ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getCliResult(String hdcStr) {
        // Each line of string read
        String temp = "";
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(hdcStr);
            // Obtain the input stream through the process object to obtain the successful execution of the command
            // Get the error flow through the process object to get the command error situation
            try (InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                BufferedReader brInput = new BufferedReader(new InputStreamReader(inputStream));
                BufferedReader brError = new BufferedReader(new InputStreamReader(errorStream))) {
                while ((temp = brInput.readLine()) != null || (temp = brError.readLine()) != null) {
                    temp = temp.trim();
                    if (!"".equals(temp)) {
                        ArrayList<String> list = new ArrayList<>();
                        String[] newLine = temp.split(":");
                        for (String str : newLine) {
                            String s = str.trim();
                            if (!"".equals(s)) {
                                list.add(s);
                            }
                        }
                        result.add(list);
                    }
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException.getMessage());
        }
        return result;
    }

    /**
     * Get result list
     *
     * @param hdcStr hdc String
     * @return ArrayList<ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getListResult(String hdcStr) {
        // Each line of string read
        String temp = "";
        ArrayList<ArrayList<String>> devices = new ArrayList<>();
        // Number of rows read to return value
        int lines = 0;
        try {
            Process process = Runtime.getRuntime().exec(hdcStr);
            try (InputStream inputStream = process.getInputStream();
                BufferedReader brInput = new BufferedReader(new InputStreamReader(inputStream));
                InputStream errorStream = process.getErrorStream();
                BufferedReader brError = new BufferedReader(new InputStreamReader(errorStream))) {
                while ((temp = brInput.readLine()) != null || (temp = brError.readLine()) != null) {
                    temp = temp.trim();
                    if (lines > 0 && !"".equals(temp)) {
                        ArrayList<String> list = new ArrayList<>();
                        String[] newLine = temp.split(" ");
                        for (String str : newLine) {
                            String s = str.trim();
                            if (!"".equals(s)) {
                                list.add(s);
                            }
                        }
                        devices.add(list);
                    }
                    lines++;
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException.getMessage());
        }
        return devices;
    }
}
