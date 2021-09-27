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

package com.openharmony.hdc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HdcCommand for base hdc command ¡¢create at 20210912
 */
class HdcCommand {
    private static final String VERSION = "-v"; // Print hdc version
    private static final String TAG = "HdcCommand";
    private static final String DEFAULT_HDC_VERSION = "Ver: 1.1.1b";

    private String mHdcPath;
    private boolean CorrectVersion;

    private Process mProcess;
    private int status = -1;

    private ExecutorService mCachedThreadPool = Executors.newCachedThreadPool();

    /**
     * hdc bin command ,we must start hdc server
     *
     * @param path bin path
     */
    protected HdcCommand(String path) {
        Hilog.debug(TAG, "init HdcCommand");
        mHdcPath = path;
    }

    /**
     * execute hdc bin command
     *
     * @param command hdc bin command (start/kill)
     * @return whether execute successfully
     */
    protected boolean executeHdcCommand(String command) {
        Hilog.debug(TAG, "command is :" + command);
        if (command != null) {
            return processCommand(getHdcCommand(command));
        } else {
            return false;
        }
    }

    /**
     * whether current hdc server is correct
     *
     * @param HdcLocation hdc bin path
     * @return whether execute successfully
     */
    protected boolean isCorrectVersion(String hdcLocation) {
        if (mHdcPath == hdcLocation) {
            executeHdcCommand(VERSION);
        }
        return CorrectVersion;
    }

    private String[] getHdcCommand(String option) {
        List<String> command = new ArrayList<String>(1);
        command.add(mHdcPath);
        command.add(option);
        return command.toArray(new String[command.size()]);
    }

    private boolean processCommand(String[] strings) {
        ProcessBuilder processBuilder = new ProcessBuilder(strings);
        ArrayList<String> errorOutput = new ArrayList<String>();
        ArrayList<String> stdOutput = new ArrayList<String>();
        try {
            mProcess = processBuilder.start();
            status = getProcessOutput(mProcess, errorOutput, stdOutput);
        } catch (IOException error) {
            Hilog.debug(TAG, "processBuilder error " + error);
        } catch (InterruptedException ie) {
            Hilog.error(TAG, "Unable to run 'hdc': " + ie.getMessage());
        }

        if (strings.length == 2 && strings[1].equals(VERSION)) {
            if (stdOutput.size() > 0 && stdOutput.get(0).equals(DEFAULT_HDC_VERSION)) {
                CorrectVersion = true;
            } else {
                CorrectVersion = false;
            }
        }

        if (status != 0) {
            Hilog.error(TAG, "exec " + processBuilder.command().toString() + " failed -- run manually if necessary");
            return false;
        } else {
            Hilog.debug(TAG, "exec " + processBuilder.command().toString() + "succeeded");
            return true;
        }
    }

    private int getProcessOutput(final Process process, final ArrayList<String> error,
            final ArrayList<String> std) throws InterruptedException {
        mCachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // create a buffer to read the stderr output
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            error.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException error) {
                    Hilog.error(TAG, error);
                } finally {
                    close(is, errReader);
                }
            }
        });
        mCachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            std.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException error) {
                    Hilog.error(TAG, error);
                } finally {
                    close(is, outReader);
                }
            }
        });
        return process.waitFor();
    }

    private void close(InputStreamReader input , BufferedReader buff) {
        try {
            if (input != null) {
                input.close();
            }
            if (buff != null) {
                buff.close();
            }
        } catch (IOException err1) {
            err1.printStackTrace();
        }
    }
}