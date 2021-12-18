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

package ohos.devtools.services.hiperf;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * HiperfParseTest
 *
 * @since 2021/10/29 10:54
 */
public class HiperfParseTest {
    private File file;

    /**
     * parse File Test
     *
     * @tc.name: parseFileTest
     * @tc.number: OHOS_JAVA_perf_HiperfParseTest_parseFileTest_0001
     * @tc.desc: parse File Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void parseFileTest01() {
        try {
            new HiperfParse().parseFile(null);
        } catch (IOException exception) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    /**
     * parse File Test
     *
     * @tc.name: parseFileTest
     * @tc.number: OHOS_JAVA_perf_HiperfParseTest_parseFileTest_0002
     * @tc.desc: parse File Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void parseFileTest02() {
        file = new File(this.getClass().getResource("/").getFile());
        try {
            new HiperfParse().parseFile(file);
        } catch (IOException exception) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    /**
     * parse File Test
     *
     * @tc.name: parseFileTest
     * @tc.number: OHOS_JAVA_perf_HiperfParseTest_parseFileTest_0003
     * @tc.desc: parse File Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void parseFileTest03() {
        file = new File(this.getClass().getResource("/perf_data20211104125932.trace").getFile());
        try {
            new HiperfParse().parseFile(file);
        } catch (IOException exception) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    /**
     * parse File Test
     *
     * @tc.name: parseFileTest
     * @tc.number: OHOS_JAVA_perf_HiperfParseTest_parseFileTest_0004
     * @tc.desc: parse File Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void parseFileTest04() {
        file = new File(this.getClass().getResource("/perf_data20211104125932.trace").getFile());
        try {
            new HiperfParse().parseFile(file);
        } catch (IOException | IllegalStateException exception) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    /**
     * parse File Test
     *
     * @tc.name: parseFileTest
     * @tc.number: OHOS_JAVA_perf_HiperfParseTest_parseFileTest_0005
     * @tc.desc: parse File Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void parseFileTest05() {
        try {
            new HiperfParse().parseFile(new File(""));
        } catch (IOException exception) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }
}
