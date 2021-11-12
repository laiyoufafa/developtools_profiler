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

import com.intellij.icons.AllIcons;
import ohos.devtools.services.memory.nativebean.HookDataBean;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

/**
 * NativeHookTreeTableRenderer
 *
 * @since 2021/10/25
 */
public class NativeHookTreeTableRenderer extends DefaultTreeCellRenderer {

    /**
     * HookDataBeanEnum
     */
    public enum HookDataBeanEnum {
        HEAP_ENUM, CALLSTACK_ENUM, MALLOC_ENUM;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
        boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof HookDataBean) {
                HookDataBean node = (HookDataBean) userObject;
                switch (node.getBeanEnum()) {
                    case HEAP_ENUM:
                        setIcon(AllIcons.Actions.ModuleDirectory);
                        break;
                    case MALLOC_ENUM:
                        setIcon(AllIcons.Debugger.Db_array);
                        break;
                    default:
                        setIcon(AllIcons.Nodes.Method);
                }
            }
        }
        return this;
    }
}
