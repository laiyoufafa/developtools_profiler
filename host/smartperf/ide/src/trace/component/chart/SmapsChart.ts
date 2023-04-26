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

import { SpSystemTrace } from '../SpSystemTrace.js';
import {
    querySmapsData,
    querySmapsDataMax,
    querySmapsExits,
} from '../../database/SqlLite.js';
import { TraceRow } from '../trace/base/TraceRow.js';
import { BaseStruct } from '../../bean/BaseStruct.js';
import { renders } from '../../database/ui-worker/ProcedureWorker.js';
import {
    SmapsRender,
    SmapsStruct,
} from '../../database/ui-worker/ProcedureWorkerSmaps.js';
import { Utils } from '../trace/base/Utils.js';
import { EmptyRender } from '../../database/ui-worker/ProcedureWorkerCPU.js';

export class SmapsChart {
    private trace: SpSystemTrace;

    constructor(trace: SpSystemTrace) {
        this.trace = trace;
    }

    async init() {
        let result = await querySmapsExits();
        if (result.length <= 0) return;
        let smapsRow = this.initSmapsRow();
        let rowNameList: Array<string> = [
            'Dirty Size',
            'Swapped Size',
            'Resident Size',
        ];
        for (let rowName of rowNameList) {
            await this.initRows(smapsRow, rowName);
        }
    }

    private initSmapsRow = () => {
        let smapsRow = TraceRow.skeleton<any>();
        smapsRow.rowId = `smapsRow`;
        smapsRow.rowType = TraceRow.ROW_TYPE_SMAPS;
        smapsRow.rowParentId = '';
        smapsRow.style.height = '40px';
        smapsRow.folder = true;
        smapsRow.name = 'VM Tracker';
        smapsRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        smapsRow.selectChangeHandler = this.trace.selectChangeHandler;
        smapsRow.supplier = () =>
            new Promise<Array<any>>((resolve) => resolve([]));
        smapsRow.onThreadHandler = (useCache) => {
            smapsRow.canvasSave(this.trace.canvasPanelCtx!);
            if (smapsRow.expansion) {
                this.trace.canvasPanelCtx?.clearRect(
                    0,
                    0,
                    smapsRow.frame.width,
                    smapsRow.frame.height
                );
            } else {
                (renders['empty'] as EmptyRender).renderMainThread(
                    {
                        context: this.trace.canvasPanelCtx,
                        useCache: useCache,
                        type: ``,
                    },
                    smapsRow
                );
            }
            smapsRow.canvasRestore(this.trace.canvasPanelCtx!);
        };
        this.trace.rowsEL?.appendChild(smapsRow);
        return smapsRow;
    };

    private initRows = async (
        nodeRow: TraceRow<BaseStruct>,
        rowName: string
    ) => {
        let traceRow = TraceRow.skeleton<SmapsStruct>();
        traceRow.rowParentId = `smapsRow`;
        traceRow.rowHidden = !nodeRow.expansion;
        traceRow.rowId = rowName;
        traceRow.rowType = TraceRow.ROW_TYPE_SMAPS;
        traceRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        traceRow.selectChangeHandler = this.trace.selectChangeHandler;
        traceRow.style.height = '40px';
        traceRow.style.width = `100%`;
        traceRow.setAttribute('children', '');
        traceRow.name = rowName;
        let columnName = '';
        if (rowName == 'Dirty Size') {
            columnName = 'dirty';
        } else if (rowName == 'Swapped Size') {
            columnName = 'swapper';
        } else {
            columnName = 'resident_size';
        }
        traceRow.supplier = () => querySmapsData(columnName);
        let maxList = await querySmapsDataMax(columnName);
        let maxValue = maxList[0].max_value;
        traceRow.focusHandler = (ev) => {
            this.trace?.displayTip(
                traceRow,
                SmapsStruct.hoverSmapsStruct,
                `<span>${Utils.getBinaryByteWithUnit(
                    (SmapsStruct.hoverSmapsStruct?.value || 0) * 1024
                )}</span>`
            );
        };
        traceRow.onThreadHandler = (useCache) => {
            let context = traceRow.collect
                ? this.trace.canvasFavoritePanelCtx!
                : this.trace.canvasPanelCtx!;
            traceRow.canvasSave(context);
            (renders['smaps'] as SmapsRender).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `smaps`,
                    rowName: columnName,
                    maxValue: maxValue,
                },
                traceRow
            );
            traceRow.canvasRestore(context);
        };
        this.trace.rowsEL?.appendChild(traceRow);
    };
}
