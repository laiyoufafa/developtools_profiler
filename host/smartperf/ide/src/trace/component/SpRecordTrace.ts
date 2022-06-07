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

import {BaseElement, element} from "../../base-ui/BaseElement.js";
import "../../base-ui/popover/LitPopover.js"
import {LitMainMenuGroup} from "../../base-ui/menu/LitMainMenuGroup.js";
import {LitMainMenuItem} from "../../base-ui/menu/LitMainMenuItem.js";
import {SpRecordSetting} from "./setting/SpRecordSetting.js";
import {MenuItem} from "../../base-ui/menu/LitMainMenu.js";
import {SpProbesConfig} from "./setting/SpProbesConfig.js";
import {SpTraceCommand} from "./setting/SpTraceCommand.js";
import {
    CpuConfig,
    CreateSessionRequest,
    DiskioConfig,
    FpsConfig,
    HilogConfig,
    HiperfPluginConfig,
    levelFromJSON,
    MemoryConfig,
    NativeHookConfig,
    NetworkConfig,
    ProcessConfig,
    ProfilerPluginConfig,
    ProfilerSessionConfig,
    ProfilerSessionConfigBufferConfig,
    ProfilerSessionConfigBufferConfigPolicy,
    ProfilerSessionConfigMode,
    sysMeminfoTypeFromJSON,
    sysVMeminfoTypeFromJSON,
    TracePluginConfig,
    Type
} from "./setting/bean/ProfilerServiceTypes.js";
import {PluginConvertUtils} from "./setting/utils/PluginConvertUtils.js";
import {SpAllocations} from "./setting/SpAllocations.js";
import {SpRecordPerf} from "./setting/SpRecordPerf.js";

@element('sp-record-trace')
export class SpRecordTrace extends BaseElement {
    static MEM_INFO = ["MEMINFO_ACTIVE", "MEMINFO_ACTIVE_ANON", "MEMINFO_ACTIVE_FILE", "MEMINFO_ANON_PAGES", "MEMINFO_BUFFERS",
        "MEMINFO_CACHED", "MEMINFO_CMA_FREE", "MEMINFO_CMA_TOTAL", "MEMINFO_COMMIT_LIMIT", "MEMINFO_COMMITED_AS",
        "MEMINFO_DIRTY", "MEMINFO_INACTIVE", "MEMINFO_INACTIVE_ANON", "MEMINFO_INACTIVE_FILE",
        "MEMINFO_KERNEL_STACK", "MEMINFO_MAPPED", "MEMINFO_MEM_AVAILABLE", "MEMINFO_MEM_FREE", "MEMINFO_MEM_TOTAL",
        "MEMINFO_MLOCKED", "MEMINFO_PAGE_TABLES", "MEMINFO_SHMEM", "MEMINFO_SLAB", "MEMINFO_SLAB_RECLAIMABLE",
        "MEMINFO_SLAB_UNRECLAIMABLE", "MEMINFO_SWAP_CACHED", "MEMINFO_SWAP_FREE", "MEMINFO_SWAP_TOTAL",
        "MEMINFO_UNEVICTABLE", "MEMINFO_VMALLOC_CHUNK", "MEMINFO_VMALLOC_TOTAL", "MEMINFO_VMALLOC_USED",
        "MEMINFO_WRITEBACK", "MEMINFO_KERNEL_RECLAIMABLE"]
    static VMEM_INFO = ["VMEMINFO_UNSPECIFIED", "VMEMINFO_NR_FREE_PAGES", "VMEMINFO_NR_ALLOC_BATCH",
        "VMEMINFO_NR_INACTIVE_ANON", "VMEMINFO_NR_ACTIVE_ANON", "VMEMINFO_NR_INACTIVE_FILE",
        "VMEMINFO_NR_ACTIVE_FILE", "VMEMINFO_NR_UNEVICTABLE", "VMEMINFO_NR_MLOCK", "VMEMINFO_NR_ANON_PAGES",
        "VMEMINFO_NR_MAPPED", "VMEMINFO_NR_FILE_PAGES", "VMEMINFO_NR_DIRTY", "VMEMINFO_NR_WRITEBACK",
        "VMEMINFO_NR_SLAB_RECLAIMABLE", "VMEMINFO_NR_SLAB_UNRECLAIMABLE", "VMEMINFO_NR_PAGE_TABLE_PAGES",
        "VMEMINFO_NR_KERNEL_STACK", "VMEMINFO_NR_OVERHEAD", "VMEMINFO_NR_UNSTABLE", "VMEMINFO_NR_BOUNCE",
        "VMEMINFO_NR_VMSCAN_WRITE", "VMEMINFO_NR_VMSCAN_IMMEDIATE_RECLAIM", "VMEMINFO_NR_WRITEBACK_TEMP",
        "VMEMINFO_NR_ISOLATED_ANON", "VMEMINFO_NR_ISOLATED_FILE", "VMEMINFO_NR_SHMEM", "VMEMINFO_NR_DIRTIED",
        "VMEMINFO_NR_WRITTEN", "VMEMINFO_NR_PAGES_SCANNED", "VMEMINFO_WORKINGSET_REFAULT",
        "VMEMINFO_WORKINGSET_ACTIVATE", "VMEMINFO_WORKINGSET_NODERECLAIM", "VMEMINFO_NR_ANON_TRANSPARENT_HUGEPAGES",
        "VMEMINFO_NR_FREE_CMA", "VMEMINFO_NR_SWAPCACHE", "VMEMINFO_NR_DIRTY_THRESHOLD",
        "VMEMINFO_NR_DIRTY_BACKGROUND_THRESHOLD", "VMEMINFO_PGPGIN", "VMEMINFO_PGPGOUT", "VMEMINFO_PGPGOUTCLEAN",
        "VMEMINFO_PSWPIN", "VMEMINFO_PSWPOUT", "VMEMINFO_PGALLOC_DMA"]
    static VMEM_INFO_SECOND = ["VMEMINFO_PGALLOC_NORMAL", "VMEMINFO_PGALLOC_MOVABLE", "VMEMINFO_PGFREE", "VMEMINFO_PGACTIVATE",
        "VMEMINFO_PGDEACTIVATE", "VMEMINFO_PGFAULT", "VMEMINFO_PGMAJFAULT", "VMEMINFO_PGREFILL_DMA",
        "VMEMINFO_PGREFILL_NORMAL", "VMEMINFO_PGREFILL_MOVABLE", "VMEMINFO_PGSTEAL_KSWAPD_DMA",
        "VMEMINFO_PGSTEAL_KSWAPD_NORMAL", "VMEMINFO_PGSTEAL_KSWAPD_MOVABLE", "VMEMINFO_PGSTEAL_DIRECT_DMA",
        "VMEMINFO_PGSTEAL_DIRECT_NORMAL", "VMEMINFO_PGSTEAL_DIRECT_MOVABLE", "VMEMINFO_PGSCAN_KSWAPD_DMA",
        "VMEMINFO_PGSCAN_KSWAPD_NORMAL", "VMEMINFO_PGSCAN_KSWAPD_MOVABLE", "VMEMINFO_PGSCAN_DIRECT_DMA",
        "VMEMINFO_PGSCAN_DIRECT_NORMAL", "VMEMINFO_PGSCAN_DIRECT_MOVABLE", "VMEMINFO_PGSCAN_DIRECT_THROTTLE",
        "VMEMINFO_PGINODESTEAL", "VMEMINFO_SLABS_SCANNED", "VMEMINFO_KSWAPD_INODESTEAL",
        "VMEMINFO_KSWAPD_LOW_WMARK_HIT_QUICKLY", "VMEMINFO_KSWAPD_HIGH_WMARK_HIT_QUICKLY", "VMEMINFO_PAGEOUTRUN",
        "VMEMINFO_ALLOCSTALL", "VMEMINFO_PGROTATED", "VMEMINFO_DROP_PAGECACHE", "VMEMINFO_DROP_SLAB",
        "VMEMINFO_PGMIGRATE_SUCCESS", "VMEMINFO_PGMIGRATE_FAIL", "VMEMINFO_COMPACT_MIGRATE_SCANNED",
        "VMEMINFO_COMPACT_FREE_SCANNED", "VMEMINFO_COMPACT_ISOLATED", "VMEMINFO_COMPACT_STALL",
        "VMEMINFO_COMPACT_FAIL", "VMEMINFO_COMPACT_SUCCESS", "VMEMINFO_COMPACT_DAEMON_WAKE",
        "VMEMINFO_UNEVICTABLE_PGS_CULLED", "VMEMINFO_UNEVICTABLE_PGS_SCANNED", "VMEMINFO_UNEVICTABLE_PGS_RESCUED",
        "VMEMINFO_UNEVICTABLE_PGS_MLOCKED", "VMEMINFO_UNEVICTABLE_PGS_MUNLOCKED"]
    static VMEM_INFO_THIRD = [
        "VMEMINFO_UNEVICTABLE_PGS_CLEARED", "VMEMINFO_UNEVICTABLE_PGS_STRANDED", "VMEMINFO_NR_ZSPAGES",
        "VMEMINFO_NR_ION_HEAP", "VMEMINFO_NR_GPU_HEAP", "VMEMINFO_ALLOCSTALL_DMA", "VMEMINFO_ALLOCSTALL_MOVABLE",
        "VMEMINFO_ALLOCSTALL_NORMAL", "VMEMINFO_COMPACT_DAEMON_FREE_SCANNED",
        "VMEMINFO_COMPACT_DAEMON_MIGRATE_SCANNED", "VMEMINFO_NR_FASTRPC", "VMEMINFO_NR_INDIRECTLY_RECLAIMABLE",
        "VMEMINFO_NR_ION_HEAP_POOL", "VMEMINFO_NR_KERNEL_MISC_RECLAIMABLE", "VMEMINFO_NR_SHADOW_CALL_STACK_BYTES",
        "VMEMINFO_NR_SHMEM_HUGEPAGES", "VMEMINFO_NR_SHMEM_PMDMAPPED", "VMEMINFO_NR_UNRECLAIMABLE_PAGES",
        "VMEMINFO_NR_ZONE_ACTIVE_ANON", "VMEMINFO_NR_ZONE_ACTIVE_FILE", "VMEMINFO_NR_ZONE_INACTIVE_ANON",
        "VMEMINFO_NR_ZONE_INACTIVE_FILE", "VMEMINFO_NR_ZONE_UNEVICTABLE", "VMEMINFO_NR_ZONE_WRITE_PENDING",
        "VMEMINFO_OOM_KILL", "VMEMINFO_PGLAZYFREE", "VMEMINFO_PGLAZYFREED", "VMEMINFO_PGREFILL",
        "VMEMINFO_PGSCAN_DIRECT", "VMEMINFO_PGSCAN_KSWAPD", "VMEMINFO_PGSKIP_DMA", "VMEMINFO_PGSKIP_MOVABLE",
        "VMEMINFO_PGSKIP_NORMAL", "VMEMINFO_PGSTEAL_DIRECT", "VMEMINFO_PGSTEAL_KSWAPD", "VMEMINFO_SWAP_RA",
        "VMEMINFO_SWAP_RA_HIT", "VMEMINFO_WORKINGSET_RESTORE"
    ]
    // sys.mem.total   sys.mem.free sys.mem.buffers sys.mem.cached  sys.mem.shmem  sys.mem.slab  sys.mem.swap.total
    // sys.mem.swap.free sys.mem.mapped  sys.mem.vmalloc.used  sys.mem.page.tables  sys.mem.kernel.stack
    // sys.mem.active sys.mem.inactive  sys.mem.unevictable  sys.mem.vmalloc.total sys.mem.slab.unreclaimable
    // sys.mem.cma.total sys.mem.cma.free
    static ABALITY_MEM_INFO = ["MEMINFO_MEM_TOTAL", "MEMINFO_MEM_FREE", "MEMINFO_BUFFERS", "MEMINFO_CACHED",
        "MEMINFO_SHMEM", "MEMINFO_SLAB", "MEMINFO_SWAP_TOTAL", "MEMINFO_SWAP_FREE", "MEMINFO_MAPPED",
        "MEMINFO_VMALLOC_USED", "MEMINFO_PAGE_TABLES", "MEMINFO_KERNEL_STACK", "MEMINFO_ACTIVE", "MEMINFO_INACTIVE",
        "MEMINFO_UNEVICTABLE", "MEMINFO_VMALLOC_TOTAL", "MEMINFO_SLAB_UNRECLAIMABLE", "MEMINFO_CMA_TOTAL",
        "MEMINFO_CMA_FREE", "MEMINFO_KERNEL_RECLAIMABLE"]

    schedulingEvents = [
        "sched/sched_switch",
        "power/suspend_resume",
        "sched/sched_wakeup",
        "sched/sched_wakeup_new",
        "sched/sched_waking",
        "sched/sched_process_exit",
        "sched/sched_process_free",
        "task/task_newtask",
        "task/task_rename"
    ]
    powerEvents = [
        "regulator/regulator_set_voltage",
        "regulator/regulator_set_voltage_complete",
        "power/clock_enable",
        "power/clock_disable",
        "power/clock_set_rate",
        "power/suspend_resume"
    ]
    cpuFreqEvents = [
        "power/cpu_frequency",
        "power/cpu_idle",
        "power/suspend_resume"
    ]
    sysCallsEvents = [
        "raw_syscalls/sys_enter",
        "raw_syscalls/sys_exit"
    ]
    highFrequencyEvents = [
        "mm_event/mm_event_record",
        "kmem/rss_stat",
        "ion/ion_stat",
        "dmabuf_heap/dma_heap_stat",
        "kmem/ion_heap_grow",
        "kmem/ion_heap_shrink"
    ]
    advancedConfigEvents = ["sched/sched_switch",
        "sched/sched_wakeup",
        "sched/sched_wakeup_new",
        "sched/sched_waking",
        "sched/sched_process_exit",
        "sched/sched_process_free",
        "irq/irq_handler_entry",
        "irq/irq_handler_exit",
        "irq/softirq_entry",
        "irq/softirq_exit",
        "irq/softirq_raise",
        "power/clock_disable",
        "power/clock_enable",
        "power/clock_set_rate",
        "power/cpu_frequency",
        "power/cpu_idle",
        "clk/clk_disable",
        "clk/clk_disable_complete",
        "clk/clk_enable",
        "clk/clk_enable_complete",
        "clk/clk_set_rate",
        "clk/clk_set_rate_complete",
        "binder/binder_transaction",
        "binder/binder_transaction_alloc_buf",
        "binder/binder_transaction_received",
        "binder/binder_lock",
        "binder/binder_locked",
        "binder/binder_unlock",
        "workqueue/workqueue_execute_start",
        "workqueue/workqueue_execute_end",
        "oom/oom_score_adj_update",
        "ftrace/print"
    ]
    private _menuItems: Array<MenuItem> | undefined

    initElements(): void {
        let that = this
        let parentElement = this.parentNode as HTMLElement;
        parentElement.style.overflow = 'hidden'
        let recordSetting = new SpRecordSetting();
        let probesConfig = new SpProbesConfig();
        let traceCommand = new SpTraceCommand();
        let spAllocations = new SpAllocations();
        let spRecordPerf = new SpRecordPerf();
        let menuGroup = this.shadowRoot?.querySelector('#menu-group') as LitMainMenuGroup
        let appContent = this.shadowRoot?.querySelector('#app-content') as HTMLElement
        appContent.append(recordSetting)
        this._menuItems = [
            {
                title: "Record setting",
                icon: "properties",
                fileChoose: false,
                clickHandler: function (ev: InputEvent) {
                    appContent!.innerHTML = ""
                    appContent.append(recordSetting)
                }
            },
            {
                title: "Trace command",
                icon: "dbsetbreakpoint",
                fileChoose: false,
                clickHandler: function (ev: InputEvent) {
                    let maxDur = recordSetting.maxDur;
                    let bufferSize = recordSetting.bufferSize;
                    let bufferConfig: ProfilerSessionConfigBufferConfig = {
                        pages: bufferSize * 256,
                        policy: ProfilerSessionConfigBufferConfigPolicy.RECYCLE
                    }
                    let sessionConfig: ProfilerSessionConfig = {
                        buffers: [bufferConfig],
                        sessionMode: ProfilerSessionConfigMode.OFFLINE,
                        resultFile: "/data/local/tmp/hiprofiler_data.htrace",
                        resultMaxSize: 0,
                        sampleDuration: maxDur * 1000,
                        keepAliveTime: 0
                    }
                    let request: CreateSessionRequest = {
                        requestId: 1,
                        sessionConfig: sessionConfig,
                        pluginConfigs: []
                    }
                    let hasMonitorMemory = false;
                    if (probesConfig.traceConfig.length > 0) {
                        if (probesConfig.traceConfig.find(value => {
                            return value != "FPS" && value != "AbilityMonitor"
                        })) {
                            request.pluginConfigs.push(that.createHtracePluginConfig(that, probesConfig, recordSetting))
                        }
                        if (probesConfig.traceConfig.indexOf("FPS") != -1) {
                            request.pluginConfigs.push(that.createFpsPluginConfig())
                        }
                        if (probesConfig.traceConfig.indexOf("AbilityMonitor") != -1) {
                            hasMonitorMemory = true;
                            let processConfig: ProcessConfig = {
                                report_process_tree: true,
                                report_cpu: true,
                                report_diskio: true,
                                report_pss: true,
                            }
                            let processPlugin: ProfilerPluginConfig<ProcessConfig> = {
                                pluginName: "process-plugin",
                                sampleInterval: 1000,
                                configData: processConfig
                            }
                            request.pluginConfigs.push(processPlugin)
                            let cpuConfig: CpuConfig = {
                                pid: 0,
                                reportProcessInfo: true
                            }
                            let cpuPlugin: ProfilerPluginConfig<CpuConfig> = {
                                pluginName: "cpu-plugin",
                                sampleInterval: 1000,
                                configData: cpuConfig
                            }
                            request.pluginConfigs.push(cpuPlugin)
                            let diskIoConfig: DiskioConfig = {
                                reportIoStats: "IO_REPORT"
                            }
                            let diskIoPlugin: ProfilerPluginConfig<DiskioConfig> = {
                                pluginName: "diskio-plugin",
                                sampleInterval: 1000,
                                configData: diskIoConfig
                            }
                            request.pluginConfigs.push(diskIoPlugin)
                            let netWorkConfig: NetworkConfig = {
                                testFile: "/data/local/tmp/"
                            }
                            let netWorkPlugin: ProfilerPluginConfig<NetworkConfig> = {
                                pluginName: "network-plugin",
                                sampleInterval: 1000,
                                configData: netWorkConfig
                            }
                            request.pluginConfigs.push(netWorkPlugin)
                        }
                    }
                    let reportingFrequency: number;
                    if (maxDur > 20) {
                        reportingFrequency = 5
                    } else {
                        reportingFrequency = 2
                    }
                    if (probesConfig.memoryConfig.length > 0 || hasMonitorMemory) {
                        request.pluginConfigs.push(that.createMemoryPluginConfig(probesConfig, reportingFrequency, hasMonitorMemory))
                    }
                    if (spAllocations.appProcess != "") {
                        request.pluginConfigs.push(that.createNativePluginConfig(spAllocations, reportingFrequency))
                    }
                    if (spRecordPerf.startSamp) {
                        request.pluginConfigs.push(that.createHiperConfig(spRecordPerf, reportingFrequency))
                    }
                    appContent!.innerHTML = ""
                    appContent.append(traceCommand)
                    traceCommand.hdcCommon =
                        PluginConvertUtils.createHdcCmd(
                            PluginConvertUtils.BeanToCmdTxt(request, false), maxDur)
                }
            },
            {
                title: "Probes config", icon: "realIntentionBulb", fileChoose: false,
                clickHandler: function (ev: InputEvent) {
                    appContent!.innerHTML = ""
                    appContent.append(probesConfig)
                }
            },
            {
                title: "Allocations",
                icon: "externaltools",
                fileChoose: false,
                clickHandler: function (ev: InputEvent) {
                    appContent!.innerHTML = ""
                    appContent.append(spAllocations)
                }
            },
            {
                title: "Hiperf", icon: "realIntentionBulb", fileChoose: false,
                clickHandler: function (ev: InputEvent) {
                    appContent!.innerHTML = ""
                    appContent.append(spRecordPerf)
                }
            }
        ]
        this._menuItems?.forEach(item => {
            let th = new LitMainMenuItem();
            th.setAttribute('icon', item.icon || "");
            th.setAttribute('title', item.title || "");
            th.style.height = "60px"
            th.style.fontFamily = "Helvetica-Bold"
            th.style.fontSize = "16px"
            th.style.lineHeight = "28px"
            th.style.fontWeight = "700"
            th.style.opacity = "0.9"
            th.removeAttribute('file');
            th.addEventListener('click', e => {
                if (item.clickHandler) {
                    item.clickHandler(item)
                }
            })
            menuGroup.appendChild(th);
        })
    }

    createTraceEvents(traceConfig: Array<string>): Array<string> {
        let traceEvents = new Set<string>();
        traceConfig.forEach(config => {
                switch (config) {
                    case "Scheduling details":
                        this.schedulingEvents.forEach((eve: string) => {
                            traceEvents.add(eve)
                        });
                        break;
                    case "CPU Frequency and idle states":
                        this.cpuFreqEvents.forEach((eve: string) => {
                            traceEvents.add(eve)
                        });
                        break;
                    case "High frequency memory":
                        this.highFrequencyEvents.forEach((eve: string) => {
                            traceEvents.add(eve)
                        });
                        break;
                    case "Advanced ftrace config":
                        this.advancedConfigEvents.forEach((eve: string) => {
                            traceEvents.add(eve)
                        });
                        break;
                    case "Syscalls":
                        this.sysCallsEvents.forEach((eve: string) => {
                            traceEvents.add(eve)
                        });
                        break;
                    case "Board voltages & frequency":
                        this.powerEvents.forEach((eve: string) => {
                            traceEvents.add(eve)
                        });
                        break;
                }
            }
        )
        let ftraceEventsArray: string[] = [];
        for (const ftraceEvent of traceEvents) {
            ftraceEventsArray.push(ftraceEvent)
        }
        return ftraceEventsArray
    }

    initHtml(): string {
        return `
        <style>
        :host{
            display: block;
            width: 100%;
            height: 100%;
            background-color: var(--dark-background5,#F6F6F6);
        }
        .container {
            display: grid;
            grid-template-columns: 1fr;
            /*
            grid-template-rows: 100px 1fr;
            */
            grid-template-rows:1fr;
            background-color: var(--dark-background5,#F6F6F6);
            min-height: 100%;
        }

        .header {
            display: grid;
            grid-template-columns: 1fr 1fr;
            grid-template-rows: 1fr 1fr;
            grid-gap: 10px;
            padding-left: 20px;
            padding-top: 30px;
            padding-bottom: 20px;
            background-color: #FFFFFF;
            width: 100%;
        }

        .span-col-2{
             grid-column: span 2 / auto;
             height: 15px;
        }

        .header-right {
           display: flex;
           margin-left: auto;
           margin-right: 5%;
        }
        .header-des{
          font-family: PingFangSC-Regular;
          font-size: 1em;
          color:  var(--dark-background3,#999999);
          text-align: left;
          font-weight: 400;
        }

        .target {
           font-family: Helvetica;
           font-size: 1em;
           color: #212121;
           line-height: 16px;
           font-weight: 400;
        }

        .select{
           width: 196px;
           height: 32px;
           margin-left: 14px;
           margin-right: 24px;
           border: 1px solid #D5D5D5;
        }
        .add {
           width: 164px;
           height: 32px;
           border: 1px solid cornflowerblue
        }
        .record {
           background: #3391FF;
           border-radius: 1px;
           border-color:rgb(0,0,0,0.1);
           width: 96px;
           height: 32px;
           margin-right: 0px;
           font-family: Helvetica;
           font-size: 1em;
           color: #FFFFFF;
           text-align: center;
           line-height: 20px;
           font-weight: 400;
        }

        .body{
            width: 90%;
            margin-left: 3%;
            margin-top: 2%;
            margin-bottom: 2%;
            display: grid;
            grid-template-columns: min-content  1fr;
            background-color: var(--dark-background3,#FFFFFF);
            border-radius: 16px 16px 16px 16px;
        }

        .menugroup{
           height: 100%;
           background: var(--dark-background3,#FFFFFF);
        }
        .menuitem{
          background: var(--dark-background3,#FFFFFF);
        }
        .content{
          background: var(--dark-background3,#FFFFFF);
          border-style: none none none solid;
          border-width: 1px;
          border-color: rgba(166,164,164,0.2);
          border-radius: 0px 16px 16px 0px;
        }
        </style>
        <div class="container">
         <div class="header" style="display: none">
              <div>
                <span class="target">Target Platform:<span>
                <select class="select">
                    <option class="select" value="volvo">Volvo</option>
                    <option class="select" value="saab">Saab</option>
                    <option class="select" value="opel">Opel</option>
                    <option class="select" value="audi">Audi</option>
                </select>
               <button class="add">Add Device</button>
              </div>
              <div class="header-right">
                <button class="record">Record</button>
              </div>
              <div class="span-col-2" >
                  <span class="header-des">It looks like you didn’t add any probes. Please add at least one to get a non-empty trace.</span>
              </div>
         </div>
         <div class="body">
            <lit-main-menu-group class="menugroup" id= "menu-group" title="" nocollapsed radius></lit-main-menu-group>
            <div id="app-content" class="content">
            </div>
         </div>
        </div>
        `;
    }

    private createHilogConfig(probesConfig: SpProbesConfig, reportingFrequency: number) {
        let hilogConfig: HilogConfig = {
            deviceType: Type.HI3516,
            logLevel: levelFromJSON(probesConfig.hilogConfig[0]),
            needClear: true
        }
        let hilogConfigProfilerPluginConfig: ProfilerPluginConfig<HilogConfig> = {
            pluginName: "hilog-plugin",
            sampleInterval: reportingFrequency * 1000,
            configData: hilogConfig,
        }
        return hilogConfigProfilerPluginConfig;
    }

    private createHiperConfig(spRecordPerf: SpRecordPerf, reportingFrequency: number) {
        let perfConfig = spRecordPerf.getPerfConfig();
        let recordArgs = "";
        recordArgs = recordArgs + "-f " + perfConfig?.frequency;
        if (perfConfig?.process && !perfConfig?.process.includes("ALL") && perfConfig?.process.length > 0) {
            recordArgs = recordArgs + " -p " + perfConfig?.process;
        } else {
            recordArgs = recordArgs + " -a ";
        }
        if (perfConfig?.cpu && !perfConfig?.cpu.includes("ALL") && perfConfig?.cpu.length > 0) {
            recordArgs = recordArgs + " -c " + perfConfig?.cpu;
        }
        if (perfConfig?.cpuPercent != 0) {
            recordArgs = recordArgs + " --cpu-limit " + perfConfig?.cpuPercent;
        }
        if (perfConfig?.eventList && !perfConfig?.eventList.includes("ALL") && perfConfig?.eventList.length > 0) {
            recordArgs = recordArgs + " -e " + perfConfig?.eventList;
        }
        if (perfConfig?.callStack != "none") {
            recordArgs = recordArgs + " --call-stack " + perfConfig?.callStack
        }

        if (perfConfig?.branch != "none") {
            recordArgs = recordArgs + " -j " + perfConfig?.branch
        }

        if (perfConfig?.clockType) {
            recordArgs = recordArgs + " --clockid " + perfConfig?.clockType
        }

        if (perfConfig?.isOffCpu) {
            recordArgs = recordArgs + " --offcpu"
        }

        if (perfConfig?.noInherit) {
            recordArgs = recordArgs + " --no-inherit"
        }

        if (perfConfig?.mmap) {
            recordArgs = recordArgs + " -m " + perfConfig.mmap;
        }

        let hiPerf: HiperfPluginConfig = {
            isRoot: false,
            outfileName: "/data/local/tmp/perf.data",
            recordArgs: recordArgs
        }
        let hiPerfPluginConfig: ProfilerPluginConfig<HiperfPluginConfig> = {
            pluginName: "hiperf-plugin",
            sampleInterval: reportingFrequency * 1000,
            configData: hiPerf,
        }
        return hiPerfPluginConfig;
    }

    private createNativePluginConfig(spAllocations: SpAllocations, reportingFrequency: number) {
        let appProcess = spAllocations.appProcess;
        let re = /^[0-9]+.?[0-9]*/;
        let pid = 0;
        let processName = "";
        if (re.test(appProcess)) {
            pid = Number(appProcess);
        } else {
            processName = appProcess;
        }
        let nativeConfig: NativeHookConfig = {
            pid: pid,
            saveFile: false,
            fileName: "",
            filterSize: spAllocations.filter,
            smbPages: spAllocations.shared,
            maxStackDepth: spAllocations.unwind,
            processName: processName
        }
        let nativePluginConfig: ProfilerPluginConfig<NativeHookConfig> = {
            pluginName: "nativehook",
            sampleInterval: reportingFrequency * 1000,
            configData: nativeConfig,
        }
        return nativePluginConfig;
    }

    private createMemoryPluginConfig(probesConfig: SpProbesConfig, reportingFrequency: number, hasMonitorMemory: boolean) {
        let memoryconfig: MemoryConfig = {
            reportProcessTree: true,
            reportSysmemMemInfo: true,
            sysMeminfoCounters: [],
            reportSysmemVmemInfo: true,
            sysVmeminfoCounters: [],
            reportProcessMemInfo: true,
            reportAppMemInfo: false,
            reportAppMemByMemoryService: false,
            pid: []
        }
        if (hasMonitorMemory) {
            SpRecordTrace.ABALITY_MEM_INFO.forEach(va => {
                memoryconfig.sysMeminfoCounters.push(sysMeminfoTypeFromJSON(va));
            })
        }
        probesConfig.memoryConfig.forEach(value => {
            if (value.indexOf("Kernel meminfo") != -1) {
                if (hasMonitorMemory) {
                    memoryconfig.sysMeminfoCounters = [];
                }
                SpRecordTrace.MEM_INFO.forEach(va => {
                    memoryconfig.sysMeminfoCounters.push(sysMeminfoTypeFromJSON(va));
                })
            }
            if (value.indexOf("Virtual memory stats") != -1) {
                SpRecordTrace.VMEM_INFO.forEach((me => {
                    memoryconfig.sysVmeminfoCounters.push(sysVMeminfoTypeFromJSON(me))
                }))
                SpRecordTrace.VMEM_INFO_SECOND.forEach((me => {
                    memoryconfig.sysVmeminfoCounters.push(sysVMeminfoTypeFromJSON(me))
                }))
                SpRecordTrace.VMEM_INFO_THIRD.forEach((me => {
                    memoryconfig.sysVmeminfoCounters.push(sysVMeminfoTypeFromJSON(me))
                }))
            }
        })
        let profilerPluginConfig: ProfilerPluginConfig<MemoryConfig> = {
            pluginName: "memory-plugin",
            sampleInterval: reportingFrequency * 1000,
            configData: memoryconfig,
        }
        return profilerPluginConfig;
    }

    private createFpsPluginConfig() {
        let fpsConfig: FpsConfig = {
            reportFps: true
        }
        let fpsPlugin: ProfilerPluginConfig<FpsConfig> = {
            pluginName: "hidump-plugin",
            sampleInterval: 1000,
            configData: fpsConfig
        }
        return fpsPlugin;
    }

    private createHtracePluginConfig(that: this, probesConfig: SpProbesConfig, recordSetting: SpRecordSetting) {
        let tracePluginConfig: TracePluginConfig = {
            ftraceEvents: that.createTraceEvents(probesConfig.traceConfig),
            hitraceCategories: [],
            hitraceApps: [],
            bufferSizeKb: 2048,
            flushIntervalMs: 1000,
            flushThresholdKb: 4096,
            parseKsyms: true,
            clock: "mono",
            tracePeriodMs: 200,
            rawDataPrefix: "",
            traceDurationMs: 0,
            debugOn: false,
            hitraceTime: recordSetting.maxDur
        }
        if (probesConfig.traceEvents.length > 0) {
            tracePluginConfig.hitraceCategories = probesConfig.traceEvents
        }
        let htraceProfilerPluginConfig: ProfilerPluginConfig<TracePluginConfig> = {
            pluginName: "ftrace-plugin",
            sampleInterval: 1000,
            configData: tracePluginConfig
        }
        return htraceProfilerPluginConfig;
    }
}