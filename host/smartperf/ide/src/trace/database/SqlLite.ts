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
import {SPTChild, StateProcessThread} from "../bean/StateProcessThread.js";
import {CpuUsage, Freq} from "../bean/CpuUsage.js";
import {HeapStruct} from "../bean/HeapStruct.js";

class DbThread extends Worker {
    busy: boolean = false;
    isCancelled: boolean = false;
    id: number = -1;
    taskMap: any = {};
    cacheArray: Array<any> = [];

    uuid(): string {
        // @ts-ignore
        return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c => (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16));
    }

    queryFunc(name: string, sql: string, args: any, handler: Function) {
        this.busy = true;
        let id = this.uuid();
        this.taskMap[id] = handler
        this.postMessage({
            id: id,
            name: name,
            action: "exec",
            sql: sql,
            params: args,
        })
    }

    dbOpen = async (): Promise<{status:boolean,msg:string} > => {
        return new Promise<any>((resolve, reject) => {
            let id = this.uuid();
            this.taskMap[id] = (res: any) => {
                if(res.init) {
                    resolve({status:res.init,msg: res.msg});
                } else {
                    resolve({status:res.init,msg: res.msg});
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
    maxThreadNumber: number = 0;
    works: Array<DbThread> = [];
    static sharedBuffer: ArrayBuffer | null = null;
    progress: Function | undefined | null;
    init = async (type: string, threadBuild: (() => DbThread) | undefined = undefined) => { // wasm | server | sqlite
        await this.close();
        if (type === "wasm") {
            this.maxThreadNumber = 1;
        } else if (type === "server") {
            this.maxThreadNumber = 1;
        } else if (type === "sqlite") {
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
                // thread = new DbThread("trace/database/worker.sql-wasm.js")
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
                        let num=Math.floor(Math.random()*10+1)+20;
                        this.progress!("database opened", num + event.data.index)
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
                } else {
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
            let {status,msg} = await thread.dbOpen()
            if(!status){
                return {status,msg}
            }
        }
        return {status:true,msg:"ok"};
    }
    initSqlite = async (buf: ArrayBuffer, progress: Function) =>{
        this.progress = progress;
        progress("database loaded", 15)
        DbPool.sharedBuffer = buf;
        progress("parse database", 20)
        for (let i = 0; i < this.works.length; i++) {
            let thread = this.works[i];
            let {status,msg} = await thread.dbOpen()
            if(!status){
                return {status,msg}
            }
        }
        return {status:true,msg:"ok"};
    }

    close = async () => {
        for (let i = 0; i < this.works.length; i++) {
            let thread = this.works[i];
            thread.terminate();
        }
        this.works.length = 0;
    }

    submit(name: string, sql: string, args: any, handler: Function) {
        let noBusyThreads = this.works.filter(it => !it.busy);
        let thread: DbThread
        if (noBusyThreads.length > 0) {
            thread = noBusyThreads[0];
            thread.queryFunc(name, sql, args, handler)
        } else {
            thread = this.works[Math.floor(Math.random() * this.works.length)]
            thread.queryFunc(name, sql, args, handler)
        }
    }
}

export const threadPool = new DbPool()

function query<T extends any>(name: string, sql: string, args: any = null): Promise<Array<T>> {
    return new Promise<Array<T>>((resolve, reject) => {
        threadPool.submit(name, sql, args, (res: any) => {
            resolve(res)
        })
    })
}


/*-------------------------------------------------------------------------------------*/
export const queryProcess = (): Promise<Array<{
    pid: number | null
    processName: string | null
}>> =>
    query("queryProcess", `SELECT pid,processName FROM temp_query_process`)
/*-------------------------------------------------------------------------------------*/
export const queryTotalTime = (): Promise<Array<{ total: number }>> =>
    query("queryTotalTime", `select end_ts-start_ts as total from trace_section;`)
/*-------------------------------------------------------------------------------------*/
export const queryCpu = async (): Promise<Array<{ cpu: number }>> =>
    query("queryCpu", `select cpu from cpu_measure_filter where name='cpu_idle' order by cpu;`)
/*-------------------------------------------------------------------------------------*/
export const getAsyncEvents = (): Promise<Array<any>> =>
    query("getAsyncEvents", `select *,p.pid as pid,c.ts - t.start_ts as "startTime" from callstack c,trace_section t 
left join process p on c.callid = p.id where cookie is not null;`)

export const getCpuUtilizationRate = (startNS: number, endNS: number): Promise<Array<{
    cpu: number
    ro: number
    rate: number
}>> =>
    query("getCpuUtilizationRate", `select * from temp_get_cpu_rate;`, {})
/*-------------------------------------------------------------------------------------*/
export const getFps = () =>
    query<FpsStruct>("getFps", `select distinct(ts-tb.start_ts) as startNS,fps
from hidump c ,trace_section tb
where startNS >= 0
order by startNS;`, {})

/*-------------------------------------------------------------------------------------*/
export const getFunDataByTid = (tid: number): Promise<Array<FuncStruct>> =>
    query("getFunDataByTid", `select * from temp_query_thread_function where tid = $tid`, {$tid: tid})
/*-------------------------------------------------------------------------------------*/
export const getTabStatesGroupByProcessThread = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByProcessThread", `select IP.name as process,
       IP.pid as processId,
       A.name as thread,
       a.tid as threadId,
    sum(B.dur) as wallDuration,
    round(avg(B.dur),2) as avgDuration,
    min(B.dur) as minDuration,
    max(B.dur) as maxDuration,
    --round(stdev(B.dur),2) as stdDuration,
    count(A.tid) as count
from thread_state AS B
    left join  thread as A on B.itid = A.id
    left join process AS IP on A.ipid = IP.id
    left join trace_section AS TR
where pid not null and
    B.dur > 0 and
    not ((ts - TR.start_ts + dur < $leftNS) or (ts - TR.start_ts > $rightNS))
group by IP.name, IP.pid,thread,threadId
order by wallDuration`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabStatesGroupByProcess = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByProcess", `select IP.name as process,IP.pid as processId,
       sum(dur) as wallDuration,
       round(avg(dur),2) as avgDuration,
       min(dur) as minDuration,
       max(dur) as maxDuration,
       --round(stdev(dur),2) as stdDuration,
       count(Ip.id) as count
from thread_state as A,trace_section as B
    left join  thread as C on A.itid = C.id
    left join process AS IP on C.ipid = IP.id
where A.dur > 0 and processId not null and not ((ts - B.start_ts + dur < $leftNS) or (ts - B.start_ts > $rightNS))
group by process,processId`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByState = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByState", `select state,
       sum(dur) as wallDuration,
       round(avg(dur),2) as avgDuration,
       min(dur) as minDuration,
       max(dur) as maxDuration,
       --round(stdev(dur),2) as stdDuration,
       count(state) as count
from thread_state as A,trace_section as B
    left join  thread as C on A.itid = C.id
    left join process AS IP on C.ipid = IP.id
where A.dur > 0 and IP.pid not null and not ((ts - B.start_ts + dur < $leftNS) or (ts - B.start_ts > $rightNS))
group by state`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByStatePid = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByStatePid", `select IP.name as process,
       IP.pid as processId,
       B.state as state,
    sum(B.dur) as wallDuration,
    round(avg(B.dur),2) as avgDuration,
    min(B.dur) as minDuration,
    max(B.dur) as maxDuration,
    --round(stdev(B.dur),2) as stdDuration,
    count(A.tid) as count
from thread_state AS B
    left join  thread as A on B.itid = A.id
    left join process AS IP on A.ipid = IP.id
    left join trace_section AS TR
where pid not null and
    B.dur > 0 and
    not ((ts - TR.start_ts + dur < $leftNS) or (ts - TR.start_ts > $rightNS))
group by IP.name, IP.pid,state
order by wallDuration`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabStatesGroupByStatePidTid = (leftNs: number, rightNs: number): Promise<Array<StateProcessThread>> =>
    query<StateProcessThread>("getTabStatesGroupByStatePidTid", `select  IP.name as process,
    IP.pid as processId,
    A.name as thread,
    B.state as state,
    A.tid as threadId,
    sum(B.dur) as wallDuration,
    round(avg(B.dur),2) as avgDuration,
    min(B.dur) as minDuration,
    max(B.dur) as maxDuration,
    --round(stdev(B.dur),2) as stdDuration,
    count(A.tid) as count
from thread_state AS B
    left join  thread as A on B.itid = A.id
    left join process AS IP on A.ipid = IP.id
    left join trace_section AS TR
where
    B.dur > 0 and IP.pid not null and
    not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS))
group by IP.name, IP.pid, A.name, A.tid,state
order by wallDuration`, {$leftNS: leftNs, $rightNS: rightNs});

export const getTabBoxChildData = (leftNs: number, rightNs: number, state: string | undefined, processId: number | undefined, threadId: number | undefined): Promise<Array<SPTChild>> =>
    query<SPTChild>("getTabBoxChildData", `select  IP.name as process,
    IP.pid as processId,
    A.name as thread,
    B.state as state,
    A.tid as threadId,
       B.dur as duration,
       B.ts - TR.start_ts as startNs,
       B.cpu,
       C.priority,
       '-' as note
from thread_state AS B
    left join  thread as A on B.itid = A.id
    left join process AS IP on A.ipid = IP.id
    left join trace_section AS TR
    left join sched_slice as C on B.itid = C.itid and C.ts = B.ts
where
    B.dur > 0 and IP.pid not null
    and not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS))
    ${ state != undefined ? 'and B.state = $state':''}
    ${ processId != undefined ? 'and IP.pid = $processID':''}
    ${ threadId != undefined ? 'and A.tid = $threadID':''}
    `, {$leftNS: leftNs, $rightNS: rightNs, $state: state, $processID: processId, $threadID: threadId})

/*-------------------------------------------------------------------------------------*/
export const getTabCpuUsage = (cpus: Array<number>, leftNs: number, rightNs: number): Promise<Array<CpuUsage>> =>
    query<CpuUsage>("getTabCpuUsage", `select cpu,
    sum(case
        when (A.ts - B.start_ts) < $leftNS then (A.ts - B.start_ts + A.dur - $leftNS)
        when (A.ts - B.start_ts) >= $leftNS and (A.ts - B.start_ts + A.dur) <= $rightNS then A.dur
        when (A.ts - B.start_ts + A.dur) > $rightNS then ($rightNS - (A.ts - B.start_ts)) end) / cast($rightNS - $leftNS as float) as usage
from thread_state A ,trace_section B
where (A.ts - B.start_ts) > 0 and A.dur > 0
  and cpu in (${cpus.join(",")})
  and  (A.ts - B.start_ts + A.dur) > $leftNS and (A.ts - B.start_ts) < $rightNS
group by cpu`, {$leftNS: leftNs, $rightNS: rightNs})

export const getTabCpuFreq = (cpus: Array<number>, leftNs: number, rightNs: number): Promise<Array<Freq>> =>
    query<Freq>("getTabCpuFreq", `select cpu,value,(ts - tb.start_ts) as startNs
from measure c ,trace_section tb
inner join cpu_measure_filter t on c.filter_id = t.id
where (name = 'cpufreq' or name='cpu_frequency')
  and cpu in (${cpus.join(",")})
  and startNs > 0
  and startNs < $rightNS
  order by startNs`, {$leftNS: leftNs, $rightNS: rightNs})
/*-------------------------------------------------------------------------------------*/
export const getTabFps = (leftNs: number, rightNs: number): Promise<Array<Fps>> =>
    query<Fps>("getTabFps", `select distinct(ts-tb.start_ts) as startNS,fps
from hidump c ,trace_section tb
where startNS <= $rightNS and startNS >= 0
order by startNS;`, {$leftNS: leftNs, $rightNS: rightNs})
/*-------------------------------------------------------------------------------------*/
export const getTabCounters = (filterIds: Array<number>, startTime: number) =>
    query<Counter>("getTabCounters", `select t1.filter_id as trackId,t2.name,value, t1.ts - t3.start_ts as startTime
from measure t1
left join process_measure_filter t2 on t1.filter_id = t2.id
left join trace_section t3 where filter_id in (${filterIds.join(",")})
and startTime <= $startTime
order by startTime asc;`, {$startTime: startTime})
/*-------------------------------------------------------------------------------------*/
export const getTabCpuByProcess = (cpus: Array<number>, leftNS: number, rightNS: number) =>
    query<SelectionData>("getTabCpuByProcess", `select  IP.name as process,
    IP.pid as pid,
    sum(B.dur) as wallDuration,
    avg(B.dur) as avgDuration,
    count(A.tid) as occurrences
from thread_state AS B
    left join  thread as A
    left join trace_section AS TR
    left join process AS IP
where B.itid = A.id
    and A.ipid = IP.id
    and B.cpu in (${cpus.join(",")})
    and not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS ))
group by IP.name, IP.pid
order by wallDuration desc;`, {$rightNS: rightNS, $leftNS: leftNS})
/*-------------------------------------------------------------------------------------*/
export const getTabCpuByThread = (cpus: Array<number>, leftNS: number, rightNS: number) =>
    query<SelectionData>("getTabCpuByThread", `select  IP.name as process,
    IP.pid as pid,
    A.name as thread,
    A.tid as tid,
    sum(B.dur) as wallDuration,
    avg(B.dur) as avgDuration,
    count(A.tid) as occurrences
from thread_state AS B
    left join  thread as A
    left join trace_section AS TR
    left join process AS IP
where B.itid = A.id
    and A.ipid = IP.id
    and B.cpu in (${cpus.join(",")})
    and not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS))
group by IP.name, IP.pid, A.name, A.tid
order by wallDuration desc;`, {$rightNS: rightNS, $leftNS: leftNS})
/*-------------------------------------------------------------------------------------*/
export const getTabSlices = (funTids: Array<number>, leftNS: number, rightNS: number): Promise<Array<any>> =>
    query<SelectionData>("getTabSlices", `select
      c.name as name,
      sum(c.dur) as wallDuration,
      avg(c.dur) as avgDuration,
      count(c.name) as occurrences
from thread A,trace_section D
left join callstack C on A.id = C.callid
where C.ts not null
      and c.dur >= 0
      and A.tid in (${funTids.join(",")})
      and c.name not like 'binder%'
      and not ((C.ts - D.start_ts + C.dur < $leftNS) or (C.ts - D.start_ts > $rightNS))
group by c.name
order by wallDuration desc;`, {$leftNS: leftNS, $rightNS: rightNS})
/*-------------------------------------------------------------------------------------*/
export const getTabThreadStates = (tIds: Array<number>, leftNS: number, rightNS: number): Promise<Array<any>> =>
    query<SelectionData>("getTabThreadStates", `select
    IP.name as process,
    IP.pid,
    A.name as thread,
    A.tid,
    B.state,
    sum(B.dur) as wallDuration,
    avg(B.dur) as avgDuration,
    count(A.tid) as occurrences
from thread_state AS B
left join thread as A on A.id = B.itid
left join trace_section AS TR
left join process AS IP on IP.id=ipid
where A.tid in (${tIds.join(",")})
and not ((B.ts - TR.start_ts + B.dur < $leftNS) or (B.ts - TR.start_ts > $rightNS))
group by IP.name, IP.pid, A.name, A.tid, B.state
order by wallDuration desc;`, {$leftNS: leftNS, $rightNS: rightNS})
/*-------------------------------------------------------------------------------------*/
export const getThreadFuncData = (tId: number): Promise<Array<any>> =>
    query("getThreadFuncData", `select tid,
    A.start_ts,
    A.end_ts,
    A.name as threadName,
    is_main_thread,
    c.callid as track_id,
    c.ts-D.start_ts as startTs,
    c.ts + c.dur as endTs,
    c.dur,
    c.name as funName,
    c.depth,
   c.parent_id,
   c.id
from thread A,trace_section D
left join callstack C on A.id = C.callid
where startTs not null and A.tid = $tid;`, {$tid: tId})
/*-------------------------------------------------------------------------------------*/
export const queryBinderArgsByArgset = (argset: number): Promise<Array<BinderArgBean>> =>
    query("queryBinderArgsByArgset", `select * from args_view where argset = $argset;`, {$argset: argset})
/*-------------------------------------------------------------------------------------*/
export const queryClockFrequency = (): Promise<Array<any>> =>
    query("queryClockFrequency", `with freq as (  select measure.filter_id, measure.ts, measure.type, measure.value from clock_event_filter
left join measure
where clock_event_filter.name = '%s' and clock_event_filter.type = 'clock_set_rate' and clock_event_filter.id = measure.filter_id
order by measure.ts)
select freq.filter_id,freq.ts - r.start_ts as ts,freq.type,freq.value from freq,trace_section r;`, {})
/*-------------------------------------------------------------------------------------*/
export const queryClockList = (): Promise<Array<any>> =>
    query("queryClockList", `with list as (
    select distinct name from clock_event_filter
    where  clock_event_filter.type = 'clock_set_rate' order by name
),freq as(
    select measure.filter_id, measure.ts, measure.type, measure.value , clock_event_filter.name from clock_event_filter
    left join measure
    where  clock_event_filter.type = 'clock_set_rate' and clock_event_filter.id = measure.filter_id
    order by measure.ts
),state as (
    select filter_id, ts, endts, endts-ts as dur, type, value,name from
    (select measure.filter_id, measure.ts, lead(ts, 1, null) over( order by measure.ts) endts, measure.type, measure.value,clock_event_filter.name from clock_event_filter,trace_section
    left join measure
    where clock_event_filter.type != 'clock_set_rate' and clock_event_filter.id = measure.filter_id
    order by measure.ts)
),count_freq as (
    select COUNT(*) num,name srcname from freq group by name
),count_state as (
    select COUNT(*) num,name srcname from state group by name
)
select count_freq.srcname||' Frequency' as name,* from count_freq union select count_state.srcname||' State' as name,* from count_state order by name;`)
/*-------------------------------------------------------------------------------------*/
export const queryClockState = (): Promise<Array<any>> =>
    query("queryClockState", `with state as (
select filter_id, ts, endts, endts-ts as dur, type, value from
(select measure.filter_id, measure.ts, lead(ts, 1, null) over( order by measure.ts) endts, measure.type, measure.value from clock_event_filter,trace_section
left join measure
where clock_event_filter.name = '%s' and clock_event_filter.type != 'clock_set_rate' and clock_event_filter.id = measure.filter_id
order by measure.ts))
-- select * from state;
select s.filter_id,s.ts-r.start_ts as ts,s.type,s.value,s.dur from state s,trace_section r;`)
/*-------------------------------------------------------------------------------------*/
export const queryCpuData = (cpu: number, startNS: number, endNS: number): Promise<Array<CpuStruct>> =>
    query("queryCpuData", `select * from temp_query_cpu_data where cpu = $cpu and startTime between $startNS and $endNS;`, {
        $cpu: cpu,
        $startNS: startNS,
        $endNS: endNS
    })
/*-------------------------------------------------------------------------------------*/
export const queryCpuFreq = (): Promise<Array<{ cpu: number }>> =>
    query("queryCpuFreq", `select cpu from temp_query_cpu_freq;`)
/*-------------------------------------------------------------------------------------*/
export const queryCpuFreqData = (cpu: number): Promise<Array<CpuFreqStruct>> =>
    query("queryCpuFreqData", `select * from temp_query_freq_data where cpu = $cpu;`, {$cpu: cpu})
/*-------------------------------------------------------------------------------------*/
export const queryCpuMax = (): Promise<Array<any>> =>
    query("queryCpuMax", `select cpu from sched_slice order by cpu desc limit 1;`)
/*-------------------------------------------------------------------------------------*/
export const queryCpuMaxFreq = (): Promise<Array<any>> =>
    query("queryCpuMaxFreq", `select * from temp_query_cpu_max_freq;`)
// /*-------------------------------------------------------------------------------------*/
export const queryLogs = (): Promise<Array<any>> =>
    query("queryLogs", `select l.*,l.ts-t.start_ts as "startTime" from log as l left join trace_section AS t
 where "startTime" between %s and %s order by "startTime"
 limit %s offset %s;`)
/*-------------------------------------------------------------------------------------*/
export const queryLogsCount = (): Promise<Array<any>> =>
    query("queryLogsCount", `select l.*,l.ts-t.start_ts as "startTime" from log as l left join trace_section AS t
 where "startTime" between %s and %s;`)
/*-------------------------------------------------------------------------------------*/
export const queryProcessData = (pid: number, startNS: number, endNS: number): Promise<Array<any>> =>
    query("queryProcessData", `select * from temp_query_process_data where pid = $pid and startTime between $startNS and $endNS;`, {
        $pid: pid,
        $startNS: startNS,
        $endNS: endNS
    })
/*-------------------------------------------------------------------------------------*/
export const queryProcessDataCount = (): Promise<Array<any>> =>
    query("queryProcessDataCount", `select ta.id,type, ts, dur, ta.cpu, itid as utid, state
     ,ts-tb.start_ts as startTime,tc.tid,tc.pid,tc.process,tc.thread
from thread_state ta,trace_section tb
left join (
    select it.id,tid,pid,ip.name as process,it.name as thread from thread as it left join process ip on it.ipid = ip.id
    ) tc on ta.itid = tc.id
where tc.pid = %d
  and startTime between  %s and  %s
and ta.cpu is not null
order by startTime;`)
/*-------------------------------------------------------------------------------------*/
export const queryProcessDataLimit = (pid: number, startNS: number, endNS: number, limit: number): Promise<Array<any>> =>
    query("queryProcessDataLimit", `with list as (select ta.id,type, ts, dur, ta.cpu, itid as utid, state
     ,ts-tb.start_ts as startTime,tc.tid,tc.pid,tc.process,tc.thread
from thread_state ta,trace_section tb
left join (
    select it.id,tid,pid,ip.name as process,it.name as thread from thread as it left join process ip on it.ipid = ip.id
    ) tc on ta.itid = tc.id
where tc.pid = $pid
  and startTime between  $startNS and  $endNS
and ta.cpu is not null
order by startTime )
select * from list order by random() limit $limit;`, {$pid: pid, $startNS: startNS, $endNS: endNS, $limit: limit})
/*-------------------------------------------------------------------------------------*/
export const queryProcessMem = (): Promise<Array<any>> =>
    query("queryProcessMem", `select process_measure_filter.id   as trackId,
       process_measure_filter.name as trackName,
       ipid as upid,
       process_view.pid,
       process_view.name               as processName
from process_measure_filter join process_view using (ipid)
order by trackName;`)
/*-------------------------------------------------------------------------------------*/
export const queryProcessMemData = (trackId: number): Promise<Array<ProcessMemStruct>> =>
    query("queryProcessMemData", `select c.type,
    ts, value,
    filter_id as track_id,
    c.ts-tb.start_ts startTime
from measure c,trace_section tb where filter_id = $id;`, {$id: trackId})
/*-------------------------------------------------------------------------------------*/
export const queryProcessNOrder = (): Promise<Array<any>> =>
    query("queryProcessNOrder", `select pid,name as processName from process;`)
/*-------------------------------------------------------------------------------------*/
export const queryProcessThreads = (): Promise<Array<ThreadStruct>> =>
    query("queryProcessThreads", `select
  the_tracks.ipid as upid,
  the_tracks.itid as utid,
  total_dur as hasSched,
  process_view.pid as pid,
  thread_view.tid as tid,
  process_view.name as processName,
  thread_view.name as threadName
from (
  select ipid, itid from sched_view join thread_view using(itid) group by itid
) the_tracks
left join (select ipid, sum(dur) as total_dur
  from sched_view join thread_view using(itid)
  group by ipid
) using(ipid)
left join thread_view using(itid)
left join process_view using(ipid)
order by
  total_dur desc,
  the_tracks.ipid,
  the_tracks.itid;`, {})
/*-------------------------------------------------------------------------------------*/
export const queryProcessThreadsNOrder = (): Promise<Array<any>> =>
    query("queryProcessThreadsNOrder", `select p.id as upid,
       t.id as utid,
       p.pid,
       t.tid,
       p.name as processName,
       t.name as threadName
       from thread t left join process p on t.ipid = p.id;`)
/*-------------------------------------------------------------------------------------*/
export const queryScreenState = (): Promise<Array<any>> =>
    query("queryScreenState", `select m.type, m.ts-r.start_ts as ts, value, filter_id from measure m,trace_section r where filter_id in (select id from process_measure_filter where name = 'ScreenState');`)
/*-------------------------------------------------------------------------------------*/
export const queryThreadData = (tid: number): Promise<Array<ThreadStruct>> =>
    query("queryThreadData", `select * from temp_query_thread_data where tid = $tid;`, {$tid: tid})
/*-------------------------------------------------------------------------------------*/
export const queryWakeUpThread_Desc = (): Promise<Array<any>> =>
    query("queryWakeUpThread_Desc", `This is the interval from when the task became eligible to run
(e.g.because of notifying a wait queue it was a suspended on) to when it started running.`)
/*-------------------------------------------------------------------------------------*/
export const queryWakeUpThread_WakeThread = (wakets: number): Promise<Array<WakeupBean>> =>
    query("queryWakeUpThread_WakeThread", `select TB.tid,TB.name as thread,TA.cpu,TC.pid,TC.name as process
from sched_view TA
left join thread TB on TA.itid = TB.id
left join process TC on TB.ipid = TC.id
where itid = (select itid from raw where name = 'sched_waking' and ts = $wakets )
    and TA.ts < $wakets
    and Ta.ts + Ta.dur >= $wakets`, {$wakets: wakets})
/*-------------------------------------------------------------------------------------*/
export const queryWakeUpThread_WakeTime = (tid: number, startTime: number): Promise<Array<WakeUpTimeBean>> =>
    query("queryWakeUpThread_WakeTime", `select * from
    ( select ts as wakeTs,start_ts as startTs from instants_view,trace_section
       where name = 'sched_waking'
       and ref = $tid
       and ts < start_ts + $startTime
       order by ts desc limit 1) TA
       left join
    (select ts as preRow from sched_view,trace_section
       where itid = $tid
       and ts < start_ts + $startTime
       order by ts desc limit 1) TB`, {$tid: tid, $startTime: startTime})
/*-------------------------------------------------------------------------------------*/
export const queryThreadsByPid = (pid: number): Promise<Array<any>> =>
    query("queryThreadsByPid", `select
                the_tracks.ipid as upid,
                the_tracks.itid as utid,
                total_dur as hasSched,
                process_view.pid as pid,
                thread_view.tid as tid,
                process_view.name as processName,
                thread_view.name as threadName
              from (
                select ipid, itid from sched_view join thread_view using(itid) group by itid
              ) the_tracks
              left join (select ipid, sum(dur) as total_dur
                from sched_view join thread_view using(itid)
                group by ipid
              ) using(ipid)
              left join thread_view using(itid)
              left join process_view using(ipid)
              where  pid = $pid
              order by
                total_dur desc,
                the_tracks.ipid,
                the_tracks.itid`, {$pid: pid})
/*-------------------------------------------------------------------------------------*/
export const queryHeapByPid = (startTs: number, endTs: number, ipid: number): Promise<Array<HeapStruct>> =>
    query("queryHeapByPid", `select a.maxheap maxHeapSize,current_size_dur as dur,h.all_heap_size heapsize,h.start_ts - t.start_ts as startTime,h.end_ts - t.start_ts as endTime
from heap h left join trace_section t left join (select max(all_heap_size) maxheap from heap) a where  ipid = ${ipid} and startTime between ${startTs} and ${endTs};
`, {$ipid: ipid, $startTs: startTs, $endTs: endTs})
/*-------------------------------------------------------------------------------------*/
export const queryHeapPid = (): Promise<Array<any>> =>
    query("queryHeapPid", `select ipid,pid from heap h left join process p on h.ipid = p.id group by ipid,pid`, {})
/*-------------------------------------------------------------------------------------*/
export const queryHeapTable = (startTs: number, endTs: number, ipids: Array<number>): Promise<Array<HeapBean>> =>
    query("queryHeapTable", `select *,Allocations - Deallocations Total,AllocationSize - DeAllocationSize RemainingSize from (select f.file_path MoudleName,
       sum(case when h.event_type = 'AllocEvent' then 1 else 0 end) Allocations,
       sum(case when h.event_type = 'FreeEvent' then 1 else 0 end) Deallocations,
       sum(case when h.event_type = 'AllocEvent' then heap_size else 0 end) AllocationSize,
       sum(case when h.event_type = 'FreeEvent' then heap_size else 0 end) DeAllocationSize,
        f.symbol_name AllocationFunction
 from (select heap.start_ts - t.start_ts as startTime,* from heap
     left join trace_range t where ipid in (${ipids.join(",")}) and startTime between ${startTs} and ${endTs}) h
     left join (select * from heap_frame where depth = 0) f
     on f.eventId = h.eventId group by f.file_path)`,
        {ipids: ipids, $startTs: startTs, $endTs: endTs})



