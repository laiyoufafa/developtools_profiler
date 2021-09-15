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

import java.security.InvalidParameterException;
import java.util.Locale;

import com.openharmony.hdc.HarmonyDebugConnector;
import com.openharmony.hdc.Hilog;

/**
 * client which send command to server ¡¢create at 20210912
 */
public class Client {
    private static final String TAG = "Client";

    private RemoteCommand mRemoteCommand;
    private LocalCommand mLocalCommand;
    private ShellCommand mShellCommand;
    private String mSerialNumber;
    private HarmonyDebugConnector mHdc;

    /**
     * init Client to send command
     *
     * @param serialNumber device id
     * @param hdc Instantiated HarmonyDebugConnector
     * @throws  InvalidParameterException value error
     */
    public Client(String serialNumber, HarmonyDebugConnector hdc) {
        if (serialNumber == null || isUnValid(serialNumber) || hdc == null) {
            throw new InvalidParameterException("client value error");
        }
        mSerialNumber = serialNumber;
        mHdc = hdc;
        mRemoteCommand = new RemoteCommand(hdc, serialNumber);
        mShellCommand = new ShellCommand(mRemoteCommand);
        mLocalCommand = new LocalCommand(hdc);
    }

    /**
     * reboot device
     */
    public void reboot() {
        mRemoteCommand.sendRemoteCommand(Command.REBOOT_COMMAND);
    }

    /**
     * get device SerialNumber of current client
     *
     * @return device SerialNumber
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * get HarmonyDebugConnector of current client
     *
     * @return this client HarmonyDebugConnector info
     */
    public HarmonyDebugConnector getHarmonyDebugConnector() {
        return mHdc;
    }

    /**
     * install hap with argv install -r xx.hap
     *
     * @param argv    install argv
     * @param hapPath hap path
     * @return whether Install successfully
     */
    public boolean installHap(String argv, String hapPath) {
        String result = mRemoteCommand
                .sendRemoteCommand(Command.INSTALL_COMMAND + Command.BLANK + argv + Command.BLANK + hapPath);
        if (result != null && result.contains(Command.INSTALL_SUCCESS)) {
            return true;
        } else {
            Hilog.e(TAG, "install Hap error:" + result);
            return false;
        }
    }

    /**
     * uninstall hap uninstall -k hap name
     *
     * @param argv    uninstall argv
     * @param hapName hap package name
     * @return whether uninstall successfully
     */
    public boolean uninstallHap(String argv, String hapName) {
        String result = mRemoteCommand
                .sendRemoteCommand(Command.UNINSTALL_COMMAND + Command.BLANK + argv + Command.BLANK + hapName);
        if (result != null && result.contains(Command.UNINSTALL_SUCCESS)) {
            return true;
        } else {
            Hilog.e(TAG, "uninstall Hap error:" + result);
            return false;
        }
    }

    /**
     * start app with shell command hdc_std shell aa start -a
     * com.example.MainAbility -b com.example.test
     *
     * @param packageName hap package
     * @param className   hap class name
     * @return whether start hap successfully
     */
    public boolean startHap(String packageName, String className) {
        String startCommand = Command.SHELL_COMMAND + Command.BLANK + Command.START_HAP_CLASS_COMMAND + Command.BLANK
                + className + Command.BLANK + Command.START_HAP_PACKAGE_COMMAND + Command.BLANK + packageName;
        String result = mRemoteCommand.sendRemoteCommand(startCommand);
        if (result != null && result.contains(Command.START_SUCCESS)) {
            return true;
        } else {
            Hilog.e(TAG, "start hap error:" + result);
            return false;
        }
    }

    /**
     * send file to device
     *
     * @param local      local file path
     * @param devicePath device dir path
     * @return whether send file successfully
     */
    public String sendFile(String local, String devicePath) {
        String command = Command.BACK_FLAG + Command.FILE_SEND + Command.BLANK + local + Command.BLANK + devicePath;
        return mRemoteCommand.sendRemoteCommand(command);
    }

    /**
     * get File from device
     *
     * @param devicePath device file path
     * @param local      local dir path
     * @return whether received file successfully
     */
    public String recvFile(String devicePath, String local) {
        if (isWindowOS() && illegalWindowFile(devicePath)) {
            return Command.RECV_ERROR_RESP;
        }
        String command = Command.BACK_FLAG + Command.FILE_RECV + Command.BLANK + devicePath + Command.BLANK + local;
        return mRemoteCommand.sendRemoteCommand(command);
    }

    /**
     * get prop
     *
     * @param prop         we want get
     * @param defaultProp if we can't get it
     * @return prop values
     */
    public String getProp(String prop, String defaultProp) {
        String command = Command.SHELL_COMMAND + Command.BLANK + Command.GETPROP_COMMAND + Command.BLANK + prop;
        String temp = mRemoteCommand.sendRemoteCommand(command);
        if (isUnValid(temp)) {
            return defaultProp;
        }
        return temp;
    }

    /**
     * send shell command
     *
     * @param shellCommand    shell Command
     * @param sync false is asynchronous,true is synchronization
     */
    public void sendShellCommand(String shellCommand, boolean sync) {
        if (shellCommand.isEmpty()) {
            return ;
        }
        if (sync) {
            mShellCommand.sendSequentialShellCommand(shellCommand);
        }else {
            mShellCommand.sendImmediateShellCommand(shellCommand);
        }
    }

    /**
     * root device if device support
     *
     * @return whether root device successfully
     */
    public String root() {
        if (getProp(Command.DEBUGABLE_COMMAND, "0").equals("0")) {
            Hilog.e(TAG, "This version can't root");
            return Command.ROOT_ERROR_RESP;
        }
        String temp = mRemoteCommand.sendRemoteCommand(Command.ROOT_COMMAND);
        if (temp.isEmpty()) {
            return Command.ROOT_SUCCESS_RESP;
        } else {
            return Command.ROOT_ERROR_RESP + Command.BLANK + temp;
        }
    }

    /**
     * remount device if device support
     *
     * @return whether remount device successfully
     */
    public String remount() {
        if (getProp(Command.DEBUGABLE_COMMAND, "0").equals("0")) {
            Hilog.e(TAG, "Please root first");
            return Command.REMOUNT_ERROR_RESP;
        }
        String temp = mRemoteCommand.sendRemoteCommand(Command.REMOUNT_COMMAND);
        if (temp.contains(Command.REMOUNT_RESP)) {
            return Command.REMOUNT_SUCCESS_RESP;
        } else {
            return Command.REMOUNT_ERROR_RESP + Command.BLANK + temp;
        }
    }

    /**
     * set Remote Connect tconn 192.168.0.100:10178
     *
     * @param ip 192.168.0.100
     * @param port 10178
     * @return command exec result
     */
    public String setRemoteConnect(String ip, String port) {
        String ip_format = Command.CONN_COMMAND + Command.BLANK + ip;
        String port_format = Command.COLON_COMMAND + port;
        return mLocalCommand.sendLocalCommand(ip_format + port_format);
    }

    /**
     * cancel Remote Connect tconn 192.168.0.100:10178 -remove
     *
     * @param ip 192.168.0.100
     * @param port 10178
     * @return command exec result
     */
    public String removeRemoteConnect(String ip, String port) {
        String ip_format = Command.CONN_COMMAND + Command.BLANK + ip;
        String port_format = Command.COLON_COMMAND + port;
        return mLocalCommand.sendLocalCommand(ip_format + port_format + Command.BLANK + Command.CONN_RM_COMMAND);
    }

    /**
     * Forward local traffic to remote device
     * forward port  fport tcp:1234 jdwp:aaa
     *    examples are below:
     *    tcp:port
     *    localfilesystem:unix domain socket name
     *    localreserved:unix domain socket name
     *    localabstract:unix domain socket name
     *    dev:device name
     *    jdwp:pid remote only
     *
     * @param localnode  tcp:1234
     * @param remotenode tcp:4321
     * @return command exec result
     */
    public String forwardPort(String localnode, String remotenode) {
        String command = Command.FPORT_ADD_COMMAND + Command.BLANK + localnode + Command.BLANK + remotenode;
        return mRemoteCommand.sendRemoteCommand(command);
    }

    /**
     *  cancel forward port  tcp:1234 tcp:4321
     *
     * @param localnode   tcp:1234
     * @param remotenode  tcp:4321
     * @return command exec result
     */
    public String removeForwardPort(String localnode, String remotenode) {
        String command = Command.FPORT_RM_COMMAND + Command.BLANK + localnode + Command.BLANK + remotenode;
        return mLocalCommand.sendLocalCommand(command);
    }

    /**
     * getAllForwardPort
     *
     * @return result of All Forward Port
     */
    public String getAllForwardPort() {
        return mLocalCommand.sendLocalCommand(Command.FPORT_COMMAND);
    }

    /**
     * send hilog command
     *
     * @return hilog command,it need check callback
     */
    public String getHilog() {
        return mRemoteCommand.sendRemoteCommand(Command.BACK_FLAG + Command.HILOG_COMMAND);
    }

    /**
     * send dump Hiperf command
     *
     * @param time how long we need catch
     * @param path where hiprof file locate,eg: /data/hiperf
     */
    public void dumpHiperf(int time, String path) {
        mShellCommand.sendSequentialShellCommand(Command.HIPERF_1_COMMAND + time + Command.HIPERF_2_COMMAND + path);
    }

    /**
     * send bytrace command
     *
     * @param time how long we need catch
     * @param path where bytrace file locate,eg: /data/bytrace
     */
    public void bytrace(int time, String path) {
        mShellCommand.sendSequentialShellCommand(Command.BYTRACE_1_COMMAND + time + Command.BYTRACE_2_COMMAND + path);
    }

    /**
     * send GC command
     */
    public void sendGC() {
        Hilog.e(TAG, "SendGC is not support");
    }

    /**
     * send Propfiler command
     */
    public void getPropfiler() {
        Hilog.e(TAG, "GetPropfiler is not support");
    }

    /**
     * get Screenshot
     */
    public void getScreenshot() {
        Hilog.e(TAG, "GetScreenshot is not support");
    }

    private boolean illegalWindowFile(String file) {
        int length = file.split("/").length;
        Locale englishLocale = Locale.ENGLISH;
        String temp = file.split("/")[length - 1].toUpperCase(englishLocale);
        for (int filetype = 0; filetype < Command.ILLEGAL_01.length; filetype++) {
            if (temp.equals(Command.ILLEGAL_01[filetype]) || temp.startsWith(Command.ILLEGAL_01[filetype] + ".")) {
                return true;
            }
        }

        for (int filetype = 0; filetype < Command.ILLEGAL_02.length; filetype++) {
            for (int fileCount = 0; fileCount <= 9; fileCount++) {
                if (temp.equals(Command.ILLEGAL_02[filetype] + Integer.toString(fileCount))
                        || temp.startsWith(Command.ILLEGAL_02[filetype] + Integer.toString(fileCount) + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWindowOS() {
        return System.getProperties().getProperty("os.name").indexOf("Windows") != -1;
    }

    private static boolean isUnValid(String string) {
        return string.trim().length() == 0;
    }
}
