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

//@ts-ignore
import { HeapNode } from '../../dist/js-heap/model/DatabaseStruct.js';
//@ts-ignore
import { LoadDatabase } from '../../dist/js-heap/LoadDatabase.js';
//@ts-ignore
import { ParseListener } from '../../dist/js-heap/HeapDataInterface.js';
//@ts-ignore
import { FileInfo } from '../../dist/js-heap/model/UiStruct.js';
import { HeapDataInterface } from '../../src/js-heap/HeapDataInterface';

// @ts-ignore
window.ResizeObserver =
    window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));
jest.mock('../../dist/js-heap/utils/Utils.js', () => {
    return {
        HeapNodeToConstructorItem: (node: HeapNode) => {},
        getTimeForLog: (node: any) => {},
    };
});

jest.mock('../../dist/js-heap/HeapDataInterface.js', () => {
    return {
        ParseListener: {
            parseDone: (fileModule: Array<FileInfo>) => {},
            process: (info: string, process: number) => {},
        },
        HeapDataInterface: {
            getInstance: (fileModule: any) => {
                return {
                    setPraseListener: (prase: any) => {},
                    parseData: (prase: any) => {},
                };
            },
        },
    };
});

const sqlite = require('../../dist/trace/database/SqlLite.js');
jest.mock('../../dist/trace/database/SqlLite.js');

describe('LoadDataBase Test', () => {
    let heapFileData = {
        id: '',
        file_name: '',
        start_time: '',
        end_time: '',
        fpid: '',
    };
    let queryHeapFileMock = sqlite.queryHeapFile;
    queryHeapFileMock.mockResolvedValue([heapFileData]);

    let heapInfoData = [
        {
            key: 'node_count',
            int_value: '',
        },
        {
            key: 'edge_count',
            int_value: '',
        },
        {
            key: 'trace_function_count',
            int_value: '',
        },
        {
            key: 'types',
            int_value: '',
        },
    ];
    let queryHeapInfoMock = sqlite.queryHeapInfo;
    queryHeapInfoMock.mockResolvedValue(heapInfoData);

    let HeapNodeData = [
        {
            node_index: '2',
            type: '',
            name: 'name1',
            id: '',
            self_size: '',
            edge_count: '',
            trace_node_id: '',
            detachedness: '',
        },
        {
            node_index: '1',
            type: '',
            name: 'name2',
            id: '',
            self_size: '',
            edge_count: '',
            trace_node_id: '',
            detachedness: '',
        },
    ];
    let queryHeapNodeMock = sqlite.queryHeapNode;
    queryHeapNodeMock.mockResolvedValue(HeapNodeData);

    let heapEdgeData = [
        {
            edge_index: '',
            type: '',
            name_or_index: '0',
            to_node: '',
            from_node_id: '',
            to_node_id: '',
        },
        {
            edge_index: '',
            type: '',
            name_or_index: '0',
            to_node: '',
            from_node_id: '',
            to_node_id: '',
        },
    ];
    let queryHeapEdgeMock = sqlite.queryHeapEdge;
    queryHeapEdgeMock.mockResolvedValue(heapEdgeData);

    let heapFunctionData = [
        {
            function_index: '',
            name: '',
            script_name: '0',
            script_id: '',
            function_id: '',
            line: '',
            column: '',
        },
        {
            function_index: '',
            name: '',
            script_name: '0',
            script_id: '',
            function_id: '',
            line: '',
            column: '',
        },
    ];
    let queryHeapFunctionMock = sqlite.queryHeapFunction;
    queryHeapFunctionMock.mockResolvedValue(heapFunctionData);

    let heapTraceNodeData = [
        {
            id: '',
            name: '',
            script_name: '0',
            script_id: '',
            size: '',
            line: '',
            column: '',
            live_count: '',
            live_size: '',
        },
        {
            id: '',
            name: '',
            script_name: '0',
            script_id: '',
            size: '',
            line: '',
            column: '',
            live_count: '',
            live_size: '',
        },
    ];
    let queryHeapTraceNodeMock = sqlite.queryHeapTraceNode;
    queryHeapTraceNodeMock.mockResolvedValue(heapTraceNodeData);

    let heapSampleData = [
        {
            timestamp_us: '',
            last_assigned_id: '',
        },
        {
            timestamp_us: '',
            last_assigned_id: '',
        },
    ];
    let queryHeapSampleMock = sqlite.queryHeapSample;
    queryHeapSampleMock.mockResolvedValue(heapSampleData);

    let heapLocationData = [
        {
            object_index: '',
            script_id: '',
            line: '',
            column: '',
        },
        {
            object_index: '',
            script_id: '',
            line: '',
            column: '',
        },
    ];
    let queryHeapLocationMock = sqlite.queryHeapLocation;
    queryHeapLocationMock.mockResolvedValue(heapLocationData);

    let heapStringData = [
        {
            string: '',
        },
        {
            string: '',
        },
    ];
    let queryHeapStringMock = sqlite.queryHeapString;
    queryHeapStringMock.mockResolvedValue(heapStringData);

    it('LoadDataBase01', async () => {
        let instance = LoadDatabase.getInstance();
        let ss: ParseListener = {
            process: (info: string, process: number) => {},
            parseDone: (fileModule: Array<FileInfo>) => {},
        };
        await instance.loadFile(ss);
    });
});
