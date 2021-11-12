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

class DistributedFuncBeanTest {

    @Test
    void isSelected() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("isSelected");
        field.setAccessible(true);
        boolean isSelected = true;
        field.set(distributedFuncBean, isSelected);
        assertEquals(isSelected, distributedFuncBean.isSelected());
    }

    @Test
    void setSelected() throws NoSuchFieldException, IllegalAccessException {
        boolean isSelected = true;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setSelected(isSelected);
        final Field field = distributedFuncBean.getClass().getDeclaredField("isSelected");
        field.setAccessible(true);
        assertEquals(isSelected, field.get(distributedFuncBean));
    }

    @Test
    void getSpanId() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("spanId");
        field.setAccessible(true);
        Integer spanId = 123;
        field.set(distributedFuncBean, spanId);
        assertEquals(spanId, distributedFuncBean.getSpanId());
    }

    @Test
    void setSpanId() throws NoSuchFieldException, IllegalAccessException {
        Integer spanId = 123;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setSpanId(spanId);
        final Field field = distributedFuncBean.getClass().getDeclaredField("spanId");
        field.setAccessible(true);
        assertEquals(spanId, field.get(distributedFuncBean));
    }

    @Test
    void getParentSpanId() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("parentSpanId");
        field.setAccessible(true);
        Integer parentSpanId = 123;
        field.set(distributedFuncBean, parentSpanId);
        assertEquals(parentSpanId, distributedFuncBean.getParentSpanId());
    }

    @Test
    void setParentSpanId() throws NoSuchFieldException, IllegalAccessException {
        Integer parentSpanId = 123;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setParentSpanId(parentSpanId);
        final Field field = distributedFuncBean.getClass().getDeclaredField("parentSpanId");
        field.setAccessible(true);
        assertEquals(parentSpanId, field.get(distributedFuncBean));
    }

    @Test
    void getFlag() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("flag");
        field.setAccessible(true);
        String flag = "flag";
        field.set(distributedFuncBean, flag);
        assertEquals(flag, distributedFuncBean.getFlag());
    }

    @Test
    void setFlag() throws NoSuchFieldException, IllegalAccessException {
        String flag = "flag";
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setFlag(flag);
        final Field field = distributedFuncBean.getClass().getDeclaredField("flag");
        field.setAccessible(true);
        assertEquals(flag, field.get(distributedFuncBean));
    }

    @Test
    void getArgs() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("args");
        field.setAccessible(true);
        String args = "args";
        field.set(distributedFuncBean, args);
        assertEquals(args, distributedFuncBean.getArgs());
    }

    @Test
    void setArgs() throws NoSuchFieldException, IllegalAccessException {
        String args = "args";
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setArgs(args);
        final Field field = distributedFuncBean.getClass().getDeclaredField("args");
        field.setAccessible(true);
        assertEquals(args, field.get(distributedFuncBean));
    }

    @Test
    void getId() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("id");
        field.setAccessible(true);
        Integer id = 123;
        field.set(distributedFuncBean, id);
        assertEquals(id, distributedFuncBean.getId());
    }

    @Test
    void setId() throws NoSuchFieldException, IllegalAccessException {
        Integer id = 123;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setId(id);
        final Field field = distributedFuncBean.getClass().getDeclaredField("id");
        field.setAccessible(true);
        assertEquals(id, field.get(distributedFuncBean));
    }

    @Test
    void getParentId() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("parentId");
        field.setAccessible(true);
        Integer parentId = 123;
        field.set(distributedFuncBean, parentId);
        assertEquals(parentId, distributedFuncBean.getParentId());
    }

    @Test
    void setParentId() throws NoSuchFieldException, IllegalAccessException {
        Integer parentId = 123;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setParentId(parentId);
        final Field field = distributedFuncBean.getClass().getDeclaredField("parentId");
        field.setAccessible(true);
        assertEquals(parentId, field.get(distributedFuncBean));
    }

    @Test
    void getIsMainThread() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("isMainThread");
        field.setAccessible(true);
        Integer isMainThread = 1;
        field.set(distributedFuncBean, isMainThread);
        assertEquals(isMainThread, distributedFuncBean.getIsMainThread());
    }

    @Test
    void setIsMainThread() throws NoSuchFieldException, IllegalAccessException {
        Integer isMainThread = 1;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setIsMainThread(isMainThread);
        final Field field = distributedFuncBean.getClass().getDeclaredField("isMainThread");
        field.setAccessible(true);
        assertEquals(isMainThread, field.get(distributedFuncBean));
    }

    @Test
    void getTrackId() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("trackId");
        field.setAccessible(true);
        Integer trackId = 1;
        field.set(distributedFuncBean, trackId);
        assertEquals(trackId, distributedFuncBean.getTrackId());
    }

    @Test
    void setTrackId() throws NoSuchFieldException, IllegalAccessException {
        Integer trackId = 1;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setTrackId(trackId);
        final Field field = distributedFuncBean.getClass().getDeclaredField("trackId");
        field.setAccessible(true);
        assertEquals(trackId, field.get(distributedFuncBean));
    }

    @Test
    void getFuncName() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("funcName");
        field.setAccessible(true);
        String funcName = "funcName";
        field.set(distributedFuncBean, funcName);
        assertEquals(funcName, distributedFuncBean.getFuncName());
    }

    @Test
    void setFuncName() throws NoSuchFieldException, IllegalAccessException {
        String funcName = "funcName";
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setFuncName(funcName);
        final Field field = distributedFuncBean.getClass().getDeclaredField("funcName");
        field.setAccessible(true);
        assertEquals(funcName, field.get(distributedFuncBean));
    }

    @Test
    void getTid() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        Integer tid = 1;
        field.set(distributedFuncBean, tid);
        assertEquals(tid, distributedFuncBean.getTid());
    }

    @Test
    void setTid() throws NoSuchFieldException, IllegalAccessException {
        Integer tid = 1;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setTid(tid);
        final Field field = distributedFuncBean.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        assertEquals(tid, field.get(distributedFuncBean));
    }

    @Test
    void getDepth() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("depth");
        field.setAccessible(true);
        Integer depth = 1;
        field.set(distributedFuncBean, depth);
        assertEquals(depth, distributedFuncBean.getDepth());
    }

    @Test
    void setDepth() throws NoSuchFieldException, IllegalAccessException {
        Integer depth = 1;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setDepth(depth);
        final Field field = distributedFuncBean.getClass().getDeclaredField("depth");
        field.setAccessible(true);
        assertEquals(depth, field.get(distributedFuncBean));
    }

    @Test
    void getThreadName() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("threadName");
        field.setAccessible(true);
        String threadName = "threadName";
        field.set(distributedFuncBean, threadName);
        assertEquals(threadName, distributedFuncBean.getThreadName());
    }

    @Test
    void setThreadName() throws NoSuchFieldException, IllegalAccessException {
        String threadName = "threadName";
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setThreadName(threadName);
        final Field field = distributedFuncBean.getClass().getDeclaredField("threadName");
        field.setAccessible(true);
        assertEquals(threadName, field.get(distributedFuncBean));
    }

    @Test
    void getStartTs() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        Long startTs = 123L;
        field.set(distributedFuncBean, startTs);
        assertEquals(startTs, distributedFuncBean.getStartTs());
    }

    @Test
    void setStartTs() throws NoSuchFieldException, IllegalAccessException {
        Long startTs = 123L;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setStartTs(startTs);
        final Field field = distributedFuncBean.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        assertEquals(startTs, field.get(distributedFuncBean));
    }

    @Test
    void getDur() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("dur");
        field.setAccessible(true);
        Long dur = 123L;
        field.set(distributedFuncBean, dur);
        assertEquals(dur, distributedFuncBean.getDur());
    }

    @Test
    void setDur() throws NoSuchFieldException, IllegalAccessException {
        Long dur = 123L;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setDur(dur);
        final Field field = distributedFuncBean.getClass().getDeclaredField("dur");
        field.setAccessible(true);
        assertEquals(dur, field.get(distributedFuncBean));
    }

    @Test
    void getChainId() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("chainId");
        field.setAccessible(true);
        String chainId = "chainId";
        field.set(distributedFuncBean, chainId);
        assertEquals(chainId, distributedFuncBean.getChainId());
    }

    @Test
    void setChainId() throws NoSuchFieldException, IllegalAccessException {
        String chainId = "chainId";
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setChainId(chainId);
        final Field field = distributedFuncBean.getClass().getDeclaredField("chainId");
        field.setAccessible(true);
        assertEquals(chainId, field.get(distributedFuncBean));
    }

    @Test
    void getCurrentType() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("currentType");
        field.setAccessible(true);
        DistributedFuncBean.BeanDataType currentType = DistributedFuncBean.BeanDataType.TYPE_A;
        field.set(distributedFuncBean, currentType);
        assertEquals(currentType, distributedFuncBean.getCurrentType());
    }

    @Test
    void setCurrentType() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean.BeanDataType currentType = DistributedFuncBean.BeanDataType.TYPE_A;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setCurrentType(currentType);
        final Field field = distributedFuncBean.getClass().getDeclaredField("currentType");
        field.setAccessible(true);
        assertEquals(currentType, field.get(distributedFuncBean));
    }

    @Test
    void getEndTs() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setStartTs(1L);
        distributedFuncBean.setDur(2L);
        assertEquals(3L, distributedFuncBean.getEndTs());
    }

    @Test
    void getStringList() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean detailBean = new DistributedFuncBean();
        detailBean.setStartTs(1L);
        detailBean.setDur(2L);
        detailBean.setFuncName("functionName");
        assert detailBean.getStringList("time").size() > 0;
    }

    @Test
    void getDelay() throws NoSuchFieldException, IllegalAccessException {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        final Field field = distributedFuncBean.getClass().getDeclaredField("delay");
        field.setAccessible(true);
        Long delay = 1L;
        field.set(distributedFuncBean, delay);
        assertEquals(delay, distributedFuncBean.getDelay());
    }

    @Test
    void setDelay() throws NoSuchFieldException, IllegalAccessException {
        Long delay = 1L;
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setDelay(delay);
        final Field field = distributedFuncBean.getClass().getDeclaredField("delay");
        field.setAccessible(true);
        assertEquals(delay, field.get(distributedFuncBean));
    }

}