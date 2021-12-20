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

package ohos.devtools.views.applicationtrace;

import ohos.devtools.views.applicationtrace.bean.AppFunc;
import ohos.devtools.views.applicationtrace.bean.TreeTableBean;
import ohos.devtools.views.applicationtrace.util.TimeUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * DataProcess
 *
 * @since 2021/5/12 16:34
 */
public class DataProcess {
    /**
     * get the get TopDown FuncTree by startNS、endNS and threadIds
     *
     * @param funcMap funcMap
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     * @return list <DefaultMutableTreeNode> nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeTopDown(Map<Integer, List<AppFunc>> funcMap, long startNS,
        long endNS, List<Integer> threadIds) {
        if (Objects.isNull(funcMap)) {
            return new ArrayList<>();
        }
        List<AppFunc> funcs =
            funcMap.entrySet().stream().filter(entry -> threadIds == null || threadIds.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList());
        Map<String, TreeTableBean> map = funcGroupByStackId(startNS, endNS, funcs, null);
        List<TreeTableBean> treeTableBeans = setNumForNodes(map);
        Map<String, DefaultMutableTreeNode> treeNodeMap = treeTableBeans.stream()
            .collect(Collectors.toMap(TreeTableBean::getPrefStackId, DefaultMutableTreeNode::new));
        treeTableBeans.forEach(treeTableBean -> {
            if (!treeTableBean.getPrefParentStackId().isEmpty()) {
                if (treeNodeMap.containsKey(treeTableBean.getPrefParentStackId())) {
                    treeNodeMap.get(treeTableBean.getPrefParentStackId())
                        .add(treeNodeMap.get(treeTableBean.getPrefStackId()));
                }
            }
        });
        List<DefaultMutableTreeNode> threadNodes = treeNodeMap.values().stream().filter(node -> {
            if (node.getUserObject() instanceof TreeTableBean) {
                return ((TreeTableBean) node.getUserObject()).getPrefParentStackId().isEmpty();
            }
            return false;
        }).collect(Collectors.toList());
        return threadNodes;
    }

    /**
     * get the get BottomUp FuncTree by startNS、endNS and threadIds
     *
     * @param funcMap funcMap
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     * @return list <DefaultMutableTreeNode> nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeBottomUp(Map<Integer, List<AppFunc>> funcMap, long startNS,
        long endNS, List<Integer> threadIds) {
        long dur = TimeUnit.NANOSECONDS.toMicros(endNS - startNS);
        ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();
        Map<String, List<String>> nameToId = new HashMap<>();
        List<AppFunc> funcs =
            funcMap.entrySet().stream().filter(entry -> threadIds == null || threadIds.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList());
        Map<String, TreeTableBean> treeNodeMap = funcGroupByStackId(startNS, endNS, funcs, nameToId);
        setNumForNodes(treeNodeMap);
        nameToId.forEach((name, ids) -> {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
            rootNode.setUserObject(new TreeTableBean(dur) {{
                setName(name);
                long totalNum = 0;
                long childrenNum = 0;
                long selfNum = 0;
                for (String id : ids) {
                    TreeTableBean tableBean = treeNodeMap.get(id);
                    totalNum += tableBean.getTotalNum();
                    childrenNum += tableBean.getChildrenNum();
                    selfNum += tableBean.getSelfNum();
                }
                setTotalNum(totalNum);
                setSelfNum(selfNum);
                setChildrenNum(childrenNum);
            }});
            ids.forEach(id -> recursionNode(rootNode, treeNodeMap.get(id).getPrefParentStackId(), treeNodeMap, id));
            nodes.add(rootNode);
        });
        return nodes;
    }

    private static Map<String, TreeTableBean> funcGroupByStackId(long startNS, long endNS, List<AppFunc> list,
        Map<String, List<String>> nameToId) { // Group by stacked
        long dur = TimeUnit.NANOSECONDS.toMicros(endNS - startNS);
        List<Map.Entry<String, List<AppFunc>>> list1 = list.stream().filter(func -> {
            long funcEndTs = func.getEndTs();
            long funcStartTs = func.getStartTs();
            return funcEndTs >= startNS && funcStartTs <= endNS;
        }).collect(groupingBy(AppFunc::getBloodId)).entrySet().stream().collect(Collectors.toList());
        return list1.stream().collect(Collectors.toMap(Map.Entry::getKey, a1 -> {
            TreeTableBean uniteBean = new TreeTableBean(dur);
            uniteBean.setThreadDur(dur);
            uniteBean.setPrefStackId(a1.getKey());
            if (a1.getValue().size() > 0) {
                uniteBean.setName(a1.getValue().get(0).getFuncName());
                uniteBean.setPrefParentStackId(a1.getValue().get(0).getParentBloodId());
                if (nameToId != null) {
                    if (nameToId.containsKey(a1.getValue().get(0).getFuncName())) {
                        nameToId.get(a1.getValue().get(0).getFuncName()).add(a1.getValue().get(0).getBloodId());
                    } else {
                        nameToId.put(a1.getValue().get(0).getFuncName(), new ArrayList<>() {{
                            add(a1.getValue().get(0).getBloodId());
                        }});
                    }
                }
            }
            long childrenTotal = a1.getValue().stream()
                .mapToLong(child -> TimeUtils.getIntersection(startNS, endNS, child.getStartTs(), child.getEndTs()))
                .sum();
            uniteBean.setTotalNum(childrenTotal);
            uniteBean.setChildrenNS(a1.getValue().stream()
                .mapToLong(child -> TimeUtils.getNanoIntersection(startNS, endNS, child.getStartTs(), child.getEndTs()))
                .sum());
            return uniteBean;
        }));
    }

    /**
     * Set up presentation data
     *
     * @param map map
     * @return list <TreeTableBean>
     */
    private static List<TreeTableBean> setNumForNodes(Map<String, TreeTableBean> map) {
        List<TreeTableBean> treeNodes = new ArrayList<>(map.values()); // Sort the array
        for (TreeTableBean ts : treeNodes) { // Loop set children and total data
            ts.setSelfNum(ts.getTotalNum() - ts.getChildrenNum());
            if (map.containsKey(ts.getPrefParentStackId())) {
                TreeTableBean mapUserObject = map.get(ts.getPrefParentStackId());
                mapUserObject.setChildrenNum(mapUserObject.getChildrenNum() + ts.getTotalNum());
                mapUserObject.setSelfNum(mapUserObject.getTotalNum() - mapUserObject.getChildrenNum());
            }
        }
        return treeNodes;
    }

    private static void recursionNode(DefaultMutableTreeNode rootNode, String parentId,
        Map<String, TreeTableBean> treeNodeMap, String id) {
        if (rootNode.getUserObject() instanceof TreeTableBean) {
            TreeTableBean topBean = (TreeTableBean) rootNode.getUserObject();
            TreeTableBean timeBean = treeNodeMap.get(id);
            if (parentId.isEmpty()) { // Leaf node
                recursionNodeLeaf(rootNode, timeBean);
            } else { // Non-leaf nodes
                Map<String, String> Ids = new HashMap();
                Ids.put("parentId", parentId);
                Ids.put("id", id);
                recursionNodeNonLeaf(rootNode, timeBean, topBean, treeNodeMap, Ids);
            }
        }
    }

    private static void recursionNodeNonLeaf(DefaultMutableTreeNode rootNode, TreeTableBean timeBean,
        TreeTableBean topBean, Map<String, TreeTableBean> treeNodeMap, Map<String, String> Ids) {
        final TreeTableBean idBean = treeNodeMap.get(Ids.get("parentId"));
        boolean sameName = false;
        Enumeration<TreeNode> enumeration = rootNode.children();
        while (enumeration.hasMoreElements()) {
            /* Compare whether there are node names in the current hierarchy that need to be merged */
            TreeNode treeNode = enumeration.nextElement();
            if (treeNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treeNode;
                if (nextElement.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean nextElementUserObject = (TreeTableBean) nextElement.getUserObject();
                    if (nextElementUserObject.getName().equals(idBean.getName())) { // The merge time difference
                        nextElementUserObject.mergeTime(timeBean);
                        recursionNode(nextElement, idBean.getPrefParentStackId(), treeNodeMap, Ids.get("id"));
                        sameName = true;
                    }
                }
            }
        }
        if (!sameName) { // No same node needs to be merged
            DefaultMutableTreeNode addNode = new DefaultMutableTreeNode() { {
                setUserObject(new TreeTableBean(topBean.getThreadDur()) { { // Calculate the time difference
                    setName(idBean.getName());
                    setTime(timeBean);
                }});
            }};
            rootNode.add(addNode);
            recursionNode(addNode, idBean.getPrefParentStackId(), treeNodeMap, Ids.get("id"));
        }
    }

    private static void recursionNodeLeaf(DefaultMutableTreeNode rootNode, TreeTableBean timeBean) {
        if (rootNode.getChildCount() != 0) { // The child node is thread and there are currently no child nodes
            TreeNode child = rootNode.getChildAt(rootNode.getChildCount() - 1);
            if (child instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode leafNode = (DefaultMutableTreeNode) child;
                if (leafNode.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean leafNodeUserObject = (TreeTableBean) leafNode.getUserObject();
                    leafNodeUserObject.mergeTime(timeBean);
                    leafNode.setUserObject(leafNodeUserObject);
                }
            }
        }
    }
}
