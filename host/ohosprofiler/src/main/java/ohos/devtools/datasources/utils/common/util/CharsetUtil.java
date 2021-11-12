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

package ohos.devtools.datasources.utils.common.util;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @Description String utilities class
 */
public class CharsetUtil {
    private static final Logger LOGGER = LogManager.getLogger(CharsetUtil.class);

    private CharsetUtil() {
    }

    /**
     * GBK
     */
    public static final String GBK = "GBK";

    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * ISO-8859-1
     */
    public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * ISO-8859-1
     */
    public static final Charset CHARSET_ISO_8859_1 = StandardCharsets.ISO_8859_1;

    /**
     * UTF-8
     */
    public static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

    /**
     * GBK
     */
    public static final Charset CHARSET_GBK;

    static {
        Charset gbkCharset = null;
        try {
            gbkCharset = Charset.forName(GBK);
        } catch (UnsupportedCharsetException throwAbles) {
            LOGGER.info("Get GBKCharset Error {}", throwAbles.getMessage());
        }
        CHARSET_GBK = gbkCharset;
    }

    /**
     * Parses a Charset object.
     *
     * @param charsetName Charset to be parsed. The default Charset will be returned if it is left empty.
     * @return Parsed Charset if parsing is successful; default Charset if parsing fails.
     */
    public static Charset parse(String charsetName) {
        return parse(charsetName, Charset.defaultCharset());
    }

    /**
     * Parses a Charset object.
     *
     * @param charsetName Indicates the Charset to be used if the parsing fails.
     * @param defaultCharset 解析失败使用的默认编码
     * @return Returns the parsed Charset if the parsing is successful; returns the default Charset otherwise.
     */
    public static Charset parse(String charsetName, Charset defaultCharset) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("parse");
        }
        if (isBlank(charsetName)) {
            return defaultCharset;
        }

        Charset result;
        try {
            result = Charset.forName(charsetName);
        } catch (UnsupportedCharsetException exception) {
            result = defaultCharset;
        }

        return result;
    }

    /**
     * Convert Charset of a given string.
     *
     * @param source Sting to be converted.
     * @param srcCharset Source Charset, which is GBK by default.
     * @param destCharset Target Charset, which is UTF-8 by default.
     * @return New Charset after the conversion.
     */
    public static String convert(String source, String srcCharset, String destCharset) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("convert");
        }
        return convert(source, Charset.forName(srcCharset), Charset.forName(destCharset));
    }

    /**
     * Convert Charset of a given string.
     *
     * @param source Sting to be converted.
     * @param srcCharset Source Charset, which is GBK by default.
     * @param destCharset Target Charset, which is UTF-8 by default.
     * @return New Charset after the conversion.
     */
    public static String convert(String source, Charset srcCharset, Charset destCharset) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("convert");
        }
        Charset srcSet = srcCharset;
        if (srcSet == null) {
            srcSet = CHARSET_GBK;
        }
        Charset desSet = destCharset;
        if (desSet == null) {
            desSet = StandardCharsets.UTF_8;
        }

        if (isBlank(source) || srcSet.equals(desSet)) {
            return source;
        }
        return new String(source.getBytes(srcSet), desSet);
    }

    /**
     * Default Charset name
     *
     * @return String
     */
    public static String defaultCharsetName() {
        return defaultCharset().name();
    }

    /**
     * Default Charset
     *
     * @return Charset
     */
    public static Charset defaultCharset() {
        return Charset.defaultCharset();
    }
}
