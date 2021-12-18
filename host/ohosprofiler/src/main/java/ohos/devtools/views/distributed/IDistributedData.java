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

package ohos.devtools.views.distributed;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

/**
 * IDistributedData
 *
 * @since 2021/08/06 15:22
 */
public interface IDistributedData {
    /**
     * expandTree
     *
     * @param tree tree
     */
    default void expandTree(JTree tree) {
        if (tree.getModel().getRoot() instanceof TreeNode) {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            expandAll(tree, new TreePath(root), true);
        }
    }

    /**
     * expandTree
     *
     * @param tree tree
     * @param parent parent
     * @param expand expand
     */
    default void expandAll(JTree tree, TreePath parent, boolean expand) {
        if (parent.getLastPathComponent() instanceof TreeNode) {
            TreeNode node = (TreeNode) parent.getLastPathComponent();
            if (node.getChildCount() >= 0) {
                for (Enumeration enumeration = node.children(); enumeration.hasMoreElements();) {
                    Object nextElement = enumeration.nextElement();
                    if (nextElement instanceof TreeNode) {
                        TreeNode treeNode = (TreeNode) nextElement;
                        TreePath path = parent.pathByAddingChild(treeNode);
                        expandAll(tree, path, expand);
                    }
                }
            }
            if (expand) {
                tree.expandPath(parent);
            } else {
                tree.collapsePath(parent);
            }
        }
    }
}
