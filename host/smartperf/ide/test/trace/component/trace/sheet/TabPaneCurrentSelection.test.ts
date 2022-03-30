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

// @ts-ignore
import {TabPaneCurrentSelection, getTimeString} from "../../../../../dist/trace/component/trace/sheet/TabPaneCurrentSelection.js"

describe('TabPaneCurrentSelection Test', () => {
    let tabPaneCurrentSelection = new TabPaneCurrentSelection();

    const canvas = document.createElement('canvas');
    canvas.width = 1;
    canvas.height = 1;

    let cpuData = [{
        cpu: 1,
        dur: 1,
        end_state: 'string',
        id: 12,
        name: 'name',
        priority: 11,
        processCmdLine: 'processCmdLine',
        processId: 111,
        processName: 'processName',
        schedId: 22,
        startTime: 0,
        tid: 100,
        type: 'type',
    }]
    let functionData = [{
        argsetid:  53161,
        depth:  0,
        dur:  570000,
        funName: "binder transaction",
        id: 92749,
        is_main_thread:  0,
        parent_id:  null,
        startTs:  9729867000,
        threadName: "Thread-15",
        tid: 2785,
    }]
    let memData = [{
        trackId: 100,
        processName:'processName',
        pid: 11,
        upid:1,
        trackName:'trackName',
        type:'type',
        track_id: 'track_id',
        value: 111,
        startTime:0,
        duration:1000,
        maxValue:4000,
        delta: 2,
    }]
    let threadData = [{
        hasSched: 14724852000,
        pid: 2519,
        processName: null,
        threadName: "ACCS0",
        tid: 2716,
        upid:  1,
        utid:  1,
        cpu: null,
        dur: 405000,
        end_ts: null,
        id: 1,
        is_main_thread: 0,
        name: "ACCS0",
        startTime: 58000,
        start_ts: null,
        state: "S",
        type: "thread",
    }]
    let wakeupBean = [{
        wakeupTime:0,
        cpu:1,
        process:'process',
        pid:11,
        thread:'thread',
        tid:22,
        schedulingLatency:33,
        schedulingDesc:'schedulingDesc',
    }]

    tabPaneCurrentSelection.queryWakeUpData = jest.fn(()=> 'WakeUpData')

    it('TabPaneCurrentSelectionTest01', function () {
        let result = tabPaneCurrentSelection.setFunctionData(functionData)
        expect(result).toBeUndefined();
    });

    it('TabPaneCurrentSelectionTest02', function () {
        let result = tabPaneCurrentSelection.setMemData(memData)
        expect(result).toBeUndefined();
    });

    it('TabPaneCurrentSelectionTest03', function () {
        let result = tabPaneCurrentSelection.setThreadData(threadData)
        expect(result).toBeUndefined();
    });

    it('TabPaneCurrentSelectionTest04', function () {
        let result = tabPaneCurrentSelection.drawRight(canvas, wakeupBean)
        expect(result).toBeUndefined();
    });

    it('TabPaneCurrentSelectionTest05', function () {
        let result = tabPaneCurrentSelection.dprToPx(2)
        expect(result).toBe(2);
    });

    it('TabPaneCurrentSelectionTest06', function () {
        let result = getTimeString(3600000000001)
        expect(result).toBe('1h 1ns ');
    });

    it('TabPaneCurrentSelectionTest07', function () {
        let result = getTimeString(60000000001)
        expect(result).toBe('1m 1ns ');
    });

    it('TabPaneCurrentSelectionTest08', function () {
        let result = getTimeString(1000000001)
        expect(result).toBe('1s 1ns ');
    });

    it('TabPaneCurrentSelectionTest9', function () {
        let result = getTimeString(1000001)
        expect(result).toBe('1ms 1ns ');
    });

    it('TabPaneCurrentSelectionTest10', function () {
        let result = getTimeString(1001)
        expect(result).toBe('1μs 1ns ');
    });

    it('TabPaneCurrentSelectionTest11', function () {
        let result = getTimeString(101)
        expect(result).toBe('101ns ');
    });
})
