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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date Time Util Test
 */
public class DateTimeUtilTest {
    private static DateTimeFormatter COMMON_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * functional testing dateToString
     *
     * @tc.name: dateToString
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_dateToString_0001
     * @tc.desc: dateToString
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void dateToStringTest() {
        String str = DateTimeUtil.dateToString(LocalDateTime.now());
        Assert.assertNotNull(str);
    }

    /**
     * functional testing stringToDate
     *
     * @tc.name: stringToDate
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_stringToDate_0001
     * @tc.desc: stringToDate
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void stringToDateTest() {
        LocalDateTime localDateTime = DateTimeUtil.stringToDate("2021-04-09 10:14:48");
        Assert.assertNotNull(localDateTime);
    }

    /**
     * functional testing getNowTime
     *
     * @tc.name: getNowTime
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_getNowTime_0001
     * @tc.desc: getNowTime
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getNowTimeTest() {
        LocalDateTime localDateTime = DateTimeUtil.getNowTime();
        Assert.assertNotNull(localDateTime);
    }

    /**
     * functional testing dateToString
     *
     * @tc.name: dateToString
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_dateToString_0001
     * @tc.desc: dateToString
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testDateToString() {
        String str = DateTimeUtil.dateToString(LocalDateTime.now(), COMMON_FORMATTER);
        Assert.assertNotNull(str);
    }

    /**
     * functional testing stringToDate
     *
     * @tc.name: stringToDate
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_stringToDate_0001
     * @tc.desc: stringToDate
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testStringToDate() {
        LocalDateTime localDateTime = DateTimeUtil.stringToDate("2021-04-09 10:14:48", COMMON_FORMATTER);
        Assert.assertNotNull(localDateTime);
    }

    /**
     * functional testing dateToTimeMillis
     *
     * @tc.name: dateToTimeMillis
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_dateToTimeMillis_0001
     * @tc.desc: dateToTimeMillis
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void dateToTimeMillisTest() {
        long timeMillis = DateTimeUtil.dateToTimeMillis(LocalDateTime.now());
        Assert.assertNotNull(timeMillis);
    }

    /**
     * functional testing timeMillisToDate
     *
     * @tc.name: timeMillisToDate
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_timeMillisToDate_0001
     * @tc.desc: timeMillisToDate
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void timeMillisToDateTest() {
        LocalDateTime localDateTime = DateTimeUtil.timeMillisToDate(System.currentTimeMillis());
        Assert.assertNotNull(localDateTime);
    }

    /**
     * functional testing getNowTimeString
     *
     * @tc.name: getNowTimeString
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_getNowTimeString_0001
     * @tc.desc: getNowTimeString
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getNowTimeStringTest() {
        String str = DateTimeUtil.getNowTimeString("yyyy-MM-dd HH:mm:ss");
        Assert.assertNotNull(str);
    }

    /**
     * functional testing getNowTimeLong
     *
     * @tc.name: getNowTimeLong
     * @tc.number: OHOS_JAVA_common_DateTimeUtil_getNowTimeLong_0001
     * @tc.desc: getNowTimeLong
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void getNowTimeLongTest() {
        Long timeLong = DateTimeUtil.getNowTimeLong();
        Assert.assertNotNull(timeLong);
    }
}