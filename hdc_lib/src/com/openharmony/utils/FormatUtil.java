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

package com.openharmony.utils;

/**
 * FormatUtil for asciiString To Int¡¢create at 20210912
 */
public class FormatUtil {
    /**
     * AsciiString to int ,such as "*" is 43 .
     *
     * @param array byte
     * @return  Int of ascii string
     */
    public static int asciiStringToInt(byte[] array) {
        int count = 0;
        for (int arr = 0; arr < array.length; arr++) {
            count += (int) array[arr];
        }
        return count;
    }
}
