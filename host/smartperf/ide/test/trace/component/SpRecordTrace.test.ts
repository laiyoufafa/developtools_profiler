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
import {SpRecordTrace} from "../../../dist/trace/component/SpRecordTrace.js"

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe('SpRecordTrace Test', () => {
    it('SpRecordTraceTest01', function () {
        expect(SpRecordTrace.initHtml).not.toBe('')
    });

    it('SpRecordTraceTest02', function () {
        SpRecordTrace.patentNode=jest.fn(()=>true);
        expect(SpRecordTrace.initElements).toBeUndefined()
    });

    it('SpRecordTraceTest04', function () {
        let traceEvents = SpRecordTrace.createTraceEvents = ['Scheduling details', 'CPU Frequency and idle states',
            'High frequency memory', 'Advanced ftrace config', 'Syscalls' , 'Board voltages & frequency'];
        expect(traceEvents[0].indexOf('binder/binder_lock')).toBe(-1)
    });
})
