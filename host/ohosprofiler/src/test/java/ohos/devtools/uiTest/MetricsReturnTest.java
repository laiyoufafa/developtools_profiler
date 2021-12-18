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
 * Metrics Return Test
 *
 * @since 2021/2/1 9:31
 */
public class MetricsReturnTest {
    private RemoteRobot remoteRobot = new RemoteRobot("http://127.0.0.1:8082");

    /**
     * metrics Return Test
     *
     * @tc.name: metricsReturnTest
     * @tc.number: OHOS_JAVA_metricsReturnTest_0001
     * @tc.desc: metrics Return Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void metricsReturnTest() throws InterruptedException {
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='WelcomePanel']")).button(byXpath(
            "//div[@accessiblename=' + New Task' and @class='JBLabel' and @name='newTaskBtn' and @text=' + New Task']"),
            Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).jLabel(byXpath(
            "//div[@accessiblename='System Tuning' and @class='JBLabel' "
                + "and @name='System Tuning' and @text='System Tuning']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).button(
            byXpath("//div[@accessiblename='Choose' and @class='JButton' and @name='Choose' and @text='Choose']"),
            Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='SystemConfigPanel']"))
            .jLabel(byXpath("//div[@accessiblename='Probes' "
                    + "and @class='JBLabel' and @name='Probes' and @text='Probes']"),
                Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).checkBox(byXpath(
            "//div[@accessiblename='Bytrace categories' and @class='JBCheckBox' and "
                + "@name='Bytrace categories' and @text='Bytrace categories']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']"))
            .checkBox(byXpath("//div[@accessiblename='Audio' and @class='JBCheckBox' and @text='Audio']"),
                Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']"))
            .checkBox(byXpath("//div[@accessiblename='Graphics' and @class='JBCheckBox' and @text='Graphics']"),
                Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).button(byXpath(
            "//div[@accessiblename='Start Task' and @class='JButton' and @name='Start Task' and @text='Start Task']"),
            Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(30);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).button(byXpath(
            "//div[@accessiblename='Metrics' and @class='JButton' " + "and @name='metricsButton' and @text='Metrics']"),
            Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(5);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='MetricsPanel']"))
            .jLabel(byXpath("//div[@accessiblename='Metrics' and @class='JBLabel' and @text='Metrics']"),
                Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(5);
        // top close
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='JBPanel' and @name='chartTopClose']"))
            .click();
    }
}
