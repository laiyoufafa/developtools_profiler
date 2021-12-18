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

package ohos.devtools.views.trace.component;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test ScrollTablePanel class .
 *
 * @date 2021/4/24 18:03
 */
class ScrollTablePanelTest {
    /**
     * test set the ColumnsAndData .
     */
    @Test
    void setColumnsAndData() {
        String[] columns = new String[] {};
        ArrayList<Object> dataSource = new ArrayList<>();
        ScrollTablePanel scrollTablePanel = new ScrollTablePanel(columns, dataSource);
        scrollTablePanel.setColumnsAndData(columns, dataSource);
        final Field columnsField;
        final Field dataSourceField;
        try {
            columnsField = scrollTablePanel.getClass().getDeclaredField("columns");
            dataSourceField = scrollTablePanel.getClass().getDeclaredField("dataSource");
            columnsField.setAccessible(true);
            dataSourceField.setAccessible(true);
            assertEquals(columns, columnsField.get(scrollTablePanel));
            assertEquals(dataSource, dataSourceField.get(scrollTablePanel));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}