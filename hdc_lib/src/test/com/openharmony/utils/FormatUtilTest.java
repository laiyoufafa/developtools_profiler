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

package test.com.openharmony.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import com.openharmony.utils.FormatUtil;

/**
 * FormatUtil UT Test¡¢create at 20210912
 */
public class FormatUtilTest {
    /**
     * test a Ascii to int
     */
    @Test
    public void testAsciiStringToInt01() {
        String ascii = ":";
        byte[] byte_ascii = new byte[4];
        System.arraycopy(ascii.getBytes(), 0, byte_ascii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byte_ascii);
        assertEquals(58, format);
    }

    /**
     * test two Ascii to int
     */
    @Test
    public void testAsciiStringToInt02() {
        String ascii = "::";
        byte[] byte_ascii = new byte[4];
        System.arraycopy(ascii.getBytes(), 0, byte_ascii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byte_ascii);
        assertEquals(116, format);
    }

    /**
     * test three Ascii to int
     */
    @Test
    public void testAsciiStringToInt03() {
        String ascii = ":::";
        byte[] byte_ascii = new byte[4];
        System.arraycopy(ascii.getBytes(), 0, byte_ascii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byte_ascii);
        assertEquals(174, format);
    }

    /**
     * test four Ascii to int
     */
    @Test
    public void testAsciiStringToInt04() {
        String ascii = "::::";
        byte[] byte_ascii = new byte[4];
        System.arraycopy(ascii.getBytes(), 0, byte_ascii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byte_ascii);
        assertEquals(232, format);
    }

    /**
     * test empty Ascii to int
     */
    @Test
    public void testAsciiStringToInt05() {
        String ascii = "";
        byte[] byte_ascii = new byte[4];
        System.arraycopy(ascii.getBytes(), 0, byte_ascii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byte_ascii);
        assertEquals(0, format);
    }
}
