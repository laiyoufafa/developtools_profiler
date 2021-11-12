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

package ohos.devtools.uitest;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * HiLog Button Test
 *
 * @since 2021/2/1 9:31
 */
public class HiLogButtonTest {
    private RemoteRobot remoteRobot = new RemoteRobot("http://127.0.0.1:8082");

    /**
     * hiLog Button Test
     *
     * @tc.name: hiLogButtonTest
     * @tc.number: OHOS_JAVA_hiLogButtonTest_0001
     * @tc.desc: hiLog Button Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void hiLogButtonTest() throws InterruptedException {
        remoteRobot.find(CommonContainerFixture.class,
            byXpath("//div[@accessiblename='HiLog' and @class='JMenu' and @name='hiLogMenu' and @text='HiLog']"))
            .click();
        TimeUnit.SECONDS.sleep(3);
        // top close
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']"))
            .jLabel(byXpath("//div[@class='JBLabel' and @name='tabClose']"), Duration.ofSeconds(2)).click();
    }
}
