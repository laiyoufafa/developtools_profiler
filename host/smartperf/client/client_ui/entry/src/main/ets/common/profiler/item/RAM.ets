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
import { createGPData, extractNumber } from '../base/BaseProfilerUtils';
import { BaseProfiler } from '../base/BaseProfiler'
import { CollectorType } from '../base/ProfilerConstant'
import { SocketProfiler} from '../base/SocketProfiler'

export class RAM extends BaseProfiler implements SocketProfiler{
  private ramMap: Map<String, String> = new Map

  public static instance: RAM = null
  public static getInstance() {
    if (this.instance == null) {
      this.instance = new RAM()
    }
    return this.instance
  }

  init() {
    //初始化RAM节点
    return CollectorType.TYPE_RAM
  }

  isSupport(){
    return true
  }

  readData() {
    if (globalThis.useDaemon) {
      this.readMessageQueue()
    }else{
      let ramStr:string = globalThis.CreateNativeRam(globalThis.collectPkg)
      if (ramStr.length > 0) {
        this.ramMap.set("pss", ramStr)
      }
    }
    return createGPData("RAM", this.ramMap)
  }
  readMessageQueue(){
    if (globalThis.ramArr.length > 0) {
      let ramQueue: Array<String> = globalThis.ramArr
      let curRAM = ramQueue.pop()
      this.ramMap.set("pss", extractNumber(curRAM))
    }
  }
}
