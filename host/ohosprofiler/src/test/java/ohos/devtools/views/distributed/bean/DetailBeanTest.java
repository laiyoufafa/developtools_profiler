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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DetailBeanTest {

    @Test
    void getChainId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("chainId");
        field.setAccessible(true);
        String chainId = "chainId";
        field.set(detailBean, chainId);
        assertEquals(chainId, detailBean.getChainId());
    }

    @Test
    void setChainId() throws NoSuchFieldException, IllegalAccessException {
        String chainId = "chainId";
        DetailBean detailBean = new DetailBean();
        detailBean.setChainId(chainId);
        final Field field = detailBean.getClass().getDeclaredField("chainId");
        field.setAccessible(true);
        assertEquals(chainId, field.get(detailBean));
    }

    @Test
    void getSpanId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("spanId");
        field.setAccessible(true);
        Integer spanId = 123;
        field.set(detailBean, spanId);
        assertEquals(spanId, detailBean.getSpanId());
    }

    @Test
    void setSpanId() throws NoSuchFieldException, IllegalAccessException {
        Integer spanId = 123;
        DetailBean detailBean = new DetailBean();
        detailBean.setSpanId(spanId);
        final Field field = detailBean.getClass().getDeclaredField("spanId");
        field.setAccessible(true);
        assertEquals(spanId, field.get(detailBean));
    }

    @Test
    void getParentSpanId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("parentSpanId");
        field.setAccessible(true);
        Integer parentSpanid = 123;
        field.set(detailBean, parentSpanid);
        assertEquals(parentSpanid, detailBean.getParentSpanId());
    }

    @Test
    void setParentSpanId() throws NoSuchFieldException, IllegalAccessException {
        Integer parentSpanid = 123;
        DetailBean detailBean = new DetailBean();
        detailBean.setParentSpanId(parentSpanid);
        final Field field = detailBean.getClass().getDeclaredField("parentSpanId");
        field.setAccessible(true);
        assertEquals(parentSpanid, field.get(detailBean));
    }

    @Test
    void mergeFuncBean() {
        DetailBean detailBean = new DetailBean();
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setChainId("123");
        detailBean.mergeFuncBean(distributedFuncBean);
        assertEquals(detailBean.getChainId(), distributedFuncBean.getChainId());
    }

    @Test
    void mergeDetailcBean() {
        DetailBean detailBean = new DetailBean();
        DetailBean merageBean = new DetailBean();
        merageBean.setMiddleNs(100L);
        detailBean.mergeDetailcBean(merageBean);
        assertEquals(merageBean.getMiddleNs(), detailBean.getMiddleNs());
    }

    @Test
    void getStackId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("stackId");
        field.setAccessible(true);
        Long stackId = 123L;
        field.set(detailBean, stackId);
        assertEquals(stackId, detailBean.getStackId());
    }

    @Test
    void setStackId() throws NoSuchFieldException, IllegalAccessException {
        Long stackId = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setStackId(stackId);
        final Field field = detailBean.getClass().getDeclaredField("stackId");
        field.setAccessible(true);
        assertEquals(stackId, field.get(detailBean));
    }

    @Test
    void getParentStackId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("parentStackId");
        field.setAccessible(true);
        Long parentStackId = 123L;
        field.set(detailBean, parentStackId);
        assertEquals(parentStackId, detailBean.getParentStackId());
    }

    @Test
    void setParentStackId() throws NoSuchFieldException, IllegalAccessException {
        Long parentStackId = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setParentStackId(parentStackId);
        final Field field = detailBean.getClass().getDeclaredField("parentStackId");
        field.setAccessible(true);
        assertEquals(parentStackId, field.get(detailBean));
    }

    @Test
    void getMiddleNs() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("middleNs");
        field.setAccessible(true);
        Long middleNs = 123L;
        field.set(detailBean, middleNs);
        assertEquals(middleNs, detailBean.getMiddleNs());
    }

    @Test
    void setMiddleNs() throws NoSuchFieldException, IllegalAccessException {
        Long parentStackId = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setParentStackId(parentStackId);
        final Field field = detailBean.getClass().getDeclaredField("parentStackId");
        field.setAccessible(true);
        assertEquals(parentStackId, field.get(detailBean));
    }

    @Test
    void getAvgNs() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("avgNs");
        field.setAccessible(true);
        Long avgNs = 123L;
        field.set(detailBean, avgNs);
        assertEquals(avgNs, detailBean.getAvgNs());
    }

    @Test
    void setAvgNs() throws NoSuchFieldException, IllegalAccessException {
        Long avgNs = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setAvgNs(avgNs);
        final Field field = detailBean.getClass().getDeclaredField("avgNs");
        field.setAccessible(true);
        assertEquals(avgNs, field.get(detailBean));
    }

    @Test
    void getMiddleDelayNS() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("middleDelayNS");
        field.setAccessible(true);
        Long middelDelayNs = 123L;
        field.set(detailBean, middelDelayNs);
        assertEquals(middelDelayNs, detailBean.getMiddleDelayNS());
    }

    @Test
    void setMiddleDelayNS() throws NoSuchFieldException, IllegalAccessException {
        Long middelDelayNs = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setMiddleDelayNS(middelDelayNs);
        final Field field = detailBean.getClass().getDeclaredField("middleDelayNS");
        field.setAccessible(true);
        assertEquals(middelDelayNs, field.get(detailBean));
    }

    @Test
    void getName() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("name");
        field.setAccessible(true);
        String name = "name";
        field.set(detailBean, name);
        assertEquals(name, detailBean.getName());
    }

    @Test
    void setName() throws NoSuchFieldException, IllegalAccessException {
        String name = "name";
        DetailBean detailBean = new DetailBean();
        detailBean.setName(name);
        final Field field = detailBean.getClass().getDeclaredField("name");
        field.setAccessible(true);
        assertEquals(name, field.get(detailBean));
    }

    @Test
    void getParams() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("params");
        field.setAccessible(true);
        String params = "params";
        field.set(detailBean, params);
        assertEquals(params, detailBean.getParams());
    }

    @Test
    void setParams() throws NoSuchFieldException, IllegalAccessException {
        String params = "params";
        DetailBean detailBean = new DetailBean();
        detailBean.setParams(params);
        final Field field = detailBean.getClass().getDeclaredField("params");
        field.setAccessible(true);
        assertEquals(params, field.get(detailBean));
    }

    @Test
    void getTotal() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("total");
        field.setAccessible(true);
        String total = "total";
        field.set(detailBean, total);
        assertEquals(total, detailBean.getTotal());
    }

    @Test
    void setTotal() throws NoSuchFieldException, IllegalAccessException {
        String total = "total";
        DetailBean detailBean = new DetailBean();
        detailBean.setTotal(total);
        final Field field = detailBean.getClass().getDeclaredField("total");
        field.setAccessible(true);
        assertEquals(total, field.get(detailBean));
    }

    @Test
    void getTotalNS() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("totalNS");
        field.setAccessible(true);
        Long totalNS = 123L;
        field.set(detailBean, totalNS);
        assertEquals(totalNS, detailBean.getTotalNS());
    }

    @Test
    void setTotalNS() throws NoSuchFieldException, IllegalAccessException {
        Long totalNS = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setTotalNS(totalNS);
        final Field field = detailBean.getClass().getDeclaredField("totalNS");
        field.setAccessible(true);
        assertEquals(totalNS, field.get(detailBean));
    }

    @Test
    void getDelay() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("delay");
        field.setAccessible(true);
        String delay = "delay";
        field.set(detailBean, delay);
        assertEquals(delay, detailBean.getDelay());
    }

    @Test
    void setDelay() throws NoSuchFieldException, IllegalAccessException {
        String delay = "delay";
        DetailBean detailBean = new DetailBean();
        detailBean.setDelay(delay);
        final Field field = detailBean.getClass().getDeclaredField("delay");
        field.setAccessible(true);
        assertEquals(delay, field.get(detailBean));
    }

    @Test
    void getDelayNS() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("delayNS");
        field.setAccessible(true);
        Long delayNS = 123L;
        field.set(detailBean, delayNS);
        assertEquals(delayNS, detailBean.getDelayNS());
    }

    @Test
    void setDelayNS() throws NoSuchFieldException, IllegalAccessException {
        Long delayNS = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setDelayNS(delayNS);
        final Field field = detailBean.getClass().getDeclaredField("delayNS");
        field.setAccessible(true);
        assertEquals(delayNS, field.get(detailBean));
    }

    @Test
    void getDelayAvgNs() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("delayAvgNs");
        field.setAccessible(true);
        Long delayAvgNs = 123L;
        field.set(detailBean, delayAvgNs);
        assertEquals(delayAvgNs, detailBean.getDelayAvgNs());
    }

    @Test
    void setDelayAvgNs() throws NoSuchFieldException, IllegalAccessException {
        Long delayAvgNs = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setDelayAvgNs(delayAvgNs);
        final Field field = detailBean.getClass().getDeclaredField("delayAvgNs");
        field.setAccessible(true);
        assertEquals(delayAvgNs, field.get(detailBean));
    }

    @Test
    void getContainType() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("containType");
        field.setAccessible(true);
        int contentType = 1;
        field.set(detailBean, contentType);
        assertEquals(contentType, detailBean.getContainType());
    }

    @Test
    void setContainType() throws NoSuchFieldException, IllegalAccessException {
        int contentType = 1;
        DetailBean detailBean = new DetailBean();
        detailBean.setContainType(contentType);
        final Field field = detailBean.getClass().getDeclaredField("containType");
        field.setAccessible(true);
        assertEquals(contentType, field.get(detailBean));
    }

    @Test
    void getId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("id");
        field.setAccessible(true);
        int id = 1;
        field.set(detailBean, id);
        assertEquals(id, detailBean.getId());
    }

    @Test
    void setId() throws NoSuchFieldException, IllegalAccessException {
        int id = 1;
        DetailBean detailBean = new DetailBean();
        detailBean.setId(id);
        final Field field = detailBean.getClass().getDeclaredField("id");
        field.setAccessible(true);
        assertEquals(id, field.get(detailBean));
    }

    @Test
    void getParentId() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("parentId");
        field.setAccessible(true);
        int parentId = 1;
        field.set(detailBean, parentId);
        assertEquals(parentId, detailBean.getParentId());
    }

    @Test
    void setParentId() throws NoSuchFieldException, IllegalAccessException {
        int parentId = 1;
        DetailBean detailBean = new DetailBean();
        detailBean.setParentId(parentId);
        final Field field = detailBean.getClass().getDeclaredField("parentId");
        field.setAccessible(true);
        assertEquals(parentId, field.get(detailBean));
    }

    @Test
    void getStartTs() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        Long startTs = 123L;
        field.set(detailBean, startTs);
        assertEquals(startTs, detailBean.getStartTs());
    }

    @Test
    void setStartTs() throws NoSuchFieldException, IllegalAccessException {
        Long startTs = 123L;
        DetailBean detailBean = new DetailBean();
        detailBean.setStartTs(startTs);
        final Field field = detailBean.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        assertEquals(startTs, field.get(detailBean));
    }

    @Test
    void getCurrentType() throws NoSuchFieldException, IllegalAccessException {
        DetailBean detailBean = new DetailBean();
        final Field field = detailBean.getClass().getDeclaredField("currentType");
        field.setAccessible(true);
        DistributedFuncBean.BeanDataType beanDataType = DistributedFuncBean.BeanDataType.TYPE_A;
        field.set(detailBean, beanDataType);
        assertEquals(beanDataType, detailBean.getCurrentType());
    }

    @Test
    void setCurrentType() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean.BeanDataType beanDataType = DistributedFuncBean.BeanDataType.TYPE_A;
        DetailBean detailBean = new DetailBean();
        detailBean.setCurrentType(beanDataType);
        assertEquals(beanDataType, detailBean.getCurrentType());
    }

    @Test
    void getStringList() {
        DetailBean detailBean = new DetailBean();
        detailBean.setName("name");
        assert detailBean.getStringList().size() > 0;
    }
}