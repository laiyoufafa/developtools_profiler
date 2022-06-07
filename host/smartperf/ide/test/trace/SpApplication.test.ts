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
import {SpApplication} from "../../dist/trace/SpApplication.js";
import {LitMainMenu} from "../../src/base-ui/menu/LitMainMenu";

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe('spApplication Test', ()=>{
    it('spApplicationTest01', function () {
        let spApplication = new SpApplication();
        expect(spApplication).not.toBeUndefined()
    });


    it('spApplicationTest02', function () {
        let spApplication = new SpApplication();
        expect(spApplication.sqlite).toBeFalsy()
    });


    it('spApplicationTest03', function () {
        let spApplication = new SpApplication();
        expect(spApplication.wasm).toBeFalsy()
    });

    it('spApplicationTest04', function () {
        let spApplication = new SpApplication();
        expect(spApplication.server).toBeFalsy()
    });


    it('spApplicationTest05', function () {
        let spApplication = new SpApplication();
        spApplication.server = true;
        expect(spApplication.server).toBeTruthy()
    });

    it('spApplicationTest06', function () {
        let spApplication = new SpApplication();
        spApplication.server = false;
        expect(spApplication.server).toBeFalsy()
    });

    it('spApplicationTest07', function () {
        let spApplication = new SpApplication();
        spApplication.search = false;
        expect(spApplication.search).toBeFalsy()
    });

    it('spApplicationTest08', function () {
        let spApplication = new SpApplication();
        spApplication.search = true;
        expect(spApplication.search).toBeUndefined()
    });

    it('spApplicationTest09', function () {
        let spApplication = new SpApplication();
        expect(spApplication.dark).toBeFalsy()
    });
    it('spApplicationTest010', function () {
        let spApplication = new SpApplication();
        spApplication.dark = true;
        expect(spApplication.dark).toBeTruthy()
    });

    it('spApplicationTest11', function () {
        let spApplication = new SpApplication();
        spApplication.dark = false;
        expect(spApplication.dark).toBeFalsy()
    });

    it('spApplicationTest12', function () {
        let spApplication = new SpApplication();
        expect(spApplication.vs).toBeFalsy()
    });
    it('spApplicationTest013', function () {
        let spApplication = new SpApplication();
        spApplication.vs = true;
        expect(spApplication.vs).toBeTruthy()
    });

    it('spApplicationTest14', function () {
        let spApplication = new SpApplication();
        spApplication.vs = false;
        expect(spApplication.vs).toBeFalsy()
    });
    it('spApplicationTest20', function () {
        let spApplication = new SpApplication();
        expect(spApplication.querySql).toBeFalsy()
    });

    it('spApplicationTest15',function (){
        let spApplication = new SpApplication();
        expect(spApplication.freshMenuDisable()).toBeUndefined()
    })

    it('spApplicationTest16',function (){
        let spApplication = new SpApplication();
        expect(spApplication.addSkinListener()).toBeUndefined()
    })

    it('spApplicationTest17',function (){
        let spApplication = new SpApplication();
        expect(spApplication.removeSkinListener()).toBeUndefined()
    })


    it('spApplicationTest18',function (){
        document.body.innerHTML= "<sp-application id='sp'></sp-application>"
        let element = document.querySelector("#sp") as SpApplication;
        element.dispatchEvent(new Event("dragleave"))
    })

    it('spApplicationTest19',function (){
        document.body.innerHTML= "<sp-application id='sp'></sp-application>"
        let element = document.querySelector("#sp") as SpApplication;
        element.dispatchEvent(new Event("drop"))
        //expect(SpApplication.removeSkinListener()).toBeUndefined()
    })

    it('spApplicationTest19',function (){
        document.body.innerHTML= "<sp-application id='sp'></sp-application>"
        let element = document.querySelector("#sp") as SpApplication;
        element.dispatchEvent(new Event("drop"))
        //expect(SpApplication.removeSkinListener()).toBeUndefined()
    })
    it('spApplicationTest21', function () {
        let spApplication = new SpApplication();
        spApplication.showContent = true;
        expect(spApplication.showContent).toBeTruthy()
    });

    it('spApplicationTest22', function () {
        let spApplication = new SpApplication();
        spApplication.showConten = false;
        expect(spApplication.showContent).toBeFalsy()
    });

    it('spApplicationTest23', function () {
        let spApplication = new SpApplication();
        spApplication.openTraceFile = true;
        expect(spApplication.openTraceFile).toBeTruthy()
    });

    it('spApplicationTest24', function () {
        let spApplication = new SpApplication();
        spApplication.openTraceFile = false;
        expect(spApplication.openTraceFile).toBeFalsy()
    });

    it('spApplicationTest25', function () {
        let spApplication = new SpApplication();
        expect(spApplication.initHtml()).toMatchInlineSnapshot(`
"
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
        <div class=\\"root\\">
            <lit-main-menu id=\\"main-menu\\" class=\\"menu\\" data=''></lit-main-menu>
            <div class=\\"search-container\\">
                <div class=\\"search\\" style=\\"position: relative;\\">
                    <div class=\\"sidebar-button\\" style=\\"width: 0\\">
                        <svg class=\\"icon\\" id=\\"icon\\" aria-hidden=\\"true\\" viewBox=\\"0 0 1024 1024\\">
                             <use id=\\"use\\" xlink:href=\\"./base-ui/icon.svg#icon-menu\\"></use>
                        </svg>
                    </div>
                    <lit-search id=\\"lit-search\\"></lit-search>
                </div>
                <lit-progress-bar class=\\"progress\\"></lit-progress-bar>
            </div>
            <div id=\\"app-content\\" class=\\"content\\">
                <sp-welcome style=\\"visibility:visible;top:0px;left:0px;position:absolute;z-index: 100\\" id=\\"sp-welcome\\">
                </sp-welcome>
                <sp-system-trace style=\\"visibility:visible;\\" id=\\"sp-system-trace\\">
                </sp-system-trace>
                <sp-record-trace style=\\"width:100%;height:100%;overflow:auto;visibility:hidden;top:0px;left:0px;right:0;bottom:0px;position:absolute;z-index: 102\\" id=\\"sp-record-trace\\">
                </sp-record-trace>
                <sp-metrics style=\\"width:100%;height:100%;overflow:auto;visibility:hidden;top:0;left:0;right:0;bottom:0;position:absolute;\\" id=\\"sp-metrics\\">
                </sp-metrics>
                <sp-query-sql style=\\"width:100%;height:100%;overflow:auto;visibility:hidden;top:0;left:0;right:0;bottom:0;position:absolute;\\" id=\\"sp-query-sql\\">
                </sp-query-sql>
                <sp-info-and-stats style=\\"width:100%;height:100%;overflow:auto;visibility:hidden;top:0;left:0;right:0;bottom:0;position:absolute;\\" id=\\"sp-info-and-stats\\">
                </sp-info-and-stats>
                <sp-help style=\\"width:100%;height:100%;overflow:auto;visibility:hidden;top:0px;left:0px;right:0;bottom:0px;position:absolute;z-index: 103\\" id=\\"sp-help\\">
                </sp-help>
            </div>
        </div>
        "
`);
    });
})
