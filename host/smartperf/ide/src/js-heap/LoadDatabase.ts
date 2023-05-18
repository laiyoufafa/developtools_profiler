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
import { HeapDataInterface, ParseListener } from './HeapDataInterface.js';
import { AllocationFunction, FileType } from './model/UiStruct.js';
import { getTimeForLog } from './utils/Utils.js';
import {
  HeapEdge,
  HeapNode,
  HeapTraceFunctionInfo,
  HeapSample,
  HeapLocation,
  FileStruct,
} from './model/DatabaseStruct.js';
import {
  queryHeapFile,
  queryHeapInfo,
  queryHeapNode,
  queryHeapEdge,
  queryHeapFunction,
  queryHeapTraceNode,
  queryHeapSample,
  queryHeapLocation,
  queryHeapString,
} from '../trace/database/SqlLite.js';
import { info } from '../log/Log.js';

export class LoadDatabase {
  private static loadDB: LoadDatabase;
  private fileModule!: Array<FileStruct>;

  static getInstance() {
    if (!this.loadDB) {
      this.loadDB = new LoadDatabase();
    }
    return this.loadDB;
  }

  private async loadFile(listener: ParseListener) {
    this.fileModule = new Array<FileStruct>();
    let result = await queryHeapFile();
    listener.process('start loading file  ', 1);
    for (let row of result) {
      let fileStruct = new FileStruct();
      fileStruct.id = row.id;
      fileStruct.name = row.file_name;
      fileStruct.start_ts = row.start_time;
      fileStruct.end_ts = row.end_time;
      fileStruct.pid = row.pid;
      if (fileStruct.name.startsWith('Snapshot')) {
        fileStruct.type = FileType.SNAPSHOT;
      } else {
        fileStruct.type = FileType.TIMELINE;
      }
      info(`read ${fileStruct.name} from db  ${getTimeForLog()}`);
      //fileStruct.profile.root_index = 0
      await this.loadInfo(fileStruct);
      await this.loadStrings(fileStruct);
      await this.loadNode(fileStruct);
      await this.loadEdge(fileStruct);
      await this.loadTraceFunctionInfos(fileStruct);
      await this.loadTraceTree(fileStruct);
      await this.loadSamples(fileStruct);
      await this.loadLocations(fileStruct);
      let percent = Math.floor(50 / result.length) * (row.id + 1);
      listener.process('loading file ' + fileStruct.name + ' from db ', percent);
      info(`read ${fileStruct.name} from db Success  ${getTimeForLog()}`);
      this.fileModule.push(fileStruct);
    }
    listener.process('Loading completed ', 50);
    let dataParse = HeapDataInterface.getInstance();
    dataParse.setPraseListener(listener);
    dataParse.parseData(this.fileModule);
  }

  private async loadInfo(file: FileStruct) {
    let result = await queryHeapInfo(file.id);
    for (let row of result) {
      if (row.key.includes('types')) continue;
      switch (row.key) {
        case 'node_count':
          file.snapshotStruct.nodeCount = row.int_value;
          break;
        case 'edge_count':
          file.snapshotStruct.edgeCount = row.int_value;
          break;
        case 'trace_function_count':
          file.snapshotStruct.functionCount = row.int_value;
          break;
      }
    }
  }

  private async loadNode(file: FileStruct) {
    let result = await queryHeapNode(file.id);
    let heapNodes = file.snapshotStruct.nodeMap;

    let items = new Array<number>();
    let firstEdgeIndex = 0;
    for (let row of result) {
      let node = new HeapNode(
        file.id,
        row.node_index,
        row.type,
        file.snapshotStruct.strings[row.name],
        row.id,
        row.self_size,
        row.edge_count,
        row.trace_node_id,
        row.detachedness,
        firstEdgeIndex
      );
      if (file.snapshotStruct.rootNodeId === -1) {
        file.snapshotStruct.rootNodeId = row.id;
      }
      heapNodes.set(row.id, node);
      items.push(...[row.type, row.name, row.id, row.self_size, row.edge_count, row.trace_node_id, row.detachedness]);
      firstEdgeIndex += node.edgeCount;
    }
  }

  private async loadEdge(file: FileStruct) {
    let result = await queryHeapEdge(file.id);
    let heapEdges = file.snapshotStruct.edges;

    let items = new Array<number>();
    for (let row of result) {
      let edge = new HeapEdge(
        row.edge_index,
        row.type,
        file.snapshotStruct.strings[row.name_or_index],
        row.to_node,
        row.from_node_id,
        row.to_node_id
      );
      heapEdges.push(edge);
      items.push(...[row.type, row.name_or_index, row.to_node]);
    }
  }

  private async loadTraceFunctionInfos(file: FileStruct) {
    let result = await queryHeapFunction(file.id);
    let heapFunction = file.snapshotStruct.functionInfos;

    let items = new Array<number>();
    for (let row of result) {
      let functionInfo = new HeapTraceFunctionInfo(
        row.function_id,
        row.function_index,
        file.snapshotStruct.strings[row.name],
        file.snapshotStruct.strings[row.script_name],
        row.script_id,
        row.line,
        row.column
      );
      heapFunction.push(functionInfo);

      items.push(...[row.function_id, row.name, row.script_name, row.script_id, row.line, row.column]);
    }
  }

  private async loadTraceTree(file: FileStruct) {
    let result = await queryHeapTraceNode(file.id);
    let heapTraceNode = file.snapshotStruct.traceNodes;
    let strings = file.snapshotStruct.strings;
    for (let row of result) {
      let traceNode = new AllocationFunction(
        row.id,
        strings[row.name],
        strings[row.script_name],
        row.script_id,
        row.line,
        row.column,
        row.count,
        row.size,
        row.live_count,
        row.live_size,
        false
      );
      traceNode.parentsId.push(row.parent_id);
      traceNode.functionIndex = row.function_info_index;
      traceNode.fileId = file.id;
      heapTraceNode.push(traceNode);
    }
  }

  private async loadSamples(file: FileStruct) {
    let result = await queryHeapSample(file.id);
    let samples = file.snapshotStruct.samples;

    for (let row of result) {
      let functionInfo = new HeapSample(row.timestamp_us, row.last_assigned_id);
      samples.push(functionInfo);
    }
  }

  private async loadLocations(file: FileStruct) {
    let result = await queryHeapLocation(file.id);
    let locations = file.snapshotStruct.locations;

    for (let row of result) {
      let location = new HeapLocation(row.object_index / 7, row.script_id, row.line, row.column);
      locations.set(location.objectIndex, location);
    }
  }

  private async loadStrings(file: FileStruct) {
    let result = await queryHeapString(file.id);
    for (let row of result) {
      file.snapshotStruct.strings.push(row.string);
    }
  }

  async loadDatabase(listener: ParseListener) {
    await this.loadFile(listener);
  }
}
