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

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.openharmony.utils.FormatUtil;

/**
 * FormatUtil UT Test¡¢create at 20210912
 */
public class FormatUtilTest {
    /**
     * test a Ascii to int
     *
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we test
     */
    @Test
    public void testAsciiStringToInt01() throws UnsupportedEncodingException {
        String ascii = ":";
        String encoding = "ISO-8859-1";
        byte[] byteAscii = new byte[4];
        System.arraycopy(ascii.getBytes(encoding), 0, byteAscii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byteAscii);
        assertEquals(58, format);
    }

    /**
     * test two Ascii to int
     *
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we test
     */
    @Test
    public void testAsciiStringToInt02() throws UnsupportedEncodingException {
        String ascii = "::";
        String encoding = "ISO-8859-1";
        byte[] byteAscii = new byte[4];
        System.arraycopy(ascii.getBytes(encoding), 0, byteAscii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byteAscii);
        assertEquals(116, format);
    }

    /**
     * test three Ascii to int
     *
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we test
     */
    @Test
    public void testAsciiStringToInt03() throws UnsupportedEncodingException {
        String ascii = ":::";
        String encoding = "ISO-8859-1";
        byte[] byteAscii = new byte[4];
        System.arraycopy(ascii.getBytes(encoding), 0, byteAscii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byteAscii);
        assertEquals(174, format);
    }

    /**
     * test four Ascii to int
     *
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we test
     */
    @Test
    public void testAsciiStringToInt04() throws UnsupportedEncodingException {
        String ascii = "::::";
        String encoding = "ISO-8859-1";
        byte[] byteAscii = new byte[4];
        System.arraycopy(ascii.getBytes(encoding), 0, byteAscii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byteAscii);
        assertEquals(232, format);
    }

    /**
     * test empty Ascii to int
     *
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we test
     */
    @Test
    public void testAsciiStringToInt05() throws UnsupportedEncodingException {
        String ascii = "";
        String encoding = "ISO-8859-1";
        byte[] byteAscii = new byte[4];
        System.arraycopy(ascii.getBytes(encoding), 0, byteAscii, 0, ascii.length());
        int format = FormatUtil.asciiStringToInt(byteAscii);
        assertEquals(0, format);
    }
}
