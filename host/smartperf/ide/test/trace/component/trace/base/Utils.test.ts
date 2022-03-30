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
import {Utils} from "../../../../../dist/trace/component/trace/base/Utils.js";

describe("Utils Test", () => {
    beforeAll(() => {
    })

    it('Utils Test01', () => {
        let instance = Utils.getInstance();
        let instance2 = Utils.getInstance();
        expect(instance).toBe(instance2)
    });

    it('Utils Test02', () => {
        let instance = Utils.getInstance();
        expect(instance.getStatusMap().get("D")).toBe("Uninterruptible Sleep")
    });

    it('Utils Test03', () => {
        expect(Utils.getEndState("D")).toBe("Uninterruptible Sleep")
    });

    it('Utils Test04', () => {
        expect(Utils.getEndState("")).toBe("")
    });

    it('Utils Test05', () => {
        expect(Utils.getEndState("ggg")).toBe("Unknown State")
    });

    it('Utils Test06', () => {
        expect(Utils.getStateColor("D")).toBe("#f19b38")
    });

    it('Utils Test07', () => {
        expect(Utils.getStateColor("R")).toBe("#a0b84d")
    });
    it('Utils Test08', () => {
        expect(Utils.getStateColor("I")).toBe("#673ab7")
    });

    it('Utils Test09', () => {
        expect(Utils.getStateColor("Running")).toBe("#467b3b")
    });

    it('Utils Test09', () => {
        expect(Utils.getStateColor("S")).toBe("#e0e0e0")
    });


    it('Utils Test10', () => {
        expect(Utils.getTimeString(5900_000_000_000)).toBe("1h 38m ")
    });

    it('Utils Test11', () => {
        expect(Utils.getByteWithUnit(1_000_000_001)).toBe("1.00Gb")
    });

    it('Utils Test12', () => {
        expect(Utils.getByteWithUnit(1_000_000_000)).toBe("1000.00Mb")
    });

    it('Utils Test12', () => {
        expect(Utils.getByteWithUnit(1000_000)).toBe("1000.00kb")
    });


    afterAll(() => {
        // 后处理操作
    })
})