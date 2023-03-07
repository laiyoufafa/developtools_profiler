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

import {SpSystemTrace} from "../SpSystemTrace.js";
import {TraceRow} from "../trace/base/TraceRow.js";
import {procedurePool} from "../../database/Procedure.js";
import {
    getDiskIOLatencyChartDataByProcess,
    getDiskIOProcess,
    getFileSysChartDataByType, getFileSysVirtualMemoryChartData,
    hasFileSysData
} from "../../database/SqlLite.js";
import {FileSysChartStruct, FileSystemRender} from "../../database/ui-worker/ProcedureWorkerFileSystem.js";
import { ColorUtils } from "../trace/base/ColorUtils.js";
import {Utils} from "../trace/base/Utils.js";
import {renders} from "../../database/ui-worker/ProcedureWorker.js";
import {EmptyRender} from "../../database/ui-worker/ProcedureWorkerCPU.js";

export class SpFileSystemChart {
    private trace: SpSystemTrace;

    constructor(trace: SpSystemTrace) {
        this.trace = trace;
    }

    async init() {
        let sys = await hasFileSysData();
        if(sys.length > 0){
            let fsCount = sys[0]['fsCount'] ?? 0;
            let vmCount = sys[0]['vmCount'] ?? 0;
            let ioCount = sys[0]['ioCount'] ?? 0;
            if(sys && sys.length > 0 && (fsCount > 0 || vmCount > 0 || ioCount > 0)){
                let folder = await this.initFolder();
                await this.initFileCallchain()
                if(fsCount > 0){
                    await this.initLogicalRead(folder);
                    await this.initLogicalWrite(folder);
                }
                if(vmCount > 0){
                    await this.initVirtualMemoryTrace(folder);
                }
                if(ioCount > 0){
                    await this.initDiskIOLatency(folder)
                    await this.initProcessDiskIOLatency(folder)
                }
            }
        }
    }

    async initFileCallchain(){
        return new Promise<any>((resolve, reject) => {
            procedurePool.submitWithName("logic0","fileSystem-init",SpSystemTrace.DATA_DICT,undefined,(res:any)=>{
                resolve(res)
            })
        })
    }

    async initFolder():Promise<TraceRow<any>>{
        let folder = TraceRow.skeleton();
        folder.rowId = `FileSystem`;
        folder.index = 0;
        folder.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM_GROUP
        folder.rowParentId = '';
        folder.style.height = '40px'
        folder.folder = true;
        folder.name = `EBPF` ;/* & I/O Latency */
        folder.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        folder.selectChangeHandler = this.trace.selectChangeHandler;
        folder.supplier = () => new Promise<Array<any>>((resolve) => resolve([]));
        folder.onThreadHandler = (useCache) => {
            folder.canvasSave(this.trace.canvasPanelCtx!);
            if(folder.expansion){
                this.trace.canvasPanelCtx?.clearRect(0, 0, folder.frame.width, folder.frame.height);
            } else {
                (renders["empty"] as EmptyRender).renderMainThread(
                    {
                        context: this.trace.canvasPanelCtx,
                        useCache: useCache,
                        type: ``,
                    },
                    folder,
                );
            }
            folder.canvasRestore(this.trace.canvasPanelCtx!);
        }
        this.trace.rowsEL?.appendChild(folder)
        return folder;
    }

    async initLogicalRead(folder:TraceRow<any>){
        let row = TraceRow.skeleton<FileSysChartStruct>();
        row.rowId = `FileSystemLogicalRead`;
        row.index = 1;
        row.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM
        row.rowParentId = folder.rowId;
        row.rowHidden = !folder.expansion
        row.style.height = '40px'
        row.setAttribute('children', '');
        row.name = `FileSystem Logical Read`;
        row.supplier = () => getFileSysChartDataByType(2);
        row.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        row.selectChangeHandler = this.trace.selectChangeHandler;
        row.focusHandler = ()=> this.focusHandler(row);
        row.onThreadHandler = (useCache) => {
            let context = row.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
            row.canvasSave(context);
            (renders[TraceRow.ROW_TYPE_FILE_SYSTEM] as FileSystemRender).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `${TraceRow.ROW_TYPE_FILE_SYSTEM}-logical-read`,
                    chartColor:ColorUtils.MD_PALETTE[0]
                },
                row
            );
            row.canvasRestore(context);
        }
        this.trace.rowsEL?.appendChild(row)
    }

    async initLogicalWrite(folder:TraceRow<any>){
        let row = TraceRow.skeleton<FileSysChartStruct>();
        row.rowId = `FileSystemLogicalWrite`;
        row.index = 2;
        row.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM
        row.rowParentId = folder.rowId;
        row.rowHidden = !folder.expansion;
        row.style.height = '40px'
        row.setAttribute('children', '');
        row.name = `FileSystem Logical Write`;
        row.supplier = () => getFileSysChartDataByType(3);
        row.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        row.selectChangeHandler = this.trace.selectChangeHandler;
        row.focusHandler = ()=> this.focusHandler(row);
        row.onThreadHandler = (useCache) => {
            let context = row.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
            row.canvasSave(context);
            (renders[TraceRow.ROW_TYPE_FILE_SYSTEM] as FileSystemRender).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `${TraceRow.ROW_TYPE_FILE_SYSTEM}-logical-write`,
                    chartColor:ColorUtils.MD_PALETTE[8]
                },
                row
            );
            row.canvasRestore(context);
        }
        this.trace.rowsEL?.appendChild(row)
    }

    async initDiskIOLatency(folder:TraceRow<any>){
        let row = TraceRow.skeleton<FileSysChartStruct>();
        row.rowId = `FileSystemDiskIOLatency`;
        row.index = 4;
        row.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM
        row.rowParentId = folder.rowId;
        row.rowHidden = !folder.expansion
        row.style.height = '40px'
        row.style.width = `100%`;
        row.setAttribute('children', '');
        row.name = `Disk I/O Latency`;
        row.supplier = () => getDiskIOLatencyChartDataByProcess(true,0,[1,2,3,4]);
        row.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        row.selectChangeHandler = this.trace.selectChangeHandler;
        row.focusHandler =  ()=> this.focusHandler(row);
        row.onThreadHandler = (useCache) => {
            let context = row.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
            row.canvasSave(context);
            (renders[TraceRow.ROW_TYPE_FILE_SYSTEM] as FileSystemRender).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `${TraceRow.ROW_TYPE_FILE_SYSTEM}-disk-io`,
                    chartColor:ColorUtils.MD_PALETTE[0]
                },
                row
            );
            row.canvasRestore(context);
        }
        this.trace.rowsEL?.appendChild(row)
    }

    async initProcessDiskIOLatency(folder:TraceRow<any>){
        let processes = await getDiskIOProcess() || [];
        for (let i = 0,len = processes.length; i < len; i++) {
            let process = processes[i];
            let rowRead = TraceRow.skeleton<FileSysChartStruct>();
            rowRead.index = 5 + 2 * i;
            rowRead.rowId = `FileSystemDiskIOLatency-read-${process['ipid']}`;
            rowRead.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM
            rowRead.rowParentId = folder.rowId;
            rowRead.rowHidden = !folder.expansion
            rowRead.style.height = '40px'
            rowRead.style.width = `100%`;
            rowRead.setAttribute('children', '');
            rowRead.name = `${process['name'] ?? 'Process'}(${process['pid']}) Max Read Latency`;
            rowRead.supplier = () => getDiskIOLatencyChartDataByProcess(false,process['ipid'],[1,3]);
            rowRead.favoriteChangeHandler = this.trace.favoriteChangeHandler;
            rowRead.selectChangeHandler = this.trace.selectChangeHandler;
            rowRead.focusHandler =  ()=> this.focusHandler(rowRead);
            rowRead.onThreadHandler = (useCache) => {
                let context = rowRead.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                rowRead.canvasSave(context);
                (renders[TraceRow.ROW_TYPE_FILE_SYSTEM] as FileSystemRender).renderMainThread(
                    {
                        context: context,
                        useCache: useCache,
                        type: `${TraceRow.ROW_TYPE_FILE_SYSTEM}-disk-io-process-read-${process['pid']}`,
                        chartColor:ColorUtils.MD_PALETTE[0]
                    },
                    rowRead
                );
                rowRead.canvasRestore(context);
            }
            this.trace.rowsEL?.appendChild(rowRead)
            let rowWrite = TraceRow.skeleton<FileSysChartStruct>();
            rowWrite.index = 5 + 2 * i + 1;
            rowWrite.rowId = `FileSystemDiskIOLatency-write-${process['ipid']}`;
            rowWrite.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM
            rowWrite.rowParentId = folder.rowId;
            rowWrite.rowHidden = !folder.expansion
            rowWrite.style.height = '40px'
            rowWrite.style.width = `100%`;
            rowWrite.setAttribute('children', '');
            rowWrite.name = `${process['name'] ?? 'Process'}(${process['pid']}) Max Write Latency`;
            rowWrite.supplier = () => getDiskIOLatencyChartDataByProcess(false,process['ipid'],[2,4]);
            rowWrite.favoriteChangeHandler = this.trace.favoriteChangeHandler;
            rowWrite.selectChangeHandler = this.trace.selectChangeHandler;
            rowWrite.focusHandler =  ()=> this.focusHandler(rowWrite);
            rowWrite.onThreadHandler = (useCache) => {
                let context = rowWrite.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                rowWrite.canvasSave(context);
                (renders[TraceRow.ROW_TYPE_FILE_SYSTEM] as FileSystemRender).renderMainThread(
                    {
                        context: context,
                        useCache: useCache,
                        type: `${TraceRow.ROW_TYPE_FILE_SYSTEM}-disk-io-process-write-${process['pid']}`,
                        chartColor:ColorUtils.MD_PALETTE[8]
                    },
                    rowWrite
                );
                rowWrite.canvasRestore(context);
            }
            this.trace.rowsEL?.appendChild(rowWrite)
        }
    }

    async initVirtualMemoryTrace(folder:TraceRow<any>){
        let row = TraceRow.skeleton<FileSysChartStruct>();
        row.rowId = `FileSystemVirtualMemory`;
        row.index = 3;
        row.rowType = TraceRow.ROW_TYPE_FILE_SYSTEM
        row.rowParentId = folder.rowId;
        row.rowHidden = !folder.expansion
        row.rangeSelect = true;
        row.style.height = '40px'
        row.style.width = `100%`;
        row.setAttribute('children', '');
        row.name = `Page Fault Trace`;
        row.supplier = () => getFileSysVirtualMemoryChartData();
        row.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        row.selectChangeHandler = this.trace.selectChangeHandler;
        row.focusHandler =  ()=> this.focusHandler(row);
        row.onThreadHandler = (useCache) => {
            let context = row.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
            row.canvasSave(context);
            (renders[TraceRow.ROW_TYPE_FILE_SYSTEM] as FileSystemRender).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `${TraceRow.ROW_TYPE_FILE_SYSTEM}-virtual-memory`,
                    chartColor:ColorUtils.MD_PALETTE[0]
                },
                row
            );
            row.canvasRestore(context);
        }
        this.trace.rowsEL?.appendChild(row)
    }

    focusHandler(row:TraceRow<FileSysChartStruct>){
        let num = 0;
        let tip = '';
        if (FileSysChartStruct.hoverFileSysStruct) {
            num = FileSysChartStruct.hoverFileSysStruct.size ?? 0;
            let group10Ms = FileSysChartStruct.hoverFileSysStruct.group10Ms ?? false;
            if (row.rowId!.startsWith("FileSystemDiskIOLatency")) {
                if (num > 0) {
                    let tipStr = Utils.getProbablyTime(num);
                    if (group10Ms) {
                        tip = `<span>${tipStr} (10.00ms)</span>`
                    } else {
                        tip = `<span>${tipStr}</span>`
                    }
                }
            } else {
                if (num > 0) {
                    if (group10Ms) {
                        tip = `<span>${num} (10.00ms)</span>`
                    } else {
                        tip = `<span>${num}</span>`
                    }
                }
            }
        }
        this.trace?.displayTip(row,FileSysChartStruct.hoverFileSysStruct,tip)
    }
}