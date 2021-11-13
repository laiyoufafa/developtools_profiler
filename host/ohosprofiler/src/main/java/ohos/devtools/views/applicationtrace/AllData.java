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
import ohos.devtools.views.applicationtrace.bean.Cpu;
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.applicationtrace.bean.Thread;
import ohos.devtools.views.applicationtrace.bean.TreeTableBean;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.bean.Process;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * app data
 *
 * @since 2021/5/20 18:00
 */
public class AllData {
    /**
     * cpu data map
     */
    public static Map<Integer, List<Cpu>> cpuMap = new HashMap<>();

    /**
     * thread data map
     */
    public static Map<Integer, List<Thread>> threadMap = new HashMap<>();

    /**
     * function data map
     */
    public static Map<Integer, List<Func>> funcMap = new HashMap<>();

    /**
     * function names map
     */
    public static Map<Integer, String> threadNames = new HashMap<>();

    /**
     * list of process data
     */
    public static List<Process> processes = new ArrayList<>();

    /**
     * get right TopDown tree by time range
     *
     * @param startNS startNS
     * @param endNS   endNS
     * @return node List
     */
    public static List<DefaultMutableTreeNode> getFuncTreeTopDown(long startNS, long endNS) {
        if (Objects.isNull(funcMap)) {
            return new ArrayList<>();
        }
        if (Objects.isNull(threadMap)) {
            return new ArrayList<>();
        }
        return getFuncTreeTopDown(startNS, endNS, null);
    }

    /**
     * get right TopDown tree by time range and selected thread
     *
     * @param startNS   startNS
     * @param endNS     endNS
     * @param threadIds threadIds
     * @return node List
     */
    public static List<DefaultMutableTreeNode> getFuncTreeTopDown(long startNS, long endNS, List<Integer> threadIds) {
        if (Objects.isNull(funcMap)) {
            return new ArrayList<>();
        }
        if (Objects.isNull(threadMap)) {
            return new ArrayList<>();
        }
        Map<Integer, List<AppFunc>> collect = funcMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
        return DataProcess.getFuncTreeTopDown(collect, startNS, endNS, threadIds);
    }

    /**
     * get right BottomUp tree by time range
     *
     * @param startNS startNS
     * @param endNS   endNS
     * @return tree node List
     */
    public static List<DefaultMutableTreeNode> getFuncTreeBottomUp(long startNS, long endNS) {
        return getFuncTreeBottomUp(startNS, endNS, null);
    }

    /**
     * get right BottomUp tree by time range and thread
     *
     * @param startNS   startNS
     * @param endNS     endNS
     * @param threadIds threadIds
     * @return tree node List
     */
    public static List<DefaultMutableTreeNode> getFuncTreeBottomUp(long startNS, long endNS, List<Integer> threadIds) {
        Map<Integer, List<AppFunc>> collect = funcMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
        return DataProcess.getFuncTreeBottomUp(collect, startNS, endNS, threadIds);
    }

    /**
     * get right TopDown tree by selected func
     *
     * @param func func
     * @return tree node List
     */
    public static List<DefaultMutableTreeNode> getFuncTreeByFuncTopDown(Func func) {
        List<Func> collect = funcMap.get(func.getTid()).stream().filter(
            item -> TimeUtils.isRangeCross(func.getStartTs(), func.getEndTs(), item.getStartTs(), item.getEndTs())
                && item.getDepth() > func.getDepth()).collect(Collectors.toList());
        Map<Long, TreeTableBean> longTreeTableBeanMap = funcGroupByStackId(func, collect, null);
        List<TreeTableBean> treeTableBeans = setNumForNodes(longTreeTableBeanMap);
        TreeTableBean treeTableBean = new TreeTableBean();
        treeTableBean.setName(func.getFuncName());
        long totalUs = TimeUnit.NANOSECONDS.toMicros(func.getDur());
        treeTableBean.setThreadDur(totalUs);
        treeTableBean.setTotalNum(totalUs);
        long threadFuncDuration = TimeUnit.NANOSECONDS.toMicros(
            collect.stream().filter(item -> item.getDepth() == func.getDepth() + 1).mapToLong(Func::getDur).sum());
        treeTableBean.setChildrenNS(func.getDur());
        treeTableBean.setChildrenNum(threadFuncDuration);
        treeTableBean.setSelfNum(totalUs - threadFuncDuration);
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(treeTableBean);
        Map<Long, DefaultMutableTreeNode> treeNodeMap =
            treeTableBeans.stream().collect(Collectors.toMap(TreeTableBean::getStackId, DefaultMutableTreeNode::new));
        treeTableBeans.forEach(listBean -> {
            if (listBean.getParentStackId() == func.getStackId()) {
                rootNode.add(treeNodeMap.get(listBean.getStackId()));
            } else {
                if (treeNodeMap.containsKey(listBean.getParentStackId())) {
                    treeNodeMap.get(listBean.getParentStackId()).add(treeNodeMap.get(listBean.getStackId()));
                }
            }
        });
        ArrayList<DefaultMutableTreeNode> defaultMutableTreeNodes = new ArrayList<>();
        defaultMutableTreeNodes.add(rootNode);
        return defaultMutableTreeNodes;
    }

    /**
     * get right BottomUp tree by selected func
     *
     * @param func func
     * @return tree node List
     */
    public static List<DefaultMutableTreeNode> getFuncTreeByFuncBottomUp(Func func) {
        ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();
        List<Func> collect = funcMap.get(func.getTid()).stream().filter(
                item -> TimeUtils.isRangeCross(func.getStartTs(), func.getEndTs(), item.getStartTs(), item.getEndTs()))
            .collect(Collectors.toList());
        Map<String, List<Long>> nameToId = new HashMap<>();
        Map<Long, TreeTableBean> treeNodeMap = funcGroupByStackId(func, collect, nameToId);
        setNumForNodes(treeNodeMap);
        nameToId.forEach((name, ids) -> {
            TreeTableBean treeTableBean = new TreeTableBean(TimeUnit.NANOSECONDS.toMicros(func.getDur()));
            treeTableBean.setName(name);
            long totalNum = 0;
            long childrenNum = 0;
            long selfNum = 0;
            for (Long id : ids) {
                TreeTableBean tableBean = treeNodeMap.get(id);
                totalNum += tableBean.getTotalNum();
                childrenNum += tableBean.getChildrenNum();
                selfNum += tableBean.getSelfNum();
            }
            treeTableBean.setTotalNum(totalNum);
            treeTableBean.setSelfNum(selfNum);
            treeTableBean.setChildrenNum(childrenNum);
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(treeTableBean);
            ids.forEach(
                id -> recursionNode(rootNode, treeNodeMap.get(id).getParentStackId(), threadNames.get(func.getTid()),
                    treeNodeMap, id));
            if (ids.stream().noneMatch(id ->
                collect.stream().filter(item -> item.getStackId() == id && item.getDepth() < func.getDepth())
                    .toArray().length > 0)) {
                nodes.add(rootNode);
            }
        });
        return nodes;
    }

    private static Map<Long, TreeTableBean> funcGroupByStackId(Func func, List<Func> collect,
        Map<String, List<Long>> nameToId) {
        long totalUs = TimeUnit.NANOSECONDS.toMicros(func.getDur());
        Map<Long, TreeTableBean> map = collect.stream().collect(groupingBy(Func::getStackId)).entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> {
                TreeTableBean uniteBean = new TreeTableBean(totalUs);
                uniteBean.setStackId(entry.getKey());
                if (entry.getValue().size() > 0) {
                    uniteBean.setName(entry.getValue().get(0).getFuncName());
                    uniteBean.setParentStackId(entry.getValue().get(0).getParentStackId());
                    if (nameToId != null) {
                        if (nameToId.containsKey(entry.getValue().get(0).getFuncName())) {
                            nameToId.get(entry.getValue().get(0).getFuncName())
                                .add(entry.getValue().get(0).getStackId());
                        } else {
                            List<Long> ids = new ArrayList<>();
                            ids.add(entry.getValue().get(0).getStackId());
                            nameToId.put(entry.getValue().get(0).getFuncName(), ids);
                        }
                    }
                }
                long childrenTotal = entry.getValue().stream().mapToLong(mapper -> TimeUtils
                    .getIntersection(func.getStartTs(), func.getEndTs(), mapper.getStartTs(),
                            mapper.getEndTs())).sum();
                uniteBean.setTotalNum(childrenTotal);
                uniteBean.setChildrenNS(entry.getValue().stream().mapToLong(mapper -> TimeUtils
                        .getNanoIntersection(func.getStartTs(), func.getEndTs(),
                                mapper.getStartTs(), mapper.getEndTs()))
                        .sum());
                return uniteBean;
            }));
        return map;
    }

    private static Map<Long, TreeTableBean> funcGroupByStackId(long startNS, long endNS, List<Func> list,
        Map<String, List<Long>> nameToId) {
        long dur = endNS - startNS;
        Map<Long, TreeTableBean> map =
            list.stream().filter(func -> func.getEndTs() > startNS && func.getStartTs() < endNS)
                .collect(groupingBy(Func::getStackId)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, a1 -> {
                    TreeTableBean uniteBean = new TreeTableBean(dur);
                    uniteBean.setThreadDur(dur);
                    uniteBean.setStackId(a1.getKey());
                    if (a1.getValue().size() > 0) {
                        uniteBean.setName(a1.getValue().get(0).getFuncName());
                        uniteBean.setParentStackId(a1.getValue().get(0).getParentStackId());
                        if (nameToId != null) {
                            if (nameToId.containsKey(a1.getValue().get(0).getFuncName())) {
                                nameToId.get(a1.getValue().get(0).getFuncName()).add(a1.getValue().get(0).getStackId());
                            } else {
                                ArrayList<Long> ids = new ArrayList<>();
                                ids.add(a1.getValue().get(0).getStackId());
                                nameToId.put(a1.getValue().get(0).getFuncName(), ids);
                            }
                        }
                    }
                    long childrenTotal = a1.getValue().stream().mapToLong(mapper -> TimeUtils
                            .getIntersection(startNS, endNS, mapper.getStartTs(),
                                    mapper.getStartTs() + mapper.getDur())).sum();
                    uniteBean.setTotalNum(childrenTotal);
                    uniteBean.setChildrenNS(a1.getValue().stream().mapToLong(mapper -> TimeUtils
                        .getNanoIntersection(startNS, endNS, mapper.getStartTs(),
                            mapper.getStartTs() + mapper.getDur())).sum());
                    return uniteBean;
                }));
        return map;
    }

    /**
     * set nodes data
     *
     * @param map map
     * @return set node userObject
     */
    public static List<TreeTableBean> setNumForNodes(Map<Long, TreeTableBean> map) { // Set up presentation data
        List<TreeTableBean> treeNodes = new ArrayList<>(map.values()); // Sort the array
        for (TreeTableBean ts : treeNodes) { // Loop set children and total data
            ts.setSelfNum(ts.getTotalNum() - ts.getChildrenNum());
            if (map.containsKey(ts.getParentStackId())) {
                TreeTableBean mapUserObject = map.get(ts.getParentStackId());
                mapUserObject.setChildrenNum(mapUserObject.getChildrenNum() + ts.getTotalNum());
                mapUserObject.setSelfNum(mapUserObject.getTotalNum() - mapUserObject.getChildrenNum());
            }
        }
        return treeNodes;
    }

    private static void recursionNode(DefaultMutableTreeNode rootNode, long parentId, String threadName,
        Map<Long, TreeTableBean> treeNodeMap, long id) {
        if (rootNode.getUserObject() instanceof TreeTableBean) {
            TreeTableBean topBean = (TreeTableBean) rootNode.getUserObject();
            TreeTableBean timeBean = treeNodeMap.get(id);
            if (parentId == 0) { // Leaf node
                recursionNodeLeaf(threadName, rootNode, topBean, timeBean);
            } else { // Non-leaf nodes
                Map<String, Long> idMap = new HashMap<>();
                idMap.put("parentId", parentId);
                idMap.put("id", id);
                recursionNodeNonLeaf(threadName, rootNode, timeBean, treeNodeMap, idMap);
            }
        }
    }

    private static void recursionNodeLeaf(String threadName, DefaultMutableTreeNode rootNode,
        TreeTableBean topBean, TreeTableBean timeBean) {
        if (rootNode.getChildCount() == 0) { // The child node is thread and there are currently no child nodes
            TreeTableBean bean = new TreeTableBean(topBean.getThreadDur());
            bean.setName(threadName);
            bean.setTotalNum(timeBean.getTotalNum());
            bean.setSelfNum(timeBean.getTotalNum());
            bean.setSelfNum(timeBean.getSelfNum());
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(bean);
            rootNode.add(node);
        } else { // Merge leaf nodes
            TreeNode tNode = rootNode.getChildAt(rootNode.getChildCount() - 1);
            if (tNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode leafNode = (DefaultMutableTreeNode) tNode;
                if (leafNode.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean leafNodeUserObject = (TreeTableBean) leafNode.getUserObject();
                    leafNodeUserObject.setTotalNum(leafNodeUserObject.getTotalNum() + timeBean.getTotalNum());
                    leafNodeUserObject.setSelfNum(leafNodeUserObject.getSelfNum() + timeBean.getTotalNum());
                    leafNodeUserObject
                            .setChildrenNum(leafNodeUserObject.getSelfNum() + timeBean.getChildrenNum());
                    leafNode.setUserObject(leafNodeUserObject);
                }
            }
        }
    }

    private static void recursionNodeNonLeaf(String threadName, DefaultMutableTreeNode rootNode,
        TreeTableBean timeBean, Map<Long, TreeTableBean> treeNodeMap,
        Map<String, Long> idMap) {
        final TreeTableBean idBean = treeNodeMap.get(idMap.get("parentId"));
        boolean sameName = false;
        Enumeration<TreeNode> enumeration = rootNode.children();
        while (enumeration.hasMoreElements()) {
            /* Compare whether there are node names in the current hierarchy that need to be merged */
            TreeNode nodeObj = enumeration.nextElement();
            if (nodeObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) nodeObj;
                if (nextElement.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean nextElementUserObject = (TreeTableBean) nextElement.getUserObject();
                    if (nextElementUserObject.getName().equals(idBean.getName())) { // The merge time difference
                        nextElementUserObject
                                .setSelfNum(nextElementUserObject.getSelfNum() + timeBean.getSelfNum());
                        nextElementUserObject
                                .setChildrenNum(nextElementUserObject.getChildrenNum() + timeBean.getChildrenNum());
                        nextElementUserObject
                                .setTotalNum(nextElementUserObject.getTotalNum() + timeBean.getTotalNum());
                        recursionNode(nextElement, idBean.getParentStackId(), threadName, treeNodeMap, idMap.get("id"));
                        sameName = true;
                    }
                }
            }
        }
        if (!sameName) { // No same node needs to be merged
            TreeTableBean bean = new TreeTableBean();
            bean.setName(idBean.getName());
            bean.setTotalNum(timeBean.getTotalNum());
            bean.setSelfNum(timeBean.getSelfNum());
            bean.setChildrenNum(timeBean.getChildrenNum());
            DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(bean);
            rootNode.add(addNode);
            recursionNode(addNode, idBean.getParentStackId(), threadName, treeNodeMap, idMap.get("id"));
        }
    }

    /**
     * get right flame chart data by time range
     *
     * @param startNS startNS
     * @param endNS   endNS
     * @return return flame node list
     */
    public static List<DefaultMutableTreeNode> getFuncTreeFlameChart(long startNS, long endNS) {
        List<DefaultMutableTreeNode> funcTreeTopDown = getFuncTreeTopDown(startNS, endNS);
        funcTreeTopDown.forEach(AllData::resortNode);
        sortNodeList(funcTreeTopDown);
        return funcTreeTopDown;
    }

    /**
     * get right flame chart data by selected func
     *
     * @param func func
     * @return return flame node list
     */
    public static List<DefaultMutableTreeNode> getFuncTreeFlameChart(Func func) {
        return getFuncTreeByFuncTopDown(func);
    }

    /**
     * get right flame chart data by time range and selected thread
     *
     * @param startNS   startNS
     * @param endNS     endNS
     * @param threadIds threadIds
     * @return return flame node list
     */
    public static List<DefaultMutableTreeNode> getFuncTreeFlameChart(long startNS, long endNS,
        List<Integer> threadIds) {
        List<DefaultMutableTreeNode> funcTreeTopDown = getFuncTreeTopDown(startNS, endNS, threadIds);
        funcTreeTopDown.forEach(AllData::resortNode);
        sortNodeList(funcTreeTopDown);
        return funcTreeTopDown;
    }

    private static void resortNode(DefaultMutableTreeNode root) {
        Consumer<DefaultMutableTreeNode> sort = parent -> {
            Enumeration<TreeNode> children = parent.children();
            List<DefaultMutableTreeNode> childs = new ArrayList<>();
            while (children.hasMoreElements()) {
                TreeNode node = children.nextElement();
                if (node instanceof DefaultMutableTreeNode) {
                    childs.add((DefaultMutableTreeNode) node);
                }
            }
            parent.removeAllChildren();
            sortNodeList(childs).forEach(parent::add);
        };
        Enumeration enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            Object nodeObj = enumeration.nextElement();
            if (nodeObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObj;
                if (!node.isLeaf() && node.getChildCount() > 1) {
                    sort.accept(node);
                }
            }
        }
    }

    private static List<DefaultMutableTreeNode> sortNodeList(List<DefaultMutableTreeNode> list) {
        return list.stream().sorted((child1, child2) -> {
            if (child1.getUserObject() instanceof TreeTableBean && child2.getUserObject() instanceof TreeTableBean) {
                TreeTableBean bean1 = (TreeTableBean) child1.getUserObject();
                TreeTableBean bean2 = (TreeTableBean) child2.getUserObject();
                return Long.compare(bean1.getTotalNum(), bean2.getTotalNum());
            }
            return 0;
        }).collect(Collectors.toList());
    }

    /**
     * clear all static data
     */
    public static void clearData() {
        if (cpuMap != null) {
            cpuMap.values().forEach(List::clear);
            cpuMap.clear();
        }
        if (threadMap != null) {
            threadMap.values().forEach(List::clear);
            threadMap.clear();
        }
        if (funcMap != null) {
            funcMap.values().forEach(List::clear);
            funcMap.clear();
        }
        if (threadNames != null) {
            threadNames.clear();
        }
        if (processes != null) {
            processes.clear();
        }
    }

}
