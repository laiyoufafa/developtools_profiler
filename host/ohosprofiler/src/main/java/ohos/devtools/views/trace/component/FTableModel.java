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

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * FTableModel
 *
 * @param <T> obj
 * @date: 2021/5/24 11:57
 */
public class FTableModel<T extends Object> extends AbstractTableModel {
    private List<Column<T>> columnNames;
    private List<T> dataSource = new ArrayList<>();

    /**
     * set columns
     *
     * @param columns columns
     */
    public void setColumns(List<Column<T>> columns) {
        this.columnNames = columns;
    }

    public void setDataSource(final List<T> param) {
        this.dataSource = param;
    }

    @Override
    public int getRowCount() {
        return dataSource.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames == null ? 0 : columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column).name;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columnNames.get(columnIndex).callable.map(dataSource.get(rowIndex));
    }

    /**
     * Process map
     *
     * @param <T> Data
     */
    public interface Process<T extends Object> {
        /**
         * map
         *
         * @param obj obj
         * @return Object object
         */
        Object map(T obj);
    }

    /**
     * Column
     *
     * @param <T> obj
     * @date: 2021/5/24 11:57
     */
    public static class Column<T extends Object> {
        /**
         * name
         */
        public String name;
        private Process callable;

        /**
         * construct
         *
         * @param name name
         * @param callable callable
         */
        public Column(String name, Process<T> callable) {
            this.name = name;
            this.callable = callable;
        }
    }
}
