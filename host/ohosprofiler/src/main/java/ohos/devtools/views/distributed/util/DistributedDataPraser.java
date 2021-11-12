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

package ohos.devtools.views.distributed.util;

import ohos.devtools.views.applicationtrace.util.MathUtils;
import ohos.devtools.views.distributed.bean.DetailBean;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DistributedDataPraser
 *
 * @since 2021/08/10 16:20
 */
public class DistributedDataPraser {
    /**
     * collectByName
     *
     * @param list list
     * @return DetailBean DetailBean
     */
    public static DetailBean collectByName(List<DistributedFuncBean> list) {
        DetailBean detailBean = new DetailBean();
        if (list.size() == 0) {
            return detailBean;
        }
        List<Long> sortList = list.stream().map(DistributedFuncBean::getDur).sorted().collect(Collectors.toList());
        if (sortList.size() > 1 && sortList.size() % 2 == 1) {
            detailBean.setMiddleNs((sortList.get(sortList.size() / 2) + sortList.get(sortList.size() / 2 + 1)) / 2);
        } else {
            detailBean.setMiddleNs(sortList.get(sortList.size() / 2));
        }
        detailBean.setAvgNs(
            Double.valueOf(MathUtils.average(sortList.stream().map(Long::doubleValue).collect(Collectors.toList())))
                .longValue());

        // Set delay middle and avg
        List<Long> delaySortList = list.stream().map(DistributedFuncBean::getDelay).filter(Objects::nonNull).sorted()
            .collect(Collectors.toList());
        if (delaySortList.size() > 1 && delaySortList.size() % 2 == 1) {
            detailBean.setMiddleDelayNS(
                (delaySortList.get(delaySortList.size() / 2) + delaySortList.get(delaySortList.size() / 2 + 1)) / 2);
        } else {
            detailBean.setMiddleDelayNS(delaySortList.get(delaySortList.size() / 2));
        }
        detailBean.setDelayAvgNs(Double
            .valueOf(MathUtils.average(delaySortList.stream().map(Long::doubleValue).collect(Collectors.toList())))
            .longValue());
        return detailBean;
    }
}
