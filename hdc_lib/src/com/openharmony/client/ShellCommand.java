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
 * send Shell Command to server ¡¢create at 20210912
 */
class ShellCommand {
    private RemoteCommand mCommand;

    /**
     * ShellCommand init
     *
     * @param command ShellCommand
     */
    protected ShellCommand(RemoteCommand command) {
        mCommand = command;
    }

    /**
     * sendSequentialShellCommand
     *
     * @param command shell command
     * @return check callback
     */
    protected synchronized String sendSequentialShellCommand(String command) {
        return mCommand.sendRemoteCommand(Command.BACK_FLAG + Command.SHELL_COMMAND + Command.BLANK + command);
    }

    /**
     * sendImmediateShellCommand
     *
     * @param command shell command
     * @return check callback
     */
    protected String sendImmediateShellCommand(String command) {
        return mCommand.sendRemoteCommand(Command.BACK_FLAG + Command.SHELL_COMMAND + Command.BLANK + command);
    }
}