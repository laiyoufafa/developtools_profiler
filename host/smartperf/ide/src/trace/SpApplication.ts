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
import {SpSystemTrace} from "./component/SpSystemTrace.js";
import {LitMainMenu, MenuItem} from "../base-ui/menu/LitMainMenu.js";
import "../base-ui/progress-bar/LitProgressBar.js";
import {LitProgressBar} from "../base-ui/progress-bar/LitProgressBar.js";
import {SpRecordTrace} from "./component/SpRecordTrace.js";
import {SpWelcomePage} from "./component/SpWelcomePage.js";
import {SpSearch} from "./component/trace/search/Search.js";
import {threadPool} from "./database/SqlLite.js";
import "./component/trace/search/Search.js";
import "./component/SpWelcomePage.js";
import "./component/SpSystemTrace.js";
import "./component/SpWelcomePage.js";
import "./component/SpRecordTrace.js";
import {TraceRow} from "./component/trace/base/TraceRow.js";

@element('sp-application')
export class SpApplication extends BaseElement {
    static skinChange: Function | null | undefined = null;
    static skinChange2: Function | null | undefined = null;
    skinChangeArray: Array<Function> = [];
    private rootEL: HTMLDivElement | undefined | null

    static get observedAttributes() {
        return ["server", "sqlite", "wasm", "dark", "vs"]
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
    }

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
            z-index: 101;
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
    box-shadow: 4px 0px 20px rgba(0,0,0,0.05);
    z-index: 101;
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
:host(:not([search])) #sp-search  {
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
::placeholder {
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
    <lit-main-menu id="main-menu" class="menu" data=''>
    </lit-main-menu>
    <div class="search-container">
        <div class="search" style="position: relative;">
            <div class="sidebar-button" style="width: 0">
                <svg class="icon" id="icon" aria-hidden="true" viewBox="0 0 1024 1024">
                     <use id="use" xlink:href="./base-ui/icon.svg#icon-menu"></use>
                </svg>
            </div>
            <sp-search id="sp-search"></sp-search>
        </div>
        <lit-progress-bar class="progress"></lit-progress-bar>
    </div>
    
    <div id="app-content" class="content">
        <sp-welcome style="visibility:visible;top:0px;left:0px;position:absolute;z-index: 100" id="sp-welcome"></sp-welcome>
        <sp-system-trace style="visibility:visible;" id="sp-system-trace"></sp-system-trace>
        <sp-record-trace style="width:100%;height:100%;overflow:auto;visibility:hidden;top:0px;left:0px;right:0;bottom:0px;position:absolute;z-index: 102" id="sp-record-trace"></sp-record-trace>
    </div>
</div>`;
    }

    set vs(isVs: boolean) {
        if (isVs) {
            this.setAttribute("vs", "")
        }
    }

    get vs(): boolean {
        return this.hasAttribute("vs")
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

    set search(search: boolean) {
        if (search) {
            this.setAttribute('search', '')
        } else {
            this.removeAttribute('search')
        }
    }

    initElements() {
        let that = this;
        this.rootEL = this.shadowRoot!.querySelector<HTMLDivElement>(".root")
        let spWelcomePage = this.shadowRoot!.querySelector("#sp-welcome") as SpWelcomePage
        let spSystemTrace = this.shadowRoot!.querySelector<SpSystemTrace>("#sp-system-trace")
        let spRecordTrace = this.shadowRoot!.querySelector<SpRecordTrace>("#sp-record-trace")
        let mainMenu = this.shadowRoot?.querySelector('#main-menu') as LitMainMenu
        let progressEL = this.shadowRoot?.querySelector('.progress') as LitProgressBar
        let spSearch = this.shadowRoot?.querySelector('#sp-search') as SpSearch
        let sidebarButton: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector('.sidebar-button')
        let childNodes = [spSystemTrace, spRecordTrace, spWelcomePage]
        spSearch.addEventListener("focus", () => {
            spSystemTrace!.keyboardEnable = false
        })
        spSearch.addEventListener('blur', () => {
            spSystemTrace!.keyboardEnable = true
        })
        spSearch.addEventListener("enter", (e: any) => {
            spSystemTrace!.shadowRoot?.querySelectorAll<TraceRow<any>>('trace-row').forEach(item => {
                if (e.detail.value == null || e.detail.value == undefined || e.detail.value == '') {
                    if (item.rowType == TraceRow.ROW_TYPE_CPU ||
                        item.rowType == TraceRow.ROW_TYPE_CPU_FREQ ||
                        item.rowType == TraceRow.ROW_TYPE_FPS ||
                        item.rowType == TraceRow.ROW_TYPE_PROCESS) {
                        item.rowHidden = false;
                    } else {
                        item.rowHidden = true;
                    }
                } else {
                    if (item.name.toLowerCase().indexOf(e.detail.value.toLowerCase()) >= 0) {
                        item.rowHidden = false;
                    } else {
                        item.rowHidden = true;
                    }
                }
            })
        })
        sidebarButton!.onclick = (event) => {
            let menu: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector('#main-menu')
            let menuButton: HTMLElement | undefined | null = this.shadowRoot?.querySelector('.sidebar-button')
            if (menu) {
                menu.style.width = `248px`
                // @ts-ignore
                menu.style.zIndex = 200;
                menu.style.display = `flex`
            }
            if (menuButton) {
                menuButton.style.width = `0px`
            }
        }
        let icon: HTMLDivElement | undefined | null = this.shadowRoot?.querySelector("#main-menu")?.shadowRoot?.querySelector("div.header > div")
        icon!.onclick = (mouseEvent) => {
            let menu: HTMLElement | undefined | null = this.shadowRoot?.querySelector("#main-menu")
            let menuButton: HTMLElement | undefined | null = this.shadowRoot?.querySelector('.sidebar-button')
            if (menu) {
                menu.style.width = `0px`
                menu.style.display = `none`
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

        function openTraceFile(ev: any) {
            showContent(spSystemTrace!)
            that.search = true
            progressEL.loading = true
            let fileName = (ev as any).name
            let fileSize = ((ev as any).size / 1000000).toFixed(1)
            document.title = `${fileName.substring(0, fileName.lastIndexOf('.'))} (${fileSize}M)`

            if (that.server) {
                threadPool.init("server").then(() => {
                    spSearch.setPercent("parse trace", 1);
                    const fd = new FormData()
                    that.freshMenuDisable(true)
                    fd.append('file', ev as any)
                    let uploadPath = `https://${window.location.host.split(':')[0]}:9001/upload`
                    if (that.vs) {
                        uploadPath = `http://${window.location.host.split(':')[0]}:${window.location.port}/upload`
                    }
                    fetch(uploadPath, {
                        method: 'POST',
                        body: fd,
                    }).then(res => {
                        spSearch.setPercent("load database", 5);
                        if (res.ok) {
                            mainMenu.menus!.splice(1, 1, {
                                collapsed: false,
                                title: "Current Trace",
                                describe: "Actions on the current trace",
                                children: [
                                    {
                                        title: `${fileName.substring(0, fileName.lastIndexOf('.'))} (${fileSize}M)`,
                                        icon: "file-fill",
                                        clickHandler: function () {
                                            that.search = true
                                            showContent(spSystemTrace!)
                                        }
                                    }
                                ]
                            })
                            that.freshMenuDisable(true)
                            return res.text();
                        } else {
                            if (res.status == 404) {
                                spSearch.setPercent("This File is not supported!", -1)
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                                return Promise.reject();
                            }
                        }
                    }).then(res => {
                        if (res != undefined) {
                            let loadPath = `https://${window.location.host.split(':')[0]}:9001`
                            if (that.vs) {
                                loadPath = `http://${window.location.host.split(':')[0]}:${window.location.port}`
                            }
                            spSystemTrace!.loadDatabaseUrl(loadPath + res, (command: string, percent: number) => {
                                spSearch.setPercent(command + '  ', percent);
                            }, (res) => {
                                spSearch.setPercent("", 101);
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                            })
                        } else {
                            spSearch.setPercent("", 101)
                            progressEL.loading = false;
                            that.freshMenuDisable(false)
                        }

                    })
                })
                return;
            }
            if (that.sqlite) {
                spSearch.setPercent("", 0);
                threadPool.init("sqlite").then(res => {
                    let reader = new FileReader();
                    reader.readAsArrayBuffer(ev as any)
                    reader.onloadend = function (event) {
                        spSystemTrace!.loadDatabaseArrayBuffer(this.result as ArrayBuffer, (command: string, percent: number) => {
                            spSearch.setPercent(command + '  ', percent);
                        }, () => {
                            spSearch.setPercent("", 101);
                            progressEL.loading = false;
                            that.freshMenuDisable(false)
                        })
                    }
                })
                return;
            }
            if (that.wasm) {
                spSearch.setPercent("", 1);
                threadPool.init("wasm").then(res => {
                    let reader = new FileReader();
                    reader.readAsArrayBuffer(ev as any)
                    reader.onloadend = function (event) {
                        spSearch.setPercent("ArrayBuffer loaded  ", 2);
                        that.freshMenuDisable(true)
                        mainMenu.menus!.splice(1, 1, {
                            collapsed: false,
                            title: "Current Trace",
                            describe: "Actions on the current trace",
                            children: [
                                {
                                    title: `${fileName.substring(0, fileName.lastIndexOf('.'))} (${fileSize}M)`,
                                    icon: "file-fill",
                                    clickHandler: function () {
                                        that.search = true
                                        showContent(spSystemTrace!)
                                    }
                                }
                            ]
                        })
                        spSystemTrace!.loadDatabaseArrayBuffer(this.result as ArrayBuffer, (command: string, percent: number) => {
                            spSearch.setPercent(command + '  ', percent);
                        }, (res) => {
                            if (res.status) {
                                spSearch.setPercent("", 101);
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                            } else {
                                spSearch.setPercent("", 101);
                                progressEL.loading = false;
                                that.freshMenuDisable(false)
                                alert(res.msg)
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
            }
        ]
    }

    freshMenuDisable(disable: boolean) {
        let mainMenu = this.shadowRoot?.querySelector('#main-menu') as LitMainMenu
        // @ts-ignore
        mainMenu.menus[0].children[0].disabled = disable
        mainMenu.menus = mainMenu.menus;
    }
}
