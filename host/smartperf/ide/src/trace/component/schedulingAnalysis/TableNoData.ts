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

import {BaseElement, element} from "../../../base-ui/BaseElement.js";

@element('table-no-data')
export class TableNoData extends BaseElement {

    static get observedAttributes() {
        return ['noData',"contentWidth","height"]
    }

    private dataSlot:HTMLDivElement | null | undefined;
    private noDataIcon:HTMLDivElement | null | undefined;

    initElements(): void {
        this.dataSlot = this.shadowRoot!.querySelector<HTMLDivElement>(".no-data")
        this.noDataIcon = this.shadowRoot!.querySelector<HTMLDivElement>(".d-box")
    }

    get noData(){
        return this.hasAttribute("noData");
    }

    set noData(value:boolean){
        if (value) {
            this.setAttribute('noData','');
        } else {
            this.removeAttribute("noData");
        }
    }

    get contentWidth(){
        return this.getAttribute('contentWidth')||'100%';
    }
    set contentWidth(value){
        this.shadowRoot!.querySelector<HTMLDivElement>(".d-box")!.style.width = value;
        this.setAttribute('contentWidth', value);
    }
    get contentHeight(){
        return this.getAttribute('contentHeight')||'80%';
    }
    set contentHeight(value){
        this.shadowRoot!.querySelector<HTMLDivElement>(".d-box")!.style.height = value;
        this.setAttribute('contentHeight', value);
    }


    initHtml(): string {
        return `
        <style>
        :host {
            width: 100%;
            height: 100%;
            display: block;
        }
        :host(:not([noData])) .no-data{
            display: block;
            height: 100%;
            width: 100%;
        }
        :host([noData]) .no-data{
            display: none;
        }
        :host(:not([noData])) .d-box{
            display: none;
        }
        :host([noData]) .d-box{
            width: ${this.contentWidth};
            height: ${this.contentHeight};
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
        }
        .no-data-icon{
            background-image: url("data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHN2ZyB3aWR0aD0iNDg5cHgiIGhlaWdodD0iMzAzcHgiIHZpZXdCb3g9IjAgMCA0ODkgMzAzIiB2ZXJzaW9uPSIxLjEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiPgogICAgPHRpdGxlPue9kee7nOe8uuecgemhtTwvdGl0bGU+CiAgICA8ZGVmcz4KICAgICAgICA8bGluZWFyR3JhZGllbnQgeDE9IjUwJSIgeTE9IjEwMCUiIHgyPSI1MCUiIHkyPSIwJSIgaWQ9ImxpbmVhckdyYWRpZW50LTEiPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRkZGOUVGIiBzdG9wLW9wYWNpdHk9IjAiIG9mZnNldD0iMCUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0ZFRjhFRiIgc3RvcC1vcGFjaXR5PSIwLjAyIiBvZmZzZXQ9IjE3JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRkJGNUVFIiBzdG9wLW9wYWNpdHk9IjAuMDYiIG9mZnNldD0iMzIlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGNUYxRUMiIHN0b3Atb3BhY2l0eT0iMC4xNCIgb2Zmc2V0PSI0NiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0VERUJFQSIgc3RvcC1vcGFjaXR5PSIwLjI2IiBvZmZzZXQ9IjYwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRTNFMkU3IiBzdG9wLW9wYWNpdHk9IjAuNCIgb2Zmc2V0PSI3NCUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q2RDhFNCIgc3RvcC1vcGFjaXR5PSIwLjU4IiBvZmZzZXQ9Ijg3JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjQzhDREUwIiBzdG9wLW9wYWNpdHk9IjAuNzkiIG9mZnNldD0iOTklIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNDN0NDRTAiIHN0b3Atb3BhY2l0eT0iMC44IiBvZmZzZXQ9IjEwMCUiPjwvc3RvcD4KICAgICAgICA8L2xpbmVhckdyYWRpZW50PgogICAgICAgIDxsaW5lYXJHcmFkaWVudCB4MT0iNTAlIiB5MT0iMTAwJSIgeDI9IjUwJSIgeTI9IjAlIiBpZD0ibGluZWFyR3JhZGllbnQtMiI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSI0Mi4xOTA4JSIgeTE9Ijc3Ljg5JSIgeDI9IjU3LjgwOTIlIiB5Mj0iMjIuMTElIiBpZD0ibGluZWFyR3JhZGllbnQtMyI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGRkY5RUYiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRkVGOEVGIiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGQkY1RUUiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Y1RjFFQyIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRURFQkVBIiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNFM0UyRTciIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDZEOEU0IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNDOENERTAiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0M3Q0NFMCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSI0Mi4xOTA4JSIgeTE9Ijc3Ljg5JSIgeDI9IjU3LjgwOTIlIiB5Mj0iMjIuMTElIiBpZD0ibGluZWFyR3JhZGllbnQtNCI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSI0Mi4xOTA4JSIgeTE9Ijc3Ljg4JSIgeDI9IjU3LjgwOTIlIiB5Mj0iMjIuMTElIiBpZD0ibGluZWFyR3JhZGllbnQtNSI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGRkY5RUYiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRkVGOEVGIiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGQkY1RUUiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Y1RjFFQyIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRURFQkVBIiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNFM0UyRTciIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDZEOEU0IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNDOENERTAiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0M3Q0NFMCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSI0Mi4xOTA4JSIgeTE9Ijc3Ljg4JSIgeDI9IjU3LjgwOTIlIiB5Mj0iMjIuMTElIiBpZD0ibGluZWFyR3JhZGllbnQtNiI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSI0Mi4xOTM2JSIgeTE9Ijc3Ljg5JSIgeDI9IjU3LjgwOTIlIiB5Mj0iMjIuMTIlIiBpZD0ibGluZWFyR3JhZGllbnQtNyI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGRkY5RUYiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRkVGOEVGIiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGQkY1RUUiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Y1RjFFQyIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRURFQkVBIiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNFM0UyRTciIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDZEOEU0IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNDOENERTAiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0M3Q0NFMCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSI0Mi4xOTM2JSIgeTE9Ijc3Ljg5JSIgeDI9IjU3LjgwOTIlIiB5Mj0iMjIuMTIlIiBpZD0ibGluZWFyR3JhZGllbnQtOCI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMCIgb2Zmc2V0PSIwJSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMDIiIG9mZnNldD0iMTclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC4wNiIgb2Zmc2V0PSIzMiUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjE0IiBvZmZzZXQ9IjQ2JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuMjYiIG9mZnNldD0iNjAlIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC40IiBvZmZzZXQ9Ijc0JSI+PC9zdG9wPgogICAgICAgICAgICA8c3RvcCBzdG9wLWNvbG9yPSIjRDhEOEQ4IiBzdG9wLW9wYWNpdHk9IjAuNTgiIG9mZnNldD0iODclIj48L3N0b3A+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEOEQ4RDgiIHN0b3Atb3BhY2l0eT0iMC43OSIgb2Zmc2V0PSI5OSUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0Q4RDhEOCIgc3RvcC1vcGFjaXR5PSIwLjgiIG9mZnNldD0iMTAwJSI+PC9zdG9wPgogICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICA8L2RlZnM+CiAgICA8ZyBpZD0i572R57uc57y655yB6aG1IiBzdHJva2U9Im5vbmUiIHN0cm9rZS13aWR0aD0iMSIgZmlsbD0ibm9uZSIgZmlsbC1ydWxlPSJldmVub2RkIj4KICAgICAgICA8ZyBmaWxsLXJ1bGU9Im5vbnplcm8iPgogICAgICAgICAgICA8ZWxsaXBzZSBpZD0i5qSt5ZyG5b2iIiBmaWxsPSJ1cmwoI2xpbmVhckdyYWRpZW50LTIpIiBjeD0iMjUwIiBjeT0iMjUwLjU5MjExMyIgcng9IjE2NC42ODY0NDkiIHJ5PSI1Mi41OTM0MTQ0Ij48L2VsbGlwc2U+CiAgICAgICAgICAgIDxwYXRoIGQ9Ik00OS4yMzUzMDc4LDY3LjU2NDAxOTEgTDUyLjgwMTI2MDcsNjcuNTY0MDE5MSBDNjAuNTMzMjQ3Miw2Ny41NjQwMTkxIDY2LjgwMTI2MDcsNzMuODMyMDMyNiA2Ni44MDEyNjA3LDgxLjU2NDAxOTEgTDY2LjgwMTI2MDcsMTY2LjI5OTU2NSBDNjYuODAxMjYwNywxNzQuMDMxNTUyIDYwLjUzMzI0NzIsMTgwLjI5OTU2NSA1Mi44MDEyNjA3LDE4MC4yOTk1NjUgTDQ5LjIzNTMwNzgsMTgwLjI5OTU2NSBDNDEuNTAzMzIxMywxODAuMjk5NTY1IDM1LjIzNTMwNzgsMTc0LjAzMTU1MiAzNS4yMzUzMDc4LDE2Ni4yOTk1NjUgTDM1LjIzNTMwNzgsODEuNTY0MDE5MSBDMzUuMjM1MzA3OCw3My44MzIwMzI2IDQxLjUwMzMyMTMsNjcuNTY0MDE5MSA0OS4yMzUzMDc4LDY3LjU2NDAxOTEgWiIgaWQ9IuefqeW9oiIgZmlsbD0idXJsKCNsaW5lYXJHcmFkaWVudC00KSIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoNTEuMDE4Mjg0LCAxMjMuOTMxNzkyKSByb3RhdGUoNDUuMDAwMDAwKSB0cmFuc2xhdGUoLTUxLjAxODI4NCwgLTEyMy45MzE3OTIpICI+PC9wYXRoPgogICAgICAgICAgICA8cGF0aCBkPSJNNDI2LjkwMTA0NiwxMzQuMDc4MDcyIEw0MzAuNDY2OTk5LDEzNC4wNzgwNzIgQzQzOC4xOTg5ODUsMTM0LjA3ODA3MiA0NDQuNDY2OTk5LDE0MC4zNDYwODYgNDQ0LjQ2Njk5OSwxNDguMDc4MDcyIEw0NDQuNDY2OTk5LDIzMi44MTM2MTggQzQ0NC40NjY5OTksMjQwLjU0NTYwNSA0MzguMTk4OTg1LDI0Ni44MTM2MTggNDMwLjQ2Njk5OSwyNDYuODEzNjE4IEw0MjYuOTAxMDQ2LDI0Ni44MTM2MTggQzQxOS4xNjkwNiwyNDYuODEzNjE4IDQxMi45MDEwNDYsMjQwLjU0NTYwNSA0MTIuOTAxMDQ2LDIzMi44MTM2MTggTDQxMi45MDEwNDYsMTQ4LjA3ODA3MiBDNDEyLjkwMTA0NiwxNDAuMzQ2MDg2IDQxOS4xNjkwNiwxMzQuMDc4MDcyIDQyNi45MDEwNDYsMTM0LjA3ODA3MiBaIiBpZD0i55+p5b2iIiBmaWxsPSJ1cmwoI2xpbmVhckdyYWRpZW50LTYpIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSg0MjguNjg0MDIyLCAxOTAuNDQ1ODQ1KSByb3RhdGUoNDUuMDAwMDAwKSB0cmFuc2xhdGUoLTQyOC42ODQwMjIsIC0xOTAuNDQ1ODQ1KSAiPjwvcGF0aD4KICAgICAgICAgICAgPHBhdGggZD0iTTQzNi4xOTg3MzksLTUuMzQ5NDg4ODEgTDQzOS43NjQ2OTIsLTUuMzQ5NDg4ODEgQzQ0Ny40OTY2NzksLTUuMzQ5NDg4ODEgNDUzLjc2NDY5MiwwLjkxODUyNDY5NyA0NTMuNzY0NjkyLDguNjUwNTExMTkgTDQ1My43NjQ2OTIsOTMuMzg2MDU3MiBDNDUzLjc2NDY5MiwxMDEuMTE4MDQ0IDQ0Ny40OTY2NzksMTA3LjM4NjA1NyA0MzkuNzY0NjkyLDEwNy4zODYwNTcgTDQzNi4xOTg3MzksMTA3LjM4NjA1NyBDNDI4LjQ2Njc1MywxMDcuMzg2MDU3IDQyMi4xOTg3MzksMTAxLjExODA0NCA0MjIuMTk4NzM5LDkzLjM4NjA1NzIgTDQyMi4xOTg3MzksOC42NTA1MTExOSBDNDIyLjE5ODczOSwwLjkxODUyNDY5NyA0MjguNDY2NzUzLC01LjM0OTQ4ODgxIDQzNi4xOTg3MzksLTUuMzQ5NDg4ODEgWiIgaWQ9IuefqeW9oiIgZmlsbD0idXJsKCNsaW5lYXJHcmFkaWVudC04KSIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoNDM3Ljk4MTcxNiwgNTEuMDE4Mjg0KSByb3RhdGUoNDUuMDAwMDAwKSB0cmFuc2xhdGUoLTQzNy45ODE3MTYsIC01MS4wMTgyODQpICI+PC9wYXRoPgogICAgICAgICAgICA8ZyBpZD0iY3B1IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgxNzMuMDAwMDAwLCA0Ny4wMDAwMDApIiBmaWxsPSIjRDhEOEQ4Ij4KICAgICAgICAgICAgICAgIDxwYXRoIGQ9Ik0xNTIuOTU5ODExLDUzLjUyMTgzMDYgTDE1Mi45NTk4MTEsNDEuMjg4MjY5MyBMMTM4LjQ3MTYzMSw0MS4zMjg1MTEzIEwxMzguNDcxNjMxLDIyLjM1NDQxODcgQzEzOC40NzE2MzEsMTcuNTQ1NTAyNCAxMzYsMTQuODQ5Mjg5OCAxMzEuMTk3NCwxNC44NDkyODk4IEwxMTMuNzM1MjI1LDE0Ljg0OTI4OTggTDExMy43MzUyMjUsMCBMMTAxLjM3NzA2OSwwIEwxMDEuMzc3MDY5LDE0Ljg0OTI4OTggTDgyLjgwOTY5MjcsMTQuODQ5Mjg5OCBMODIuODA5NjkyNywwIEw3MC40NTE1MzY2LDAgTDcwLjQzMTQ0MjEsMTQuODQ5Mjg5OCBMNTEuODY0MDY2MiwxNC44NDkyODk4IEw1MS45MDQyNTUzLDAgTDM5LjQ4NTgxNTYsMCBMMzkuNTA1OTEwMiwxNC44NDkyODk4IEwyMy4yNDk0MDksMTQuODQ5Mjg5OCBDMTcuMjgxMzIzOSwxNC44NDkyODk4IDE0LjgyOTc4NzIsMTcuNTQ1NTAyNCAxNC44Njk5NzY0LDIzLjc4MzAwODkgTDE0Ljc0OTQwOSw0MS4zMjg1MTEzIEwwLDQxLjMyODUxMTMgTDAsNTMuNTIxODMwNiBMMTQuNzQ5NDA5LDUzLjUyMTgzMDYgTDE0Ljc0OTQwOSw3MC42MDQ1NTAyIEwwLDcwLjU4NDQyOTIgTDAsODIuOTU4ODM3NSBMMTQuNzQ5NDA5LDgyLjk5OTA3OTQgTDE0Ljc0OTQwOSwxMDAuMzQzMzcyIEwwLDEwMC4zNDMzNzIgTDAsMTEyLjQ5NjQ0OSBMMTQuNzQ5NDA5LDExMi40OTY0NDkgTDE0Ljc0OTQwOSwxMjkuMDc2MTQ0IEMxNC43NDk0MDksMTM1LjkxNzI4IDE3LjUwMjM2NDEsMTM4Ljc1NDM0IDI0LjM1NDYwOTksMTM4Ljc1NDM0IEwzOS41MDU5MTAyLDEzOC43NTQzNCBMMzkuNDg1ODE1NiwxNTMgTDUxLjgyMzg3NzEsMTUzIEw1MS44ODQxNjA4LDEzOC43NTQzNCBMNzAuNDUxNTM2NiwxMzguNzU0MzQgTDcwLjQ1MTUzNjYsMTUzIEw4Mi44NDk4ODE4LDE1MyBMODIuODI5Nzg3MiwxMzguNzU0MzQgTDEwMS4zOTcxNjMsMTM4Ljc1NDM0IEwxMDEuMzU2OTc0LDE1MyBMMTEzLjc1NTMxOSwxNTMgTDExMy43NzU0MTQsMTM4Ljc1NDM0IEwxMzAuMzEzMjM5LDEzOC43NTQzNCBDMTM1Ljg1OTMzOCwxMzguNzU0MzQgMTM4LjUxMTgyLDEzMy44NjQ5NCAxMzguNTExODIsMTI5LjA3NjE0NCBMMTM4LjUxMTgyLDExMi40OTY0NDkgTDE1MywxMTIuNDk2NDQ5IEwxNTMsMTAwLjM0MzM3MiBMMTM4LjQ3MTYzMSwxMDAuMzQzMzcyIEwxMzguNDcxNjMxLDgyLjk5OTA3OTQgTDE1Mi45NTk4MTEsODIuOTk5MDc5NCBMMTUyLjk1OTgxMSw3MC42NDQ3OTIyIEwxMzguNDcxNjMxLDcwLjYwNDU1MDIgTDEzOC40NzE2MzEsNTMuNTIxODMwNiBMMTUyLjk1OTgxMSw1My41MjE4MzA2IFogTTEyNi4wOTMzODEsMTI2LjM1OTgxMSBMMjcuMTI3NjU5NiwxMjYuMzU5ODExIEwyNy4xMjc2NTk2LDI3LjI0MzgxOSBMMTI2LjExMzQ3NSwyNy4yNDM4MTkgTDEyNi4xMTM0NzUsMTI2LjM1OTgxMSBMMTI2LjA5MzM4MSwxMjYuMzU5ODExIFogTTQ3LjgwNDk2NDUsMTEwLjcyNTgwMiBMMTA3LjI0NDY4MSwxMTAuNzI1ODAyIEMxMDkuNjE1ODM5LDExMC43MjU4MDIgMTEwLjQ3OTkwNSwxMDkuMDk2MDAyIDExMC40Nzk5MDUsMTA2Ljk0MzA1NiBMMTEwLjQ3OTkwNSw0Ny4yODQzMjQgQzExMC40Nzk5MDUsNDQuNzQ5MDc5NCAxMDguNzUxNzczLDQzLjM0MDYxMDIgMTA2LjcwMjEyOCw0My4zNDA2MTAyIEw0Ny4xODIwMzMxLDQzLjM0MDYxMDIgQzQ0LjA4NzQ3MDQsNDMuMzQwNjEwMiA0My4xODMyMTUxLDQ0LjQ4NzUwNjYgNDMuMTgzMjE1MSw0Ny42MDYyNTk5IEw0My4xODMyMTUxLDEwNi4wNzc4NTQgQzQzLjE4MzIxNTEsMTA5LjE3NjQ4NiA0NC43MTA0MDE5LDExMC43MjU4MDIgNDcuODA0OTY0NSwxMTAuNzI1ODAyIFoiIGlkPSLlvaLnirYiPjwvcGF0aD4KICAgICAgICAgICAgPC9nPgogICAgICAgICAgICA8ZyBpZD0i57yW57uELTQiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDM1OC43NTg2MDcsIDc3LjgzNzE3OCkgcm90YXRlKDM0LjAwMDAwMCkgdHJhbnNsYXRlKC0zNTguNzU4NjA3LCAtNzcuODM3MTc4KSB0cmFuc2xhdGUoMzQ1LjQ2NzQzNCwgNDYuODM3MTc4KSIgZmlsbD0iI0Q4RDhEOCI+CiAgICAgICAgICAgICAgICA8cG9seWdvbiBpZD0i6Lev5b6EIiBwb2ludHM9IjkuNjczODcxMTQgMC43MTczNjYxNTEgMjQuOTU1MjI3MSA5LjA5NDk0NzAyZS0xMyAxNy4yNjcwOTE1IDIyLjg3MDkzNzIgMjYuNTgyMzQ1NyAyMS45OTA1MzMzIDYuMDg3NDMwNDYgNjIgMTMuNTkyNTE1MiAzMS4zNjE5NDM4IDAuNTgyMzQ1NzE0IDMwLjYzODA1NjIiPjwvcG9seWdvbj4KICAgICAgICAgICAgICAgIDxwb2x5Z29uIGlkPSLot6/lvoQiIHBvaW50cz0iNy4xMzIyMjY4MyAwLjA5NTIzMjk5NjQgMjEuMzkwMzE4MSAwLjA5NTIzMjk5NjQgMTQuMjU4MDkxMyAyMi4xNDY2MzI0IDIzIDIyLjE0NjYzMjQgNS4yOTM0OTkzMSA2MS4wOTUyMzMgMTAuODA5NjgxOSAzMC41OTE5ODczIDEuNzA1MzAyNTdlLTEzIDMwLjU5MTk4NzMiPjwvcG9seWdvbj4KICAgICAgICAgICAgPC9nPgogICAgICAgIDwvZz4KICAgIDwvZz4KPC9zdmc+");
            width: 60%;
            height: 60%;
            min-height: 200px;
            min-width: 200px;
            background-size: 100% 100%;
        }
        .no-data-text{
            color: var(--dark-color1,#252525);
            opacity: 0.6;
            font-weight: 400;
        }
        </style>
        <slot class="no-data"></slot>
        <div class="d-box">
            <div class="no-data-icon"></div>
            <div class="no-data-text">Sorry, no data</div>
        </div>
        `;
    }
}