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

/**
 * hdc command type
 *
 * @version 1.0
 * @date 2021/02/01 10:47
 **/
public enum HdcCommandEnum {
    /**
     * hdc push command
     */
    HDC_FILE_SEND_STR("file send"),

    /**
     * Get device serial number command
     */
    HDC_FPORT_STR("fport"),

    /**
     * Get device serial number command
     */
    HDC_LIST_TARGETS_STR("hdc list targets -v"),

    /**
     * Get device device version
     */
    HDC_GET_TYPE("hdc -t %s shell getprop ro.product.cpu.abi"),

    /**
     * hdc shell command
     */
    HDC_SHELL_STR("shell"),

    /**
     * hdc command
     */
    HDC_STR("hdc"),

    /**
     * hdc command
     */
    HDC_RUN_OHOS("hdc -t %s shell cd /data/local/tmp && chmod +x ohosprofiler &&sh ohosprofiler unzipStart"),

    /**
     * hdc command
     */
    HDC_RUN_V7_OHOS("hdc -t %s shell cd /data/local/tmp && chmod +x ohosprofiler &&sh ohosprofiler unzipStartV7"),

    /**
     * hdc command
     */
    HDC_START_PROFILERD("hdc -t %s shell cd /data/local/tmp && chmod +x ohosprofiler && sh ohosprofiler restart"),

    /**
     * hdc command
     */
    HDC_STARTV7_PROFILERD("hdc -t %s shell cd /data/local/tmp && chmod +x ohosprofiler && sh ohosprofiler restart"),

    /**
     * hdc command
     */
    HDC_START_JAVAHEAP("hdc -t %s shell cd /data/local/tmp && chmod +x ohosprofiler && sh ohosprofiler startHeap %s"),

    /**
     * hdc command
     */
    HDC_CHECK_SERVER("hdc -t %s shell cd /data/local/tmp && chmod +x ohosprofiler && sh ohosprofiler check_server"),

    /**
     * hdc command
     */
    HDC_PUSH_OHOS_SHELL("hdc -t %s  file send %s /data/local/tmp/ohosprofiler"),

    /**
     * hdc command
     */
    HDC_PUSH_OHOS_ARMV7("hdc -t %s  file send %s /data/local/tmp/%s"),

    /**
     * hdc command
     */
    HDC_FOR_PORT("hdc -t %s fport tcp:%s tcp:50051"),
    /**
     * hdc push
     */
    HDC_PUSH_CMD("hdc -t %s file send %s /data/local/tmp/devtool");

    private String hdcCommand;

    HdcCommandEnum(String hdcCommand) {
        this.hdcCommand = hdcCommand;
    }

    public String getHdcCommand() {
        return hdcCommand;
    }
}
