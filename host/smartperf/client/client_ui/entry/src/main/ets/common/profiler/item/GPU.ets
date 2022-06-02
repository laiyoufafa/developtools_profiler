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
import { fileOpen, createGPData, isAccess} from '../base/BaseProfilerUtils';
import { BaseProfiler } from '../base/BaseProfiler'
import { CollectorType } from '../base/ProfilerConstant'

enum GPU_CONFIG {
  GPU_FREQ_PATH = "/sys/class/devfreq/fde60000.gpu/cur_freq", //RK
  GPU_LOAD_PATH = "/sys/class/devfreq/fde60000.gpu/load",   //RK
}

export class GPU extends BaseProfiler {
  private gpuMap: Map<String, String> = new Map

  public static instance: GPU = null
  public static getInstance() {
    if (this.instance == null) {
      this.instance = new GPU()
    }
    return this.instance
  }

  init() {
    //初始化GPU
    return CollectorType.TYPE_GPU
  }
  isSupport(){
    if(isAccess(GPU_CONFIG.GPU_FREQ_PATH)&&isAccess(GPU_CONFIG.GPU_LOAD_PATH)){
      return true
    }
    return false
  }
  readData() {
    console.log("GPU:BaseProfiler called")
    let gpuFreq = fileOpen(GPU_CONFIG.GPU_FREQ_PATH)
    let gpuLoad = fileOpen(GPU_CONFIG.GPU_LOAD_PATH)
    console.log("GPU:BaseProfiler called1111")
    let loadStr: string[] = gpuLoad.split("@")
    let load = "-1"
    if (loadStr.length > 0) {
      load = loadStr[0].toString()
    }else{
      load = gpuLoad.toString()
    }
    this.gpuMap.set("gpuFreq", gpuFreq)
    this.gpuMap.set("gpuLoad", load)
    console.log("GPU:BaseProfiler called2222")
    return createGPData("GPU", this.gpuMap)
  }
}
