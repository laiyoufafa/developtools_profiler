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
import { queryClockData, queryClockFrequency, queryClockState, queryScreenState } from '../../database/SqlLite.js';
import { TraceRow } from '../trace/base/TraceRow.js';
import { renders } from '../../database/ui-worker/ProcedureWorker.js';
import { info } from '../../../log/Log.js';
import { ClockRender, ClockStruct } from '../../database/ui-worker/ProcedureWorkerClock.js';
import { ColorUtils } from '../trace/base/ColorUtils.js';
import { EmptyRender } from '../../database/ui-worker/ProcedureWorkerCPU.js';
import { Utils } from '../trace/base/Utils.js';

export class SpClockChart {
  private trace: SpSystemTrace;

  constructor(trace: SpSystemTrace) {
    this.trace = trace;
  }

  async init() {
    let folder = await this.initFolder();
    await this.initData(folder);
  }

  async initData(folder: TraceRow<any>) {
    let clockStartTime = new Date().getTime();
    let clockList = await queryClockData();
    if (clockList.length == 0) {
      return;
    }
    info('clockList data size is: ', clockList!.length);
    this.trace.rowsEL?.appendChild(folder);
    ClockStruct.maxValue = clockList.map((item) => item.num).reduce((a, b) => Math.max(a, b));
    for (let i = 0; i < clockList.length; i++) {
      const it = clockList[i];
      let maxValue = 0;
      let traceRow = TraceRow.skeleton<ClockStruct>();
      let isState = it.name.endsWith(' State');
      let isScreenState = it.name.endsWith('ScreenState');
      traceRow.rowId = it.name;
      traceRow.rowType = TraceRow.ROW_TYPE_CLOCK;
      traceRow.rowParentId = folder.rowId;
      traceRow.style.height = '40px';
      traceRow.name = it.name;
      traceRow.rowHidden = !folder.expansion;
      traceRow.setAttribute('children', '');
      traceRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
      traceRow.selectChangeHandler = this.trace.selectChangeHandler;
      traceRow.supplier = () => {
        let promiseData = null;
        if (it.name.endsWith(' Frequency')) {
          promiseData = queryClockFrequency(it.srcname);
        } else if (isState) {
          promiseData = queryClockState(it.srcname);
        } else if (isScreenState) {
          promiseData = queryScreenState();
        }
        if (promiseData == null) {
          return new Promise<Array<any>>((resolve) => resolve([]));
        } else {
          return promiseData.then((res) => {
            for (let j = 0; j < res.length; j++) {
              if (!isState) {
                if (j == res.length - 1) {
                  res[j].dur = (TraceRow.range?.totalNS || 0) - (res[j].startNS || 0);
                } else {
                  res[j].dur = (res[j + 1].startNS || 0) - (res[j].startNS || 0);
                }
              }
              if ((res[j].value || 0) > maxValue) {
                maxValue = res[j].value || 0;
              }
              if (j > 0) {
                res[j].delta = (res[j].value || 0) - (res[j - 1].value || 0);
              } else {
                res[j].delta = 0;
              }
            }
            return res;
          });
        }
      };
      traceRow.focusHandler = (ev) => {
        this.trace?.displayTip(
          traceRow,
          ClockStruct.hoverClockStruct,
          `<span>${ColorUtils.formatNumberComma(ClockStruct.hoverClockStruct?.value!)}</span>`
        );
      };
      traceRow.onThreadHandler = (useCache) => {
        let context = traceRow.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
        traceRow.canvasSave(context);
        (renders['clock'] as ClockRender).renderMainThread(
          {
            context: context,
            useCache: useCache,
            type: it.name,
            maxValue: maxValue === 0 ? 1 : maxValue,
            index: i,
            maxName:
              isState || isScreenState ? maxValue.toString() : Utils.getFrequencyWithUnit(maxValue / 1000).maxFreqName,
          },
          traceRow
        );
        traceRow.canvasRestore(context);
      };
      this.trace.rowsEL?.appendChild(traceRow);
    }
    let durTime = new Date().getTime() - clockStartTime;
    info('The time to load the ClockData is: ', durTime);
  }

  async initFolder(): Promise<TraceRow<any>> {
    let folder = TraceRow.skeleton();
    folder.rowId = `Clocks`;
    folder.index = 0;
    folder.rowType = TraceRow.ROW_TYPE_CLOCK_GROUP;
    folder.rowParentId = '';
    folder.style.height = '40px';
    folder.folder = true;
    folder.name = `Clocks`; /* & I/O Latency */
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
