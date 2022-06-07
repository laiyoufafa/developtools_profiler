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

// @ts-ignore
import {TraceSheet} from "../../../../../dist/trace/component/trace/base/TraceSheet.js";

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe("TraceSheet Test", () => {
    beforeAll(() => {
    })
    let val = {
        hasFps: 1,
        cpus:{length:1},
        threadIds:[{length:2}],
        funTids:{length:1},
        trackIds: {length:1},
        heapIds: {length:1},
        nativeMemory: {length:1},
        cpuAbilityIds:{length:1},
        memoryAbilityIds:{length:1},
        diskAbilityIds:{length:1},
        networkAbilityIds:{length:1},
    }
    let e = {detail:{
            title:1,
            state:0,
            threadId:1,
            processId:2
        }}
        let selection ={
            hasFps: 1,
            cpus:{length:1},
            threadIds:[{length:2}],
            funTids:{length:1},
            trackIds: {length:1},
            heapIds: {length:1},
            nativeMemory: {length:1},
            cpuAbilityIds:{length:0},
            memoryAbilityIds:{length:0},
            diskAbilityIds:{length:0},
            networkAbilityIds:{length:0},
            perfSampleIds:{length:0},

        }
    it('TraceSheet Test01', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet).not.toBeUndefined()
    });

    it('TraceSheet Test02', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet.recoveryBoxSelection).not.toBeUndefined()
    });


    it('TraceSheet Test03', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet.hideBoxTab()).toBeUndefined()
    });

    it('TraceSheet Test08', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet.connectedCallback()).toBeUndefined()
    });
    it('TraceSheet Test09', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet.loadTabPaneData()).toBeUndefined()
    });

    it('TraceSheet Test10', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet.clear()).toBeUndefined()
    });

    it('TraceSheet Test12', () => {
        let traceSheet = new TraceSheet();
        traceSheet.litTabs = jest.fn(()=>true)
        traceSheet.litTabs.activeByKey = jest.fn(()=>true)
        let value = traceSheet.hideOtherBoxTab("11")
        expect(value).toBeUndefined()
    });

    it('TraceSheet Test13', () => {
        let traceSheet = new TraceSheet();
        traceSheet.litTabs = jest.fn(()=>true)
        traceSheet.litTabs.activeByKey = jest.fn(()=>true)
        let value = traceSheet.hideOtherBoxTab("12")
        expect(value).toBeUndefined()
    });

    it('TraceSheet Test14', () => {
        let traceSheet = new TraceSheet();
        traceSheet.litTabs = jest.fn(()=>true)
        traceSheet.litTabs.activeByKey = jest.fn(()=>true)
        let value = traceSheet.hideOtherBoxTab("13")
        expect(value).toBeUndefined()
    });

    it('TraceSheet Test16', () => {
        let traceSheet = new TraceSheet();
        traceSheet.litTabs = jest.fn(()=>true)
        traceSheet.litTabs.activeByKey = jest.fn(()=>true)
        let value = traceSheet.hideOtherBoxTab("14")
        expect(value).toBeUndefined()
    });

    it('TraceSheet Test15', () => {
        let traceSheet = new TraceSheet();
        traceSheet.setBoxActiveKey = jest.fn(()=>true)
        expect(traceSheet.boxSelection(selection)).toBeTruthy()
    });

    it('TraceSheet Test17', () => {
        let traceSheet = new TraceSheet();
        traceSheet.selection = jest.fn(()=>undefined)
        traceSheet.selection.cpus = jest.fn(()=>[1])
        traceSheet.selection.threadIds = jest.fn(()=>[1])
        traceSheet.selection.funTids = jest.fn(()=>[1])
        traceSheet.selection.trackIds = jest.fn(()=>[1])
        traceSheet.selection.heapIds = jest.fn(()=>[1])
        traceSheet.selection.nativeMemory = jest.fn(()=>[1])
        traceSheet.selection.perfSampleIds = jest.fn(()=>[1])
        expect(traceSheet.recoveryBoxSelection()).toBeUndefined()
    });

    it('TraceSheet Test18', () => {
        let traceSheet = new TraceSheet();
        traceSheet.litTabs = jest.fn(()=>undefined)
        traceSheet.litTabs.activeByKey = jest.fn(()=>true)
        traceSheet.loadTabPaneData = jest.fn(()=>"")
        expect(traceSheet.setBoxActiveKey(val)).toBeUndefined()
    });

    it('TraceSheet Test19', () => {
        let traceSheet = new TraceSheet();
        expect(traceSheet.initHtml()).toMatchInlineSnapshot(`
"
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
            <div style=\\"border-top: 0.5px solid var(--dark-border1,#D5D5D5);\\">
                <lit-tabs id=\\"tabs\\" position=\\"top-left\\" activekey=\\"1\\" mode=\\"card\\" >
                    <div slot=\\"right\\" style=\\"margin: 0 10px; color: var(--dark-icon,#606060)\\">
                        <lit-icon id=\\"max-btn\\" name=\\"vertical-align-top\\" style=\\"font-weight: bold;cursor: pointer;\\" size=\\"20\\">
                        </lit-icon>
                        <lit-icon id=\\"min-btn\\" name=\\"down\\" style=\\"font-weight: bold;cursor: pointer;\\" size=\\"20\\">
                        </lit-icon>
                    </div>
                    <lit-tabpane id=\\"current-selection\\" key=\\"1\\" hidden tab=\\"Current Selection\\" class=\\"tabHeight\\">
                        <tabpane-current-selection id=\\"tabpane-cpu\\">
                        </tabpane-current-selection>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-cpu-thread\\" key=\\"2\\" hidden tab=\\"CPU by thread\\" class=\\"tabHeight\\">
                        <tabpane-cpu-thread id=\\"tab-cpu-thread\\">
                        </tabpane-cpu-thread>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-cpu-process\\" key=\\"3\\" hidden tab=\\"CPU by process\\" class=\\"tabHeight\\">
                        <tabpane-cpu-process id=\\"tab-cpu-process\\">
                        </tabpane-cpu-process>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-thread-states\\" key=\\"4\\" hidden tab=\\"Thread States\\" class=\\"tabHeight\\">
                        <tabpane-thread-states id=\\"tab-thread-states\\" >
                        </tabpane-thread-states>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-slices\\" key=\\"5\\" hidden tab=\\"Slices\\" class=\\"tabHeight\\">
                        <tabpane-slices id=\\"tab-slices\\">
                        </tabpane-slices>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-counters\\" key=\\"6\\" hidden tab=\\"Counters\\" class=\\"tabHeight\\">
                        <tabpane-counter id=\\"tab-counters\\">
                        </tabpane-counter>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-fps\\" key=\\"7\\" hidden tab=\\"FPS\\" class=\\"tabHeight\\">
                        <tabpane-fps id=\\"tab-fps\\">
                        </tabpane-fps>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-cpu-usage\\" key=\\"8\\" hidden tab=\\"CPU Usage\\" class=\\"tabHeight\\">
                        <tabpane-cpu-usage id=\\"tab-cpu-usage\\">
                        </tabpane-cpu-usage>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-heap\\" key=\\"9\\" hidden tab=\\"Native Hook\\" class=\\"tabHeight\\">
                        <tabpane-heap id=\\"tab-heap\\">
                        </tabpane-heap>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-flag\\" key=\\"10\\" hidden tab=\\"Current Selection\\" class=\\"tabHeight\\">
                        <tabpane-flag id=\\"tab-flag\\">
                        </tabpane-flag>
                    </lit-tabpane>

                    <lit-tabpane id=\\"box-spt\\" key=\\"11\\" hidden tab=\\"States List\\" class=\\"tabHeight\\">
                        <tabpane-spt id=\\"tab-spt\\">
                        </tabpane-spt>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-cs\\" key=\\"13\\" hidden tab=\\"Switches List\\" class=\\"tabHeight\\">
                        <tabpane-context-switch id=\\"tab-cs\\">
                        </tabpane-context-switch>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-pts\\" key=\\"12\\" hidden tab=\\"Thread States\\" class=\\"tabHeight\\">
                        <tabpane-pts id=\\"tab-pts\\">
                        </tabpane-pts>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-ts\\" key=\\"14\\" hidden tab=\\"Thread Switches\\" class=\\"tabHeight\\">
                        <tabpane-thread-switch id=\\"tab-ts\\"><
                        /tabpane-thread-switch>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-cpu-child\\" key=\\"15\\" hidden tab=\\"\\" closeable class=\\"tabHeight\\">
                        <tabpane-box-child id=\\"tab-box-child\\">
                        </tabpane-box-child>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-native-statstics\\" key=\\"16\\" hidden tab=\\"Statistics\\" class=\\"tabHeight\\">
                        <tabpane-native-statistics id=\\"tab-box-native-stats\\">
                        </tabpane-native-statistics>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-native-callinfo\\" key=\\"17\\" hidden tab=\\"Call Info\\" class=\\"tabHeight\\">
                        <tabpane-native-callinfo id=\\"tab-box-native-callinfo\\">
                        </tabpane-native-callinfo>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-native-memory\\" key=\\"18\\" hidden tab=\\"Native Memory\\" class=\\"tabHeight\\">
                        <tabpane-native-memory id=\\"tab-box-native-memory\\">
                        </tabpane-native-memory>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-native-sample\\" key=\\"19\\" hidden tab=\\"Snapshot List\\" class=\\"tabHeight\\">
                        <tabpane-native-sample id=\\"tab-box-native-sample\\">
                        </tabpane-native-sample>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-perf-profile\\" key=\\"20\\" hidden tab=\\"Perf Profile\\" class=\\"tabHeight\\">
                        <tabpane-perf-profile id=\\"tab-box-perf-profile\\"></tabpane-perf-profile>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-perf-sample\\" key=\\"21\\" hidden tab=\\"Sample List\\" class=\\"tabHeight\\">
                        <tabpane-perf-sample id=\\"tab-box-perf-sample\\"></tabpane-perf-sample>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-live-processes-child\\" key=\\"30\\" hidden tab=\\"Live Processes\\" class=\\"tabHeight\\">
                        <tabpane-live-processes id=\\"tab-live-processes-child\\">
                        </tabpane-live-processes>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-history-processes-child\\" key=\\"31\\" hidden tab=\\"Processes History\\" class=\\"tabHeight\\">
                        <tabpane-history-processes id=\\"tab-history-processes-child\\">
                        </tabpane-history-processes>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-system-cpu-child\\" key=\\"32\\" hidden tab=\\"System CPU Summary\\" class=\\"tabHeight\\">
                        <tabpane-cpu-ability id=\\"tab-system-cpu-child\\">
                        </tabpane-cpu-ability>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-system-memory-child\\" key=\\"33\\" hidden tab=\\"System Memory Summary\\" class=\\"tabHeight\\">
                        <tabpane-memory-ability id=\\"tab-system-memory-child\\">
                        </tabpane-memory-ability>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-system-diskIo-child\\" key=\\"34\\" hidden tab=\\"System Disk Summary\\" class=\\"tabHeight\\">
                        <tabpane-disk-ability id=\\"tab-system-diskIo-child\\">
                        </tabpane-disk-ability>
                    </lit-tabpane>
                    <lit-tabpane id=\\"box-system-network-child\\" key=\\"35\\" hidden tab=\\"System Network Summary\\" class=\\"tabHeight\\">
                        <tabpane-network-ability id=\\"tab-system-network-child\\">
                        </tabpane-network-ability>
                    </lit-tabpane>
                </lit-tabs>
            </div>
        "
`)
    });
})