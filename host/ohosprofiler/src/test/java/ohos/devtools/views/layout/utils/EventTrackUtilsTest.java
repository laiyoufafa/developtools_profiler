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

package ohos.devtools.views.layout.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * EventTrackUtilsTest
 *
 * @since 2021/2/1 9:31
 */
public class EventTrackUtilsTest {
    /**
     * get Instance getSystemPanelTest
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_layout_EventTrackUtils_getInstanceTest_0001
     * @tc.desc: getInstanceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest01() {
        EventTrackUtils eventTrackUtils = new EventTrackUtils().getInstance();
        Assert.assertNotNull(eventTrackUtils);
    }

    /**
     * get Instance getSystemPanelTest
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_layout_EventTrackUtils_getInstanceTest_0002
     * @tc.desc: getInstanceTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest02() {
        EventTrackUtils eventTrackUtils1 = new EventTrackUtils().getInstance();
        EventTrackUtils eventTrackUtils2 = new EventTrackUtils().getInstance();
        Assert.assertEquals(eventTrackUtils1, eventTrackUtils2);
    }
}
