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

package ohos.devtools.views.layout.chartview.memory.nativehook;

import ohos.devtools.services.memory.nativebean.HookDataBean;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * NativeHookDataFilter
 *
 * @since 2021/10/25
 */
public class NativeHookDataFilter {
    boolean aBoolean = true;

    /**
     * passTreeNode
     *
     * @param treeNode treeNode
     * @return boolean
     */
    public boolean passTreeNode(TreeNode treeNode) {
        if (aBoolean) {
            return true;
        }
        if (treeNode instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treeNode;
            if (nextElement.getUserObject() instanceof HookDataBean) {
                HookDataBean bean = (HookDataBean) nextElement.getUserObject();
                return bean.getContainType() < 3;
            }
        }
        return true;
    }

    /**
     * setFilter
     *
     * @param aBoolean aBoolean
     * @param str str
     */
    public void setFilter(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

}
