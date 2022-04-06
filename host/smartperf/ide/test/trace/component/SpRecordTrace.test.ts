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

describe('SpRecordTrace Test', () => {

    let spRecordTrace = new SpRecordTrace();

    it('SpRecordTraceTest01', function () {
        expect(spRecordTrace.initHtml()).not.toBe('')
    });
    it('SpRecordTraceTest02', function () {
        expect(spRecordTrace.initElements()).toBeUndefined()
    });
    it('SpRecordTraceTest03', function () {
        let toReturnWith = spRecordTrace.createFpsPluginConfig();
        expect(toReturnWith.sampleInterval).toBe(1000);
    });
    it('SpRecordTraceTest04', function () {
        let traceEvents = spRecordTrace.createTraceEvents(['Scheduling details', 'CPU Frequency and idle states',
            'High frequency memory', 'Advanced ftrace config', 'Syscalls']);
        expect(traceEvents[0].indexOf('binder/binder_lock')).toBe(-1)
    });
})
