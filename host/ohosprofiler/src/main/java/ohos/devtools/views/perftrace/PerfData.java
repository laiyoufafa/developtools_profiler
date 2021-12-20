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

package ohos.devtools.views.perftrace;

import ohos.devtools.views.applicationtrace.DataProcess;
import ohos.devtools.views.applicationtrace.bean.AppFunc;
import ohos.devtools.views.applicationtrace.bean.TreeTableBean;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.perftrace.bean.PrefFile;
import ohos.devtools.views.perftrace.bean.PrefFunc;
import ohos.devtools.views.perftrace.bean.PrefRange;
import ohos.devtools.views.perftrace.bean.PrefSample;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Comparator;
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
 * c++ function PerfData
 *
 * @since 2021/04/22 12:25
 */
public class PerfData {
    /**
     * current Range
     */
    private static PrefRange prefRange;

    /**
     * current files from db
     */
    private static Map<Long, List<PrefFile>> prefFiles;

    /**
     * current func map from db
     */
    private static Map<Integer, List<PrefFunc>> funcMap = new HashMap<>();

    /**
     * current thread name map from db
     */
    private static Map<Integer, String> threadNames = new HashMap<>();

    /**
     * get the PrefFunc by PrefSample object
     *
     * @param sampleList sampleList
     * @return list PrefFunc
     */
    public static List<PrefFunc> formatSampleList(List<PrefSample> sampleList) {
        if (sampleList.size() == 0) {
            return new ArrayList<>();
        }
        Map<Long, List<PrefSample>> sampleMap = sampleList.stream().collect(groupingBy(PrefSample::getSampleId));
        List<Map.Entry<Long, List<PrefSample>>> collect =
            sampleMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        List<PrefSample> lastEntry = collect.get(0).getValue();
        PrefFunc threadFunc = new PrefFunc();
        int threadId = Long.valueOf(sampleList.get(0).getThreadId()).intValue();
        threadFunc.setFuncName(threadNames.get(threadId));
        threadFunc.setThreadName(threadNames.get(threadId));
        threadFunc.setBloodId(Utils.md5String(threadFunc.getFuncName()));
        threadFunc.setDepth(-1);
        threadFunc.setStartTs(sampleList.get(0).getTs());
        int collectIndex = 1;
        List<PrefFunc> funcList = new ArrayList<>();
        PrefFunc currentEndNode = addFuncNodes(null, lastEntry, funcList, 0);
        while (collectIndex < collect.size()) {
            List<PrefSample> prefSamples = collect.get(collectIndex).getValue().stream()
                .sorted(Comparator.comparingLong(PrefSample::getId).reversed()).collect(Collectors.toList());
            int splitPoint = getSplitPoint(lastEntry, prefSamples);
            if (splitPoint < lastEntry.size()) {
                currentEndNode =
                    updateParentEndTime(lastEntry.size() - splitPoint, prefSamples.get(0).getTs(), currentEndNode);
            }
            if (splitPoint < prefSamples.size()) {
                currentEndNode = addFuncNodes(currentEndNode, prefSamples, funcList, splitPoint);
            }
            lastEntry = prefSamples;
            collectIndex++;
        }
        if (prefRange != null) {
            threadFunc.setEndTs(prefRange.getEndTime());
            threadFunc.setDur(prefRange.getEndTime() - threadFunc.getStartTs());
            currentEndNode.updateParentEndTime(0, prefRange.getEndTime() - prefRange.getStartTime());
        }
        funcList.add(threadFunc);
        return funcList;
    }

    private static PrefFunc updateParentEndTime(int index, long ts, PrefFunc node) {
        PrefFunc current = node;
        for (int pos = 0; pos < index; pos++) {
            current.setEndTs(ts);
            current = current.getParentNode();
        }
        return current;
    }

    private static int getSplitPoint(List<PrefSample> last, List<PrefSample> current) {
        int index = 0;
        while (index < last.size() && index < current.size() && last.get(index).isSameStack(current.get(index))) {
            index++;
        }
        return index;
    }

    private static PrefFunc addFuncNodes(PrefFunc parentNode, List<PrefSample> sampleList, List<PrefFunc> funcList,
        int startIndex) {
        PrefFunc node = parentNode;
        for (int sampleIndex = startIndex; sampleIndex < sampleList.size(); sampleIndex++) {
            PrefFunc childNode = new PrefFunc(sampleList.get(sampleIndex));
            if (node != null) {
                childNode.setDepth(node.getDepth() + 1);
                childNode.setParentNode(node);
                childNode.setParentBloodId(node.getBloodId());
                node.getChildrenNodes().add(childNode);
            } else {
                childNode.setDepth(startIndex);
            }
            childNode.createBloodId();
            funcList.add(childNode);
            node = childNode;
        }
        return node;
    }

    /**
     * get the get TopDown FuncTree by startNS and endNS
     *
     * @param startNS startNS
     * @param endNS endNS
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeTopDown(long startNS, long endNS) {
        if (Objects.isNull(funcMap)) {
            return new ArrayList<>();
        }
        return getFuncTreeTopDown(startNS, endNS, null);
    }

    /**
     * get the get TopDown FuncTree by startNS、endNS and threadIds
     *
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeTopDown(long startNS, long endNS, List<Integer> threadIds) {
        if (Objects.isNull(funcMap)) {
            return new ArrayList<>();
        }
        Map<Integer, List<AppFunc>> collect = funcMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
        return DataProcess.getFuncTreeTopDown(collect, startNS, endNS, threadIds);
    }

    /**
     * get BottomUp FuncTree data
     *
     * @param startNS startNS
     * @param endNS endNS
     * @return list TreeTableBean
     */
    public static List<DefaultMutableTreeNode> getFuncTreeBottomUp(long startNS, long endNS) {
        return getFuncTreeBottomUp(startNS, endNS, null);
    }

    /**
     * get the get BottomUp FuncTree by startNS、endNS and threadIds
     *
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeBottomUp(long startNS, long endNS, List<Integer> threadIds) {
        if (Objects.isNull(funcMap)) {
            return new ArrayList<>();
        }
        Map<Integer, List<AppFunc>> collect = funcMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
        return DataProcess.getFuncTreeBottomUp(collect, startNS, endNS, threadIds);
    }

    /**
     * get the get BottomUp FuncTree by func
     *
     * @param func func
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeByFuncTopDown(PrefFunc func) {
        List<PrefFunc> collect = funcMap.get(Long.valueOf(func.getTid()).intValue()).stream().filter(
            item -> TimeUtils.isRangeCross(func.getStartTs(), func.getEndTs(), item.getStartTs(), item.getEndTs())
                && item.getDepth() > func.getDepth()).collect(Collectors.toList());
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.setUserObject(new TreeTableBean() {
            {
                setName(func.getFuncName());
                long totalUs = TimeUnit.NANOSECONDS.toMicros(func.getDur());
                setThreadDur(totalUs);
                setTotalNum(totalUs);
                long threadFuncDuration = TimeUnit.NANOSECONDS.toMicros(collect.stream().filter(
                    item -> item.getDepth() == func.getDepth() + 1 && TimeUtils
                        .isRangeCross(func.getStartTs(), func.getEndTs(), item.getStartTs(), item.getEndTs()))
                    .mapToLong(PrefFunc::getDur).sum());
                setChildrenNS(func.getDur());
                setChildrenNum(threadFuncDuration);
                setSelfNum(totalUs - threadFuncDuration);
            }
        });
        Map<String, TreeTableBean> longTreeTableBeanMap = funcGroupByStackId(func, collect, null);
        List<TreeTableBean> treeTableBeans = setNumForNodes(longTreeTableBeanMap);
        Map<String, DefaultMutableTreeNode> treeNodeMap = treeTableBeans.stream()
            .collect(Collectors.toMap(TreeTableBean::getPrefStackId, DefaultMutableTreeNode::new));
        treeTableBeans.forEach(listBean -> {
            if (listBean.getPrefParentStackId().equals(func.getBloodId())) {
                rootNode.add(treeNodeMap.get(listBean.getPrefStackId()));
            } else {
                if (treeNodeMap.containsKey(listBean.getPrefParentStackId())) {
                    treeNodeMap.get(listBean.getPrefParentStackId()).add(treeNodeMap.get(listBean.getPrefStackId()));
                }
            }
        });
        List<DefaultMutableTreeNode> objects = new ArrayList<DefaultMutableTreeNode>();
        objects.add(rootNode);
        return objects;
    }

    /**
     * get the get BottomUp FuncTree by func
     *
     * @param func func
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeByFuncBottomUp(PrefFunc func) {
        long totalUs = TimeUnit.NANOSECONDS.toMicros(func.getDur());
        ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();
        List<PrefFunc> collect = funcMap.get(Long.valueOf(func.getTid()).intValue()).stream().filter(
            item -> TimeUtils.isRangeCross(func.getStartTs(), func.getEndTs(), item.getStartTs(), item.getEndTs()))
            .collect(Collectors.toList());
        Map<String, List<String>> nameToId = new HashMap<>();
        Map<String, TreeTableBean> treeNodeMap = funcGroupByStackId(func, collect, nameToId);
        setNumForNodes(treeNodeMap);
        nameToId.forEach((name, ids) -> {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
            rootNode.setUserObject(new TreeTableBean(totalUs) {
                {
                    setName(name);
                    long totalNum = 0L;
                    long childrenNum = 0L;
                    long selfNum = 0L;
                    for (String id : ids) {
                        TreeTableBean tableBean = treeNodeMap.get(id);
                        totalNum += tableBean.getTotalNum();
                        childrenNum += tableBean.getChildrenNum();
                        selfNum += tableBean.getSelfNum();
                    }
                    setTotalNum(totalNum);
                    setSelfNum(selfNum);
                    setChildrenNum(childrenNum);
                }
            });
            ids.forEach(id -> recursionNode(rootNode, treeNodeMap.get(id).getPrefParentStackId(),
                threadNames.get(Long.valueOf(func.getTid()).intValue()), treeNodeMap, id));
            if (ids.stream().noneMatch(id ->
                collect.stream().filter(item -> item.getBloodId().equals(id) && item.getDepth() < func.getDepth())
                    .toArray().length > 0)) {
                nodes.add(rootNode);
            }
        });
        return nodes;
    }

    private static Map<String, TreeTableBean> funcGroupByStackId(PrefFunc func, List<PrefFunc> collect,
        Map<String, List<String>> nameToId) {
        long totalUs = TimeUnit.NANOSECONDS.toMicros(func.getDur());
        return collect.stream().collect(groupingBy(PrefFunc::getBloodId)).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                TreeTableBean uniteBean = new TreeTableBean(totalUs);
                uniteBean.setPrefStackId(entry.getKey());
                if (entry.getValue().size() > 0) {
                    uniteBean.setName(entry.getValue().get(0).getFuncName());
                    uniteBean.setPrefParentStackId(entry.getValue().get(0).getParentBloodId());
                    if (nameToId != null) {
                        if (nameToId.containsKey(entry.getValue().get(0).getFuncName())) {
                            nameToId.get(entry.getValue().get(0).getFuncName())
                                .add(entry.getValue().get(0).getBloodId());
                        } else {
                            ArrayList<String> list = new ArrayList<String>();
                            list.add(entry.getValue().get(0).getBloodId());
                            nameToId.put(entry.getValue().get(0).getFuncName(), list);
                        }
                    }
                }
                long childrenTotal = entry.getValue().stream().mapToLong(child -> TimeUtils
                    .getIntersection(func.getStartTs(), func.getEndTs(), child.getStartTs(), child.getEndTs())).sum();
                uniteBean.setTotalNum(childrenTotal);
                uniteBean.setChildrenNS(entry.getValue().stream().mapToLong(child -> TimeUtils
                    .getNanoIntersection(func.getStartTs(), func.getEndTs(), child.getStartTs(), child.getEndTs()))
                    .sum());
                return uniteBean;
            }));
    }

    /**
     * Set up presentation data
     *
     * @param map map
     * @return list TreeTableBean
     */
    public static List<TreeTableBean> setNumForNodes(Map<String, TreeTableBean> map) {
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

    private static void recursionNode(DefaultMutableTreeNode rootNode, String parentId, String threadName,
        Map<String, TreeTableBean> treeNodeMap, String id) {
        if (!(rootNode.getUserObject() instanceof TreeTableBean)) {
            return;
        }
        TreeTableBean topBean = (TreeTableBean) rootNode.getUserObject();
        TreeTableBean timeBean = treeNodeMap.get(id);
        if (parentId.isEmpty()) { // Leaf node
            if (rootNode.getChildCount() != 0) { // Merge leaf nodes
                if (rootNode.getChildAt(rootNode.getChildCount() - 1) instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode leafNode =
                        (DefaultMutableTreeNode) rootNode.getChildAt(rootNode.getChildCount() - 1);
                    if (leafNode.getUserObject() instanceof TreeTableBean) {
                        TreeTableBean leafNodeUserObject = (TreeTableBean) leafNode.getUserObject();
                        leafNodeUserObject.mergeTime(timeBean);
                        leafNode.setUserObject(leafNodeUserObject);
                    }
                }
            }
        } else { // Non-leaf nodes
            final TreeTableBean idBean = treeNodeMap.get(parentId);
            boolean sameName = false;
            Enumeration<TreeNode> enumeration = rootNode.children();

            // Compare whether there are node names in the current hierarchy that need to be merged
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
                if (nextElement.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean nextElementUserObject = (TreeTableBean) nextElement.getUserObject();
                    if (nextElementUserObject.getName().equals(idBean.getName())) { // The merge time difference
                        nextElementUserObject.mergeTime(timeBean);
                        recursionNode(nextElement, idBean.getPrefParentStackId(), threadName, treeNodeMap, id);
                        sameName = true;
                    }
                }
            }
            if (!sameName) { // No same node needs to be merged
                DefaultMutableTreeNode addNode = createNewNode(topBean, timeBean, idBean.getName());
                rootNode.add(addNode);
                recursionNode(addNode, idBean.getPrefParentStackId(), threadName, treeNodeMap, id);
            }
        }
    }

    private static DefaultMutableTreeNode createNewNode(TreeTableBean topBean, TreeTableBean timeBean, String name) {
        TreeTableBean treeTableBean = new TreeTableBean(topBean.getThreadDur());
        treeTableBean.setName(name);
        treeTableBean.setTime(timeBean);
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode();
        defaultMutableTreeNode.setUserObject(treeTableBean);
        return defaultMutableTreeNode;
    }

    /**
     * get FlameChart data
     *
     * @param func func
     * @return list TreeTableBean
     */
    public static List<DefaultMutableTreeNode> getFuncTreeFlameChart(PrefFunc func) {
        return getFuncTreeByFuncTopDown(func);
    }

    /**
     * get FlameChart data
     *
     * @param startNS startNS
     * @param endNS endNS
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeFlameChart(long startNS, long endNS) {
        List<DefaultMutableTreeNode> funcTreeTopDown = getFuncTreeTopDown(startNS, endNS);
        funcTreeTopDown.forEach(PerfData::resortNode);
        sortNodeList(funcTreeTopDown);
        return funcTreeTopDown;
    }

    /**
     * get FlameChart data
     *
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     * @return list nodes
     */
    public static List<DefaultMutableTreeNode> getFuncTreeFlameChart(long startNS, long endNS,
        List<Integer> threadIds) {
        List<DefaultMutableTreeNode> funcTreeTopDown = getFuncTreeTopDown(startNS, endNS, threadIds);
        funcTreeTopDown.forEach(PerfData::resortNode);
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
        while (enumeration.hasMoreElements() && enumeration.nextElement() instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (node != null) {
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
                return Long.compare(bean2.getTotalNum(), bean1.getTotalNum());
            }
            return 0;
        }).collect(Collectors.toList());
    }

    /**
     * clear all static data
     */
    public static void clearData() {
        if (prefRange != null) {
            prefRange = null;
        }
        if (funcMap != null) {
            if (funcMap.size() > 0) {
                funcMap.values().forEach(List::clear);
            }
            funcMap.clear();
        }
        if (threadNames != null) {
            threadNames.clear();
        }
        if (prefFiles != null) {
            prefFiles.clear();
        }
    }

    public static PrefRange getPrefRange() {
        return prefRange;
    }

    public static void setPrefRange(PrefRange prefRange) {
        PerfData.prefRange = prefRange;
    }

    public static Map<Long, List<PrefFile>> getPrefFiles() {
        return prefFiles;
    }

    public static void setPrefFiles(Map<Long, List<PrefFile>> prefFiles) {
        PerfData.prefFiles = prefFiles;
    }

    public static Map<Integer, List<PrefFunc>> getFuncMap() {
        return funcMap;
    }

    public static void setFuncMap(Map<Integer, List<PrefFunc>> funcMap) {
        PerfData.funcMap = funcMap;
    }

    public static Map<Integer, String> getThreadNames() {
        return threadNames;
    }

    public static void setThreadNames(Map<Integer, String> threadNames) {
        PerfData.threadNames = threadNames;
    }
}
