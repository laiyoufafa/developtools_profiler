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

import {BaseElement, element} from "../../../../base-ui/BaseElement.js";
import {LitTabs} from "../../../../base-ui/tabs/lit-tabs.js";
import "../../../../base-ui/tabs/lit-tabpane.js";
import {CpuStruct} from "../../../bean/CpuStruct.js";
import "../../../../base-ui/table/lit-table.js";
import {LitTabpane} from "../../../../base-ui/tabs/lit-tabpane.js";
import "../sheet/TabPaneCpu.js";
import "../sheet/TabPaneThreadStates.js"
import "../sheet/TabPaneSlices.js"
import "../sheet/TabPaneCounter.js"
import "../sheet/TabPaneCpuByProcess.js"
import "../sheet/TabPaneCpuByThread.js"
import "../sheet/TabPaneFps.js"
import "../sheet/TabPaneSPT.js"
import "../sheet/TabPanePTS.js"
import "../sheet/TabPaneContextSwitch.js"
import "../sheet/TabPaneThreadSwitch.js"
import "../sheet/TabPaneCpuUsage.js";
import "../sheet/TabPaneBoxChild.js";
import "../sheet/TabPaneHeap.js";
import "../sheet/TabPaneNMStatstics.js";
import "../sheet/TabPaneNMCallInfo.js";
import "../sheet/TabPaneNMemory.js";
import "../sheet/TabPaneNMSampleList.js";
import "../sheet/TabPerfProfile.js";
import "../sheet/TabPerfSampleList.js";
import {BoxJumpParam, SelectionParam} from "../../../bean/BoxSelection.js";
import {TabPaneThreadStates} from "../sheet/TabPaneThreadStates.js";
import {TabPaneCpuByProcess} from "../sheet/TabPaneCpuByProcess.js";
import {TabPaneCpuByThread} from "../sheet/TabPaneCpuByThread.js";
import {TabPaneSlices} from "../sheet/TabPaneSlices.js";
import {TabPaneSPT} from "../sheet/TabPaneSPT.js";
import {TabPanePTS} from "../sheet/TabPanePTS.js";
import {TabPaneContextSwitch} from "../sheet/TabPaneContextSwitch.js";
import {TabPaneThreadSwitch} from "../sheet/TabPaneThreadSwitch.js";
import {TabPaneBoxChild} from "../sheet/TabPaneBoxChild.js";
import {TabPaneCounter} from "../sheet/TabPaneCounter.js";
import "../sheet/TabPaneCurrentSelection.js";
import {TabPaneCurrentSelection} from "../sheet/TabPaneCurrentSelection.js";
import {FuncStruct} from "../../../bean/FuncStruct.js";
import {ProcessMemStruct} from "../../../bean/ProcessMemStruct.js";
import {ThreadStruct} from "../../../bean/ThreadStruct.js";
import {TabPaneFps} from "../sheet/TabPaneFps.js";
import {TabPaneCpuUsage} from "../sheet/TabPaneCpuUsage.js";
import "../timer-shaft/TabPaneFlag.js";
import {TabPaneFlag} from "../timer-shaft/TabPaneFlag.js";
import {Flag} from "../timer-shaft/Flag.js";
import {TabPaneHeap} from "../sheet/TabPaneHeap.js";
import {TabPaneNMStatstics} from "../sheet/TabPaneNMStatstics.js";
import {TabPaneNMCallInfo} from "../sheet/TabPaneNMCallInfo.js";
import {TabPaneNMemory} from "../sheet/TabPaneNMemory.js";
import {TabPaneNMSampleList} from "../sheet/TabPaneNMSampleList.js";
import {WakeupBean} from "../../../bean/WakeupBean.js";
import {LitIcon} from "../../../../base-ui/icon/LitIcon.js";
import {TabPaneCpuAbility} from "../sheet/TabPaneCpuAbility.js";
import "../sheet/TabPaneCpuAbility.js";
import {TabPaneDiskAbility} from "../sheet/TabPaneDiskAbility.js";
import "../sheet/TabPaneDiskAbility.js";
import {TabPaneMemoryAbility} from "../sheet/TabPaneMemoryAbility.js";
import "../sheet/TabPaneMemoryAbility.js";
import {TabPaneNetworkAbility} from "../sheet/TabPaneNetworkAbility.js";
import "../sheet/TabPaneNetworkAbility.js";
import {TabPaneHistoryProcesses} from "../sheet/TabPaneHistoryProcesses.js";
import "../sheet/TabPaneHistoryProcesses.js";
import {TabPaneLiveProcesses} from "../sheet/TabPaneLiveProcesses.js";
import "../sheet/TabPaneLiveProcesses.js";
import {TabpanePerfProfile} from "../sheet/TabPerfProfile.js";
import {TabPanePerfSample} from "../sheet/TabPerfSampleList.js";

@element("trace-sheet")
export class TraceSheet extends BaseElement {
    private litTabs: LitTabs | undefined | null
    private nav: HTMLDivElement | undefined | null
    private tabCurrentSelection: LitTabpane | undefined | null
    private tabBoxCpuThread: LitTabpane | undefined | null
    private tabBoxCpuProcess: LitTabpane | undefined | null
    private tabBoxThreadStates: LitTabpane | undefined | null
    private tabBoxSlices: LitTabpane | undefined | null
    private tabBoxCounters: LitTabpane | undefined | null
    private tabBoxFps: LitTabpane | undefined | null
    private tabBoxSPT: LitTabpane | undefined | null
    private tabBoxPTS: LitTabpane | undefined | null
    private tabBoxContextSwitch: LitTabpane | undefined | null
    private tabBoxThreadSwitch: LitTabpane | undefined | null
    private tabBoxCpuUsage: LitTabpane | undefined | null
    private tabBoxChild: LitTabpane | undefined | null
    private tabBoxFlag: TabPaneFlag | undefined | null
    private tabBoxHeap: LitTabpane | undefined | null
    private tabBoxNMStatistics: LitTabpane | undefined | null
    private tabBoxNMCallInfo: LitTabpane | undefined | null
    private tabBoxNMemory: LitTabpane | undefined | null
    private tabBoxNMSample: LitTabpane | undefined | null
    private tabBoxPerfProfile: LitTabpane | undefined | null
    private tabBoxPerfSample: LitTabpane | undefined | null
    private tabSPT: TabPaneSPT | undefined | null
    private tabPTS: TabPanePTS | undefined | null
    private tabCs: TabPaneContextSwitch | undefined | null
    private tabTs: TabPaneThreadSwitch | undefined | null
    private tabChild: TabPaneBoxChild | undefined | null
    private tabNativeStatistics: TabPaneNMStatstics | undefined | null
    private tabNativeMemory: TabPaneNMemory | undefined | null
    private tabNativeCallInfo: TabPaneNMCallInfo | undefined | null
    private tabNativeSample: TabPaneNMSampleList | undefined | null
    private tabPerfProfile: TabpanePerfProfile | undefined | null
    private tabPerfSample: TabPanePerfSample | undefined | null
    private currentKey: string = "1";
    private selection: SelectionParam | undefined | null;

    private tabBoxLiveProcesses: LitTabpane | undefined | null
    private tabBoxHistoryProcesses: LitTabpane | undefined | null
    private tabBoxSystemCpu: LitTabpane | undefined | null
    private tabBoxSystemMemory: LitTabpane | undefined | null
    private tabBoxSystemDiskIo: LitTabpane | undefined | null
    private tabBoxSystemNetwork: LitTabpane | undefined | null

    private tabLiveProcesses: TabPaneLiveProcesses | undefined | null
    private tabHistoryProcesses: TabPaneHistoryProcesses | undefined | null
    private tabSystemCpu: TabPaneCpuAbility | undefined | null
    private tabSystemMemory: TabPaneMemoryAbility | undefined | null
    private tabSystemDiskIo: TabPaneDiskAbility | undefined | null
    private tabSystemNetwork: TabPaneNetworkAbility | undefined | null

    static get observedAttributes() {
        return ['mode'];
    }

    initElements(): void {
        this.litTabs = this.shadowRoot?.querySelector("#tabs");
        this.tabCurrentSelection = this.shadowRoot?.querySelector("#current-selection");
        this.tabBoxCpuThread = this.shadowRoot?.querySelector("#box-cpu-thread");
        this.tabBoxCpuProcess = this.shadowRoot?.querySelector("#box-cpu-process");
        this.tabBoxThreadStates = this.shadowRoot?.querySelector("#box-thread-states");
        this.tabBoxSlices = this.shadowRoot?.querySelector("#box-slices");
        this.tabBoxCounters = this.shadowRoot?.querySelector("#box-counters");
        this.tabBoxFps = this.shadowRoot?.querySelector("#box-fps");
        this.tabBoxSPT = this.shadowRoot?.querySelector("#box-spt");
        this.tabBoxPTS = this.shadowRoot?.querySelector("#box-pts");
        this.tabBoxContextSwitch = this.shadowRoot?.querySelector("#box-cs");
        this.tabBoxThreadSwitch = this.shadowRoot?.querySelector("#box-ts");
        this.tabBoxCpuUsage = this.shadowRoot?.querySelector("#box-cpu-usage");
        this.tabBoxChild = this.shadowRoot?.querySelector("#box-cpu-child");
        this.tabBoxFlag = this.shadowRoot?.querySelector<TabPaneFlag>("#box-flag");
        this.tabBoxHeap = this.shadowRoot?.querySelector("#box-heap");

        this.tabBoxNMStatistics = this.shadowRoot?.querySelector("#box-native-statstics");
        this.tabBoxNMCallInfo = this.shadowRoot?.querySelector("#box-native-callinfo");
        this.tabBoxNMemory = this.shadowRoot?.querySelector("#box-native-memory");
        this.tabBoxNMSample = this.shadowRoot?.querySelector("#box-native-sample");

        this.tabBoxLiveProcesses = this.shadowRoot?.querySelector("#box-live-processes-child");
        this.tabBoxHistoryProcesses = this.shadowRoot?.querySelector("#box-history-processes-child");
        this.tabBoxSystemCpu = this.shadowRoot?.querySelector("#box-system-cpu-child");
        this.tabBoxSystemMemory = this.shadowRoot?.querySelector("#box-system-memory-child");
        this.tabBoxSystemDiskIo = this.shadowRoot?.querySelector("#box-system-diskIo-child");
        this.tabBoxSystemNetwork = this.shadowRoot?.querySelector("#box-system-network-child");

        this.tabBoxPerfProfile = this.shadowRoot?.querySelector("#box-perf-profile");
        this.tabBoxPerfSample = this.shadowRoot?.querySelector("#box-perf-sample");

        this.tabSPT = this.shadowRoot!.querySelector<TabPaneSPT>('#tab-spt');
        this.tabPTS = this.shadowRoot!.querySelector<TabPanePTS>('#tab-pts');
        this.tabCs = this.shadowRoot!.querySelector<TabPaneContextSwitch>('#tab-cs');
        this.tabTs = this.shadowRoot!.querySelector<TabPaneThreadSwitch>('#tab-ts');
        this.tabChild = this.shadowRoot!.querySelector<TabPaneBoxChild>('#tab-box-child');

        this.tabNativeStatistics = this.shadowRoot!.querySelector<TabPaneNMStatstics>('#tab-box-native-stats');
        this.tabNativeCallInfo = this.shadowRoot!.querySelector<TabPaneNMCallInfo>('#tab-box-native-callinfo');
        this.tabNativeMemory = this.shadowRoot!.querySelector<TabPaneNMemory>('#tab-box-native-memory');
        this.tabNativeSample = this.shadowRoot!.querySelector<TabPaneNMSampleList>('#tab-box-native-sample');

        this.tabLiveProcesses = this.shadowRoot?.querySelector<TabPaneLiveProcesses>("#tab-live-processes-child");
        this.tabHistoryProcesses = this.shadowRoot?.querySelector<TabPaneHistoryProcesses>("#tab-history-processes-child");
        this.tabSystemCpu = this.shadowRoot?.querySelector<TabPaneCpuAbility>("#tab-system-cpu-child");
        this.tabSystemMemory = this.shadowRoot?.querySelector<TabPaneMemoryAbility>("#tab-system-memory-child");
        this.tabSystemDiskIo = this.shadowRoot?.querySelector<TabPaneDiskAbility>("#tab-system-diskIo-child");
        this.tabSystemNetwork = this.shadowRoot?.querySelector<TabPaneNetworkAbility>("#tab-system-network-child");

        this.tabPerfProfile = this.shadowRoot!.querySelector<TabpanePerfProfile>('#tab-box-perf-profile');
        this.tabPerfSample = this.shadowRoot!.querySelector<TabPanePerfSample>('#tab-box-perf-sample');
        let minBtn = this.shadowRoot?.querySelector("#min-btn");
        minBtn?.addEventListener('click', (e) => {
        })
        this.litTabs!.onTabClick = (e: any) => {
            this.loadTabPaneData(e.detail.key)
        }
        this.litTabs!.addEventListener("close-handler", (e) => {
            this.recoveryBoxSelection();
            this.tabBoxChild!.hidden = true;
            this.litTabs?.activeByKey(this.currentKey);
        })
        this.tabSPT!.addEventListener("row-click", (e) => {
            this.jumpBoxChild("11", e)
        })
        this.tabPTS!.addEventListener("row-click", (e) => {
            this.jumpBoxChild("12", e)
        })
        this.tabCs!.addEventListener("row-click", (e) => {
            this.jumpBoxChild("13", e)
        })
        this.tabTs!.addEventListener("row-click", (e) => {
            this.jumpBoxChild("14", e)
        })
        this.tabNativeStatistics!.addEventListener("row-click", (e) => {
            // @ts-ignore
            this.selection!.statisticsSelectData = e.detail
            this.tabNativeMemory?.fromStastics(this.selection)
            this.litTabs?.activeByKey("18");
        })
    }

    connectedCallback() {
        this.nav = this.shadowRoot?.querySelector("#tabs")?.shadowRoot?.querySelector('#nav')
        let tabs: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector('#tabs')
        let navRoot: HTMLDivElement | null | undefined = this.shadowRoot?.querySelector("#tabs")?.shadowRoot?.querySelector('.nav-root')

        let search: HTMLDivElement | undefined | null = document.querySelector("body > sp-application")?.shadowRoot?.querySelector("div > div.search-container")
        let timerShaft: HTMLDivElement | undefined | null = this.parentElement?.querySelector(".timer-shaft")

        let borderTop: number = 1;
        let initialHeight = {
            tabs: `calc(30vh + 39px)`,
            node: "30vh"
        }
        this.nav!.onmousedown = (event) => {
            let litTabpane: NodeListOf<HTMLDivElement> | undefined | null = this.shadowRoot?.querySelectorAll("#tabs > lit-tabpane")
            let preY = event.pageY;

            let preHeight = tabs!.offsetHeight;

            document.onmousemove = function (event) {
                let moveY: number;

                moveY = preHeight - (event.pageY - preY)
                litTabpane!.forEach((node: HTMLDivElement, b) => {
                    if (navRoot!.offsetHeight <= moveY && (search!.offsetHeight + timerShaft!.offsetHeight + borderTop) <= (window.innerHeight - moveY)) {
                        tabs!.style.height = moveY + "px"
                        node!.style.height = (moveY - navRoot!.offsetHeight) + "px"
                        tabsPackUp!.name = "down"
                    } else if (navRoot!.offsetHeight >= moveY) {
                        tabs!.style.height = navRoot!.offsetHeight + "px"
                        node!.style.height = "0px"
                        tabsPackUp!.name = "up"
                    } else if ((search!.offsetHeight + timerShaft!.offsetHeight + borderTop) >= (window.innerHeight - moveY)) {
                        tabs!.style.height = (window.innerHeight - search!.offsetHeight - timerShaft!.offsetHeight - borderTop) + "px"
                        node!.style.height = (window.innerHeight - search!.offsetHeight - timerShaft!.offsetHeight - navRoot!.offsetHeight - borderTop) + "px"
                        tabsPackUp!.name = "down"
                    }
                })

            }
            document.onmouseup = function (event) {
                litTabpane!.forEach((node: HTMLDivElement, b) => {
                    if (node!.style.height !== "0px" && tabs!.style.height != "") {
                        initialHeight.node = node!.style.height;
                        initialHeight.tabs = tabs!.style.height;
                    }
                })
                this.onmousemove = null;
                this.onmouseup = null;
            }
        }
        let tabsOpenUp: LitIcon | undefined | null = this.shadowRoot?.querySelector<LitIcon>("#tabs > div > lit-icon:nth-child(1)")
        let tabsPackUp: LitIcon | undefined | null = this.shadowRoot?.querySelector<LitIcon>("#tabs > div > lit-icon:nth-child(2)")
        tabsOpenUp!.onclick = (e) => {
            tabs!.style.height = (window.innerHeight - search!.offsetHeight - timerShaft!.offsetHeight - borderTop) + "px"
            let litTabpane: NodeListOf<HTMLDivElement> | undefined | null = this.shadowRoot?.querySelectorAll("#tabs > lit-tabpane")
            litTabpane!.forEach((node: HTMLDivElement, b) => {
                node!.style.height = (window.innerHeight - search!.offsetHeight - timerShaft!.offsetHeight - navRoot!.offsetHeight - borderTop) + "px"
                initialHeight.node = node!.style.height;
            })
            initialHeight.tabs = tabs!.style.height;
            tabsPackUp!.name = "down"
        }
        tabsPackUp!.onclick = (e) => {
            let litTabpane: NodeListOf<HTMLDivElement> | undefined | null = this.shadowRoot?.querySelectorAll("#tabs > lit-tabpane")
            if (tabsPackUp!.name == "down") {
                tabs!.style.height = navRoot!.offsetHeight + "px"
                litTabpane!.forEach((node: HTMLDivElement, b) => {
                    node!.style.height = "0px"
                })
                tabsPackUp!.name = "up"
            } else {
                tabsPackUp!.name = "down"
                tabs!.style.height = initialHeight.tabs;
                litTabpane!.forEach((node: HTMLDivElement, b) => {
                    node!.style.height = initialHeight.node;
                })
            }
        }
    }

    initHtml(): string {
        return `
        <style>
            :host([mode='hidden']){
                display: none;
            }
            :host{
                display: block;
                background-color: rebeccapurple;
            }

            .tabHeight{
                height: 30vh;
                background-color: var(--dark-background,#FFFFFF);
            }

            </style>
            <div style="border-top: 0.5px solid var(--dark-border1,#D5D5D5);">
                <lit-tabs id="tabs" position="top-left" activekey="1" mode="card" >
                    <div slot="right" style="margin: 0 10px; color: var(--dark-icon,#606060)">
                        <lit-icon id="max-btn" name="vertical-align-top" style="font-weight: bold;cursor: pointer;" size="20">
                        </lit-icon>
                        <lit-icon id="min-btn" name="down" style="font-weight: bold;cursor: pointer;" size="20">
                        </lit-icon>
                    </div>
                    <lit-tabpane id="current-selection" key="1" hidden tab="Current Selection" class="tabHeight">
                        <tabpane-current-selection id="tabpane-cpu">
                        </tabpane-current-selection>
                    </lit-tabpane>
                    <lit-tabpane id="box-cpu-thread" key="2" hidden tab="CPU by thread" class="tabHeight">
                        <tabpane-cpu-thread id="tab-cpu-thread">
                        </tabpane-cpu-thread>
                    </lit-tabpane>
                    <lit-tabpane id="box-cpu-process" key="3" hidden tab="CPU by process" class="tabHeight">
                        <tabpane-cpu-process id="tab-cpu-process">
                        </tabpane-cpu-process>
                    </lit-tabpane>
                    <lit-tabpane id="box-thread-states" key="4" hidden tab="Thread States" class="tabHeight">
                        <tabpane-thread-states id="tab-thread-states" >
                        </tabpane-thread-states>
                    </lit-tabpane>
                    <lit-tabpane id="box-slices" key="5" hidden tab="Slices" class="tabHeight">
                        <tabpane-slices id="tab-slices">
                        </tabpane-slices>
                    </lit-tabpane>
                    <lit-tabpane id="box-counters" key="6" hidden tab="Counters" class="tabHeight">
                        <tabpane-counter id="tab-counters">
                        </tabpane-counter>
                    </lit-tabpane>
                    <lit-tabpane id="box-fps" key="7" hidden tab="FPS" class="tabHeight">
                        <tabpane-fps id="tab-fps">
                        </tabpane-fps>
                    </lit-tabpane>
                    <lit-tabpane id="box-cpu-usage" key="8" hidden tab="CPU Usage" class="tabHeight">
                        <tabpane-cpu-usage id="tab-cpu-usage">
                        </tabpane-cpu-usage>
                    </lit-tabpane>
                    <lit-tabpane id="box-heap" key="9" hidden tab="Native Hook" class="tabHeight">
                        <tabpane-heap id="tab-heap">
                        </tabpane-heap>
                    </lit-tabpane>
                    <lit-tabpane id="box-flag" key="10" hidden tab="Current Selection" class="tabHeight">
                        <tabpane-flag id="tab-flag">
                        </tabpane-flag>
                    </lit-tabpane>

                    <lit-tabpane id="box-spt" key="11" hidden tab="States List" class="tabHeight">
                        <tabpane-spt id="tab-spt">
                        </tabpane-spt>
                    </lit-tabpane>
                    <lit-tabpane id="box-cs" key="13" hidden tab="Switches List" class="tabHeight">
                        <tabpane-context-switch id="tab-cs">
                        </tabpane-context-switch>
                    </lit-tabpane>
                    <lit-tabpane id="box-pts" key="12" hidden tab="Thread States" class="tabHeight">
                        <tabpane-pts id="tab-pts">
                        </tabpane-pts>
                    </lit-tabpane>
                    <lit-tabpane id="box-ts" key="14" hidden tab="Thread Switches" class="tabHeight">
                        <tabpane-thread-switch id="tab-ts"><
                        /tabpane-thread-switch>
                    </lit-tabpane>
                    <lit-tabpane id="box-cpu-child" key="15" hidden tab="" closeable class="tabHeight">
                        <tabpane-box-child id="tab-box-child">
                        </tabpane-box-child>
                    </lit-tabpane>
                    <lit-tabpane id="box-native-statstics" key="16" hidden tab="Statistics" class="tabHeight">
                        <tabpane-native-statistics id="tab-box-native-stats">
                        </tabpane-native-statistics>
                    </lit-tabpane>
                    <lit-tabpane id="box-native-callinfo" key="17" hidden tab="Call Info" class="tabHeight">
                        <tabpane-native-callinfo id="tab-box-native-callinfo">
                        </tabpane-native-callinfo>
                    </lit-tabpane>
                    <lit-tabpane id="box-native-memory" key="18" hidden tab="Native Memory" class="tabHeight">
                        <tabpane-native-memory id="tab-box-native-memory">
                        </tabpane-native-memory>
                    </lit-tabpane>
                    <lit-tabpane id="box-native-sample" key="19" hidden tab="Snapshot List" class="tabHeight">
                        <tabpane-native-sample id="tab-box-native-sample">
                        </tabpane-native-sample>
                    </lit-tabpane>
                    <lit-tabpane id="box-perf-profile" key="20" hidden tab="Perf Profile" class="tabHeight">
                        <tabpane-perf-profile id="tab-box-perf-profile"></tabpane-perf-profile>
                    </lit-tabpane>
                    <lit-tabpane id="box-perf-sample" key="21" hidden tab="Sample List" class="tabHeight">
                        <tabpane-perf-sample id="tab-box-perf-sample"></tabpane-perf-sample>
                    </lit-tabpane>
                    <lit-tabpane id="box-live-processes-child" key="30" hidden tab="Live Processes" class="tabHeight">
                        <tabpane-live-processes id="tab-live-processes-child">
                        </tabpane-live-processes>
                    </lit-tabpane>
                    <lit-tabpane id="box-history-processes-child" key="31" hidden tab="Processes History" class="tabHeight">
                        <tabpane-history-processes id="tab-history-processes-child">
                        </tabpane-history-processes>
                    </lit-tabpane>
                    <lit-tabpane id="box-system-cpu-child" key="32" hidden tab="System CPU Summary" class="tabHeight">
                        <tabpane-cpu-ability id="tab-system-cpu-child">
                        </tabpane-cpu-ability>
                    </lit-tabpane>
                    <lit-tabpane id="box-system-memory-child" key="33" hidden tab="System Memory Summary" class="tabHeight">
                        <tabpane-memory-ability id="tab-system-memory-child">
                        </tabpane-memory-ability>
                    </lit-tabpane>
                    <lit-tabpane id="box-system-diskIo-child" key="34" hidden tab="System Disk Summary" class="tabHeight">
                        <tabpane-disk-ability id="tab-system-diskIo-child">
                        </tabpane-disk-ability>
                    </lit-tabpane>
                    <lit-tabpane id="box-system-network-child" key="35" hidden tab="System Network Summary" class="tabHeight">
                        <tabpane-network-ability id="tab-system-network-child">
                        </tabpane-network-ability>
                    </lit-tabpane>
                </lit-tabs>
            </div>
        `;
    }

    clear() {
        this.shadowRoot?.querySelectorAll("lit-tabpane").forEach(it => this.litTabs?.removeChild(it))
    }

    displayThreadData(data: ThreadStruct, scrollCallback: ((e: ThreadStruct) => void) | undefined) {
        this.setAttribute("mode", "max")
        this.tabCurrentSelection!.hidden = false;
        this.hideBoxTab();
        this.litTabs?.activeByKey("1")
        let tabCpu = this.shadowRoot!.querySelector<TabPaneCurrentSelection>('#tabpane-cpu');
        tabCpu!.setThreadData(data, scrollCallback);
    }

    displayMemData(data: ProcessMemStruct) {
        this.setAttribute("mode", "max")
        this.tabCurrentSelection!.hidden = false;
        this.hideBoxTab();
        this.litTabs?.activeByKey("1")
        let tabCpu = this.shadowRoot!.querySelector<TabPaneCurrentSelection>('#tabpane-cpu');
        tabCpu!.setMemData(data)
    }

    displayFuncData(data: FuncStruct) {
        this.setAttribute("mode", "max")
        this.tabCurrentSelection!.hidden = false;
        this.hideBoxTab();
        this.litTabs?.activeByKey("1")
        let tabCpu = this.shadowRoot!.querySelector<TabPaneCurrentSelection>('#tabpane-cpu');
        tabCpu!.setFunctionData(data)
    }

    displayCpuData(data: CpuStruct,
                   callback: ((data: WakeupBean | null) => void) | undefined = undefined,
                   scrollCallback?: (data: CpuStruct) => void) {
        this.setAttribute("mode", "max")
        this.tabCurrentSelection!.hidden = false;
        this.hideBoxTab();
        this.litTabs?.activeByKey("1")
        let tabCpu = this.shadowRoot!.querySelector<TabPaneCurrentSelection>('#tabpane-cpu');
        tabCpu!.setCpuData(data, callback, scrollCallback)
    }

    displayFlagData(flagObj: Flag) {
        this.setAttribute("mode", "max")
        this.tabCurrentSelection!.hidden = true;
        this.hideBoxTab();
        this.tabBoxFlag!.hidden = false;
        this.litTabs?.activeByKey("10")
        let tabFlag = this.shadowRoot!.querySelector<TabPaneFlag>('#tab-flag');
        tabFlag!.setFlagObj(flagObj)
    }

    boxSelection(selection: SelectionParam): boolean {
        this.tabBoxChild!.hidden = true;
        this.selection = selection;
        if (selection.hasFps || selection.cpus.length > 0 || selection.threadIds.length > 0
            || selection.funTids.length > 0 || selection.trackIds.length > 0 || selection.heapIds.length > 0
            || selection.nativeMemory.length > 0 || selection.cpuAbilityIds.length > 0 || selection.memoryAbilityIds.length > 0 || selection.diskAbilityIds.length > 0 || selection.networkAbilityIds.length > 0
            || selection.perfSampleIds.length > 0) {
            this.setAttribute("mode", "max")
            this.tabCurrentSelection!.hidden = true;
            this.tabBoxFlag!.hidden = true;
            this.tabBoxCpuThread!.hidden = selection.cpus.length == 0;
            this.tabBoxCpuProcess!.hidden = selection.cpus.length == 0;
            this.tabBoxCpuUsage!.hidden = selection.cpus.length == 0;
            this.tabBoxSPT!.hidden = selection.cpus.length == 0;
            this.tabBoxPTS!.hidden = selection.cpus.length == 0;
            this.tabBoxContextSwitch!.hidden = selection.cpus.length == 0;
            this.tabBoxThreadSwitch!.hidden = selection.cpus.length == 0;
            this.tabBoxThreadStates!.hidden = selection.threadIds.length == 0;
            this.tabBoxSlices!.hidden = selection.funTids.length == 0;
            this.tabBoxCounters!.hidden = selection.trackIds.length == 0;
            this.tabBoxFps!.hidden = !selection.hasFps;
            this.tabBoxHeap!.hidden = selection.heapIds.length == 0;
            this.tabBoxNMStatistics!.hidden = selection.nativeMemory.length == 0
            this.tabBoxNMCallInfo!.hidden = selection.nativeMemory.length == 0
            this.tabBoxNMemory!.hidden = selection.nativeMemory.length == 0
            this.tabBoxNMSample!.hidden = selection.nativeMemory.length == 0

            this.tabBoxLiveProcesses!.hidden = selection.cpuAbilityIds.length == 0 && selection.memoryAbilityIds.length == 0 && selection.diskAbilityIds.length == 0 && selection.networkAbilityIds.length == 0
            this.tabBoxHistoryProcesses!.hidden = selection.cpuAbilityIds.length == 0 && selection.memoryAbilityIds.length == 0 && selection.diskAbilityIds.length == 0 && selection.networkAbilityIds.length == 0
            this.tabBoxSystemCpu!.hidden = selection.cpuAbilityIds.length == 0
            this.tabBoxSystemMemory!.hidden = selection.memoryAbilityIds.length == 0
            this.tabBoxSystemDiskIo!.hidden = selection.diskAbilityIds.length == 0
            this.tabBoxSystemNetwork!.hidden = selection.networkAbilityIds.length == 0
            this.tabBoxPerfProfile!.hidden = selection.perfSampleIds.length == 0
            this.tabBoxPerfSample!.hidden = selection.perfSampleIds.length == 0
            this.setBoxActiveKey(selection);
            return true;
        } else {
            this.setAttribute("mode", "hidden")
            return false;
        }
    }

    recoveryBoxSelection() {
        this.tabCurrentSelection!.hidden = true;
        this.tabBoxCpuThread!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxCpuProcess!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxCpuUsage!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxSPT!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxPTS!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxContextSwitch!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxThreadSwitch!.hidden = !(this.selection!.cpus.length > 0);
        this.tabBoxThreadStates!.hidden = !(this.selection!.threadIds.length > 0);
        this.tabBoxSlices!.hidden = !(this.selection!.funTids.length > 0);
        this.tabBoxCounters!.hidden = !(this.selection!.trackIds.length > 0)
        this.tabBoxHeap!.hidden = !(this.selection!.heapIds.length > 0)
        this.tabBoxFps!.hidden = !this.selection?.hasFps;
        this.tabBoxNMStatistics!.hidden = !(this.selection!.nativeMemory.length > 0)
        this.tabBoxNMCallInfo!.hidden = !(this.selection!.nativeMemory.length > 0)
        this.tabBoxNMemory!.hidden = !(this.selection!.nativeMemory.length > 0)
        this.tabBoxNMSample!.hidden = !(this.selection!.nativeMemory.length > 0)
        this.tabBoxPerfProfile!.hidden = !(this.selection!.perfSampleIds.length > 0)
        this.tabBoxPerfSample!.hidden = !(this.selection!.perfSampleIds.length > 0)
    }

    setBoxActiveKey(val: SelectionParam) {
        if (val.cpus.length > 0) {
            this.litTabs?.activeByKey("2")
            this.loadTabPaneData("2")
        } else if (val.threadIds.length > 0) {
            this.litTabs?.activeByKey("4")
            this.loadTabPaneData("4")
        } else if (val.funTids.length > 0) {
            this.litTabs?.activeByKey("5")
            this.loadTabPaneData("5")
        } else if (val.trackIds.length > 0) {
            this.litTabs?.activeByKey("6")
            this.loadTabPaneData("6")
        } else if (val.hasFps) {
            this.litTabs?.activeByKey("7")
            this.loadTabPaneData("7")
        } else if (val.heapIds.length > 0) {
            this.litTabs?.activeByKey("9")
            this.loadTabPaneData("9")
        } else if (val.nativeMemory.length > 0) {
            this.litTabs?.activeByKey("16")
            this.loadTabPaneData("16")
        } else if (val.cpuAbilityIds.length > 0) {
            this.litTabs?.activeByKey("32")
            this.loadTabPaneData("32")
        } else if (val.memoryAbilityIds.length > 0) {
            this.litTabs?.activeByKey("33")
            this.loadTabPaneData("33")
        } else if (val.diskAbilityIds.length > 0) {
            this.litTabs?.activeByKey("34")
            this.loadTabPaneData("34")
        } else if (val.networkAbilityIds.length > 0) {
            this.litTabs?.activeByKey("35")
            this.loadTabPaneData("35")
        } else if (val.perfSampleIds.length > 0) {
            this.litTabs?.activeByKey("20")
            this.loadTabPaneData("20")
        } else {
            this.litTabs?.activeByKey("1")
            this.loadTabPaneData("1")
        }
    }

    loadTabPaneData(key: string) {
        if (key == "2") {
            let tabCpuThread = this.shadowRoot!.querySelector<TabPaneCpuByThread>('#tab-cpu-thread');
            tabCpuThread!.data = this.selection;
        } else if (key == "3") {
            let tabCpuProcess = this.shadowRoot!.querySelector<TabPaneCpuByProcess>('#tab-cpu-process');
            tabCpuProcess!.data = this.selection;
        } else if (key == "4") {
            let tabThreadStates = this.shadowRoot!.querySelector<TabPaneThreadStates>('#tab-thread-states');
            tabThreadStates!.data = this.selection;
        } else if (key == "5") {
            let tabSlices = this.shadowRoot!.querySelector<TabPaneSlices>('#tab-slices');
            tabSlices!.data = this.selection;
        } else if (key == "6") {
            let tabCounters = this.shadowRoot!.querySelector<TabPaneCounter>('#tab-counters');
            tabCounters!.data = this.selection;
        } else if (key == "7") {
            let tabFps = this.shadowRoot!.querySelector<TabPaneFps>('#tab-fps');
            tabFps!.data = this.selection;
        } else if (key == "8") {
            let tabCpuUsage = this.shadowRoot!.querySelector<TabPaneCpuUsage>('#tab-cpu-usage');
            tabCpuUsage!.data = this.selection;
        } else if (key == "9") {
            let tabHeap = this.shadowRoot!.querySelector<TabPaneHeap>('#tab-heap');
            tabHeap!.data = this.selection;
        } else if (key == "10") {

        } else if (key == "11") {
            this.tabSPT!.data = this.selection;
        } else if (key == "12") {
            this.tabPTS!.data = this.selection;
        } else if (key == "13") {
            this.tabCs!.data = this.selection;
        } else if (key == "14") {
            this.tabTs!.data = this.selection;
        } else if (key == "16") {
            this.tabNativeStatistics!.data = this.selection;
        } else if (key == "17") {
            this.tabNativeCallInfo!.data = this.selection;
        } else if (key == "18") {
            this.tabNativeMemory!.data = this.selection;
        } else if (key == "19") {
            this.tabNativeSample!.data = this.selection;
        } else if (key == "20") {
            this.tabPerfProfile!.data = this.selection;
        } else if (key == "21") {
            this.tabPerfSample!.data = this.selection;
        } else if (key == "30") {
            this.tabLiveProcesses!.data = this.selection;
        } else if (key == "31") {
            this.tabHistoryProcesses!.data = this.selection;
        } else if (key == "32") {
            this.tabSystemCpu!.data = this.selection;
        } else if (key == "33") {
            this.tabSystemMemory!.data = this.selection;
        } else if (key == "34") {
            this.tabSystemDiskIo!.data = this.selection;
        } else if (key == "35") {
            this.tabSystemNetwork!.data = this.selection;
        }
    }

    hideBoxTab() {
        this.tabBoxCpuThread!.hidden = true;
        this.tabBoxCpuProcess!.hidden = true;
        this.tabBoxThreadStates!.hidden = true;
        this.tabBoxSlices!.hidden = true;
        this.tabBoxCounters!.hidden = true;
        this.tabBoxFps!.hidden = true;
        this.tabBoxSPT!.hidden = true;
        this.tabBoxPTS!.hidden = true;
        this.tabBoxContextSwitch!.hidden = true;
        this.tabBoxThreadSwitch!.hidden = true;
        this.tabBoxCpuUsage!.hidden = true;
        this.tabBoxFlag!.hidden = true;
        this.tabBoxHeap!.hidden = true;
        this.tabBoxChild!.hidden = true;
        this.tabBoxNMStatistics!.hidden = true;
        this.tabBoxNMCallInfo!.hidden = true;
        this.tabBoxNMemory!.hidden = true;
        this.tabBoxNMSample!.hidden = true;
        this.tabBoxPerfProfile!.hidden = true;
        this.tabBoxPerfSample!.hidden = true;
    }

    hideOtherBoxTab(key: string) {
        this.tabBoxCpuThread!.hidden = true;
        this.tabBoxCpuProcess!.hidden = true;
        this.tabBoxThreadStates!.hidden = true;
        this.tabBoxSlices!.hidden = true;
        this.tabBoxCounters!.hidden = true;
        this.tabBoxFps!.hidden = true;
        this.tabBoxCpuUsage!.hidden = true;
        this.tabBoxHeap!.hidden = true;
        this.tabBoxNMStatistics!.hidden = true;
        this.tabBoxNMCallInfo!.hidden = true;
        this.tabBoxNMemory!.hidden = true;
        this.tabBoxNMSample!.hidden = true;
        this.tabBoxPerfProfile!.hidden = true;
        this.tabBoxPerfSample!.hidden = true;
        if (key == "11") {
            this.tabBoxPTS!.hidden = true;
            this.tabBoxContextSwitch!.hidden = true;
            this.tabBoxThreadSwitch!.hidden = true;
        } else if (key == "12") {
            this.tabBoxSPT!.hidden = true;
            this.tabBoxContextSwitch!.hidden = true;
            this.tabBoxThreadSwitch!.hidden = true;
        } else if (key == "13") {
            this.tabBoxSPT!.hidden = true;
            this.tabBoxPTS!.hidden = true;
            this.tabBoxThreadSwitch!.hidden = true;
        } else if (key == "14") {
            this.tabBoxSPT!.hidden = true;
            this.tabBoxPTS!.hidden = true;
            this.tabBoxContextSwitch!.hidden = true;
        }
        this.tabBoxChild!.hidden = false
        this.currentKey = key
        this.litTabs?.activeByKey("15")
    }

    jumpBoxChild(key: string, e: any) {
        this.hideOtherBoxTab(key)
        this.tabBoxChild!.tab = e.detail.title
        let param = new BoxJumpParam();
        param.leftNs = this.selection!.leftNs;
        param.rightNs = this.selection!.rightNs;
        param.state = e.detail.state;
        param.threadId = e.detail.threadId;
        param.processId = e.detail.processId;
        this.tabChild!.data = param;
    }

}
