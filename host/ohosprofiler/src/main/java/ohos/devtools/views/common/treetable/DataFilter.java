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

import javax.swing.tree.TreeNode;

/**
 * DataFilter
 *
 * @since 2021/5/19 16:39
 */
public class DataFilter {
    boolean aBoolean = true;
    String strFilter;

    /**
     * passTreeNode
     *
     * @param treeNode treeNode
     * @param textValue textValue
     * @return boolean
     */
    public boolean passTreeNode(TreeNode treeNode, String textValue) {
        if (aBoolean) {
            return true;
        }
        if (!treeNode.isLeaf()) {
            return true;
        }
        String treeString = treeNode.toString();
        return treeString.contains(textValue);
    }

    /**
     * setFilter
     *
     * @param aBoolean aBoolean
     * @param str str
     */
    public void setFilter(boolean aBoolean, String str) {
        this.aBoolean = aBoolean;
        strFilter = str;
    }

}
