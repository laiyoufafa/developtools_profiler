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

import { TraceRow } from '../trace/base/TraceRow.js';
import { renders } from '../../database/ui-worker/ProcedureWorker.js';
import { JankRender, JankStruct } from '../../database/ui-worker/ProcedureWorkerJank.js';
import { SpSystemTrace } from '../SpSystemTrace.js';
import { queryActualFrameDate, queryExpectedFrameDate, queryFrameTimeData } from '../../database/SqlLite.js';
import { JanksStruct } from '../../bean/JanksStruct.js';
import { ns2xByTimeShaft } from '../../database/ui-worker/ProcedureWorkerCommon.js';

export class SpFrameTimeChart {
  private trace: SpSystemTrace;

  constructor(trace: SpSystemTrace) {
    this.trace = trace;
  }

  async init() {
    let frameTimeData = await queryFrameTimeData();
    if (frameTimeData.length > 0) {
      let frameTimeLineRow = await this.initFrameTimeLine();
      await this.initExpectedChart(frameTimeLineRow);
      await this.initActualChart(frameTimeLineRow);
    }
  }

  async initFrameTimeLine() {
    let frameTimeLineRow = TraceRow.skeleton<any>();
    frameTimeLineRow.rowId = `frameTime`;
    frameTimeLineRow.rowType = TraceRow.ROW_TYPE_JANK;
    frameTimeLineRow.rowParentId = ``;
    frameTimeLineRow.style.width = `100%`;
    frameTimeLineRow.style.height = `40px`;
    frameTimeLineRow.folder = true;
    frameTimeLineRow.name = `FrameTimeline`;
    frameTimeLineRow.setAttribute('children', '');
    frameTimeLineRow.supplier = () =>
      new Promise((resolve) => {
        resolve([]);
      });
    frameTimeLineRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
    frameTimeLineRow.selectChangeHandler = this.trace.selectChangeHandler;
    frameTimeLineRow.onThreadHandler = (useCache) => {
      let context = frameTimeLineRow!.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
      frameTimeLineRow!.canvasSave(context);
      (renders['jank'] as JankRender).renderMainThread(
        {
          context: context,
          useCache: useCache,
          type: `expected_frame_timeline_slice`,
        },
        frameTimeLineRow!
      );
      frameTimeLineRow!.canvasRestore(context);
    };
    this.trace.rowsEL?.appendChild(frameTimeLineRow);
    return frameTimeLineRow;
  }

  async initExpectedChart(frameTimeLineRow: TraceRow<JanksStruct>) {
    let frameExpectedData = await queryExpectedFrameDate();
    if (frameExpectedData.length > 0) {
      let isIntersect = (a: JanksStruct, b: JanksStruct) =>
        Math.max(a.ts! + a.dur!, b.ts! + b.dur!) - Math.min(a.ts!, b.ts!) < a.dur! + b.dur!;
      let depthArray: any = [];
      for (let i = 0; i < frameExpectedData.length; i++) {
        let it = frameExpectedData[i];
        if (!it.dur || it.dur < 0) {
          continue;
        }
        if (depthArray.length == 0) {
          it.depth = 0;
          depthArray[0] = it;
        } else {
          let index = 0;
          let isContinue = true;
          while (isContinue) {
            if (isIntersect(depthArray[index], it)) {
              if (depthArray[index + 1] == undefined || !depthArray[index + 1]) {
                it.depth = index + 1;
                depthArray[index + 1] = it;
                isContinue = false;
              }
            } else {
              it.depth = index;
              depthArray[index] = it;
              isContinue = false;
            }
            index++;
          }
        }
      }
    }
    let max = Math.max(...frameExpectedData.map((it) => it.depth || 0)) + 1;
    let maxHeight = max * 20;
    let expectedTimeLineRow = TraceRow.skeleton<any>();
    expectedTimeLineRow.rowId = `expected frameTime`;
    expectedTimeLineRow.rowType = TraceRow.ROW_TYPE_JANK;
    expectedTimeLineRow.rowHidden = !frameTimeLineRow.expansion;
    expectedTimeLineRow.rowParentId = `frameTime`;
    expectedTimeLineRow.style.width = `100%`;
    expectedTimeLineRow.style.height = `40px`;
    expectedTimeLineRow.style.height = `${maxHeight}px`;
    expectedTimeLineRow.name = `Expected Timeline`;
    expectedTimeLineRow.setAttribute('height', `${maxHeight}`);
    expectedTimeLineRow.setAttribute('children', '');
    expectedTimeLineRow.supplier = () =>
      new Promise((resolve) => {
        resolve(frameExpectedData);
      });
    expectedTimeLineRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
    expectedTimeLineRow.selectChangeHandler = this.trace.selectChangeHandler;
    expectedTimeLineRow.onThreadHandler = (useCache) => {
      let context = expectedTimeLineRow!.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
      expectedTimeLineRow!.canvasSave(context);
      (renders['jank'] as JankRender).renderMainThread(
        {
          context: context,
          useCache: useCache,
          type: `expected_frame_timeline_slice`,
        },
        expectedTimeLineRow!
      );
      expectedTimeLineRow!.canvasRestore(context);
    };
    this.trace.rowsEL?.appendChild(expectedTimeLineRow);
  }

  async initActualChart(frameTimeLineRow: TraceRow<any>) {
    let frameActualData = await queryActualFrameDate();
    if (frameActualData.length > 0) {
      let isIntersect = (a: JanksStruct, b: JanksStruct) =>
        Math.max(a.ts! + a.dur!, b.ts! + b.dur!) - Math.min(a.ts!, b.ts!) < a.dur! + b.dur!;
      let depthArray: any = [];
      for (let i = 0; i < frameActualData.length; i++) {
        let it = frameActualData[i];
        if (!it.dur || it.dur < 0) {
          continue;
        }
        if (depthArray.length == 0) {
          it.depth = 0;
          depthArray[0] = it;
        } else {
          let index = 0;
          let isContinue = true;
          while (isContinue) {
            if (isIntersect(depthArray[index], it)) {
              if (depthArray[index + 1] == undefined || !depthArray[index + 1]) {
                it.depth = index + 1;
                depthArray[index + 1] = it;
                isContinue = false;
              }
            } else {
              it.depth = index;
              depthArray[index] = it;
              isContinue = false;
            }
            index++;
          }
        }
      }
    }

    let max = Math.max(...frameActualData.map((it) => it.depth || 0)) + 1;
    let maxHeight = max * 20;
    let actualTimeLineRow = TraceRow.skeleton<any>();
    actualTimeLineRow.rowId = `actual frameTime`;
    actualTimeLineRow.rowType = TraceRow.ROW_TYPE_JANK;
    actualTimeLineRow.rowHidden = !frameTimeLineRow.expansion;
    actualTimeLineRow.rowParentId = `frameTime`;
    actualTimeLineRow.style.width = `100%`;
    actualTimeLineRow.style.height = `${maxHeight}px`;
    actualTimeLineRow.name = `Actual Timeline`;
    actualTimeLineRow.setAttribute('height', `${maxHeight}`);
    actualTimeLineRow.setAttribute('children', '');
    actualTimeLineRow.supplier = () =>
      new Promise((resolve) => {
        resolve(frameActualData);
      });
    actualTimeLineRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
    actualTimeLineRow.selectChangeHandler = this.trace.selectChangeHandler;
    actualTimeLineRow.onThreadHandler = (useCache) => {
      let context = actualTimeLineRow!.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
      actualTimeLineRow!.canvasSave(context);
      (renders['jank'] as JankRender).renderMainThread(
        {
          context: context,
          useCache: useCache,
          type: `expected_frame_timeline_slice`,
        },
        actualTimeLineRow!
      );
      actualTimeLineRow!.canvasRestore(context);
    };
    this.trace.rowsEL?.appendChild(actualTimeLineRow);
    frameTimeLineRow.addEventListener('expansion-change', (e: any) => {
      if (e.detail.expansion) {
        if (JankStruct!.selectJankStruct) {
          JankStruct.delJankLineFlag = true;
        } else {
          JankStruct.delJankLineFlag = false;
        }
        this.trace.linkNodes.forEach((linkNode) => {
          JankStruct.selectJankStructList?.forEach((dat: any) => {
            if (e.detail.rowId == dat.pid) {
              JankStruct.selectJankStruct = dat;
              JankStruct.hoverJankStruct = dat;
            }
          });
          if (linkNode[0].rowEL.rowId == e.detail.rowId) {
            linkNode[0] = {
              x: ns2xByTimeShaft(linkNode[0].ns, this.trace.timerShaftEL!),
              y: actualTimeLineRow!.translateY! + linkNode[0].offsetY * 2,
              offsetY: linkNode[0].offsetY * 2,
              ns: linkNode[0].ns,
              rowEL: actualTimeLineRow!,
              isRight: true,
            };
          } else if (linkNode[1].rowEL.rowId == e.detail.rowId) {
            linkNode[1] = {
              x: ns2xByTimeShaft(linkNode[1].ns, this.trace.timerShaftEL!),
              y: actualTimeLineRow!.translateY! + linkNode[1].offsetY * 2,
              offsetY: linkNode[1].offsetY * 2,
              ns: linkNode[1].ns,
              rowEL: actualTimeLineRow!,
              isRight: true,
            };
          }
        });
        this.trace.refreshCanvas(true);
      } else {
        JankStruct.delJankLineFlag = false;
        if (JankStruct!.selectJankStruct) {
          JankStruct.selectJankStructList?.push(<JankStruct>JankStruct!.selectJankStruct);
        }
        this.trace.linkNodes.forEach((linkNode) => {
          if (linkNode[0].rowEL.rowParentId == e.detail.rowId) {
            linkNode[0] = {
              x: ns2xByTimeShaft(linkNode[0].ns, this.trace.timerShaftEL!),
              y: frameTimeLineRow!.translateY! + linkNode[0].offsetY / 2,
              offsetY: linkNode[0].offsetY / 2,
              ns: linkNode[0].ns,
              rowEL: frameTimeLineRow,
              isRight: true,
            };
          } else if (linkNode[1].rowEL.rowParentId == e.detail.rowId) {
            linkNode[1] = {
              x: ns2xByTimeShaft(linkNode[1].ns, this.trace.timerShaftEL!),
              y: frameTimeLineRow!.translateY! + linkNode[1].offsetY / 2,
              offsetY: linkNode[1].offsetY / 2,
              ns: linkNode[1].ns,
              rowEL: frameTimeLineRow,
              isRight: true,
            };
          }
        });
        this.trace.refreshCanvas(true);
      }
    });
  }
}
