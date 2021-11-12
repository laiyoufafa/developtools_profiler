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
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;
import java.util.Enumeration;

/**
 * HeapDumpTreeTableColumn
 */
public abstract class HeapDumpTreeTableColumn<T, N> extends ColumnInfo<DefaultMutableTreeNode, String> {
    private final Class<T> type;

    /**
     * HeapDumpTreeTableColum
     *
     * @param name name
     * @param typeParameterClass typeParameterClass
     */
    public HeapDumpTreeTableColumn(String name, Class<T> typeParameterClass) {
        super(name);
        type = typeParameterClass;
    }

    @Override
    public @Nullable
    String valueOf(DefaultMutableTreeNode defaultMutableTreeNode) {
        if (type.isInstance(defaultMutableTreeNode.getUserObject())) {
            if (defaultMutableTreeNode.isLeaf()) {
                T nodeData = type.cast(defaultMutableTreeNode.getUserObject());
                return this.getColumnValue(nodeData);
            } else {
                long parentNodeData = 0L;
                Enumeration<TreeNode> children = defaultMutableTreeNode.children();
                while (children.hasMoreElements()) {
                    DefaultMutableTreeNode treeNode = null;
                    Object nextElementObject = children.nextElement();
                    if (nextElementObject instanceof DefaultMutableTreeNode) {
                        treeNode = (DefaultMutableTreeNode) nextElementObject;
                        T nodeData = type.cast(treeNode.getUserObject());
                        parentNodeData = Long.parseLong(this.getColumnValue(nodeData)) + parentNodeData;
                    }
                }
                return String.valueOf(parentNodeData);
            }
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
                    long startL = Long.parseLong(this.valueOf(o1));
                    long endL = Long.parseLong(this.valueOf(o2));
                    return Long.compare(startL, endL);
                } catch (NumberFormatException numberFormatException) {
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
