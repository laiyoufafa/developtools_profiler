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

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistributedThreadBeanTest {

    @Test
    void getId() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setId(1);
        assertEquals(1, distributedThreadBean.getId());
    }

    @Test
    void setId() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setId(1);
        assertEquals(1, distributedThreadBean.getId());
    }

    @Test
    void getName() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setName("name");
        assertEquals("name", distributedThreadBean.getName());
    }

    @Test
    void setName() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setName("name");
        assertEquals("name", distributedThreadBean.getName());
    }

    @Test
    void getTid() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setTid(1);
        assertEquals(1, distributedThreadBean.getTid());
    }

    @Test
    void setTid() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setTid(1);
        assertEquals(1, distributedThreadBean.getTid());
    }

    @Test
    void getPid() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setPid(1);
        assertEquals(1, distributedThreadBean.getPid());
    }

    @Test
    void setPid() {
        DistributedThreadBean distributedThreadBean = new DistributedThreadBean();
        distributedThreadBean.setPid(1);
        assertEquals(1, distributedThreadBean.getPid());
    }
}