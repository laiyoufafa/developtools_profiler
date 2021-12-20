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

package ohos.devtools.views.distributed.util;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

/**
 * DetailTreeTableColumn
 *
 * @since 2021/08/10 16:20
 */
public abstract class DetailTreeTableColumn<T, N> extends ColumnInfo<DefaultMutableTreeNode, Object> {
    private final Class<T> type;

    /**
     * DetailTreeTableColumn
     *
     * @param name name
     * @param typeParameterClass typeParameterClass
     * @param nTypeParameterClass nTypeParameterClass
     */
    public DetailTreeTableColumn(@NlsContexts.ColumnName String name, Class<T> typeParameterClass,
        Class<N> nTypeParameterClass) {
        super(name);
        type = typeParameterClass;
    }

    @Override
    @Nullable
    public Object valueOf(DefaultMutableTreeNode defaultMutableTreeNode) {
        if (type.isInstance(defaultMutableTreeNode.getUserObject())) {
            T nodeData = type.cast(defaultMutableTreeNode.getUserObject());
            if (this.getCompareValue(nodeData) == null) {
                return "";
            }
            if (this.getCompareValue(nodeData) instanceof Long) {
                return nodeData;
            } else {
                return this.getCompareValue(nodeData).toString();
            }
        }
        return "";
    }

    @Override
    @Nullable
    public Comparator<DefaultMutableTreeNode> getComparator() {
        return (o1, o2) -> {
            if (type.isInstance(o1.getUserObject()) && type.isInstance(o2.getUserObject())) {
                T start = type.cast(o1.getUserObject());
                T end = type.cast(o2.getUserObject());
                try {
                    if (this.getCompareValue(start) instanceof Double && this.getCompareValue(end) instanceof Double) {
                        return Double.compare((double) this.getCompareValue(start), (double) this.getCompareValue(end));
                    } else if (this.getCompareValue(start) instanceof Long && this
                        .getCompareValue(end) instanceof Long) {
                        return Long.compare((long) this.getCompareValue(start), (long) this.getCompareValue(end));
                    } else {
                        return 0;
                    }
                } catch (ClassCastException exception) {
                    return 0;
                }
            }
            return 0;
        };
    }

    /**
     * getCompareValue
     *
     * @param nodeData nodeData
     * @return return the compare value
     */
    public abstract N getCompareValue(T nodeData);
}
