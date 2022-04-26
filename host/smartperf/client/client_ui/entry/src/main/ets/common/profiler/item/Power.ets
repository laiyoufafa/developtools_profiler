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
import fileio from '@ohos.fileio';
import { fileOpen,travelFile,createGPData } from '../base/BaseProfilerUtils';
import { BaseProfiler } from '../base/BaseProfiler'
import { CollectorType } from '../base/ProfilerConstant'
import SPLogger from '../../../common/utils/SPLogger'
enum POWER_CONFIG {
  //此目录 fileio 无法读取 需自行适配
//  POWER_PATH = "/data/local/tmp/battery"
    POWER_PATH = "/sys/class/power_supply/Battery" // wgr
}

export class Power extends BaseProfiler {
  private powerMap: Map<String, String> = new Map
  private enableSupportItem = ["current_now", "voltage_now", "charge_now", "temp", "status"]
  private supportItemKey = []

  public static instance: Power = null
  public static getInstance() {
    if (this.instance == null) {
      this.instance = new Power()
    }
    return this.instance
  }

  init() {
    //初始化Power节点
    let pathNodes = travelFile(POWER_CONFIG.POWER_PATH,"")
    pathNodes.forEach(path=>{
      this.enableSupportItem.forEach(item=>{
        if(path == item ){
          this.supportItemKey.push(item)
        }
      })
    })
    SPLogger.DEBUG(Power.name,"pathNodes init length:"+ pathNodes.length + "support items:" + JSON.stringify(this.supportItemKey))
    return CollectorType.TYPE_POWER
  }

  readData() {
    if (this.supportItemKey.length > 0) {
      for (let powerKey of this.supportItemKey) {
        let powerValue = fileOpen(POWER_CONFIG.POWER_PATH + "/" + powerKey)
        this.powerMap.set(powerKey, powerValue)
      }
    }
    return createGPData("Power", this.powerMap)
  }
}
