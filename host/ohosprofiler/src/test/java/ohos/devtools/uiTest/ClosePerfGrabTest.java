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
 * Close Perf Grab Test
 *
 * @since 2021/2/1 9:31
 */
public class ClosePerfGrabTest {
    private RemoteRobot remoteRobot = new RemoteRobot("http://127.0.0.1:8082");

    /**
     * close Perf Grab Test
     *
     * @tc.name: closePerfGrabTest
     * @tc.number: OHOS_JAVA_closePerfGrabTest_0001
     * @tc.desc: close Perf Grab Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void closePerfGrabTest() throws InterruptedException {
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='WelcomePanel']")).button(byXpath(
            "//div[@accessiblename=' + New Task' and @class='JBLabel' and @name='newTaskBtn' and @text=' + New Task']"),
            Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).jLabel(byXpath(
            "//div[@accessiblename='Application Tuning' and @class='JBLabel' "
                + "and @name='Application Tuning' and @text='Application Tuning']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).button(
            byXpath("//div[@accessiblename='Choose' and @class='JButton' and @name='Choose' and @text='Choose']"),
            Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(10);
        remoteRobot.find(CommonContainerFixture.class,
            byXpath("//div[@class='CustomTextField' and @name='selectedProcessName']")).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='HomePanel']")).button(byXpath(
            "//div[@accessiblename='Start Task' and @class='JButton' and @name='Start Task' and @text='Start Task']"),
            Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(10);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='CpuTitleView']"))
            .jLabel(byXpath("//div[@class='JBLabel' and @name='cpuFoldBtn']"), Duration.ofSeconds(2)).click();
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='CpuItemView' and @name='CpuItemView']"))
            .button(byXpath("//div[@accessiblename='Record' and @class='JButton' and @text='Record']"),
                Duration.ofSeconds(2)).click();
        TimeUnit.SECONDS.sleep(3);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@accessiblename='Prompt' and @class='MyDialog']"))
            .button(byXpath("//div[@accessiblename='OK' and @class='JButton' and @text='OK']"), Duration.ofSeconds(2))
            .click();
        TimeUnit.SECONDS.sleep(3);
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@accessiblename='Prompt' and @class='MyDialog']"))
            .button(byXpath("//div[@accessiblename='Close' and @class='JButton']"), Duration.ofSeconds(2)).click();
        // top close
        remoteRobot.find(CommonContainerFixture.class, byXpath("//div[@class='JBPanel' and @name='chartTopClose']"))
            .click();
    }
}
