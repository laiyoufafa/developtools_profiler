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

package ohos.devtools.views.applicationtrace.analysis;

import ohos.devtools.views.applicationtrace.bean.EventBean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventTableTest {
    private EventTable eventTable = new EventTable(new EventTable.ITableSizeChangeListener() {
        @Override
        public void onTableSizeChange(@Nullable String title) {
            title.trim();
        }
    });

    @Test
    void getData() throws NoSuchFieldException, IllegalAccessException {
        eventTable.getData(0, 0, new ArrayList<>());
        final Field field;
        field = eventTable.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        assertEquals(0, ((List<EventBean>) field.get(eventTable)).size());
    }

    @Test
    void getDataNormal() throws NoSuchFieldException, IllegalAccessException {
        eventTable.getData(0, 10L, new ArrayList<>());
        final Field field;
        field = eventTable.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        assertEquals(0, ((List<EventBean>) field.get(eventTable)).size());
    }

    @Test
    void getDataError() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Integer> threadIds = new ArrayList<>();
        threadIds.add(-1);
        eventTable.getData(0, 10L, threadIds);
        final Field field;
        field = eventTable.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        assertEquals(0, ((List<EventBean>) field.get(eventTable)).size());
    }

    @Test
    void getDataParamError() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Integer> threadIds = new ArrayList<>();
        threadIds.add(-1);
        eventTable.getData(10L, -1, threadIds);
        final Field field;
        field = eventTable.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        assertEquals(0, ((List<EventBean>) field.get(eventTable)).size());
    }

    @Test
    void getDataParamNoNormal() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Integer> threadIds = new ArrayList<>();
        threadIds.add(-1);
        eventTable.getData(100L, -1, threadIds);
        final Field field;
        field = eventTable.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        assertEquals(0, ((List<EventBean>) field.get(eventTable)).size());
    }

    @Test
    void freshDataParam() {
        eventTable.dataSource = new ArrayList<>();
        eventTable.dataSource.add(new EventBean());
        eventTable.freshData();
        assertNotNull(eventTable);
    }

    @Test
    void freshDataErrorParam() {
        eventTable.dataSource = new ArrayList<>();
        eventTable.dataSource.add(new EventBean());
        eventTable.dataSource.add(null);
        eventTable.freshData();
        assertNotNull(eventTable);
    }

    @Test
    void freshDataParamNormal() {
        eventTable.dataSource = new ArrayList<>();
        eventTable.dataSource.add(new EventBean());
        eventTable.dataSource.add(null);
        EventBean eventBean = new EventBean();
        eventBean.setCpuDuration(-1);
        eventTable.dataSource.add(eventBean);
        eventTable.freshData();
        assertNotNull(eventTable);
    }

    @Test
    void freshDataNoParam() {
        eventTable.dataSource = new ArrayList<>();
        eventTable.freshData();
        eventTable.dataSource = null;
        assertNotNull(eventTable);
    }

    @Test
    void freshData() {
        eventTable.freshData();
        assertNotNull(eventTable);
    }
}