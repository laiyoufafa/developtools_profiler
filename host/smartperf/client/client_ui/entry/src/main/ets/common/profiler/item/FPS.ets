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
import { SocketProfiler } from '../base/SocketProfiler'
import SPLogger from '../../../common/utils/SPLogger'
export class FPS extends BaseProfiler implements SocketProfiler {
  private fpsMap: Map<String, String> = new Map

  public static instance: FPS = null
  public static getInstance() {
    if (this.instance == null) {
      this.instance = new FPS()
    }
    return this.instance
  }

  init() {
    //初始化FPS
    return CollectorType.TYPE_FPS
  }

  readData() {
    this.readMessageQueue()
    return createGPData("FPS", this.fpsMap)
  }

  readMessageQueue() {
    SPLogger.DEBUG(FPS.name,"messageQueue for fps" + globalThis.fpsArr.length)
    if (globalThis.fpsArr.length > 0) {
      let fpsQueue: Array<String> = globalThis.fpsArr
      let fpsJitterQueue: Array<String> = globalThis.fpsJitterArr
      let curFPS = fpsQueue.pop()
      let curFPSJitter = fpsJitterQueue.pop()
      let fpsJitters = "\"" + curFPSJitter.split("==").join(",") + "\""
      this.fpsMap.set("fpsJitters", fpsJitters)
      this.fpsMap.set("fps", extractNumber(curFPS))
    }
  }
}
