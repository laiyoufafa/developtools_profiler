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

package ohos.devtools.views.distributed;

import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DistributedDataCallTreePaneTest {
    DistributedDataCallTreePane distributedDataCallTreePane = new DistributedDataCallTreePane();

    @Test
    void freshTreeData() {
        distributedDataCallTreePane.freshTreeData(null, new DistributedFuncBean());
        assertNotNull(distributedDataCallTreePane);
    }

    @Test
    void findHead() {
        DistributedFuncBean distributedFuncBean = new DistributedFuncBean();
        distributedFuncBean.setParentId(0);
        distributedFuncBean.setFlag("C");
        distributedFuncBean.setSpanId(0);
        distributedFuncBean.setParentSpanId(0);
        distributedDataCallTreePane.findHead(distributedFuncBean);
        assertNotNull(distributedDataCallTreePane);
    }
}