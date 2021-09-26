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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Interact with commands on the device side
 */
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
    public String getHdcStringResult(ArrayList<String> hdcCmd) {
        String line;
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
                while ((line = brInputStream.readLine()) != null || (line = brErrorStream.readLine()) != null) {
                    cmdStrResult.append(line).append(System.getProperty("line.separator"));
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException);
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
                    LOGGER.info("cmd result line {}", line);
                    cmdStrResult.append(line);
                    if ("StartDaemonSuccess".equals(line)) {
                        break;
                    }
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException);
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
        if (hdcCmd.isEmpty()) {
            return "";
        }
        Process process;
        StringBuilder cmdStrResult = new StringBuilder();
        try {
            process = new ProcessBuilder(hdcCmd).start();
            process.waitFor(timeout, TimeUnit.MILLISECONDS);
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
                    LOGGER.info("cmd result line {}", line);
                    cmdStrResult.append(line);
                    // shell return success flag
                    if ("StartDaemonSuccess".equals(line)) {
                        break;
                    }
                }
                LOGGER.info("cmd result ok {}", cmdStrResult);
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brErrorStream);
                close(brInputStream);
            }
        } catch (IOException | InterruptedException ioException) {
            LOGGER.error("ioException error: " + ioException);
        }
        return cmdStrResult.toString();
    }

    /**
     * getCliResult
     *
     * @param hdcStr hdc String
     * @return ArrayList<ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getCliResult(ArrayList<String> hdcStr) {
        // Each line of string read
        String temp;
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try {
            Process process = new ProcessBuilder(hdcStr).start();
            // Obtain the input stream through the process object to obtain the successful execution of the command
            // Get the error flow through the process object to get the command error situation
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInput = null;
            BufferedReader brError = null;
            try {
                inputStream = process.getInputStream();
                errorStream = process.getErrorStream();
                brInput = new BufferedReader(new InputStreamReader(inputStream));
                brError = new BufferedReader(new InputStreamReader(errorStream));
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
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brError);
                close(brInput);
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException);
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
        Map<String, String> resultMap = new HashMap<>();
        try {
            Process process = new ProcessBuilder(hdcStr).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInput = null;
            BufferedReader brError = null;
            try {
                inputStream = process.getInputStream();
                brInput = new BufferedReader(new InputStreamReader(inputStream));
                errorStream = process.getErrorStream();
                brError = new BufferedReader(new InputStreamReader(errorStream));
                String temp;
                while ((temp = brInput.readLine()) != null || (temp = brError.readLine()) != null) {
                    String[] strings = temp.trim().split("  ");
                    resultMap.put(strings[1], strings[0]);
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brError);
                close(brInput);
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException);
        }
        return resultMap;
    }

    /**
     * Get result list
     *
     * @param hdcStr hdc String
     * @return ArrayList<ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getListResult(ArrayList<String> hdcStr) {
        // Each line of string read
        String temp;
        ArrayList<ArrayList<String>> devices = new ArrayList<>();
        // Number of rows read to return value
        try {
            Process process = new ProcessBuilder(hdcStr).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInput = null;
            BufferedReader brError = null;
            try {
                inputStream = process.getInputStream();
                brInput = new BufferedReader(new InputStreamReader(inputStream));
                errorStream = process.getErrorStream();
                brError = new BufferedReader(new InputStreamReader(errorStream));
                int lines = 0;
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
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brError);
                close(brInput);
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException);
        }
        return devices;
    }

    /**
     * Get result list
     *
     * @param hdcStr hdc String
     * @return ArrayList<ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getListHdcStdResult(ArrayList<String> hdcStr) {
        // Each line of string read
        String temp;
        ArrayList<ArrayList<String>> devices = new ArrayList<>();
        // Number of rows read to return value
        try {
            Process process = new ProcessBuilder(hdcStr).start();
            InputStream inputStream = null;
            InputStream errorStream = null;
            BufferedReader brInput = null;
            BufferedReader brError = null;
            try {
                inputStream = process.getInputStream();
                brInput = new BufferedReader(new InputStreamReader(inputStream));
                errorStream = process.getErrorStream();
                brError = new BufferedReader(new InputStreamReader(errorStream));
                while ((temp = brInput.readLine()) != null || (temp = brError.readLine()) != null) {
                    temp = temp.trim();
                    if (!"".equals(temp)) {
                        ArrayList<String> list = new ArrayList<>();
                        String[] newLine = temp.split("\t");
                        for (String str : newLine) {
                            String s = str.trim();
                            if (!"".equals(s)) {
                                list.add(s);
                            }
                        }
                        devices.add(list);
                    }
                }
            } catch (IOException ioException) {
                LOGGER.error("ioException error: " + ioException);
            } finally {
                close(inputStream);
                close(errorStream);
                close(brError);
                close(brInput);
            }
        } catch (IOException ioException) {
            LOGGER.error("ioException error: " + ioException);
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
        return cmdlist;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException exception) {
                LOGGER.error("IOException ", exception);
            }
        }
    }
}
