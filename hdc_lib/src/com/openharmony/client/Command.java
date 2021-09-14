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

package com.openharmony.client;

/**
 * all server Command ¡¢create at 20210912
 */
class Command {
    /**
     * REBOOT COMMAND
     */
    protected static final String REBOOT_COMMAND = "target boot"; // reboot

    /**
     * INSTALL COMMAND
     */
    protected static final String INSTALL_COMMAND = "install"; // install

    /**
     * UNINSTALL COMMAND
     */
    protected static final String UNINSTALL_COMMAND = "uninstall"; // uninstall success

    /**
     * START HAP CLASS COMMAND
     */
    protected static final String START_HAP_CLASS_COMMAND = "aa start -a"; // start hap

    /**
     * START HAP PACKAGE COMMAND
     */
    protected static final String START_HAP_PACKAGE_COMMAND = "-b"; // start hap

    /**
     * FILE SEND
     */
    protected static final String FILE_SEND = "file send"; // file send

    /**
     * FILE_RECV
     */
    protected static final String FILE_RECV = "file recv"; // file recv

    /**
     * HILOG COMMAND
     */
    protected static final String HILOG_COMMAND = "hilog"; // hilog

    /**
     * SHELL COMMAND
     */
    protected static final String SHELL_COMMAND = "shell"; // shell command

    /**
     * GETPROP COMMAND
     */
    protected static final String GETPROP_COMMAND = "getparam"; // getprop

    /**
     * BACK FLAG
     */
    protected static final String BACK_FLAG = "callback"; // call back flag

    /**
     * DEBUGABLE COMMAND
     */
    protected static final String DEBUGABLE_COMMAND = "ro.debuggable"; // ROOT ?

    /**
     * ROOT COMMAND
     */
    protected static final String ROOT_COMMAND = "smode"; // root

    /**
     * REMOUNT COMMAND
     */
    protected static final String REMOUNT_COMMAND = "target mount"; // remount

    /**
     * CONN COMMAND
     */
    protected static final String CONN_COMMAND = "tconn"; // connect ip:port

    /**
     * CONN RM COMMAND
     */
    protected static final String CONN_RM_COMMAND = "-remove"; // connect ip:port

    /**
     * COLON COMMAND
     */
    protected static final String COLON_COMMAND = ":"; // connect ip:port

    /**
     * FPORT ADD COMMAND
     */
    protected static final String FPORT_ADD_COMMAND = "fport"; // add fport

    /**
     * FPORT RM COMMAND
     */
    protected static final String FPORT_RM_COMMAND = "fport rm"; // rmove fport

    /**
     * FPORT COMMAND
     */
    protected static final String FPORT_COMMAND = "fport ls"; // get all fport

    /**
     * HIPERF COMMAND part 1
     */
    protected static final String HIPERF_1_COMMAND = " hiperf record -a -d "; // hiperf

    /**
     * HIPERF COMMAND part 2
     */
    protected static final String HIPERF_2_COMMAND = " -o "; // hiperf

    /**
     * BYTRACE COMMAND part 1
     */
    protected static final String BYTRACE_1_COMMAND = " bytrace -b 4096 -t "; // bytrace.

    /**
     * BYTRACE COMMAND part 2
     */
    protected static final String BYTRACE_2_COMMAND = " -z -o "; // bytrace

    /**
     * BLANK
     */
    protected static final String BLANK = " ";

    /**
     * EMPTY RESP
     */
    protected static final String EMPTY_RESP = "empty"; // no command

    /**
     * CALLBACK RESP
     */
    protected static final String CALLBACK_RESP = "please check call back"; // call back methond

    /**
     * VERIFY ERROR RESP
     */
    protected static final String VERIFY_ERROR_RESP = "verify"; // verify code error

    /**
     * ROOT ERROR RESP
     */
    protected static final String ROOT_ERROR_RESP = "root failed"; // root error

    /**
     * REMOUNT ERROR RESP
     */
    protected static final String REMOUNT_ERROR_RESP = "remount failed"; // remount error

    /**
     * REMOUNT RESP
     */
    protected static final String REMOUNT_RESP = "Mount finish"; // remount success

    /**
     * ROOT SUCCESS RESP
     */
    protected static final String ROOT_SUCCESS_RESP = "root succeess";

    /**
     * REMOUNT SUCCESS RESP
     */
    protected static final String REMOUNT_SUCCESS_RESP = "remount success";

    /**
     * RECV ERROR RESP
     */
    protected static final String RECV_ERROR_RESP = "Illegal file at Window"; // remount error

    /**
     * INSTALL SUCCESS
     */
    protected static final String INSTALL_SUCCESS = "install bundle successfully"; // install success

    /**
     * UNINSTALL SUCCESS
     */
    protected static final String UNINSTALL_SUCCESS = "uninstall bundle successfully"; // uninstall success

    /**
     * START SUCCESS
     */
    protected static final String START_SUCCESS = "start ability successfully"; // start hap success

    /**
     * win ILLEGAL file 01
     */
    protected static final String[] ILLEGAL_01 = {"CON", "PRN", "AUX", "NUL"};

    /**
     * win ILLEGAL file 02
     */
    protected static final String[] ILLEGAL_02 = {"COM", "LPT"};
}