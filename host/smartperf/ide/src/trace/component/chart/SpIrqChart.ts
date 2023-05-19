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
import { TraceRow } from '../trace/base/TraceRow.js';
import { queryIrqData, queryIrqList } from '../../database/SqlLite.js';
import { info } from '../../../log/Log.js';
import { renders } from '../../database/ui-worker/ProcedureWorker.js';
import { EmptyRender } from '../../database/ui-worker/ProcedureWorkerCPU.js';
import { IrqRender, IrqStruct } from '../../database/ui-worker/ProcedureWorkerIrq.js';

export class SpIrqChart {
  private trace: SpSystemTrace;

  constructor(trace: SpSystemTrace) {
    this.trace = trace;
  }

  async init() {
    let folder = await this.initFolder();
    await this.initData(folder);
  }

  async initData(folder: TraceRow<any>) {
    let irqStartTime = new Date().getTime();
    let irqList = await queryIrqList();
    if (irqList.length == 0) {
      return;
    }
    info('irqList data size is: ', irqList!.length);
    this.trace.rowsEL?.appendChild(folder);
    for (let i = 0; i < irqList.length; i++) {
      const it = irqList[i];
      let traceRow = TraceRow.skeleton<IrqStruct>();
      traceRow.rowId = it.name + it.cpu;
      traceRow.rowType = TraceRow.ROW_TYPE_IRQ;
      traceRow.rowParentId = folder.rowId;
      traceRow.style.height = '40px';
      traceRow.name = `${it.name} Cpu ${it.cpu}`;
      traceRow.rowHidden = !folder.expansion;
      traceRow.setAttribute('children', '');
      traceRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
      traceRow.selectChangeHandler = this.trace.selectChangeHandler;
      traceRow.supplier = () => queryIrqData(it.cpu, it.name);
      traceRow.focusHandler = (ev) => {
        this.trace?.displayTip(
          traceRow,
          IrqStruct.hoverIrqStruct,
          `<span>${IrqStruct.hoverIrqStruct?.name || ''}</span>`
        );
      };
      traceRow.onThreadHandler = (useCache) => {
        let context = traceRow.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
        traceRow.canvasSave(context);
        (renders['irq'] as IrqRender).renderMainThread(
          {
            context: context,
            useCache: useCache,
            type: it.name,
            index: i,
          },
          traceRow
        );
        traceRow.canvasRestore(context);
      };
      folder.addChildTraceRow(traceRow);
    }
    let durTime = new Date().getTime() - irqStartTime;
    info('The time to load the ClockData is: ', durTime);
  }

  async initFolder(): Promise<TraceRow<any>> {
    let folder = TraceRow.skeleton();
    folder.rowId = `Irqs`;
    folder.index = 0;
    folder.rowType = TraceRow.ROW_TYPE_IRQ_GROUP;
    folder.rowParentId = '';
    folder.style.height = '40px';
    folder.folder = true;
    folder.name = `Irqs`; /* & I/O Latency */
    folder.favoriteChangeHandler = this.trace.favoriteChangeHandler;
    folder.selectChangeHandler = this.trace.selectChangeHandler;
    folder.supplier = () => new Promise<Array<any>>((resolve) => resolve([]));
    folder.onThreadHandler = (useCache) => {
      folder.canvasSave(this.trace.canvasPanelCtx!);
      if (folder.expansion) {
        this.trace.canvasPanelCtx?.clearRect(0, 0, folder.frame.width, folder.frame.height);
      } else {
        (renders['empty'] as EmptyRender).renderMainThread(
          {
            context: this.trace.canvasPanelCtx,
            useCache: useCache,
            type: ``,
          },
          folder
        );
      }
      folder.canvasRestore(this.trace.canvasPanelCtx!);
    };
    return folder;
  }
}
