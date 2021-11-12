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

package ohos.devtools.views.distributed.bean;

import ohos.devtools.views.applicationtrace.util.TimeUtils;

import java.util.Arrays;
import java.util.List;

/**
 * DetailBean
 *
 * @since 2021/8/26 15:10
 */
public class DetailBean {
    private String name;
    private String params;
    private String total;
    private Long totalNS;
    private String delay;
    private Long delayNS;
    private long middleNs;
    private long avgNs;
    private long middleDelayNS;
    private long delayAvgNs;
    private long stackId;
    private long parentStackId;
    private DistributedFuncBean.BeanDataType currentType;
    private String chainId;
    private Integer spanId;
    private Integer parentSpanId;
    private Long startTs;
    private Integer id;
    private Integer parentId;
    private int containType = 0; // 0 OK 1 There are keywords 2 children there are keywords 3 there are no keywords

    /**
     * DetailBean
     */
    public DetailBean() {
        super();
    }

    /**
     * getChainId
     *
     * @return chainId chainId
     */
    public String getChainId() {
        return chainId;
    }

    /**
     * setChainId
     *
     * @param chainId chainId
     */
    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    /**
     * getSpanId
     *
     * @return spanId spanId
     */
    public Integer getSpanId() {
        return spanId;
    }

    /**
     * setSpanId
     *
     * @param spanId spanId
     */
    public void setSpanId(Integer spanId) {
        this.spanId = spanId;
    }

    /**
     * getParentSpanId
     *
     * @return parentSpanId parentSpanId
     */
    public Integer getParentSpanId() {
        return parentSpanId;
    }

    /**
     * setParentSpanId
     *
     * @param parentSpanId parentSpanId
     */
    public void setParentSpanId(Integer parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    /**
     * mergeFuncBean
     *
     * @param distributedFuncBean distributedFuncBean
     */
    public void mergeFuncBean(DistributedFuncBean distributedFuncBean) {
        this.setName(distributedFuncBean.getFuncName());
        this.setParams(distributedFuncBean.getArgs());
        this.setTotalNS(distributedFuncBean.getDur());
        this.setId(distributedFuncBean.getId());
        this.setParentId(distributedFuncBean.getParentId());
        this.setStartTs(distributedFuncBean.getStartTs());
        this.setCurrentType(distributedFuncBean.getCurrentType());
        this.setChainId(distributedFuncBean.getChainId());
        this.setSpanId(distributedFuncBean.getSpanId());
        this.setParentSpanId(distributedFuncBean.getParentSpanId());
        this.setParentId(distributedFuncBean.getParentId());
        this.setDelayNS(distributedFuncBean.getDelay());
    }

    /**
     * mergeDetailcBean
     *
     * @param detailBean detailBean
     */
    public void mergeDetailcBean(DetailBean detailBean) {
        this.middleNs = detailBean.getMiddleNs();
        this.avgNs = detailBean.getAvgNs();
        this.middleDelayNS = detailBean.getMiddleDelayNS();
        this.delayAvgNs = detailBean.getDelayAvgNs();
    }

    /**
     * getStackId
     *
     * @return stackId stackId
     */
    public long getStackId() {
        return stackId;
    }

    /**
     * setStackId
     *
     * @param stackId stackId
     */
    public void setStackId(long stackId) {
        this.stackId = stackId;
    }

    /**
     * getParentStackId
     *
     * @return parentStackId parentStackId
     */
    public long getParentStackId() {
        return parentStackId;
    }

    /**
     * setParentStackId
     *
     * @param parentStackId parentStackId
     */
    public void setParentStackId(long parentStackId) {
        this.parentStackId = parentStackId;
    }

    /**
     * getMiddleNs
     *
     * @return middleNs middleNs
     */
    public long getMiddleNs() {
        return middleNs;
    }

    /**
     * setMiddleNs
     *
     * @param middleNs middleNs
     */
    public void setMiddleNs(long middleNs) {
        this.middleNs = middleNs;
    }

    /**
     * getAvgNs
     *
     * @return avgNs avgNs
     */
    public long getAvgNs() {
        return avgNs;
    }

    /**
     * setAvgNs
     *
     * @param avgNs avgNs
     */
    public void setAvgNs(long avgNs) {
        this.avgNs = avgNs;
    }

    /**
     * getMiddleDelayNS
     *
     * @return middleDelayNS middleDelayNS
     */
    public long getMiddleDelayNS() {
        return middleDelayNS;
    }

    /**
     * setMiddleDelayNS
     *
     * @param middleDelayNS middleDelayNS
     */
    public void setMiddleDelayNS(long middleDelayNS) {
        this.middleDelayNS = middleDelayNS;
    }

    /**
     * getName
     *
     * @return name name
     */
    public String getName() {
        return name;
    }

    /**
     * setName
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getParams
     *
     * @return params params
     */
    public String getParams() {
        return params;
    }

    /**
     * setParams
     *
     * @param params params
     */
    public void setParams(String params) {
        this.params = params;
    }

    /**
     * getTotal
     *
     * @return total total
     */
    public String getTotal() {
        return total;
    }

    /**
     * setTotal
     *
     * @param total total
     */
    public void setTotal(String total) {
        this.total = total;
    }

    /**
     * getTotalNS
     *
     * @return totalNS totalNS
     */
    public Long getTotalNS() {
        return totalNS;
    }

    /**
     * setTotalNS
     *
     * @param totalNS totalNS
     */
    public void setTotalNS(Long totalNS) {
        this.totalNS = totalNS;
    }

    /**
     * getDelay
     *
     * @return delay delay
     */
    public String getDelay() {
        return delay;
    }

    /**
     * setDelay
     *
     * @param delay delay
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }

    /**
     * getDelayNS
     *
     * @return delayNS delayNS
     */
    public Long getDelayNS() {
        return delayNS;
    }

    /**
     * setDelayNS
     *
     * @param delayNS delayNS
     */
    public void setDelayNS(Long delayNS) {
        this.delayNS = delayNS;
    }

    /**
     * getDelayAvgNs
     *
     * @return delayAvgNs delayAvgNs
     */
    public long getDelayAvgNs() {
        return delayAvgNs;
    }

    /**
     * setDelayAvgNs
     *
     * @param delayAvgNs delayAvgNs
     */
    public void setDelayAvgNs(long delayAvgNs) {
        this.delayAvgNs = delayAvgNs;
    }

    /**
     * containType
     *
     * @return containType containType
     */
    public int getContainType() {
        return containType;
    }

    /**
     * setContainType
     *
     * @param containType containType
     */
    public void setContainType(int containType) {
        this.containType = containType;
    }

    /**
     * getId
     *
     * @return id id
     */
    public Integer getId() {
        return id;
    }

    /**
     * setId
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * getParentId
     *
     * @return parentId parentId
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * setParentId
     *
     * @param parentId parentId
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * getStartTs
     *
     * @return startTs startTs
     */
    public Long getStartTs() {
        return startTs;
    }

    /**
     * setStartTs
     *
     * @param startTs startTs
     */
    public void setStartTs(Long startTs) {
        this.startTs = startTs;
    }

    /**
     * getCurrentType
     *
     * @return DistributedFuncBean.BeanDataType currentType
     */
    public DistributedFuncBean.BeanDataType getCurrentType() {
        return currentType;
    }

    /**
     * setCurrentType
     *
     * @param currentType DistributedFuncBean.BeanDataType
     */
    public void setCurrentType(DistributedFuncBean.BeanDataType currentType) {
        this.currentType = currentType;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * getStringList get the content value list
     *
     * @return stringList
     */
    public List<String> getStringList() {
        if (totalNS == null) {
            return Arrays.asList("" + getName());
        }
        return Arrays.asList("" + getName(), "Current Time: " + TimeUtils.getTimeWithUnit(totalNS),
            "Median Time: " + TimeUtils.getTimeWithUnit(middleNs), "Avg Time:" + TimeUtils.getTimeWithUnit(avgNs),
            "Delay:" + TimeUtils.getTimeWithUnit(delayNS), "Median Delay:" + TimeUtils.getTimeWithUnit(middleDelayNS),
            "Avg Delay:" + TimeUtils.getTimeWithUnit(delayAvgNs));
    }
}
