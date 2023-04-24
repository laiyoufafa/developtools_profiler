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

jest.mock('../../../../dist/trace/component/trace/base/TraceRow.js', () => {
    return {};
});

// @ts-ignore
import { ClockStruct } from '../../../../dist/trace/database/ui-worker/ProcedureWorkerClock.js';

describe('ProcedureWorkerClock Test', () => {
    it('ProcedureWorkerClock01', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');

        const data = {
            frame: {
                x: 20,
                y: 20,
                width: 100,
                height: 100,
            },
            filterId: 125,
            value: 556,
            startNS: 15454,
            dur: 14552,
            delta: 125,
        };
        expect(ClockStruct.draw(ctx!, data, 2)).toBeUndefined();
    });
});
