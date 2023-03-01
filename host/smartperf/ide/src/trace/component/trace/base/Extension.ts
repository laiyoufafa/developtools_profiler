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

declare global {
    interface Number {
        n2x(): number;
    }

    interface Array<T> {
        isEmpty(): boolean;

        isNotEmpty(): boolean;
    }

    interface HTMLElement {
        containPoint(ev: MouseEvent, cut?: { left?: number, right?: number, top?: number, bottom?: number }): boolean;
    }
}

Number.prototype.n2x = function (): number {
    return Number(this);
}

Array.prototype.isEmpty = function <T>(): boolean {
    return this == null || this == undefined || this.length == 0;
}
Array.prototype.isNotEmpty = function <T>(): boolean {
    return this != null && this != undefined && this.length > 0;
}

HTMLElement.prototype.containPoint = function (ev, cut) {
    let rect = this.getBoundingClientRect();
    return ev.pageX >= (rect.left + (cut?.left ?? 0))
        && ev.pageX <= (rect.right - (cut?.right ?? 0))
        && ev.pageY >= (rect.top + (cut?.top ?? 0))
        && ev.pageY <= (rect.bottom - (cut?.bottom ?? 0));
}

export {};