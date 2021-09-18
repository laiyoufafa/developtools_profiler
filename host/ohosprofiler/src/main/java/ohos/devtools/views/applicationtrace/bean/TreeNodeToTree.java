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

package ohos.devtools.views.applicationtrace.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * tree node to tree
 *
 * @param <T> data type
 */
public abstract class TreeNodeToTree<T> {
    /**
     * get key
     *
     * @param node node
     * @return key
     */
    protected abstract String getKey(T node);

    /**
     * get parent node id
     *
     * @param node node
     * @return parent id
     */
    protected abstract String getParentId(T node);

    /**
     * add childes
     *
     * @param node node
     * @param parent parent node
     */
    protected abstract void addChildrens(T node, T parent);

    /**
     * add root node
     *
     * @param node root node
     */
    protected abstract void addRootNode(T node);

    /**
     * list to tree
     *
     * @param list data list
     */
    public void listToTree(List<T> list) {
        Map<String, T> newMap = new HashMap<>();
        for (T tree : list) {
            newMap.put(getKey(tree), tree);
        }
        for (T tree : list) {
            T parent = newMap.get(getParentId(tree));
            if (parent != null) {
                addChildrens(tree, parent);
            } else {
                addRootNode(tree);
            }
        }
    }
}
