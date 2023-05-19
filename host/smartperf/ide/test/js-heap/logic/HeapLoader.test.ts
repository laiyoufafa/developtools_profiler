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
import { HeapLoader } from '../../../dist/js-heap/logic/HeapLoader.js';
//@ts-ignore
import { ConstructorItem, FileType } from '../../../dist/js-heap/model/UiStruct.js';
import { HeapNode } from '../../../src/js-heap/model/DatabaseStruct';

jest.mock('../../../dist/js-heap/utils/Utils.js', () => {
    return {
        HeapNodeToConstructorItem: (node: HeapNode) => {
            return {};
        },
    };
});

describe('HeapLoader Test', () => {
    let rootNode = {
        detachedness: 0,
        displayName: '',
        distance: 100000000,
        edgeCount: 3575,
        fileId: 0,
        firstEdgeIndex: 0,
        flag: 0,
        id: 1,
        name: 'Test',
        nodeIndex: 0,
        nodeOldIndex: 0,
        retainedSize: 1898167,
        retainsCount: 0,
        retainsEdgeIdx: [0],
        retainsNodeIdx: [0],
        selfSize: 0,
        traceNodeId: 0,
        type: 9,
        edges: [
            {
                edgeIndex: 0,
                edgeOldIndex: 0,
                fromNodeId: 1,
                nameOrIndex: '-test-',
                nodeId: 152376,
                retainEdge: [],
                retainsNode: [],
                toNodeId: 43537,
                type: 5,
            },
            {
                edgeIndex: 1,
                edgeOldIndex: 3,
                fromNodeId: 1,
                nameOrIndex: '-test-',
                nodeId: 155414,
                retainEdge: [],
                retainsNode: [],
                toNodeId: 44405,
                type: 5,
            },
        ],
    };
    let data = {
        end_ts: 88473497504466,
        id: 0,
        isParseSuccess: true,
        name: 'Test',
        path: '',
        pid: 4243,
        tart_ts: 88473061693464,
        type: 0,
        heapLoader: {
            rootNode: rootNode,
        },
        snapshotStruct: {
            traceNodes: [],
            nodeMap: new Map(),
            nodeCount: 1,
            edges: [
                {
                    edgeIndex: 0,
                    edgeOldIndex: 0,
                    fromNodeId: 1,
                    nameOrIndex: '-test-',
                    nodeId: 152376,
                    retainEdge: [],
                    retainsNode: [],
                    toNodeId: 43537,
                    type: 5,
                },
                {
                    edgeIndex: 1,
                    edgeOldIndex: 3,
                    fromNodeId: 1,
                    nameOrIndex: '-test-',
                    nodeId: 155414,
                    retainEdge: [],
                    retainsNode: [],
                    toNodeId: 44405,
                    type: 5,
                },
            ],
            samples: [],
        },
    };

    let item = {
        addedCount: 648,
        addedIndx: [319, 326],
        addedSize: 38936,
        childCount: 1296,
        children: [],
        classChildren: [],
        deletedIdx: [325, 338],
        deltaCount: 0,
        deltaSize: -16,
        distance: -1,
        edgeCount: 0,
        edgeName: '',
        fileId: 0,
        hasNext: true,
        id: -1,
        index: 0,
        isAdd: false,
        isHover: false,
        isSelected: false,
        nextId: [],
        nodeName: 'nodeName',
        objectName: 'objectName',
        removedCount: 648,
        removedSize: 38952,
        retainedSize: -1,
        retains: [],
        shallowSize: -1,
        showBox: false,
        showCut: false,
        status: true,
        targetFileId: 1,
        traceNodeId: -1,
        type: 4,
    };

    it('HeapLoaderTest01', () => {
        let heapLoader = new HeapLoader(data);
        heapLoader.fileId = jest.fn(() => true);
        expect(heapLoader).not.toBeUndefined();
    });
    it('HeapLoaderTest02', () => {
        let heapLoader = new HeapLoader(data);
        expect(heapLoader.loadAllocationParent({})).toBeUndefined();
    });

    it('HeapLoaderTest03', () => {
        let heapLoader = new HeapLoader(data);
        heapLoader.rootNode = rootNode;
        heapLoader.nodes = [rootNode];
        heapLoader.nodes[0].addEdge = jest.fn(() => true);
        heapLoader.isEssentialEdge = jest.fn(() => false);
        expect(heapLoader.preprocess()).toBeUndefined();
    });

    it('HeapLoaderTest04', () => {
        let heapLoader = new HeapLoader(data);
        heapLoader.nodes = [rootNode];
        expect(heapLoader.getClassesForSummary().keys.length).toEqual(0);
    });

    it('HeapLoaderTest05', () => {
        let heapLoader = new HeapLoader(data);
        heapLoader.nodes = [rootNode];
        heapLoader.rootNode = rootNode;
        expect(heapLoader.getRetains(item).length).toEqual(1);
    });
});
