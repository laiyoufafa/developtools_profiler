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

package ohos.devtools.offline;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import com.intellij.remoterobot.search.locators.Locators;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Metric Button Offline
 *
 * @since 2021/2/1 9:31
 */
public class MetricButtonOffline {
    private RemoteRobot remoteRobot = new RemoteRobot("http://127.0.0.1:8082");

    /**
     * metrics Button Test
     *
     * @tc.name: metricsButtonTest
     * @tc.number: OHOS_JAVA_metricsButtonTest_0001
     * @tc.desc: metrics Button Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void metricsButtonTest() throws InterruptedException {
        remoteRobot.find(CommonContainerFixture.class, Locators.byXpath("//div[@class='WelcomePanel']")).button(Locators
            .byXpath("//div[@accessiblename=' + New Task' and @class='JBLabel' and "
                + "@name='newTaskBtn' and @text=' + New Task']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, Locators.byXpath("//div[@class='HomePanel']")).jLabel(Locators
            .byXpath("//div[@accessiblename='Application Tuning' and @class='JBLabel' "
                + "and @name='Application Tuning' and @text='Application Tuning']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, Locators.byXpath("//div[@class='HomePanel']")).button(Locators
            .byXpath("//div[@accessiblename='Open File' and @class='JButton' "
                + "and @name='Open File' and @text='Open File']"), Duration.ofSeconds(2)).click();
        String resource = this.getClass().getResource("/Demo.bytrace").toString();
        resource = resource.substring(resource.indexOf("/") + 1);
        remoteRobot.find(CommonContainerFixture.class,
            Locators.byXpath("//div[@accessiblename='Open File' and @class='MyDialog']"))
            .textField(Locators.byXpath("//div[@class='ExtendableTextField']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class,
            Locators.byXpath("//div[@accessiblename='Open File' and @class='MyDialog']"))
            .textField(Locators.byXpath("//div[@class='ExtendableTextField']"), Duration.ofSeconds(2))
            .setText(resource);
        remoteRobot.find(CommonContainerFixture.class,
            Locators.byXpath("//div[@accessiblename='Open File' and @class='MyDialog']"))
            .button(Locators.byXpath("//div[@accessiblename='OK' and @class='JButton' and @text='OK']"),
                Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class,
            Locators.byXpath("//div[@accessiblename='Please Choose the type' and @class='MyDialog']"))
            .button(Locators.byXpath("//div[@accessiblename='OK' and @class='JButton' and @text='OK']"),
                Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(10);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']"))
            .button(byXpath("//div[@accessiblename='Metrics' and @class='JButton' and @text='Metrics']"),
                Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(5);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='JComboBox' and @name='metricSelect']"))
            .click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='MetricsPanel']"))
            .button(byXpath("//div[@accessiblename='Run' and @class='JButton' and @text='Run']"), Duration.ofSeconds(2))
            .click();
        TimeUnit.SECONDS.sleep(5);
        // top close
        remoteRobot
            .find(CommonContainerFixture.class, Locators.byXpath("//div[@class='JBPanel' and @name='chartTopClose']"))
            .click();
    }
}
