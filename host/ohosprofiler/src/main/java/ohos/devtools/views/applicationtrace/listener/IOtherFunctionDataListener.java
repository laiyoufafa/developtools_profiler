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

package ohos.devtools.views.applicationtrace.listener;

import ohos.devtools.views.applicationtrace.bean.Func;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * interface other function data listener
 *
 * @since 2021/5/27 12:01
 */
public interface IOtherFunctionDataListener<T extends Func> {
    /**
     * getOtherFunctionData Callback
     * @param func function
     * @return tree node
     */
    List<DefaultMutableTreeNode> getOtherFunctionData(T func);
}
