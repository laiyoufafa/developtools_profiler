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

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * interface all thread listener
 *
 * @date: 2021/5/27 12:01
 */
public interface IAllThreadDataListener {
    /**
     * getAllThreadData Calllback
     *
     * @param startNS start time
     * @param endNS end time
     * @param scale scale level
     * @return List <DefaultMutableTreeNode> tree node
     */
    List<DefaultMutableTreeNode> getAllThreadData(long startNS, long endNS, long scale);
}
