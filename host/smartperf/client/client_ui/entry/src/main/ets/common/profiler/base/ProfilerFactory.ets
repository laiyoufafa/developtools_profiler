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
import { CPU } from '../item/CPU'
import { GPU } from '../item/GPU'
import { FPS } from '../item/FPS'
import { Power } from '../item/Power'
import { RAM } from '../item/RAM'
import { Thermal } from '../item/Thermal'
import { DDR } from '../item/DDR'
import { CollectorType } from './ProfilerConstant'
import { BaseProfiler } from './BaseProfiler'
import SPLogger from '../../../common/utils/SPLogger'

export class ProfilerFactory {
  static getProfilerByConfig(moduleName: string): BaseProfiler{
    if (moduleName == CollectorType.TYPE_FPS && globalThis.collectConfigs.FPS) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_FPS:" + globalThis.collectConfigs.FPS);
      return FPS.getInstance()
    } else if (moduleName == CollectorType.TYPE_CPU && globalThis.collectConfigs.CPU) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_CPU:" + globalThis.collectConfigs.CPU);
      return CPU.getInstance()
    } else if (moduleName == CollectorType.TYPE_GPU && globalThis.collectConfigs.GPU) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_GPU:" + globalThis.collectConfigs.GPU);
      return GPU.getInstance()
    } else if (moduleName == CollectorType.TYPE_POWER && globalThis.collectConfigs.POWER) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_POWER:" + globalThis.collectConfigs.POWER);
      return Power.getInstance()
    } else if (moduleName == CollectorType.TYPE_RAM && globalThis.collectConfigs.RAM) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_RAM:" + globalThis.collectConfigs.RAM);
      return RAM.getInstance()
    } else if (moduleName == CollectorType.TYPE_TEMPERATURE && globalThis.collectConfigs.TEMP) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_TEMPERATURE:" + globalThis.collectConfigs.TEMP);
      return Thermal.getInstance()
    } else if (moduleName == CollectorType.TYPE_DDR && globalThis.collectConfigs.DDR) {
      SPLogger.DEBUG(ProfilerFactory.name,"getProfilerByConfig:TYPE_DDR:" + globalThis.collectConfigs.DDR);
      return DDR.getInstance()
    }
    return null
  }
}