/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
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

//@ts-ignore
import { TabPanePerfAnalysis } from '../../../../../../dist/trace/component/trace/sheet/hiperf/TabPanePerfAnalysis.js';
//@ts-ignore
import {queryHiPerfProcessCount} from "../../../../../../dist/trace/database/SqlLite.js";
import crypto from 'crypto';

// @ts-ignore
window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

Object.defineProperty(global.self, 'crypto', {
    value: {
        getRandomValues: (arr: string | any[]) => crypto.randomBytes(arr.length),
    },
});

jest.mock('../../../../../../dist/base-ui/chart/pie/LitChartPie.js', () => {
    return {};
});

jest.mock('../../../../../../dist/base-ui/table/lit-table.js', () => {
    return {};
});

const sqlit = require('../../../../../../dist/trace/database/SqlLite.js');
jest.mock('../../../../../../dist/trace/database/SqlLite.js');

describe('TabPanePerfAnalysis Test', () => {
    it('TabPanePerfAnalysisTest01 ', function () {
       let tabPanePerfAnalysis = new TabPanePerfAnalysis()
        expect(tabPanePerfAnalysis.clearData()).toBeUndefined();
    });
    it('TabPanePerfAnalysisTest02 ', function () {
        let tabPanePerfAnalysis = new TabPanePerfAnalysis()
        expect(tabPanePerfAnalysis.getBack()).toBeUndefined();
    });
    it('TabPanePerfAnalysisTest03 ', function () {
        let tabPanePerfAnalysis = new TabPanePerfAnalysis()
        expect(tabPanePerfAnalysis.sortByColumn({key: 'startTime'}, {sort: 1})).toBeUndefined();
    });
    it('TabPanePerfAnalysisTest04 ', function () {
        let tabPanePerfAnalysis = new TabPanePerfAnalysis()
        expect(tabPanePerfAnalysis.totalCountData(1)).toStrictEqual(
            {"allCount": 1, "count": 0, "countFormat": "1.00ms", "percent": "100.00", "pid": ""}
        );
    });
    it('TabPanePerfAnalysisTest05 ', function () {
        let tabPanePerfAnalysis = new TabPanePerfAnalysis();
        let res = [{
            count:1,
            length:1
        }]
        expect(tabPanePerfAnalysis.getPieChartData(res)).toStrictEqual(
            [{"count": 1, "length": 1}]
        );
    });

    it('TabPanePerfAnalysisTest06 ', function () {
        let tabPanePerfAnalysis = new TabPanePerfAnalysis()
        let queryHiPerf = sqlit.queryHiPerfProcessCount;
        queryHiPerf.mockResolvedValue([
            {
                "pid": 174,
                "time": 11799859602,
                "threadName": "sugov:0",
                "tid": 174,
                "id": 28347,
                "callchain_id": 10972,
                "processName": "sugov:0(174)"
            },
            {
                "pid": 388,
                "time": 11811453353,
                "threadName": "render_service",
                "tid": 871,
                "id": 28355,
                "callchain_id": 10974,
                "processName": "render_service(388)"
            },
            {
                "pid": 28826,
                "time": 11820687229,
                "threadName": "kworker/2:2-events_freezable",
                "tid": 28826,
                "id": 28361,
                "callchain_id": 10976,
                "processName": "kworker/2:2-events_freezable(28826)"
            },
            {
                "pid": 28917,
                "time": 11831719814,
                "threadName": "hiperf",
                "tid": 28922,
                "id": 28372,
                "callchain_id": 51,
                "processName": "hiperf(28917)"
            }
        ])
        let para = {
            leftNs:11799195238,
            rightNs:16844304830,
            cpus: [1,2,3],
            threads: [4,5,6],
            processes: [7,8,9],
            perfThread:[4,5,6],
            perfProcess:[4,5,6]
        }
        tabPanePerfAnalysis.tableProcess.reMeauseHeight = jest.fn(() => true);
        tabPanePerfAnalysis.getHiperfProcess(para)
        expect(tabPanePerfAnalysis.clearData()).toBeUndefined();
    });

    it('TabPanePerfAnalysisTest07 ', function () {
        let tabPanePerfAnalysis = new TabPanePerfAnalysis()
        let para = {
            count: 5,
            tid: 1,
            pid: 2,
            libId: 3
        }
        let processArr = [
            {
                "pid": 233,
                "time": 7978660718,
                "threadName": "hilogd",
                "tid": 235,
                "id": 19165,
                "callchain_id": 7492
            },
            {
                "pid": 233,
                "time": 8092040146,
                "threadName": "hilogd",
                "tid": 235,
                "id": 19408,
                "callchain_id": 7578
            },
            {
                "pid": 233,
                "time": 8117205732,
                "threadName": "hilogd",
                "tid": 235,
                "id": 19496,
                "callchain_id": 7618
            }
        ];
        tabPanePerfAnalysis.processData = processArr;
        tabPanePerfAnalysis.tableFunction.reMeauseHeight = jest.fn(() => true);
        tabPanePerfAnalysis.getHiperfFunction(para, null);
        expect(tabPanePerfAnalysis.clearData()).toBeUndefined();
    });
})