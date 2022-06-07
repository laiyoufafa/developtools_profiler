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

import {BaseElement, element} from "../base-ui/BaseElement.js";
import "../base-ui/menu/LitMainMenu.js";
import "../base-ui/icon/LitIcon.js";
import {SpMetrics} from "./component/SpMetrics.js";
import {SpHelp} from "./component/SpHelp.js";
import "./component/SpHelp.js";
import {SpQuerySQL} from "./component/SpQuerySQL.js";
import "./component/SpQuerySQL.js";
import {SpSystemTrace} from "./component/SpSystemTrace.js";
import {LitMainMenu, MenuItem} from "../base-ui/menu/LitMainMenu.js";
import {SpInfoAndStats} from "./component/SpInfoAndStas.js";
import "../base-ui/progress-bar/LitProgressBar.js";
import {LitProgressBar} from "../base-ui/progress-bar/LitProgressBar.js";
import {SpRecordTrace} from "./component/SpRecordTrace.js";
import {SpWelcomePage} from "./component/SpWelcomePage.js";
import {LitSearch} from "./component/trace/search/Search.js";
import {threadPool} from "./database/SqlLite.js";
import "./component/trace/search/Search.js";
import "./component/SpWelcomePage.js";
import "./component/SpSystemTrace.js";
import "./component/SpRecordTrace.js";
import "./component/SpMetrics.js";
import "./component/SpInfoAndStas.js";
import "./component/trace/base/TraceRow.js";

@element('sp-application')
export class SpApplication extends BaseElement {
    static skinChange: Function | null | undefined = null;
    static skinChange2: Function | null | undefined = null;
    skinChangeArray: Array<Function> = [];
    private icon: HTMLDivElement | undefined | null
    private rootEL: HTMLDivElement | undefined | null
    private spHelp: SpHelp | undefined | null
    private keyCodeMap = {
        61: true,
        107: true,
        109: true,
        173: true,
        187: true,
        189: true,
    };

    static get observedAttributes() {
        return ["server", "sqlite", "wasm", "dark", "vs", "query-sql"]
    }

    get dark() {
        return this.hasAttribute('dark');
    }

    set dark(value) {
        if (value) {
            this.rootEL!.classList.add('dark');
            this.setAttribute('dark', '');
        } else {
            this.rootEL!.classList.remove('dark');
            this.removeAttribute('dark');
        }
        if (this.skinChangeArray.length > 0) {
            this.skinChangeArray.forEach((item) => item(value));
        }
        if (SpApplication.skinChange) {
            SpApplication.skinChange(value);
        }
        if (SpApplication.skinChange2) {
            SpApplication.skinChange2(value);
        }

        if (this.spHelp) {
            this.spHelp.dark = value
        }
    }

    get vs(): boolean {
        return this.hasAttribute("vs")
    }

    set vs(isVs: boolean) {
        if (isVs) {
            this.setAttribute("vs", "")
        }
    }

    get sqlite(): boolean {
        return this.hasAttribute("sqlite")
    }

    get wasm(): boolean {
        return this.hasAttribute("wasm")
    }

    get server(): boolean {
        return this.hasAttribute("server")
    }

    set server(s: boolean) {
        if (s) {
            this.setAttribute('server', '')
        } else {
            this.removeAttribute('server')
        }
    }

    get querySql(): boolean {
        return this.hasAttribute("query-sql")
    }

    set search(search: boolean) {
        if (search) {
            this.setAttribute('search', '')
        } else {
            this.removeAttribute('search')
        }
    }

    addSkinListener(handler: Function) {
        this.skinChangeArray.push(handler)
    };

    removeSkinListener(handler: Function) {
        this.skinChangeArray.splice(this.skinChangeArray.indexOf(handler), 1);
    };

    initHtml(): string {
        return `
        <style>
        :host{

        }
        .dark{
        --dark-background: #272C34;
        --dark-background1: #424851;
        --dark-background2: #262f3c;
        --dark-background3: #292D33;
        --dark-background4: #323841;
        --dark-background5: #333840;
        --dark-background6: rgba(82,145,255,0.2);
        --dark-background7: #494d52;
        --dark-background8: #5291FF;
        --dark-color: rgba(255,255,255,0.6);
        --dark-color1: rgba(255,255,255,0.86);
        --dark-color2: rgba(255,255,255,0.9);
        --dark-border: #474F59;
        --dark-color3:#4694C2;
        --dark-color4:#5AADA0;
        --dark-border1: #454E5A;
        --bark-expansion:#0076FF;
        --bark-prompt:#9e9e9e;
        --dark-icon:#adafb3;
        --dark-img: url('img/dark_pic.png');
            background: #272C34;
            color: #FFFFFF;
        }
        .root{
            display: grid;
            grid-template-rows: min-content 1fr;
            grid-template-columns: min-content 1fr;
            grid-template-areas: 'm s'
                                 'm b';
            height: 100vh;
            width: 100vw;
        }
        .filedrag::after {
             content: 'Drop the trace file to open it';
             position: fixed;
             z-index: 2001;
             top: 0;
             left: 0;
             right: 0;
             bottom: 0;
             border: 5px dashed var(--dark-color1,#404854);
             text-align: center;
             font-size: 3rem;
             line-height: 100vh;
             background: rgba(255, 255, 255, 0.5);
        }
        .menu{
            grid-area: m;
            /*transition: all 0.2s;*/
            box-shadow: 4px 0px 20px rgba(0,0,0,0.05);
            z-index: 2000;
        }
        .search-container{
            z-index: 10;
            position: relative;
        }
        .progress{
            bottom: 0;
            position: absolute;
            height: 1px;
            left: 0;
            right: 0;
        }
        :host(:not([search])) #lit-search  {
           display: none;
        }

        :host(:not([search])) .search-container .search  {
            background-color: var(--dark-background5,#F6F6F6);
        }
        .search{
            grid-area: s;
            background-color: var(--dark-background,#FFFFFF);
            height: 48px;
            display: flex;
            justify-content: center;
            align-items: center;

        }
        .search .search-bg{
            background-color: var(--dark-background5,#fff);
            border-radius: 40px;
            padding: 3px 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            border: 1px solid var(--dark-border,#c5c5c5);
        }
        .search input{
            outline: none;
            border: 0px;
            background-color: transparent;
            font-size: inherit;
            color: var(--dark-color,#666666);
            width: 30vw;
            height: auto;
            vertical-align:middle;
            line-height:inherit;
            height:inherit;
            padding: 6px 6px 6px 6px};
            max-height: inherit;
            box-sizing: border-box;

        }
        ::placeholder { /* CSS 3 標準 */
          color: #b5b7ba;
          font-size: 1em;
        }
        .search input::placeholder {
          color: #b5b7ba;
          font-size: 1em;
        }
        .content{
            grid-area: b;
            background-color: #ffffff;
            height: 100%;
            overflow: auto;
            position:relative;
        }
        .sheet{

        }
        .sidebar-button{
            position: absolute;
            top: 0;
            left: 0;
            background-color: var(--dark-background1,#FFFFFF);
            height: 100%;
            border-radius: 0 5px 5px 0;
            width: 48px;
            display: flex;
            align-content: center;
            justify-content: center;
            cursor: pointer;
        }
        :host{
            font-size: inherit;
            display: inline-block;
            transition: .3s;
         }
         :host([spin]){
            animation: rotate 1.75s linear infinite;
         }
         @keyframes rotate {
            to{
                transform: rotate(360deg);
            }
         }
         .icon{
            display: block;
            width: 1em;
            height: 1em;
            margin: auto;
            fill: currentColor;
            overflow: hidden;
            font-size: 20px;
            color: var(--dark-color1,#4D4D4D);
         }
        </style>
        <div class="root">
            <lit-main-menu id="main-menu" class="menu" data=''></lit-main-menu>
            <div class="search-container">
                <div class="search" style="position: relative;">
                    <div class="sidebar-button" style="width: 0">
                        <svg class="icon" id="icon" aria-hidden="true" viewBox="0 0 1024 1024">
                             <use id="use" xlink:href="./base-ui/icon.svg#icon-menu"></use>
                        </svg>
                    </div>
                    <lit-search id="lit-search"></lit-search>
                </div>
                <lit-progress-bar class="progress"></lit-progress-bar>
            </div>
            <div id="app-content" class="content">
                <sp-welcome style="visibility:visible;top:0px;left:0px;position:absolute;z-index: 100" id="sp-welcome">
                </sp-welcome>
                <sp-system-trace style="visibility:visible;" id="sp-system-trace">
                </sp-system-trace>
                <sp-record-trace style="width:100%;height:100%;overflow:auto;visibility:hidden;top:0px;left:0px;right:0;bottom:0px;position:absolute;z-index: 102" id="sp-record-trace">
                </sp-record-trace>
                <sp-metrics style="width:100%;height:100%;overflow:auto;visibility:hidden;top:0;left:0;right:0;bottom:0;position:absolute;" id="sp-metrics">
                </sp-metrics>
                <sp-query-sql style="width:100%;height:100%;overflow:auto;visibility:hidden;top:0;left:0;right:0;bottom:0;position:absolute;" id="sp-query-sql">
                </sp-query-sql>
                <sp-info-and-stats style="width:100%;height:100%;overflow:auto;visibility:hidden;top:0;left:0;right:0;bottom:0;position:absolute;" id="sp-info-and-stats">
                </sp-info-and-stats>
                <sp-help style="width:100%;height:100%;overflow:auto;visibility:hidden;top:0px;left:0px;right:0;bottom:0px;position:absolute;z-index: 103" id="sp-help">
                </sp-help>
            </div>
        </div>
        `;
    }

    initElements() {
        let that = this;
        this.rootEL = this.shadowRoot!.querySelector<HTMLDivElement>(".root")
        let spWelcomePage = this.shadowRoot!.querySelector("#sp-welcome") as SpWelcomePage
        let spMetrics = this.shadowRoot!.querySelector<SpMetrics>("#sp-metrics") as SpMetrics // new SpMetrics();
        let spQuerySQL = this.shadowRoot!.querySelector<SpQuerySQL>("#sp-query-sql") as SpQuerySQL // new SpQuerySQL();
        let spInfoAndStats = this.shadowRoot!.querySelector<SpInfoAndStats>("#sp-info-and-stats") as SpInfoAndStats // new SpInfoAndStats();

        let spSystemTrace = this.shadowRoot!.querySelector<SpSystemTrace>("#sp-system-trace")
        this.spHelp = this.shadowRoot!.querySelector<SpHelp>("#sp-help")
        let spRecordTrace = this.shadowRoot!.querySelector<SpRecordTrace>("#sp-record-trace")
        let appContent = this.shadowRoot?.querySelector('#app-content') as HTMLDivElement;
        let mainMenu = this.shadowRoot?.querySelector('#main-menu') as LitMainMenu
        let progressEL = this.shadowRoot?.querySelector('.progress') as LitProgressBar
        let litSearch = this.shadowRoot?.querySelector('#lit-search') as LitSearch
        let sidebarButton: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector('.sidebar-button')
        let childNodes = [spSystemTrace, spRecordTrace, spWelcomePage, spMetrics, spQuerySQL, spInfoAndStats, this.spHelp]
        litSearch.addEventListener("focus", () => {
            spSystemTrace!.keyboardEnable = false
        })
        litSearch.addEventListener('blur', () => {
            spSystemTrace!.keyboardEnable = true
        })
        litSearch.addEventListener("previous-data", (ev: any) => {
            litSearch.index = spSystemTrace!.showPreCpuStruct(litSearch.index, litSearch.list);
            litSearch.blur();
        })
        litSearch.addEventListener("next-data", (ev: any) => {
            litSearch.index = spSystemTrace!.showNextCpuStruct(litSearch.index, litSearch.list);
            litSearch.blur();
            // spSystemTrace!.search(e.detail.value)
        })
        litSearch.valueChangeHandler = (value: string) => {
            if (value.length > 0) {
                litSearch.list = spSystemTrace!.searchCPU(value);
            } else {
                litSearch.list = [];
                spSystemTrace?.visibleRows.forEach(it => {
                    it.highlight = false;
                    it.draw();
                });
                spSystemTrace?.timerShaftEL?.removeTriangle("inverted");
            }
        }
        spSystemTrace?.addEventListener("previous-data", (ev: any) => {
            litSearch.index = spSystemTrace!.showPreCpuStruct(litSearch.index, litSearch.list);
        })
        spSystemTrace?.addEventListener("next-data", (ev: any) => {
            litSearch.index = spSystemTrace!.showNextCpuStruct(litSearch.index, litSearch.list);
        })
        //打开侧边栏
        sidebarButton!.onclick = (e) => {
            let menu: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector('#main-menu')
            let menuButton: HTMLElement | undefined | null = this.shadowRoot?.querySelector('.sidebar-button')
            if (menu) {
                menu.style.width = `248px`
                // @ts-ignore
                menu.style.zIndex = 2000;
                menu.style.display = `flex`
            }
            if (menuButton) {
                menuButton.style.width = `0px`
            }
        }
        let icon: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector("#main-menu")?.shadowRoot?.querySelector("div.header > div")
        icon!.onclick = (e) => {
            let menu: HTMLElement | undefined | null = this.shadowRoot?.querySelector("#main-menu")
            let menuButton: HTMLElement | undefined | null = this.shadowRoot?.querySelector('.sidebar-button')
            if (menu) {
                menu.style.width = `0px`
                menu.style.display = `flex`
                // @ts-ignore
                menu.style.zIndex = 0
            }
            if (menuButton) {
                menuButton.style.width = `48px`
            }
        }

        function showContent(showNode: HTMLElement) {
            childNodes.forEach((node) => {
                if (node === showNode) {
                    showNode.style.visibility = 'visible'
                } else {
                    node!.style.visibility = 'hidden'
                }
            })
        }

        function postLog(filename: string, fileSize: string) {
            fetch(`https://${window.location.host.split(':')[0]}:9000/logger`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    fileName: filename,
                    fileSize: fileSize
                }),
            }).then(response => response.json()).then(data => {
            }).catch((error) => {
            });
        }

        function openTraceFile(ev: any) {
            litSearch.clear();
            showContent(spSystemTrace!)
            that.search = true
            progressEL.loading = true
            let fileName = (ev as any).name
            let fileSize = ((ev as any).size / 1000000).toFixed(1)
            postLog(fileName, fileSize)
            document.title = `${fileName.substring(0, fileName.lastIndexOf('.'))} (${fileSize}M)`
            if (that.server) {
                threadPool.init("server").then(() => {
                    litSearch.setPercent("parse trace", 1);
                    // Load the trace file and send it to the background parse to return the db file path
                    const fd = new FormData()
                    that.freshMenuDisable(true)
                    fd.append('file', ev as any)
                    let uploadPath = `https://${window.location.host.split(':')[0]}:9000/upload`
                    if (that.vs) {
                        uploadPath = `http://${window.location.host.split(':')[0]}:${window.location.port}/upload`
                    }
                    fetch(uploadPath, {
                        method: 'POST',
                        body: fd,
                    }).then(res => {
                        litSearch.setPercent("load database", 5);
                        if (res.ok) {
                            let menus = [
                                {
                                    title: `${fileName.substring(0, fileName.lastIndexOf('.'))} (${fileSize}M)`,
                                    icon: "file-fill",
                                    clickHandler: function () {
                                        that.search = true
                                        showContent(spSystemTrace!)
                                    }
                                }
                            ];

                            mainMenu.menus!.splice(1, 1, {
                                collapsed: false,
                                title: "Current Trace",
                                describe: "Actions on the current trace",
                                children: menus
                            })

                            that.freshMenuDisable(true)
                            return res.text();
                        } else {
                            if (res.status == 404) {
                                litSearch.setPercent("This File is not supported!", -1)
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                                return Promise.reject();
                            }
                        }
                    }).then(res => {
                        if (res != undefined) {
                            let loadPath = `https://${window.location.host.split(':')[0]}:9000`
                            if (that.vs) {
                                loadPath = `http://${window.location.host.split(':')[0]}:${window.location.port}`
                            }
                            spSystemTrace!.loadDatabaseUrl(loadPath + res, (command: string, percent: number) => {
                                litSearch.setPercent(command + '  ', percent);
                            }, (res) => {
                                litSearch.setPercent("", 101);
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                            })
                        } else {
                            litSearch.setPercent("", 101)
                            progressEL.loading = false;
                            that.freshMenuDisable(false)
                        }

                    })
                })
                return;
            }
            if (that.sqlite) {
                litSearch.setPercent("", 0);
                threadPool.init("sqlite").then(res => {
                    let reader = new FileReader();
                    reader.readAsArrayBuffer(ev as any)
                    reader.onloadend = function (ev) {
                        spSystemTrace!.loadDatabaseArrayBuffer(this.result as ArrayBuffer, (command: string, percent: number) => {
                            litSearch.setPercent(command + '  ', percent);
                        }, () => {
                            litSearch.setPercent("", 101);
                            progressEL.loading = false;
                            that.freshMenuDisable(false)
                        })
                    }
                })
                return;
            }
            if (that.wasm) {
                litSearch.setPercent("", 1);
                threadPool.init("wasm").then(res => {
                    let reader = new FileReader();
                    reader.readAsArrayBuffer(ev as any)
                    reader.onloadend = function (ev) {
                        litSearch.setPercent("ArrayBuffer loaded  ", 2);
                        that.freshMenuDisable(true)
                        let menus = [
                            {
                                title: `${fileName.substring(0, fileName.lastIndexOf('.'))} (${fileSize}M)`,
                                icon: "file-fill",
                                clickHandler: function () {
                                    that.search = true
                                    showContent(spSystemTrace!)
                                }
                            }
                        ];

                        mainMenu.menus!.splice(1, 1, {
                            collapsed: false,
                            title: "Current Trace",
                            describe: "Actions on the current trace",
                            children: menus
                        })
                        spSystemTrace!.loadDatabaseArrayBuffer(this.result as ArrayBuffer, (command: string, percent: number) => {
                            litSearch.setPercent(command + '  ', percent);
                        }, (res) => {
                            if (res.status) {
                                litSearch.setPercent("", 101);
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                            } else {
                                litSearch.setPercent("This File is not supported!", -1)
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                                mainMenu.menus!.splice(1, 1);
                                mainMenu.menus = mainMenu.menus!;
                            }
                        })
                    }
                })
                return;
            }
        }

        mainMenu.menus = [
            {
                collapsed: false,
                title: 'Navigation',
                describe: 'Open or record a new trace',
                children: [
                    {
                        title: "Open trace file",
                        icon: "folder",
                        fileChoose: true,
                        fileHandler: function (ev: InputEvent) {
                            openTraceFile(ev.detail as any);
                        }
                    },
                    {
                        title: "Record new trace", icon: "copyhovered", clickHandler: function (item: MenuItem) {
                            that.search = false
                            showContent(spRecordTrace!)
                        }
                    }
                ]
            },
        ]

        let body = document.querySelector("body");
        body!.addEventListener('dragover', (e: any) => {
            e.preventDefault();
            e.stopPropagation();
            if (e.dataTransfer.items.length > 0 && e.dataTransfer.items[0].kind === "file") {
                e.dataTransfer.dropEffect = 'copy';
                if (!this.rootEL!.classList.contains('filedrag')) {
                    this.rootEL!.classList.add("filedrag")
                }
            }
        }, false);
        body!.addEventListener("dragleave", (e) => {
            e.stopPropagation();
            e.preventDefault();
            if (this.rootEL!.classList.contains('filedrag')) {
                this.rootEL!.classList.remove("filedrag")
            }
        }, false);
        body!.addEventListener('drop', (e: any) => {
            e.preventDefault();
            e.stopPropagation();
            if (this.rootEL!.classList.contains('filedrag')) {
                this.rootEL!.classList.remove("filedrag")
            }
            if (e.dataTransfer.items !== undefined && e.dataTransfer.items.length > 0) {
                let item = e.dataTransfer.items[0];
                if (item.webkitGetAsEntry()?.isFile) {
                    openTraceFile(item.getAsFile());
                } else if (item.webkitGetAsEntry()?.isDirectory) {
                    litSearch.setPercent("This File is not supported!", -1)
                    progressEL.loading = false;
                    that.freshMenuDisable(false)
                    mainMenu.menus!.splice(1, 1);
                    mainMenu.menus = mainMenu.menus!;
                    spSystemTrace!.reset(null);
                }
            }
        }, false);
        document.addEventListener("keydown",(event)=> {
            const e = event || window.event;
            const ctrlKey = e.ctrlKey || e.metaKey;
            if (ctrlKey && (this.keyCodeMap as any)[e.keyCode]) {
                e.preventDefault();
            } else if (e.detail) { // Firefox
                event.returnValue = false;
            }
        })
        document.body.addEventListener('wheel', (e) => {
            if (e.ctrlKey) {
                if (e.deltaY < 0) {
                    e.preventDefault();
                    return false;
                }
                if (e.deltaY > 0) {
                    e.preventDefault();
                    return false;
                }
            }
        }, { passive: false });
    }

    freshMenuDisable(disable: boolean) {
        let mainMenu = this.shadowRoot?.querySelector('#main-menu') as LitMainMenu
        // @ts-ignore
        mainMenu.menus[0].children[0].disabled = disable
        mainMenu.menus = mainMenu.menus;
    }
}
