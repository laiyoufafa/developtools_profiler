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
import { queryVirtualMemory, queryVirtualMemoryData } from '../../database/SqlLite.js';
import { VirtualMemoryRender, VirtualMemoryStruct } from '../../database/ui-worker/ProcedureWorkerVirtualMemory.js';
import { renders } from '../../database/ui-worker/ProcedureWorker.js';
import { EmptyRender } from '../../database/ui-worker/ProcedureWorkerCPU.js';

export class SpVirtualMemChart {
  private trace: SpSystemTrace;

  constructor(trace: SpSystemTrace) {
    this.trace = trace;
  }

  async init() {
    let array = await queryVirtualMemory();
    if (array.length == 0) {
      return;
    }
    let folder = TraceRow.skeleton();
    folder.rowId = `VirtualMemory`;
    folder.index = 0;
    folder.rowType = TraceRow.ROW_TYPE_VIRTUAL_MEMORY_GROUP;
    folder.rowParentId = '';
    folder.folder = true;
    folder.name = `Virtual Memory`;
    folder.style.height = '40px';
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
    this.trace.rowsEL?.appendChild(folder);
    array.forEach((it, idx) => this.initVirtualMemoryRow(folder, it.id, it.name, idx));
  }

  initVirtualMemoryRow(folder: TraceRow<any>, id: number, name: string, idx: number) {
    let row = TraceRow.skeleton<VirtualMemoryStruct>();
    row.rowId = `${id}`;
    row.rowType = TraceRow.ROW_TYPE_VIRTUAL_MEMORY;
    row.rowParentId = folder.rowId;
    row.rowHidden = !folder.expansion;
    row.style.height = '40px';
    row.name = `${name.substring(16)}`;
    row.setAttribute('children', '');
    row.favoriteChangeHandler = this.trace.favoriteChangeHandler;
    row.selectChangeHandler = this.trace.selectChangeHandler;
    row.supplier = () =>
      queryVirtualMemoryData(id).then((res) => {
        let maxValue = Math.max(...res.map((it) => it.value || 0));
        for (let j = 0; j < res.length; j++) {
          res[j].maxValue = maxValue;
          if (j == res.length - 1) {
            res[j].duration = (TraceRow.range?.totalNS || 0) - (res[j].startTime || 0);
          } else {
            res[j].duration = (res[j + 1].startTime || 0) - (res[j].startTime || 0);
          }
          if (j > 0) {
            res[j].delta = (res[j].value || 0) - (res[j - 1].value || 0);
          } else {
            res[j].delta = 0;
          }
        }
        return res;
      });
    row.focusHandler = () => {
      this.trace?.displayTip(
        row,
        VirtualMemoryStruct.hoverStruct,
        `<span>value:${VirtualMemoryStruct.hoverStruct?.value}</span>`
      );
    };
    row.onThreadHandler = (useCache) => {
      let context = row.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
      row.canvasSave(context);
      (renders['virtual-memory-cell'] as VirtualMemoryRender).renderMainThread(
        {
          context: context,
          useCache: useCache,
          type: `virtual-memory-cell-${id}`,
        },
        row
      );
      row.canvasRestore(context);
    };
    folder.addChildTraceRow(row);
  }
}
