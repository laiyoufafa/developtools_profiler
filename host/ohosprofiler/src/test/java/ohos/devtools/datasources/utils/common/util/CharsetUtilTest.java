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

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * CharsetUtilTest测试类
 *
 * @version 1.0
 * @date 2021/04/08 16:42
 **/
public class CharsetUtilTest {
    /**
     * GBK
     */
    private String GBK = "GBK";

    /**
     * UTF-8
     */
    private String UTF_8 = "UTF-8";

    /**
     * functional testing parse
     *
     * @tc.name: parse
     * @tc.number: OHOS_JAVA_common_CharsetUtil_parse_0001
     * @tc.desc: 解析字符串编码为Charset对象，解析失败返回系统默认编码
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void parseTest() {
        Charset charset = CharsetUtil.parse(UTF_8);
        Assert.assertNotNull(charset);
    }

    /**
     * functional testing parse
     *
     * @tc.name: parse
     * @tc.number: OHOS_JAVA_common_CharsetUtil_parse_0002
     * @tc.desc: 解析字符串编码为Charset对象，解析失败返回系统默认编码
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void parseTest01() {
        Charset charset = CharsetUtil.parse(UTF_8, Charset.forName(GBK));
        Assert.assertNotNull(charset);
    }

    /**
     * functional testing convert
     *
     * @tc.name: convert
     * @tc.number: OHOS_JAVA_common_CharsetUtil_convert_0001
     * @tc.desc: 转换字符串的字符集编码
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void convertTest() {
        String str = CharsetUtil.convert("convertTest", GBK, UTF_8);
        Assert.assertNotNull(str);
    }

    /**
     * functional testing convert
     *
     * @tc.name: convert
     * @tc.number: OHOS_JAVA_common_CharsetUtil_convert_0002
     * @tc.desc: 转换字符串的字符集编码
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void convertTest01() {
        String str = CharsetUtil.convert("convertTest", Charset.forName(GBK), Charset.forName(UTF_8));
        Assert.assertNotNull(str);
    }

    /**
     * functional testing defaultCharsetName
     *
     * @tc.name: defaultCharsetName
     * @tc.number: OHOS_JAVA_common_CharsetUtil_defaultCharsetName_0001
     * @tc.desc: 系统默认Charset
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void defaultCharsetNameTest() {
        String str = CharsetUtil.defaultCharsetName();
        Assert.assertNotNull(str);
    }

    /**
     * functional testing defaultCharset
     *
     * @tc.name: defaultCharset
     * @tc.number: OHOS_JAVA_common_CharsetUtil_defaultCharset_0001
     * @tc.desc: 系统默认Charset
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void defaultCharsetTest() {
        Charset charset = CharsetUtil.defaultCharset();
        Assert.assertNotNull(charset);
    }
}
