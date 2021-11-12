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

import java.util.ArrayList;

/**
 * ExecResult the result
 *
 * @since 2021/8/25
 */
public class ExecResult {
    private int exitCode;
    private ArrayList<String> executeOut;

    /**
     * ExecResult
     *
     * @param exitCode exitCode
     * @param executeOut executeOut
     */
    public ExecResult(int exitCode, ArrayList<String> executeOut) {
        super();
        this.exitCode = exitCode;
        this.executeOut = executeOut;
    }

    /**
     * getExitCode
     *
     * @return exitCode
     */
    public int getExitCode() {
        return exitCode;

    }

    /**
     * setExitCode
     *
     * @param exitCode exitCode
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * getExecuteOut
     *
     * @return ArrayList<String>
     */
    public ArrayList<String> getExecuteOut() {
        return executeOut;
    }

    /**
     * setExecuteOut
     *
     * @param executeOut executeOut
     */
    public void setExecuteOut(ArrayList<String> executeOut) {
        this.executeOut = executeOut;
    }

    @Override
    public String toString() {
        return "ExecResult{" + "exitCode=" + exitCode + ", executeOut=" + executeOut + '}';
    }
}
