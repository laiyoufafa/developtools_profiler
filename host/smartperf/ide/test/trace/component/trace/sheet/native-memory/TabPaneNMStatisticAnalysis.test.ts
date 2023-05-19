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

import crypto from 'crypto';
// @ts-ignore
import { TabPaneNMStatisticAnalysis } from '../../../../../../dist/trace/component/trace/sheet/native-memory/TabPaneNMStatisticAnalysis.js';
// @ts-ignore
import { LitTable } from '../../../../../../dist/base-ui/table/lit-table.js';

window.ResizeObserver =
    window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

jest.mock('../../../../../../dist/base-ui/chart/pie/LitChartPie.js', () => {
    return {};
});

jest.mock('../../../../../../dist/base-ui/table/lit-table.js', () => {
    return {};
});

Object.defineProperty(global.self, 'crypto', {
    value: {
        getRandomValues: (arr: string | any[]) => crypto.randomBytes(arr.length),
    },
});

describe('TabPaneNMStatisticAnalysis Test', () => {
    let tabStatisticAnalysis = new TabPaneNMStatisticAnalysis();
    tabStatisticAnalysis.tableType.reMeauseHeight = jest.fn(() => true);
    tabStatisticAnalysis.tableSo.reMeauseHeight = jest.fn(() => true);
    tabStatisticAnalysis.tableFunction.reMeauseHeight = jest.fn(() => true);
    let dataArray = {
        applyCount: 25336,
        applyCountPercent: '99',
        applySize: 13372754,
        applySizeFormat: '12.75MB',
        applySizePercent: '25023.87',
        existCount: 1011,
        existCountPercent: '98.54',
        existSize: 809825,
        existSizeFormat: '790.84KB',
        existSizePercent: '2.36',
        releaseCount: 52168,
        releaseCountPercent: '99.53',
        releaseSize: 12562929,
        releaseSizeFormat: '11.98MB',
        releaseSizePercent: '1.79',
        tableName: 'Test',
        typeId: 0,
        typeName: 'Test',
    };
    let select = {
        recordStartNs: 8406282873525,
        leftNs: 16648778040,
        rightNs: 48320174407,
        hasFps: false,
        nativeMemory: ['All Heap & Anonymous VM', 'All Heap'],
    };
    let processData = [
        {
            applyId: 22945,
            callChainId: 91,
            count: 1,
            endTs: 4831690110,
            id: 22945,
            libId: 420,
            libName: 'test.z.so',
            pid: 1,
            size: 304,
            startTs: 4806916233,
            symbolId: 429,
            symbolName: 'test',
            tid: 5211,
            type: 0,
        },
    ];
    tabStatisticAnalysis.processData = processData;

    it('statisticAnalysis01', function () {
        tabStatisticAnalysis.data = select;
        expect(tabStatisticAnalysis.currentSelection).toEqual(select);
    });

    it('statisticAnalysis02', function () {
        tabStatisticAnalysis.tabName.textContent = 'Statistic By Library Size';
        tabStatisticAnalysis.eventTypeData = [dataArray];
        let mouseMoveEvent: MouseEvent = new MouseEvent('click', <MouseEventInit>{ movementX: 1, movementY: 2 });
        tabStatisticAnalysis.back.dispatchEvent(mouseMoveEvent);
        tabStatisticAnalysis.back.dispatchEvent(mouseMoveEvent);
    });

    it('statisticAnalysis03', function () {
        tabStatisticAnalysis.getLibSize(dataArray, select);
        expect(tabStatisticAnalysis.currentLevel).toEqual(1);
    });

    it('statisticAnalysis04', function () {
        expect(tabStatisticAnalysis.getNMFunctionSize(dataArray, select)).toBeUndefined();
    });

    it('statisticAnalysis05', function () {
        tabStatisticAnalysis.calTypeSize(select, processData);
        expect(tabStatisticAnalysis.processData.length).toBe(1);
    });

    it('statisticAnalysis06', function () {
        let processData = [
            {
                applyId: 22945,
                callChainId: 91,
                count: 1,
                endTs: 4831690110,
                id: 22945,
                libId: 420,
                libName: 'test.z.so',
                pid: 1,
                size: 304,
                startTs: 4806916233,
                symbolId: 429,
                symbolName: 'test',
                tid: 5211,
                type: 1,
            },
        ];
        tabStatisticAnalysis.calTypeSize(select, processData);
        expect(tabStatisticAnalysis.currentLevelReleaseCount).toBe(0);
    });

    it('statisticAnalysis07', function () {
        let processData = [
            {
                applyId: 22945,
                callChainId: 91,
                count: 1,
                endTs: 4831690110,
                id: 22945,
                libId: 420,
                libName: 'test.z.so',
                pid: 1,
                size: 304,
                startTs: 4806916233,
                symbolId: 429,
                symbolName: 'test',
                tid: 5211,
                type: 2,
            },
        ];
        tabStatisticAnalysis.calTypeSize(select, processData);
        expect(tabStatisticAnalysis.currentLevelApplySize).toBe(0);
    });

    it('statisticAnalysis08', function () {
        let processData = [
            {
                applyId: 22945,
                callChainId: 91,
                count: 1,
                endTs: 4831690110,
                id: 22945,
                libId: 420,
                libName: 'test.z.so',
                pid: 1,
                size: 304,
                startTs: 4806916233,
                symbolId: 429,
                symbolName: 'test',
                tid: 5211,
                type: 3,
            },
        ];
        tabStatisticAnalysis.calTypeSize(select, processData);
        expect(tabStatisticAnalysis.processData.length).toBe(1);
    });
});
