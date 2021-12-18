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

package ohos.devtools.views.common.treetable;

import com.intellij.util.ui.ColumnInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

/**
 * FieldsTreeTableColumn
 */
public abstract class FieldsTreeTableColumn<T, N> extends ColumnInfo<DefaultMutableTreeNode, String> {
    private final Class<T> type;

    /**
     * TreeTableColumn
     *
     * @param name name
     * @param typeParameterClass typeParameterClass
     */
    public FieldsTreeTableColumn(String name, Class<T> typeParameterClass) {
        super(name);
        type = typeParameterClass;
    }

    @Override
    public String valueOf(DefaultMutableTreeNode defaultMutableTreeNode) {
        if (type.isInstance(defaultMutableTreeNode.getUserObject())) {
            T nodeData = type.cast(defaultMutableTreeNode.getUserObject());
            return this.getColumnValue(nodeData);
        }
        return "";
    }

    @Override
    public Comparator<DefaultMutableTreeNode> getComparator() {
        return (o1, o2) -> {
            if (type.isInstance(o1.getUserObject()) && type.isInstance(o2.getUserObject())) {
                T start = type.cast(o1.getUserObject());
                T end = type.cast(o2.getUserObject());
                try {
                    String columnValue = this.getColumnValue(start);
                    if ("-".equals(columnValue)) {
                        columnValue = "" + Integer.MAX_VALUE;
                    }
                    String columnValue1 = this.getColumnValue(end);
                    if ("-".equals(columnValue)) {
                        columnValue1 = "" + Integer.MAX_VALUE;
                    }
                    if ("--".equals(columnValue) || "--".equals(columnValue)) {
                        return 0;
                    }
                    long startL = Long.parseLong(columnValue);
                    long endL = Long.parseLong(columnValue1);
                    return Long.compare(startL, endL);
                } catch (NumberFormatException exception) {
                    return this.getColumnValue(start).compareTo(this.getColumnValue(end));
                }
            }
            return 0;
        };
    }

    /**
     * get Column Value
     *
     * @param nodeData nodeData
     * @return String
     */
    public abstract String getColumnValue(T nodeData);
}