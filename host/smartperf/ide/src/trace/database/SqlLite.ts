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

import './sql-wasm.js'
import {CpuStruct} from "../bean/CpuStruct.js";
import {CpuFreqStruct} from "../bean/CpuFreqStruct.js";
import {ThreadStruct} from "../bean/ThreadStruct.js";
import {ProcessMemStruct} from "../bean/ProcessMemStruct.js";
import {Counter, Fps, SelectionData} from "../bean/BoxSelection.js";
import {FuncStruct} from "../bean/FuncStruct.js";
import {WakeUpTimeBean} from "../bean/WakeUpTimeBean.js";
import {WakeupBean} from "../bean/WakeupBean.js";
import {BinderArgBean} from "../bean/BinderArgBean.js";
import {FpsStruct} from "../bean/FpsStruct.js";
import {HeapBean} from "../bean/HeapBean.js";
import {SPT, SPTChild, SptSlice, StateProcessThread, ThreadProcess, ThreadState} from "../bean/StateProcessThread.js";
import {CpuUsage, Freq} from "../bean/CpuUsage.js";
import {HeapStruct} from "../bean/HeapStruct.js";
import {HeapTreeDataBean} from "../bean/HeapTreeDataBean.js";
import {
    NativeEvent,
    NativeEventHeap,
    NativeHookMalloc,
    NativeHookProcess,
    NativeHookSampleQueryInfo,
    NativeHookStatistics
} from "../bean/NativeHook.js";
import {
    LiveProcess,
    ProcessHistory,
    SystemCpuSummary,
    SystemDiskIOSummary,
    SystemNetworkSummary
} from "../bean/AbilityMonitor.js";
import {NetworkAbilityMonitorStruct} from "../bean/NetworkAbilityMonitorStruct.js";
import {DiskAbilityMonitorStruct} from "../bean/DiskAbilityMonitorStruct.js";
import {MemoryAbilityMonitorStruct} from "../bean/MemoryAbilityMonitorStruct.js";
import {CpuAbilityMonitorStruct} from "../bean/CpuAbilityMonitorStruct.js";
import {
    PerfCall,
    PerfCallChain,
    PerfCmdLine,
    PerfFile,
    PerfSample,
    PerfStack,
    PerfThread
} from "../bean/PerfProfile.js";
import {SearchFuncBean} from "../bean/SearchFuncBean.js";
import {info} from "../../log/Log.js";

class DbThread extends Worker {
    busy: boolean = false;
    isCancelled: boolean = false;
    id: number = -1;
    taskMap: any = {};
    cacheArray: Array<any> = [];

    uuid(): string {
        // @ts-ignore
        return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11)
            .replace(/[018]/g, (c: any) => (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16));
    }

    queryFunc(name: string, sql: string, args: any, handler: Function, action: string | null) {
        this.busy = true;
        let id = this.uuid();
        this.taskMap[id] = handler
        let msg = {
            id: id,
            name: name,
            action: action || "exec",
            sql: sql,
            params: args,
        }
        this.postMessage(msg);
    }

    dbOpen = async (): Promise<{ status: boolean, msg: string, buffer: ArrayBuffer }> => {
        return new Promise<any>((resolve, reject) => {
            let id = this.uuid();
            this.taskMap[id] = (res: any) => {
                if (res.init) {
                    resolve({status: res.init, msg: res.msg, buffer: res.buffer});
                } else {
                    resolve({status: res.init, msg: res.msg});
                }
            }
            this.postMessage({
                id: id,
                action: "open",
                buffer: DbPool.sharedBuffer!, /*Optional. An ArrayBuffer representing an SQLite Database file*/
            }, [DbPool.sharedBuffer!]);
        })
    }
}

export class DbPool {
    static sharedBuffer: ArrayBuffer | null = null;
    maxThreadNumber: number = 0;
    works: Array<DbThread> = [];
    progress: Function | undefined | null;
    num = Math.floor(Math.random() * 10 + 1) + 20;
    init = async (type: string, threadBuild: (() => DbThread) | undefined = undefined) => { // wasm | server | sqlite
        await this.close();
        if (type === "wasm") {
            this.maxThreadNumber = 1;
        } else if (type === "server") {
            this.maxThreadNumber = 1;
        } else if (type === "sqlite") {
            this.maxThreadNumber = 1;
        } else if (type === "duck") {
            this.maxThreadNumber = 1;
        }
        for (let i = 0; i < this.maxThreadNumber; i++) {
            let thread: DbThread
            if (threadBuild) {
                thread = threadBuild()
            } else {
                if (type === "wasm") {
                    thread = new DbThread("trace/database/TraceWorker.js")
                } else if (type === "server") {
                    thread = new DbThread("trace/database/SqlLiteWorker.js")
                } else if (type === "sqlite") {
                    thread = new DbThread("trace/database/SqlLiteWorker.js")
                }
            }
            thread!.onmessage = (event: MessageEvent) => {
                thread.busy = false;
                if (Reflect.has(thread.taskMap, event.data.id)) {
                    if (event.data.results) {
                        let fun = thread.taskMap[event.data.id];
                        if (fun) {
                            fun(event.data.results);
                        }
                        Reflect.deleteProperty(thread.taskMap, event.data.id);
                    } else if (Reflect.has(event.data, 'ready')) {
                        this.progress!("database opened", this.num + event.data.index)
                    } else if (Reflect.has(event.data, 'init')) {
                        this.progress!("database ready", 40)
                        let fun = thread.taskMap[event.data.id];
                        if (fun) {
                            fun(event.data)
                        }
                        Reflect.deleteProperty(thread.taskMap, event.data.id)
                    } else {
                        let fun = thread.taskMap[event.data.id];
                        if (fun) {
                            fun([])
                        }
                        Reflect.deleteProperty(thread.taskMap, event.data.id)
                    }

                }
            }
            thread!.onmessageerror = e => {
            }
            thread!.onerror = e => {
            }
            thread!.id = i;
            thread!.busy = false;
            this.works?.push(thread!);
        }
    }

    initServer = async (url: string, progress: Function) => {
        this.progress = progress;
        progress("database loaded", 15)
        let buf = await fetch(url).then(res => res.arrayBuffer());
        DbPool.sharedBuffer = buf;
        progress("open database", 20)
        for (let i = 0; i < this.works.length; i++) {
            let thread = this.works[i];
            let {status, msg} = await thread.dbOpen()
            if (!status) {
                return {status, msg}
            }
        }
        return {status: true, msg: "ok"};
    }
    initSqlite = async (buf: ArrayBuffer, progress: Function) => {
        this.progress = progress;
        progress("database loaded", 15)
        DbPool.sharedBuffer = buf;
        progress("parse database", 20)
        for (let i = 0; i < this.works.length; i++) {
            let thread = this.works[i];
            let {status, msg, buffer} = await thread.dbOpen()
            if (!status) {
                return {status, msg}
            } else {
                DbPool.sharedBuffer = buffer;
            }
        }
        return {status: true, msg: "ok"};
    }

    close = async () => {
        for (let i = 0; i < this.works.length; i++) {
            let thread = this.works[i];
            thread.terminate();
        }
        this.works.length = 0;
    }

    submit(name: string, sql: string, args: any, handler: Function, action: string | null) {
        let noBusyThreads = this.works.filter(it => !it.busy);
        let thread: DbThread
        if (noBusyThreads.length > 0) { //取第一个空闲的线程进行任务
            thread = noBusyThreads[0];
            thread.queryFunc(name, sql, args, handler, action)
        } else { // 随机插入一个线程中
            thread = this.works[Math.floor(Math.random() * this.works.length)]
            thread.queryFunc(name, sql, args, handler, action)
        }
    }
}

export const threadPool = new DbPool()

export function query<T extends any>(name: string, sql: string, args: any = null, action: string | null = null): Promise<Array<T>> {
    let a = new Date().getTime();
    return new Promise<Array<T>>((resolve, reject) => {
        threadPool.submit(name, sql, args, (res: any) => {
            info("查询耗时", name, new Date().getTime() - a)
            resolve(res)
        }, action);
    })
}

export const queryEventCountMap = ():Promise<Array<{
    eventName:string,
    count:number
}>> => query("queryEventCountMap",`select event_name as eventName,count from stat where stat_type = 'received';`);

export const queryProcess = (): Promise<Array<{
    pid: number | null
    processName: string | null
}>> =>
    query("queryProcess", `
    SELECT
      pid, processName
    FROM
      temp_query_process where pid != 0`)

export const queryProcessByTable = (): Promise<Array<{
    pid: number | null
    processName: string | null
}>> =>
    query("queryProcessByTable", `
    SELECT
      pid, name as processName
    FROM
      process where pid != 0`)

export const queryProcessAsyncFunc = (limit: number, offset: number):Promise<Array<any>> => query("queryProcessAsyncFunc",`
select tid,
    P.pid,
    A.name as threadName,
    is_main_thread,
    c.callid as track_id,
    c.ts-D.start_ts as startTs,
    c.dur,
    c.name as funName,
    c.parent_id,
    c.id,
    c.cookie,
    c.depth,
    c.argsetid
from thread A,trace_range D
left join callstack C on A.id = C.callid
left join process P on P.id = A.ipid
where startTs not null and cookie not null limit $limit
offset $offset;`, {$limit: limit, $offset: offset})

export const queryProcessAsyncFuncCount = ():Promise<Array<any>> => query("queryProcessAsyncFuncCount",`
select count(*) as count 
from thread A,trace_range D
left join callstack C on A.id = C.callid
left join process P on P.id = A.ipid
where c.ts not null and cookie not null `, {})

export const queryTotalTime = (): Promise<Array<{ total: number }>> =>
    query("queryTotalTime", `
    select
      end_ts-start_ts as total
    from
      trace_range;`)

export const getAsyncEvents = (): Promise<Array<any>> =>
    query("getAsyncEvents", `
    select
      *,
      p.pid as pid,
      c.ts - t.start_ts as "startTime"
    from
      callstack c,trace_range t
    left join
      process p
    on
      c.callid = p.id
    where
      cookie is not null;`)

export const getCpuUtilizationRate = (startNS: number, endNS: number): Promise<Array<{
    cpu: number
    ro: number
    rate: number
}>> =>
    query("getCpuUtilizationRate", `
    select
      *
    from
      temp_get_cpu_rate;`, {})

export const getFps = () =>
    query<FpsStruct>("getFps", `
    select
      distinct(ts-tb.start_ts) as startNS, fps
    from
      hidump c ,trace_range tb
    where
      startNS >= 0
    order by
      startNS;`, {})

export const getFunDataByTid = (tid: number): Promise<Array<FuncStruct>> =>
    query("getFunDataByTid", `
    select
      *
    from
      temp_query_thread_function
    where
      tid = $tid`, {$tid: tid})

export const getThreadStateDataCount = (): Promise<Array<SPT>> =>
    query<SPT>("getThreadStateDataCount", `
    select count(1) as count from thread_state,trace_range where dur > 0 and (ts - start_ts) >= 0;
`, {});

export const getThreadStateData = (limit: number, offset: number): Promise<Array<ThreadState>> =>
    query<ThreadState>("getThreadStateData", `
    select itid,
       state,
       dur,
       ts,
       (ts - start_ts + dur) as end_ts,
       (ts - start_ts) as start_ts,
       cpu
from thread_state,trace_range where dur > 0 and (ts - start_ts) >= 0 
limit $limit
offset $offset;
`, {$limit: limit, $offset: offset});

export const getThreadProcessData = (): Promise<Array<ThreadProcess>> =>
    query<ThreadProcess>("getThreadProcessData", `
    select A.id,
       A.tid as threadId,
       A.name as thread,
       IP.pid as processId,
       IP.name as process
from thread as A left join process as IP on A.ipid = IP.id
where IP.pid not null;
`, {});

export const getSliceDataCount = (): Promise<Array<SPT>> =>
    query<SPT>("getSliceDataCount", `
    select count(1) as count from sched_slice;
`, {});

export const getSliceData = (limit: number, offset: number): Promise<Array<SptSlice>> =>
    query<SptSlice>("getSliceData", `
    select itid,ts,priority 
    from sched_slice 
limit $limit
offset $offset;
`, {$limit: limit, $offset: offset});

export const getStatesProcessThreadDataCount = (): Promise<Array<SPT>> =>
    query<SPT>("getStatesProcessThreadData", `
    select
      count(1) as count
    from
      (select
        IP.name as process,
        IP.pid as processId,
        A.name as thread,
        B.state as state,
        A.tid as threadId,
        B.dur,
        (B.ts - TR.start_ts + B.dur) as end_ts,
        (B.ts - TR.start_ts) as start_ts,
        B.cpu,
        C.priority,
        '-' as note
      from
        thread_state as B
      left join
        thread as A
      on
        B.itid = A.id
      left join
        process as IP
      on
        A.ipid = IP.id
      left join
        trace_range as TR
      left join
        sched_slice as C
      on
        B.itid = C.itid
      and
        C.ts = B.ts
      where
        B.dur > 0
      and
        IP.pid not null
      and
        (B.ts - TR.start_ts) >= 0);
`, {});
export const getStatesProcessThreadData = (limit: number, offset: number): Promise<Array<SPT>> =>
    query<SPT>("getStatesProcessThreadData", `
    select
      IP.name as process,
      IP.pid as processId,
      A.name as thread,
      B.state as state,
      A.tid as threadId,
      B.dur,
      (B.ts - TR.start_ts + B.dur) as end_ts,
      (B.ts - TR.start_ts) as start_ts,
      B.cpu,
      C.priority,
      '-' as note
    from
      thread_state as B
    left join
      thread as A
    on
      B.itid = A.id
    left join
      process as IP
    on
      A.ipid = IP.id
    left join
      trace_range as TR
    left join
      sched_slice as C
    on
      B.itid = C.itid
    and
      C.ts = B.ts
    where
      B.dur > 0
    and
      IP.pid not null
    and (B.ts - TR.start_ts) >= 0
    limit $limit
    offset $offset;
`, {$limit: limit, $offset: offset});

export const getTabStatesGroupByProcessThread = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByProcessThread", `
    select
      process,
      processId,
      thread,
      threadId,
      sum(dur) as wallDuration,
      round(avg(dur),2) as avgDuration,
      min(dur) as minDuration,
      max(dur) as maxDuration,
      count(threadId) as count
    from
      temp_get_process_thread_state_data
    where
      not (end_ts < $leftNS or start_ts > $rightNS)
    group by
      process,
      processId,
      thread,
      threadId`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByProcess = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByProcess", `
    select
      process,
      processId,
      sum(dur) as wallDuration,
      round(avg(dur),2) as avgDuration,
      min(dur) as minDuration,
      max(dur) as maxDuration,
      count(processId) as count
    from
      temp_get_process_thread_state_data
    where
      not (end_ts < $leftNS or start_ts > $rightNS)
    group by
      process,processId`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByState = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByState", `
    select
      state,
      sum(dur) as wallDuration,
      round(avg(dur),2) as avgDuration,
      min(dur) as minDuration,
      max(dur) as maxDuration,
      count(state) as count
    from
      temp_get_process_thread_state_data
    where
      not (end_ts < $leftNS or start_ts > $rightNS)
    group by
      state`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByStatePid = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByStatePid", `
    select
      process,
      processId,
      state,
      sum(dur) as wallDuration,
      round(avg(dur),2) as avgDuration,
      min(dur) as minDuration,
      max(dur) as maxDuration,
      count(processId) as count
    from
      temp_get_process_thread_state_data
    where
      not (end_ts < $leftNS or start_ts > $rightNS)
    group by
      process,
      processId,
      state`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByStatePidTid = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByStatePidTid", `
    select
      process,
      processId,
      thread,
      state,
      threadId,
      sum(dur) as wallDuration,
      round(avg(dur),2) as avgDuration,
      min(dur) as minDuration,
      max(dur) as maxDuration,
      count(threadId) as count
    from
      temp_get_process_thread_state_data
    where
      not (end_ts < $leftNS or start_ts > $rightNS)
    group by
      process,
      processId,
      thread,
      threadId,
      state`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabBoxChildData = (leftNs: number, rightNs: number, state: string | undefined,
                                   processId: number | undefined, threadId: number | undefined): Promise<Array<SPTChild>> =>
    query<SPTChild>("getTabBoxChildData", `
    select
      IP.name as process,
      IP.pid as processId,
      A.name as thread,
      B.state as state,
      A.tid as threadId,
      B.dur as duration,
      B.ts - TR.start_ts as startNs,
      B.cpu,
      C.priority,
      '-' as note
    from
      thread_state AS B
    left join
      thread as A
    on
      B.itid = A.id
    left join
      process AS IP
    on
      A.ipid = IP.id
    left join
      trace_range AS TR
    left join
      sched_slice as C
    on
      B.itid = C.itid
    and
      C.ts = B.ts
    where
      B.dur > 0
    and
      IP.pid not null
    and
      not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS))
      ${state != undefined && state != '' ? 'and B.state = $state' : ''}
      ${processId != undefined && processId != -1 ? 'and IP.pid = $processID' : ''}
      ${threadId != undefined && threadId != -1 ? 'and A.tid = $threadID' : ''}
    `, {$leftNS: leftNs, $rightNS: rightNs, $state: state, $processID: processId, $threadID: threadId})

export const getTabCpuUsage = (cpus: Array<number>, leftNs: number, rightNs: number): Promise<Array<CpuUsage>> =>
    query<CpuUsage>("getTabCpuUsage", `
    select
      cpu,
      sum(case
        when (A.ts - B.start_ts) < $leftNS
          then (A.ts - B.start_ts + A.dur - $leftNS)
        when (A.ts - B.start_ts) >= $leftNS
          and (A.ts - B.start_ts + A.dur) <= $rightNS
          then A.dur
        when (A.ts - B.start_ts + A.dur) > $rightNS
          then ($rightNS - (A.ts - B.start_ts)) end) / cast($rightNS - $leftNS as float) as usage
    from
      thread_state A,
      trace_range B
    where
      (A.ts - B.start_ts) > 0 and A.dur > 0
    and
      cpu in (${cpus.join(",")})
    and
      (A.ts - B.start_ts + A.dur) > $leftNS
    and
      (A.ts - B.start_ts) < $rightNS
    group by
      cpu`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabCpuFreq = (cpus: Array<number>, leftNs: number, rightNs: number): Promise<Array<Freq>> =>
    query<Freq>("getTabCpuFreq", `
    select
      cpu,
      value,
      (ts - tb.start_ts) as startNs
    from
      measure c,
      trace_range tb
    inner join
      cpu_measure_filter t
    on
      c.filter_id = t.id
    where
      (name = 'cpufreq' or name='cpu_frequency')
    and
      cpu in (${cpus.join(",")})
    and
      startNs > 0
    and
      startNs < $rightNS
    order by
      startNs`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabFps = (leftNs: number, rightNs: number): Promise<Array<Fps>> =>
    query<Fps>("getTabFps", `
    select
      distinct(ts-tb.start_ts) as startNS,
      fps
    from
      hidump c,
      trace_range tb
    where
      startNS <= $rightNS
    and
      startNS >= 0
    order by
    startNS;`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabCounters = (filterIds: Array<number>, startTime: number) =>
    query<Counter>("getTabCounters", `
    select
      t1.filter_id as trackId,
      t2.name,
      value,
      t1.ts - t3.start_ts as startTime
    from
      measure t1
    left join
      process_measure_filter t2
    on
      t1.filter_id = t2.id
    left join
      trace_range t3
    where
      filter_id in (${filterIds.join(",")})
    and
      startTime <= $startTime
    order by
      startTime asc;`, {$startTime: startTime})

export const getTabCpuByProcess = (cpus: Array<number>, leftNS: number, rightNS: number) =>
    query<SelectionData>("getTabCpuByProcess", `
    select
      IP.name as process,
      IP.pid as pid,
      sum(B.dur) as wallDuration,
      avg(B.dur) as avgDuration,
      count(A.tid) as occurrences
    from
      thread_state AS B
    left join
      thread as A
    on
      B.itid = A.id
    left join
      trace_range AS TR
    left join
      process AS IP
    on
      A.ipid = IP.id
    where
      B.cpu in (${cpus.join(",")})
    and
      not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS ))
    group by
      IP.name,
      IP.pid
    order by
      wallDuration desc;`, {$rightNS: rightNS, $leftNS: leftNS})

export const getTabCpuByThread = (cpus: Array<number>, leftNS: number, rightNS: number) =>
    query<SelectionData>("getTabCpuByThread", `
    select
      IP.name as process,
      IP.pid as pid,
      A.name as thread,
      A.tid as tid,
      sum(B.dur) as wallDuration,
      avg(B.dur) as avgDuration,
      count(A.tid) as occurrences
    from
      thread_state AS B
    left join
      thread as A
    on
      B.itid = A.id
    left join
      trace_range AS TR
    left join
      process AS IP
    on
      A.ipid = IP.id
    where
      B.cpu in (${cpus.join(",")})
    and
      not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS))
    group by
      IP.name,
      IP.pid,
      A.name,
      A.tid
    order by
      wallDuration desc;`, {$rightNS: rightNS, $leftNS: leftNS})

export const getTabSlices = (funTids: Array<number>, leftNS: number, rightNS: number): Promise<Array<any>> =>
    query<SelectionData>("getTabSlices", `
    select
      c.name as name,
      sum(c.dur) as wallDuration,
      avg(c.dur) as avgDuration,
      count(c.name) as occurrences
    from
      thread A, trace_range D
    left join
      callstack C
    on
      A.id = C.callid
    where
      C.ts not null
    and
      c.dur >= 0
    and
      A.tid in (${funTids.join(",")})
    and
      c.name not like 'binder%'
    and
      c.cookie is null
    and
      not ((C.ts - D.start_ts + C.dur < $leftNS) or (C.ts - D.start_ts > $rightNS))
    group by
      c.name
    order by
      wallDuration desc;`, {$leftNS: leftNS, $rightNS: rightNS})

export const getTabSlicesAsyncFunc = (asyncNames:Array<string>,asyncPid: Array<number>, leftNS: number, rightNS: number): Promise<Array<any>> =>
    query<SelectionData>("getTabSlicesAsyncFunc", `
    select
      c.name as name,
      sum(c.dur) as wallDuration,
      avg(c.dur) as avgDuration,
      count(c.name) as occurrences
    from
      thread A, trace_range D
    left join
      callstack C
    on
      A.id = C.callid
    left join process P on P.id = A.ipid
    where
      C.ts not null
    and
      c.dur >= -1
    and 
      c.cookie not null
    and
      P.pid in (${asyncPid.join(",")})
    and
      c.name in (${asyncNames.map(it=>"'"+it+"'").join(",")})
    and
      not ((C.ts - D.start_ts + C.dur < $leftNS) or (C.ts - D.start_ts > $rightNS))
    group by
      c.name
    order by
      wallDuration desc;`, {$leftNS: leftNS, $rightNS: rightNS})

export const getTabThreadStates = (tIds: Array<number>, leftNS: number, rightNS: number): Promise<Array<any>> =>
    query<SelectionData>("getTabThreadStates", `
    select
      IP.name as process,
      IP.pid,
      A.name as thread,
      A.tid,
      B.state,
      sum(B.dur) as wallDuration,
      avg(ifnull(B.dur,0)) as avgDuration,
      count(A.tid) as occurrences
    from
      thread_state AS B
    left join
      thread as A
    on
      A.id = B.itid
    left join
      trace_range AS TR
    left join
      process AS IP
    on
      IP.id=A.ipid
    where
      A.tid in (${tIds.join(",")})
    and
      not ((B.ts - TR.start_ts + ifnull(B.dur,0) < $leftNS) or (B.ts - TR.start_ts > $rightNS))
    group by
      IP.name, IP.pid, A.name, A.tid, B.state
    order by
      wallDuration desc;`, {$leftNS: leftNS, $rightNS: rightNS})

export const queryBinderArgsByArgset = (argset: number): Promise<Array<BinderArgBean>> =>
    query("queryBinderArgsByArgset", `
    select
      *
    from
      args_view
    where
      argset = $argset;`, {$argset: argset})

export const queryCpuData = (cpu: number, startNS: number, endNS: number): Promise<Array<CpuStruct>> =>
    query("queryCpuData", `
    select
      *
    from
      temp_query_cpu_data
    where
      cpu = $cpu
    and
      startTime between $startNS and $endNS;`, {
        $cpu: cpu,
        $startNS: startNS,
        $endNS: endNS
    })

export const queryCpuFreq = (): Promise<Array<{ cpu: number }>> =>
    query("queryCpuFreq", `
    select
      cpu
    from
      cpu_measure_filter
    where
      (name='cpufreq' or name='cpu_frequency')
    order by cpu;`)

export const queryCpuFreqData = (cpu: number): Promise<Array<CpuFreqStruct>> =>
    query<CpuFreqStruct>("queryCpuFreqData", `
    select
      cpu,
      value,
      ts-tb.start_ts as startNS
    from
      measure c,
      trace_range tb
    inner join
      cpu_measure_filter t
    on
      c.filter_id = t.id
    where
      (name = 'cpufreq' or name='cpu_frequency')
    and
      cpu= $cpu
    order by
      ts;`, {$cpu: cpu});

export const queryCpuMax = (): Promise<Array<any>> =>
    query("queryCpuMax", `
    select
      cpu
    from
      sched_slice
    order by
      cpu
    desc limit 1;`)

export const queryCpuMaxFreq = (): Promise<Array<any>> =>
    query("queryCpuMaxFreq", `
    select
      max(value) as maxFreq
    from
      measure c
    inner join
      cpu_measure_filter t
    on
      c.filter_id = t.id
    where
      (name = 'cpufreq' or name='cpu_frequency');`)

export const queryProcessData = (pid: number, startNS: number, endNS: number): Promise<Array<any>> =>
    query("queryProcessData", `
    select
      *
    from
      temp_query_process_data
    where
      tid != 0
    and
      pid = $pid
    and
      startTime between $startNS and $endNS;`, {
        $pid: pid,
        $startNS: startNS,
        $endNS: endNS
    })

export const queryProcessMem = (): Promise<Array<any>> =>
    query("queryProcessMem", `
    select
      process_measure_filter.id as trackId,
      process_measure_filter.name as trackName,
      ipid as upid,
      process.pid,
      process.name as processName
    from
      process_measure_filter
    join
      process using (ipid)
    order by trackName;`)

export const queryProcessMemData = (trackId: number): Promise<Array<ProcessMemStruct>> =>
    query("queryProcessMemData", `
    select
      c.type,
      ts,
      value,
      filter_id as track_id,
      c.ts-tb.start_ts startTime
    from
      measure c,
      trace_range tb
    where
      filter_id = $id;`, {$id: trackId})

export const queryDataDICT = (): Promise<Array<any>> =>
    query("queryDataDICT", `select * from data_dict;`)

export const queryProcessThreadsByTable = (): Promise<Array<ThreadStruct>> =>
    query("queryProcessThreadsByTable", `
        select p.pid as pid,t.tid as tid,p.name as processName,t.name as threadName from thread t left join process  p on t.ipid = p.id where t.tid != 0;
    `)

export const queryProcessThreads = (): Promise<Array<ThreadStruct>> =>
    query("queryProcessThreads", `
    select
      the_tracks.ipid as upid,
      the_tracks.itid as utid,
      total_dur as hasSched,
      process.pid as pid,
      thread.tid as tid,
      process.name as processName,
      thread.name as threadName
    from (
      select
        ipid,
        itid
      from
        sched_slice
      group by
        itid
    ) the_tracks
    left join (
      select
        ipid,
        sum(dur) as total_dur
      from
        sched_slice
      group by
        ipid
      ) using(ipid)
      left join
        thread using(itid)
      left join
        process using(ipid)
      order by
      total_dur desc,
      the_tracks.ipid,
      the_tracks.itid;`, {})

export const queryThreadData = (tid: number): Promise<Array<ThreadStruct>> =>
    query("queryThreadData", `
    select
      *
    from
      temp_query_thread_data
    where
      tid = $tid;`, {$tid: tid})

export const queryWakeUpThread_Desc = (): Promise<Array<any>> =>
    query("queryWakeUpThread_Desc", `This is the interval from when the task became eligible to run
(e.g.because of notifying a wait queue it was a suspended on) to when it started running.`)

/*-------------------------------------------------------------------------------------*/
export const queryWakeUpFromThread_WakeThread = (wakets: number): Promise<Array<WakeupBean>> =>
    query("queryWakeUpFromThread_WakeThread", `select TB.tid,TB.name as thread,TA.cpu,(TA.ts - TR.start_ts) as ts,TC.pid,TC.name as process
from sched_slice TA
left join thread TB on TA.itid = TB.id
left join process TC on TB.ipid = TC.id
left join trace_range TR
where TA.itid = (select itid from raw where name = 'sched_waking' and ts = $wakets )
    and TA.ts < $wakets
    and TA.ts + Ta.dur >= $wakets`, {$wakets: wakets})
/*-------------------------------------------------------------------------------------*/
export const queryWakeUpFromThread_WakeTime = (itid: number, startTime: number): Promise<Array<WakeUpTimeBean>> =>
    query("queryWakeUpFromThread_WakeTime", `select * from
    ( select ts as wakeTs,start_ts as startTs from instant,trace_range
       where name = 'sched_waking'
       and ref = $itid
       and ts < start_ts + $startTime
       order by ts desc limit 1) TA
       left join
    (select ts as preRow from sched_slice,trace_range
       where itid = $itid
       and ts < start_ts + $startTime
       order by ts desc limit 1) TB`, {$itid: itid, $startTime: startTime})
/*-------------------------------------------------------------------------------------*/
export const queryThreadWakeUp = (itid: number, startTime: number,dur:number): Promise<Array<WakeupBean>> =>
    query("queryThreadWakeUp", `
select TB.tid,TB.name as thread,min(TA.ts - TR.start_ts) as ts,TC.pid,TC.name as process
from
  (select min(ts) as wakeTs,ref as itid from instant,trace_range
       where name = 'sched_wakeup'
       and wakeup_from = $itid
       and ts > start_ts + $startTime
       and ts < start_ts + $startTime + $dur
      group by ref
       ) TW
left join thread_state TA on TW.itid = TA.itid and TA.ts > TW.wakeTs
left join thread TB on TA.itid = TB.id
left join process TC on TB.ipid = TC.id
left join trace_range TR
where TB.ipid not null 
group by TB.tid, TB.name,TC.pid, TC.name;
    `, {$itid: itid, $startTime: startTime,$dur:dur})

export const queryThreadWakeUpFrom = (itid: number, startTime: number,dur:number): Promise<Array<WakeupBean>> =>
    query("queryThreadWakeUpFrom", `
select TB.tid,TB.name as thread,TA.cpu,(TA.ts - TR.start_ts) as ts,TC.pid,TC.name as process
from
  (select ts as wakeTs,wakeup_from as wakeupFromTid from instant,trace_range
       where name = 'sched_wakeup'
       and ref = $itid
       and ts > start_ts + $startTime
       and ts < start_ts + $startTime + $dur
       order by ts) TW
left join thread_state TA on TW.wakeupFromTid = TA.itid and TA.ts < TW.wakeTs and TA.ts + TA.dur >= TW.wakeTs
left join thread TB on TA.itid = TB.id
left join process TC on TB.ipid = TC.id
left join trace_range TR
where TB.ipid not null 
limit 1;
    `, {$itid: itid, $startTime: startTime,$dur:dur})
/*-------------------------------------------------------------------------------------*/

export const queryHeapGroupByEvent = (): Promise<Array<NativeEventHeap>> =>
    query("queryHeapGroupByEvent", `
    select
      event_type as eventType,
      sum(heap_size) as sumHeapSize
    from
      native_hook
    where event_type = 'AllocEvent' or event_type = 'MmapEvent'
    group by event_type`, {})

export const queryAllHeapByEvent = (): Promise<Array<NativeEvent>> =>
    query("queryAllHeapByEvent", `
    select * from (
      select h.start_ts - t.start_ts as startTime,
       h.heap_size as heapSize,
       h.event_type as eventType
from native_hook h ,trace_range t
where h.start_ts >= t.start_ts and h.start_ts <= t.end_ts
and (h.event_type = 'AllocEvent' or h.event_type = 'MmapEvent')
union
select h.end_ts - t.start_ts as startTime,
       h.heap_size as heapSize,
       (case when h.event_type = 'AllocEvent' then 'FreeEvent' else 'MunmapEvent' end) as eventType
from native_hook h ,trace_range t
where h.start_ts >= t.start_ts and h.start_ts <= t.end_ts
and (h.event_type = 'AllocEvent' or h.event_type = 'MmapEvent')
and h.end_ts not null ) order by startTime;
`, {})

export const queryHeapAllData = (startTs: number, endTs: number, ipids: Array<number>): Promise<Array<HeapTreeDataBean>> =>
    query("queryHeapAllData", `
    select
      h.start_ts - t.start_ts as startTs,
      h.end_ts - t.start_ts as endTs,
      h.heap_size as heapSize,
      h.event_type as eventType,
      h.eventId
    from
      native_hook h
    inner join
      trace_range  t
    where
      event_type = 'AllocEvent'
    and
      ipid in (${ipids.join(",")})
    and
      (h.start_ts - t.start_ts between ${startTs} and ${endTs} or h.end_ts - t.start_ts between ${startTs} and ${endTs})`,
        {ipids: ipids, $startTs: startTs, $endTs: endTs})

export const queryNativeHookStatistics = (leftNs: number, rightNs: number): Promise<Array<NativeHookMalloc>> =>
    query("queryNativeHookStatistics", `
    select
      event_type as eventType,
      sub_type_id as subTypeId,
      max(heap_size) as max,
      sum(case when ((A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}) then heap_size else 0 end) as allocByte,
      sum(case when ((A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}) then 1 else 0 end) as allocCount,
      sum(case when ((A.end_ts - B.start_ts) between ${leftNs} and ${rightNs} ) then heap_size else 0 end) as freeByte,
      sum(case when ((A.end_ts - B.start_ts) between ${leftNs} and ${rightNs} ) then 1 else 0 end) as freeCount
    from
      native_hook A,
      trace_range B
    where
      (A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}
     and (event_type = 'AllocEvent' or event_type = 'MmapEvent')
    group by event_type;`, {$leftNs: leftNs, $rightNs: rightNs})

export const queryNativeHookStatisticsMalloc = (leftNs: number, rightNs: number): Promise<Array<NativeHookMalloc>> =>
    query('queryNativeHookStatisticsMalloc', `
    select
      event_type as eventType,
      heap_size as heapSize,
      sum(case when ((A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}) then heap_size else 0 end) as allocByte,
      sum(case when ((A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}) then 1 else 0 end) as allocCount,
      sum(case when ((A.end_ts - B.start_ts) between ${leftNs} and ${rightNs} ) then heap_size else 0 end) as freeByte,
      sum(case when ((A.end_ts - B.start_ts) between ${leftNs} and ${rightNs} ) then 1 else 0 end) as freeCount
    from
      native_hook A,
      trace_range B
    where
      (A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}
    and
      (event_type = 'AllocEvent' or event_type = 'MmapEvent')
    and 
      sub_type_id is null
    group by
      event_type,
      heap_size
    order by heap_size desc
    `, {$leftNs: leftNs, $rightNs: rightNs})

export const queryNativeHookStatisticsSubType = (leftNs: number, rightNs: number): Promise<Array<NativeHookMalloc>> =>
    query('queryNativeHookStatisticsSubType', `
    select
      event_type as eventType,
      sub_type_id as subTypeId,
      max(heap_size) as max,
      sum(case when ((A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}) then heap_size else 0 end) as allocByte,
      sum(case when ((A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}) then 1 else 0 end) as allocCount,
      sum(case when ((A.end_ts - B.start_ts) between ${leftNs} and ${rightNs} ) then heap_size else 0 end) as freeByte,
      sum(case when ((A.end_ts - B.start_ts) between ${leftNs} and ${rightNs} ) then 1 else 0 end) as freeCount
    from
      native_hook A,
      trace_range B
    where
      (A.start_ts - B.start_ts) between ${leftNs} and ${rightNs}
    and
      (event_type = 'MmapEvent')
    group by
      event_type,sub_type_id;
        `, {$leftNs: leftNs, $rightNs: rightNs})


export const queryNativeHookEventTid = (leftNs: number, rightNs: number, types: Array<string>): Promise<Array<NativeHookStatistics>> =>
    query("queryNativeHookEventTid", `
    select
      eventId,
      event_type as eventType,
      heap_size as heapSize,
      addr,
      (A.start_ts - B.start_ts) as startTs,
      (A.end_ts - B.start_ts) as endTs,
      tid,
      t.name as threadName
    from
      native_hook A,
      trace_range B
    left join
      thread t
    on
      A.itid = t.id
    where
      A.start_ts - B.start_ts
    between ${leftNs} and ${rightNs} and A.event_type in (${types.join(",")})`
        , {$leftNs: leftNs, $rightNs: rightNs, $types: types})

export const queryNativeHookProcess = (): Promise<Array<NativeHookProcess>> =>
    query("queryNativeHookProcess", `
    select
      distinct native_hook.ipid,
      pid,
      name
    from
      native_hook
    left join
      process p
    on
      native_hook.ipid = p.id`, {})

export const queryNativeHookSnapshotTypes = (): Promise<Array<NativeHookSampleQueryInfo>> =>
    query("queryNativeHookSnapshotTypes", `
select
      event_type as eventType,
      data as subType
    from
      native_hook left join data_dict on native_hook.sub_type_id = data_dict.id
    where
      (event_type = 'AllocEvent' or event_type = 'MmapEvent')
    group by
      event_type,data;`, {})

export const queryAllHookData = (rightNs: number): Promise<Array<NativeHookSampleQueryInfo>> =>
    query("queryAllHookData", `
    select
      eventId,
      event_type as eventType,
      data as subType,
      addr,
      heap_size as growth,
      (n.start_ts - t.start_ts) as startTs,
      (n.end_ts - t.start_ts) as endTs
    from
      native_hook n left join data_dict on n.sub_type_id = data_dict.id,
      trace_range t
    where
      (event_type = 'AllocEvent' or event_type = 'MmapEvent')
    and
      n.start_ts between t.start_ts and ${rightNs} + t.start_ts`, {$rightNs: rightNs})


/**
 * HiPerf
 */
export const queryHiPerfEventList = (): Promise<Array<any>> => query("queryHiPerfEventList", `select id,report_value from perf_report where report_type='config_name'`, {})
export const queryHiPerfEventListData = (eventTypeId:number): Promise<Array<any>> => query("queryHiPerfEventListData", `
    select s.*,(s.timestamp_trace-t.start_ts) startNS from perf_sample s,trace_range t where event_type_id=${eventTypeId} and s.thread_id != 0;
`, {$eventTypeId:eventTypeId})
export const queryHiPerfEventData = (eventTypeId:number,cpu:number): Promise<Array<any>> => query("queryHiPerfEventList", `
    select s.*,(s.timestamp_trace-t.start_ts) startNS from perf_sample s,trace_range t where event_type_id=${eventTypeId} and cpu_id=${cpu} and s.thread_id != 0;
`, {$eventTypeId:eventTypeId,$cpu:cpu})
export const queryHiPerfCpuData = (cpu: number): Promise<Array<any>> =>
    query("queryHiPerfCpuData", `select s.*,(s.timestamp_trace-t.start_ts) startNS from perf_sample s,trace_range t where cpu_id=${cpu} and s.thread_id != 0;`, {$cpu: cpu})
export const queryHiPerfCpuMergeData = (): Promise<Array<any>> =>
    query("queryHiPerfCpuData", `select s.*,(s.timestamp_trace-t.start_ts) startNS from perf_sample s,trace_range t where s.thread_id != 0;`, {})

export const queryHiPerfProcessData = (pid: number): Promise<Array<any>> => query("queryHiPerfProcessData", `SELECT sp.*,
       th.thread_name,
       th.thread_id                     tid,
       th.process_id                    pid,
       sp.timestamp_trace - tr.start_ts startNS
from perf_sample sp,
     trace_range tr
         left join perf_thread th on th.thread_id = sp.thread_id
where pid = ${pid} and sp.thread_id != 0;;`, {$pid: pid})

export const queryHiPerfThreadData = (tid: number): Promise<Array<any>> => query("queryHiPerfThreadData", `SELECT sp.*,
       th.thread_name,
       th.thread_id                     tid,
       th.process_id                    pid,
       sp.timestamp_trace - tr.start_ts startNS
from perf_sample sp,
     trace_range tr
         left join perf_thread th on th.thread_id = sp.thread_id
where tid = ${tid} and sp.thread_id != 0;`, {$tid: tid})

export const querySelectTraceStats = (): Promise<Array<{
    event_name: string
    stat_type: string
    count: number
    source: string
    serverity: string
}>> =>
    query('querySelectTraceStats', 'select event_name,stat_type,count,source,serverity from stat');

export const queryCustomizeSelect = (sql: string): Promise<Array<any>> =>
    query('queryCustomizeSelect', sql);

export const queryDistributedTerm = (): Promise<Array<{
    threadId: string
    threadName: string
    processId: string
    processName: string
    funName: string
    dur: string
    ts: string
    chainId: string
    spanId: string
    parentSpanId: string
    flag: string
    trace_name: string
}>> =>
    query('queryDistributedTerm', `
    select
      group_concat(thread.id,',') as threadId,
      group_concat(thread.name,',') as threadName,
      group_concat(process.id,',') as processId,
      group_concat(process.name,',') as processName,
      group_concat(callstack.name,',') as funName,
      group_concat(callstack.dur,',') as dur,
      group_concat(callstack.ts,',') as ts,
      cast(callstack.chainId as varchar) as chainId,
      callstack.spanId as spanId,
      callstack.parentSpanId as parentSpanId,
      group_concat(callstack.flag,',') as flag,
      (select
        value
      from
        meta
      where
        name='source_name') as trace_name
      from
        callstack
      inner join thread on callstack.callid = thread.id
      inner join process on process.id = thread.ipid
      where (callstack.flag='S' or callstack.flag='C')
      group by callstack.chainId,callstack.spanId,callstack.parentSpanId`);

export const queryTraceCpu = (): Promise<Array<{
    tid: string
    pid: string
    cpu: string
    dur: string
    min_freq: string
    max_freq: string
    avg_frequency: string
}>> =>
    query('queryTraceCpu', `SELECT 
        itid AS tid, 
        ipid AS pid, 
        group_concat(cpu, ',') AS cpu, 
        group_concat(dur, ',') AS dur, 
        group_concat(min_freq, ',') AS min_freq, 
        group_concat(max_freq, ',') AS max_freq, 
        group_concat(avg_frequency, ',') AS avg_frequency 
        FROM 
        (SELECT 
            itid, 
            ipid, 
            cpu, 
            CAST (SUM(dur) AS INT) AS dur, 
            CAST (MIN(freq) AS INT) AS min_freq, 
            CAST (MAX(freq) AS INT) AS max_freq, 
            CAST ( (SUM(dur * freq) / SUM(dur) ) AS INT) AS avg_frequency 
            from 
            result 
            group by 
            itid, cpu
        ) 
        GROUP BY 
        ipid, itid 
        ORDER BY 
        ipid
    `);

export const queryTraceCpuTop = (): Promise<Array<{
    tid: string
    pid: string
    cpu: string
    duration: string
    min_freq: string
    max_freq: string
    avg_frequency: string
    sumNum: string
}>> =>
    query('queryTraceCpuTop', `SELECT 
        itid AS tid, 
        ipid AS pid, 
        group_concat(cpu, ',') AS cpu, 
        group_concat(dur, ',') AS dur, 
        group_concat(min_freq, ',') AS min_freq, 
        group_concat(max_freq, ',') AS max_freq, 
        group_concat(avg_frequency, ',') AS avg_frequency, 
        sum(dur * avg_frequency) AS sumNum 
        FROM 
        (SELECT 
            itid, 
            ipid, 
            cpu, 
            CAST (SUM(dur) AS INT) AS dur, 
            CAST (MIN(freq) AS INT) AS min_freq, 
            CAST (MAX(freq) AS INT) AS max_freq, 
            CAST ( (SUM(dur * freq) / SUM(dur) ) AS INT) AS avg_frequency 
            from result group by itid, cpu
        ) 
        GROUP BY 
        ipid, itid 
        ORDER BY 
        sumNum 
        DESC 
        LIMIT 10;
    `);

export const queryTraceMemory = (): Promise<Array<{
    maxNum: string
    minNum: string
    avgNum: string
    name: string
    processName: string
}>> =>
    query('queryTraceMemory', `
    select
        max(value) as maxNum,
        min(value) as minNum,
        avg(value) as avgNum,
        result.name as name,
        result.processName as processName
        from measure inner join
        (
        select filter.id,filter.name,p.name as processName from process_measure_filter as filter
        left join process as p
        on filter.ipid=p.id where filter.name = 'mem.rss.anon'
        ) as result on result.id = filter_id
    where filter_id > 0 group by filter_id order by avgNum desc`);

export const queryTraceMemoryTop = (): Promise<Array<{
    maxNum: string
    minNum: string
    avgNum: string
    name: string
    processName: string
}>> =>
    query('queryTraceMemoryTop', `
    select
        max(value) as maxNum,
        min(value) as minNum,
        avg(value) as avgNum,
        result.name as name,
        result.processName as processName
        from measure inner join
        (
        select filter.id,filter.name,p.name as processName from process_measure_filter as filter
        left join process as p
        on filter.ipid=p.id where filter.name = 'mem.rss.anon'
        ) as result on result.id = filter_id
    where filter_id > 0 group by filter_id order by avgNum desc limit 10`);

export const queryTraceMemoryUnAgg = (): Promise<Array<{
    processName: string
    name: string
    value: string
    ts: string
}>> =>
    query('queryTraceMemoryUnAgg', `
    select
        processName as processName,
        group_concat(name) as name,
        cast(group_concat(value) as varchar) as value,
        cast(group_concat(ts) as varchar) as ts
        from measure inner join
        (
        select filter.ipid,filter.id,filter.name,p.name as processName from process_measure_filter as filter
        left join process as p
        on filter.ipid=p.id where filter.name = 'mem.rss.anon' or filter.name = 'mem.rss.file' or filter.name = 'mem.swap' or filter.name = 'oom_score_adj'
        ) as result
        on result.id = filter_id
    group by processName,ipid order by ipid`);

export const queryTraceTaskName = (): Promise<Array<{
    id: string
    pid: string
    process_name: string
    thread_name: string
}>> =>
    query('queryTraceTaskName', `
    select
        P.id as id,
        P.pid as pid,
        P.name as process_name,
        group_concat(T.name,',') as thread_name
    from process as P left join thread as T where P.id = T.ipid
    group by pid`);

export const queryTraceMetaData = (): Promise<Array<{
    name: string
    valueText: string
}>> =>
    query('queryTraceMetaData', `
    select
        cast(name as varchar) as name,
        cast(value as varchar) as valueText 
        from meta
        UNION
        select 'start_ts',cast(start_ts as varchar) from trace_range
        UNION
        select 'end_ts',cast(end_ts as varchar) from trace_range`);

export const querySystemCalls = (): Promise<Array<{
    frequency: string
    minDur: string
    maxDur: string
    avgDur: string
    funName: string
}>> =>
    query('querySystemCalls', `
    select
      count(*) as frequency,
      min(dur) as minDur,
      max(dur) as maxDur,
      avg(dur) as avgDur,
      name as funName
    from
      callstack
      group by name
      order by
    frequency desc limit 100`);

export const querySystemCallsTop = (): Promise<Array<{
    tid: string
    pid: string
    funName: string
    frequency: string
    minDur: string
    maxDur: string
    avgDur: string
}>> =>
    query('querySystemCallsTop', `SELECT 
        cpu.tid AS tid, 
        cpu.pid AS pid, 
        callstack.name AS funName, 
        count(callstack.name) AS frequency, 
        min(callstack.dur) AS minDur, 
        max(callstack.dur) AS maxDur, 
        round(avg(callstack.dur)) AS avgDur 
        FROM 
        callstack 
        INNER JOIN 
        (SELECT 
            itid AS tid, 
            ipid AS pid, 
            group_concat(cpu, ',') AS cpu, 
            group_concat(dur, ',') AS dur, 
            group_concat(min_freq, ',') AS min_freq, 
            group_concat(max_freq, ',') AS max_freq, 
            group_concat(avg_frequency, ',') AS avg_frequency, 
            sum(dur * avg_frequency) AS sumNum 
            FROM 
            (SELECT 
                itid, 
                ipid, 
                cpu, 
                CAST (SUM(dur) AS INT) AS dur, 
                CAST (MIN(freq) AS INT) AS min_freq, 
                CAST (MAX(freq) AS INT) AS max_freq, 
                CAST ( (SUM(dur * freq) / SUM(dur) ) AS INT) AS avg_frequency 
                FROM 
                result 
                GROUP BY 
                itid, cpu
            ) 
            GROUP BY 
            ipid, itid 
            ORDER BY 
            sumNum 
            DESC 
            LIMIT 10
        ) AS cpu 
        ON 
        callstack.callid = cpu.tid 
        GROUP BY 
        callstack.name 
        ORDER BY 
        frequency 
        DESC
    LIMIT 10`);

export const getTabLiveProcessData = (leftNs: number, rightNs: number): Promise<Array<LiveProcess>> =>
    query<LiveProcess>("getTabLiveProcessData", `SELECT
        process.id as processId,
        process.name as processName,
        process.ppid as responsibleProcess,
        process.uud as userName,
        process.usag as cpu,
        process.threadN as threads,
        process.pss as memory,
        process.cpu_time as cpuTime,
        process.disk_reads as diskReads,
        process.disk_writes as diskWrite
        FROM
        (
        SELECT
        tt.process_id AS id,
        tt.process_name AS name,
        tt.parent_process_id AS ppid,
        tt.uid as uud,
        tt.cpu_usage as usag,
        tt.thread_num AS threadN,
        mt.maxTT - TR.start_ts as endTs,
        tt.pss_info as pss,
        tt.cpu_time,
        tt.disk_reads,
        tt.disk_writes
        FROM
        live_process tt
        LEFT JOIN trace_range AS TR 
        LEFT JOIN (select re.process_id as idd, max(re.ts) as maxTT, min(re.ts) as minTT 
        from live_process re GROUP BY re.process_name, re.process_id ) mt
        on mt.idd = tt.process_id where endTs >= $rightNS
        GROUP BY
        tt.process_name,
        tt.process_id 
        ) process ;`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabProcessHistoryData = (leftNs: number, rightNs: number,
                                         processId: number | undefined, threadId: number | undefined): Promise<Array<ProcessHistory>> =>
    query<ProcessHistory>("getTabProcessHistoryData", `SELECT
        process.id as processId,
        process.isD as alive,
        process.startTS as firstSeen,
        process.endTs as lastSeen,
        process.name as processName,
        process.ppid as responsibleProcess,
        process.uuid as userName,
        process.cpu_time as cpuTime,
        0 as pss
        FROM
        (
        SELECT
        tt.process_id AS id,
        tt.process_name AS name,
        tt.parent_process_id AS ppid,
        tt.uid AS uuid,
        tt.cpu_time,
        (mt.minTT - TR.start_ts ) AS startTS,
        mt.maxTT - TR.start_ts as endTs,
        (mt.maxTT - TR.start_ts - $rightNS) > 0 as isD
        FROM
        live_process tt
        LEFT JOIN trace_range AS TR 
        LEFT JOIN (select re.process_id as idd, max(re.ts) as maxTT, min(re.ts) as minTT 
        from live_process re GROUP BY re.process_name, re.process_id ) mt
        on mt.idd = tt.process_id 
        GROUP BY
        tt.process_name,
        tt.process_id 
        ) process;`
        , {$leftNS: leftNs, $rightNS: rightNs, $processID: processId, $threadID: threadId})

export const getTabCpuAbilityData = (leftNs: number, rightNs: number): Promise<Array<SystemCpuSummary>> =>
    query<SystemCpuSummary>("getTabCpuAbilityData", `SELECT
        ( n.ts - TR.start_ts ) AS startTime,
        n.dur AS duration,
        n.total_load AS totalLoad,
        n.user_load AS userLoad,
        n.system_load AS systemLoad,
        n.process_num AS threads 
        FROM
        cpu_usage AS n,
        trace_range AS TR 
        WHERE
        ( n.ts - TR.start_ts ) >= ifnull((
        SELECT
        ( usage.ts - TR.start_ts ) 
        FROM
        cpu_usage usage,
        trace_range TR 
        WHERE
        ( usage.ts - TR.start_ts ) <= $leftNS 
        ORDER BY
        usage.ts DESC 
        LIMIT 1 
        ),0)
        AND ( n.ts - TR.start_ts ) <= $rightNS 
        ORDER BY
        startTime ASC;
    `, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabMemoryAbilityData = (leftNs: number, rightNs: number): Promise<Array<{
    startTime: number,
    value: string,
    name: string;
}>> =>
    query("getTabMemoryAbilityData", `SELECT
        m.ts AS startTime,
        GROUP_CONCAT( IFNULL( m.value, 0 ) ) AS value,
        GROUP_CONCAT( f.name ) AS name 
        FROM
        measure AS m
        INNER JOIN sys_event_filter AS f ON m.filter_id = f.id 
        AND (f.name = 'sys.mem.total' 
         or f.name = 'sys.mem.free'
         or f.name = 'sys.mem.buffers'
         or f.name = 'sys.mem.cached' 
         or f.name = 'sys.mem.shmem'
         or f.name = 'sys.mem.slab'
         or f.name = 'sys.mem.swap.total'
         or f.name = 'sys.mem.swap.free'
         or f.name = 'sys.mem.mapped'
         or f.name = 'sys.mem.vmalloc.used'
         or f.name = 'sys.mem.page.tables'
         or f.name = 'sys.mem.kernel.stack'
         or f.name = 'sys.mem.active'
         or f.name = 'sys.mem.inactive'
         or f.name = 'sys.mem.unevictable'
         or f.name = 'sys.mem.vmalloc.total'
         or f.name = 'sys.mem.slab.unreclaimable'
         or f.name = 'sys.mem.cma.total'
         or f.name = 'sys.mem.cma.free'
         or f.name = 'sys.mem.kernel.reclaimable'
         or f.name = 'sys.mem.zram'
         ) 
        AND m.ts >= ifnull((
        SELECT
        m.ts AS startTime 
        FROM
        measure AS m
        INNER JOIN sys_event_filter AS f ON m.filter_id = f.id 
        AND m.ts <= $leftNS 
        AND (f.name = 'sys.mem.total' 
         or f.name = 'sys.mem.free'
         or f.name = 'sys.mem.buffers'
         or f.name = 'sys.mem.cached' 
         or f.name = 'sys.mem.shmem'
         or f.name = 'sys.mem.slab'
         or f.name = 'sys.mem.swap.total'
         or f.name = 'sys.mem.swap.free'
         or f.name = 'sys.mem.mapped'
         or f.name = 'sys.mem.vmalloc.used'
         or f.name = 'sys.mem.page.tables'
         or f.name = 'sys.mem.kernel.stack'
         or f.name = 'sys.mem.active'
         or f.name = 'sys.mem.inactive'
         or f.name = 'sys.mem.unevictable'
         or f.name = 'sys.mem.vmalloc.total'
         or f.name = 'sys.mem.slab.unreclaimable'
         or f.name = 'sys.mem.cma.total'
         or f.name = 'sys.mem.cma.free'
         or f.name = 'sys.mem.kernel.reclaimable'
         or f.name = 'sys.mem.zram'
         ) 
        ORDER BY
        m.ts DESC 
        LIMIT 1 
        ),0)
        AND m.ts <= $rightNS GROUP BY m.ts;`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabNetworkAbilityData = (leftNs: number, rightNs: number): Promise<Array<SystemNetworkSummary>> =>
    query<SystemNetworkSummary>("getTabNetworkAbilityData", `SELECT
            ( n.ts - TR.start_ts ) AS startTime,
            n.dur AS duration,
            n.rx AS dataReceived,
            n.tx_speed AS dataReceivedSec,
            n.tx AS dataSend,
            n.rx_speed AS dataSendSec,
            n.packet_in AS packetsIn,
            n.packet_in_sec AS packetsInSec,
            n.packet_out AS packetsOut,
            n.packet_out_sec AS packetsOutSec 
            FROM
            network AS n,
            trace_range AS TR 
            WHERE
            ( n.ts - TR.start_ts ) >= ifnull((
            SELECT
            ( nn.ts - T.start_ts ) AS startTime 
            FROM
            network nn,
            trace_range T 
            WHERE
            ( nn.ts - T.start_ts ) <= $leftNS
            ORDER BY
            nn.ts DESC 
            LIMIT 1 
            ),0)  
            AND ( n.ts - TR.start_ts ) <= $rightNS 
            ORDER BY
            startTime ASC`, {$leftNS: leftNs, $rightNS: rightNs})

/*-------------------------------------------------------------------------------------*/
export const getTabDiskAbilityData = (leftNs: number, rightNs: number): Promise<Array<SystemDiskIOSummary>> =>
    query<SystemDiskIOSummary>("getTabDiskAbilityData", `SELECT
        ( n.ts - TR.start_ts ) AS startTime,
        n.dur AS duration,
        n.rd AS dataRead,
        n.rd_speed AS dataReadSec,
        n.wr AS dataWrite,
        n.wr_speed AS dataWriteSec,
        n.rd_count AS readsIn,
        n.rd_count_speed AS readsInSec,
        n.wr_count AS writeOut,
        n.wr_count_speed AS writeOutSec 
        FROM
        diskio AS n,
        trace_range AS TR 
        WHERE
        ( n.ts - TR.start_ts ) >= ifnull((
        SELECT
        ( nn.ts - T.start_ts ) AS startTime 
        FROM
        diskio AS nn,
        trace_range AS T 
        WHERE
        ( nn.ts - T.start_ts ) <= $leftNS 
        ORDER BY
        nn.ts DESC 
        LIMIT 1 
        ),0)
        AND ( n.ts - TR.start_ts ) <= $rightNS 
        ORDER BY
        startTime ASC;
    `, {$leftNS: leftNs, $rightNS: rightNs})

export const queryCpuAbilityData = (): Promise<Array<CpuAbilityMonitorStruct>> =>
    query("queryCpuAbilityData", `select 
        (t.total_load) as value,
        (t.ts - TR.start_ts) as startNS
        from cpu_usage t, trace_range AS TR;`)

export const queryCpuAbilityUserData = (): Promise<Array<CpuAbilityMonitorStruct>> =>
    query("queryCpuAbilityUserData", `select 
        t.user_load as value,
        (t.ts - TR.start_ts) as startNS
        from cpu_usage t, trace_range AS TR;`)

export const queryCpuAbilitySystemData = (): Promise<Array<CpuAbilityMonitorStruct>> =>
    query("queryCpuAbilitySystemData", `select 
        t.system_load as value,
        (t.ts - TR.start_ts) as startNS
        from cpu_usage t, trace_range AS TR;`)

export const queryMemoryUsedAbilityData = (id: string): Promise<Array<MemoryAbilityMonitorStruct>> =>
    query("queryMemoryUsedAbilityData", `select 
        t.value as value,
        (t.ts - TR.start_ts) as startNS
        from measure t, trace_range AS TR where t.filter_id = $id;`, {$id: id})

export const queryCachedFilesAbilityData = (id: string): Promise<Array<MemoryAbilityMonitorStruct>> =>
    query("queryCachedFilesAbilityData", `select 
        t.value as value,
        (t.ts - TR.start_ts) as startNS
        from measure t, trace_range AS TR where t.filter_id = $id;`, {$id: id})

export const queryCompressedAbilityData = (id: string): Promise<Array<MemoryAbilityMonitorStruct>> =>
    query("queryCompressedAbilityData", `select 
        t.value as value,
        (t.ts - TR.start_ts) as startNS
        from measure t, trace_range AS TR where t.filter_id = $id;`, {$id: id})

export const querySwapUsedAbilityData = (id: string): Promise<Array<MemoryAbilityMonitorStruct>> =>
    query("querySwapUsedAbilityData", `select 
        t.value as value,
        (t.ts - TR.start_ts) as startNS
        from measure t, trace_range AS TR where t.filter_id = $id;`, {$id: id})

export const queryBytesReadAbilityData = (): Promise<Array<DiskAbilityMonitorStruct>> =>
    query("queryBytesReadAbilityData", `select 
        t.rd_speed as value,
        (t.ts - TR.start_ts) as startNS
        from diskio t, trace_range AS TR;`)

export const queryBytesWrittenAbilityData = (): Promise<Array<DiskAbilityMonitorStruct>> =>
    query("queryBytesWrittenAbilityData", `select 
        t.wr_speed as value,
        (t.ts - TR.start_ts) as startNS
        from diskio t, trace_range AS TR;`)

export const queryReadAbilityData = (): Promise<Array<DiskAbilityMonitorStruct>> =>
    query("queryReadAbilityData", `select 
        t.rd_count_speed as value,
        (t.ts - TR.start_ts) as startNS
        from diskio t, trace_range AS TR;`)


export const queryWrittenAbilityData = (): Promise<Array<DiskAbilityMonitorStruct>> =>
    query("queryWrittenAbilityData", `select 
        t.wr_count_speed as value,
        (t.ts - TR.start_ts) as startNS
        from diskio t, trace_range AS TR;`)

export const queryBytesInAbilityData = (): Promise<Array<NetworkAbilityMonitorStruct>> =>
    query("queryBytesInAbilityData", `select 
        t.tx_speed as value,
        (t.ts - TR.start_ts) as startNS
        from network t, trace_range AS TR;`)

export const queryBytesOutAbilityData = (): Promise<Array<NetworkAbilityMonitorStruct>> =>
    query("queryBytesOutAbilityData", `select 
        t.rx_speed as value,
        (t.ts - TR.start_ts) as startNS
        from network t, trace_range AS TR;`,)

export const queryPacketsInAbilityData = (): Promise<Array<NetworkAbilityMonitorStruct>> =>
    query("queryPacketsInAbilityData", `select 
        t.packet_in_sec as value,
        (t.ts - TR.start_ts) as startNS
        from network t, trace_range AS TR;`,)

export const queryPacketsOutAbilityData = (): Promise<Array<NetworkAbilityMonitorStruct>> =>
    query("queryPacketsOutAbilityData", `select 
        t.packet_out_sec as value,
        (t.ts - TR.start_ts) as startNS
        from network t, trace_range AS TR;`)

export const queryNetWorkMaxData = (): Promise<Array<any>> =>
    query("queryNetWorkMaxData", `select 
     ifnull(max(tx_speed),0) as maxIn, 
     ifnull(max(rx_speed),0) as maxOut,
     ifnull(max(packet_in_sec),0) as maxPacketIn,
     ifnull(max(packet_in_sec),0) as maxPacketOut
     from network`)

export const queryMemoryMaxData = (memoryName: string): Promise<Array<any>> =>
    query("queryMemoryMaxData",
        `SELECT ifnull(max(m.value),0) as maxValue,
            filter_id 
            from measure m 
            WHERE m.filter_id =
            (SELECT id FROM sys_event_filter WHERE name = $memoryName)
`, {$memoryName: memoryName})

export const queryDiskIoMaxData = (): Promise<Array<any>> =>
    query("queryDiskIoMaxData", `select
    ifnull(max(rd_speed),0) as bytesRead, 
    ifnull(max(wr_speed),0) as bytesWrite,
    ifnull(max(rd_count_speed),0) as readOps,
    ifnull(max(wr_count_speed),0)  as writeOps
    from diskio`)

export const queryAbilityExits = (): Promise<Array<any>> =>
    query("queryAbilityExits", `select 
      event_name 
      from stat s 
      where s.event_name in ('trace_diskio','trace_network', 'trace_cpu_usage','sys_memory') 
      and s.stat_type ='received' and s.count > 0`)

export const queryStartTime = (): Promise<Array<any>> =>
    query("queryStartTime", `SELECT start_ts FROM trace_range`)

export const queryPerfFiles = (): Promise<Array<PerfFile>> =>
    query("queryPerfFiles", `select file_id as fileId,symbol,path from perf_files`, {})

export const queryPerfProcess = (): Promise<Array<PerfThread>> =>
    query("queryPerfThread", `select process_id as pid,thread_name as processName from perf_thread where process_id = thread_id`, {})

export const queryPerfThread = (): Promise<Array<PerfThread>> =>
    query("queryPerfThread", `select a.thread_id as tid,a.thread_name as threadName,a.process_id as pid,b.thread_name as processName from perf_thread a left join (select * from perf_thread where thread_id = process_id) b on a.process_id = b.thread_id`, {})

export const queryPerfSampleListByTimeRange = (leftNs: number, rightNs: number, cpus: Array<number>, processes: Array<number>, threads: Array<number>): Promise<Array<PerfSample>> => {
    let sql = `
select A.sample_id as sampleId,
       A.thread_id as tid,
       C.thread_name as threadName,
       A.thread_state as state,
       C.process_id as pid,
       (timestamp_trace - R.start_ts) as time,
       cpu_id as core
from perf_sample A,trace_range R
left join perf_thread C on A.thread_id = C.thread_id
where time >= $leftNs and time <= $rightNs and A.thread_id != 0
    `
    if (cpus.length != 0 || processes.length != 0 || threads.length != 0) {
        let arg1 = cpus.length > 0 ? `or core in (${cpus.join(",")}) ` : '';
        let arg2 = processes.length > 0 ? `or pid in (${processes.join(",")}) ` : '';
        let arg3 = threads.length > 0 ? `or tid in (${threads.join(",")})` : '';
        let arg = `${arg1}${arg2}${arg3}`.substring(3);
        sql = `${sql} and (${arg})`
    }
    return query("queryPerfSampleListByTimeRange", sql, {$leftNs: leftNs, $rightNs: rightNs});
}

export const queryPerfSampleIdsByTimeRange = (leftNs: number, rightNs: number, cpus: Array<number>, processes: Array<number>, threads: Array<number>): Promise<Array<PerfSample>> => {
    let sql = `
select A.sample_id as sampleId 
from perf_sample A,trace_range R
left join perf_thread C on A.thread_id = C.thread_id
where (timestamp_trace - R.start_ts) >= $leftNs and (timestamp_trace - R.start_ts) <= $rightNs and A.thread_id != 0 
    `
    if (cpus.length != 0 || processes.length != 0 || threads.length != 0) {
        let arg1 = cpus.length > 0 ? `or A.cpu_id in (${cpus.join(",")}) ` : '';
        let arg2 = processes.length > 0 ? `or C.process_id in (${processes.join(",")}) ` : '';
        let arg3 = threads.length > 0 ? `or A.thread_id in (${threads.join(",")})` : '';
        let arg = `${arg1}${arg2}${arg3}`.substring(3);
        sql = `${sql} and (${arg})`
    }
    return query("queryPerfSampleIdsByTimeRange", sql, {$leftNs: leftNs, $rightNs: rightNs});
}

export const queryPerfSampleCallChain = (sampleId: number): Promise<Array<PerfStack>> =>
    query("queryPerfSampleCallChain", `
    select
    callchain_id as callChainId,
    sample_id as sampleId,
    file_id as fileId,
    symbol_id as symbolId,
    vaddr_in_file as vaddrInFile,
    name as symbol
from perf_callchain where sample_id = $sampleId and symbol_id != -1 and vaddr_in_file != 0 order by id desc;;
    `, {$sampleId: sampleId})

export const queryPerfCmdline = ():Promise<Array<PerfCmdLine>> =>
    query("queryPerfCmdline",`
    select report_value from perf_report  where report_type = 'cmdline'
    `,{})

export const queryCPuAbilityMaxData = (): Promise<Array<any>> =>
    query("queryCPuAbilityMaxData",
        `select ifnull(max(total_load),0) as totalLoad, 
                ifnull(max(user_load),0) as userLoad,
                ifnull(max(system_load),0) as systemLoad
                from cpu_usage`)

export const querySearchFunc = (search:string):Promise<Array<SearchFuncBean>> =>
    query("querySearchFunc",`
   select c.id,c.name as funName,c.ts - r.start_ts as startTime,c.dur,c.depth,t.tid,t.name as threadName
   ,p.pid ,'func' as type from callstack c left join thread t on c.callid = t.id left join process p on t.ipid = p.id 
   left join trace_range r 
   where c.name like '%${search}%' and startTime > 0;
    `,{$search:search})
