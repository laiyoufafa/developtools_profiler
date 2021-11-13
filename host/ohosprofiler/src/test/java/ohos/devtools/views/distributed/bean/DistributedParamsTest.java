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

package ohos.devtools.views.distributed.bean;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistributedParamsTest {
    private static DistributedParams distributedParams;
    private static DistributedParams.Builder builder;

    @BeforeAll
    static void createBean() {
        builder = new DistributedParams.Builder();
        builder.setDeviceNameA("nameA");
        builder.setDeviceNameB("nameB");
        builder.setPathA("pathA");
        builder.setPathB("pathB");
        builder.setPkgNameB("PkgNameA");
        builder.setPkgNameB("PkgNameB");
        builder.setProcessIdA(1);
        builder.setProcessIdB(2);
        builder.setOffsetA(1L);
        builder.setOffsetB(2L);
        distributedParams = new DistributedParams(builder);
    }

    @AfterAll
    static void end() {
        distributedParams = null;
        builder = null;
    }

    @Test
    void getPkgNameA() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("pkgNameA");
        field.setAccessible(true);
        assertEquals(distributedParams.getPkgNameA(), field.get(builder));
    }

    @Test
    void getPkgNameB() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("pkgNameB");
        field.setAccessible(true);
        assertEquals(distributedParams.getPkgNameB(), field.get(builder));
    }

    @Test
    void getProcessIdA() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("processIdA");
        field.setAccessible(true);
        assertEquals(distributedParams.getProcessIdA(), field.get(builder));
    }

    @Test
    void getProcessIdB() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("processIdB");
        field.setAccessible(true);
        assertEquals(distributedParams.getProcessIdB(), field.get(builder));
    }

    @Test
    void getDeviceNameA() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("deviceNameA");
        field.setAccessible(true);
        assertEquals(distributedParams.getDeviceNameA(), field.get(builder));
    }

    @Test
    void getDeviceNameB() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("deviceNameB");
        field.setAccessible(true);
        assertEquals(distributedParams.getDeviceNameB(), field.get(builder));
    }

    @Test
    void getPathA() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("pathA");
        field.setAccessible(true);
        assertEquals(distributedParams.getPathA(), field.get(builder));
    }

    @Test
    void getPathB() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("pathB");
        field.setAccessible(true);
        assertEquals(distributedParams.getPathB(), field.get(builder));
    }

    @Test
    void getOffsetA() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("offsetA");
        field.setAccessible(true);
        assertEquals(distributedParams.getOffsetA(), field.get(builder));
    }

    @Test
    void getOffsetB() throws NoSuchFieldException, IllegalAccessException {
        final Field field = builder.getClass().getDeclaredField("offsetB");
        field.setAccessible(true);
        assertEquals(distributedParams.getOffsetB(), field.get(builder));
    }
}