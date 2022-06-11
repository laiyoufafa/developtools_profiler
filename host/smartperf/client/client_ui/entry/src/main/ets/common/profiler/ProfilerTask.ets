
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
import deviceInfo from '@ohos.deviceInfo'
import CommonEvent from '@ohos.commonEvent'
import database from '../database/DatabaseUtils'
import { CPU } from './item/CPU';
import { csvGeneralInfo, csvTIndexInfo } from '../utils/CSVUtils'
import { createFilePath } from '../utils/IOUtils'
import { GPData, TIndexInfo, TGeneralInfo } from '../entity/DatabaseEntity'
import { ProfilerFactory } from './base/ProfilerFactory'
import SPLogger from '../../common/utils/SPLogger'

export class ProfilerTask {
  private collectItems:Array<string> = []
  private static instance: ProfilerTask

  public static getInstance(): ProfilerTask {
    if (this.instance == null) {
      this.instance = new ProfilerTask
    }
    return ProfilerTask.instance
  }

  initModule() {
    SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask initModule configs: ' + JSON.stringify(globalThis.collectConfigs))
    const keys: any[] = Object.keys(globalThis.collectConfigs)
    for (var key of keys) {
      if (globalThis.collectConfigs[key]) {
        let typeCollect = ProfilerFactory.getProfilerByConfig(key.toString())
        SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask initModule: ' + typeCollect)
        if (typeCollect == null) {
          continue
        }else{
          this.collectItems.push(typeCollect.init())
        }
      }
    }
    //默认不使用daemon
//    globalThis.useDaemon = false
//    let status = globalThis.CheckDaemon()
//    if (status == "Running") {
//      globalThis.useDaemon = true
//    }
//    SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask initModule:Daemon status ' + status)
  }

  getSupports(keys:string[]){
    let supports : Map<string,Boolean> = new Map<string,Boolean>()
    for (var key of keys) {
      let typeCollect = ProfilerFactory.getProfilerByConfig(key)
      let isSupport = typeCollect.isSupport()
      supports.set(key.toString(),isSupport)
    }
    return supports
  }

  taskInit() {
    SPLogger.DEBUG(ProfilerTask.name,'ProfilerUtils taskInit call');
    var now = new Date()
    //数据库时间戳标记
    globalThis.dbTime = now.getTime()
    //开始时间
    globalThis.startTime = now.getTime()
    //结束时间
    globalThis.endTime = -1
    //时间计数器
    globalThis.collectIntervalNum = -1
    //入库数据
    globalThis.tTndexs = new Array<TIndexInfo>()
    //实时数据
    globalThis.tTndex = new TIndexInfo()
    // ram队列
    globalThis.ramArr = new Array<String>()
    // fps队列
    globalThis.fpsArr = new Array<String>()
    // fpsJitter队列
    globalThis.fpsJitterArr = new Array<String>()

    //初始化数据库  2022-02-23 dbName改为时间戳
    database.createTable(globalThis.dbTime)
    SPLogger.DEBUG(ProfilerTask.name,'ProfilerUtils taskInit called');
  }

  taskStart() {
    SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask taskStart call');
    var gpDataArr: Array<GPData> = new Array<GPData>()
    this.collectItems.forEach(
      (moduleName)=>{
        let typeCollect = ProfilerFactory.getProfilerByConfig(moduleName.toString())
        if (typeCollect != null) {
          let gpData:GPData =typeCollect.readData()
          if(typeCollect instanceof CPU){
            typeCollect.readCPULoad()
          }
          gpDataArr.push(gpData)
          SPLogger.DEBUG(ProfilerTask.name,"profiler ProfilerTask:curData:"+gpData);
        }
      }
    )

    //防止cpuLoad -1
    if (globalThis.collectIntervalNum > 0) {
      let tTndex = database.gpArray2Index(gpDataArr)
      globalThis.tTndexs.push(tTndex)
      globalThis.tTndex = tTndex;
      CommonEvent.publish("event", { code: 0, data: JSON.stringify(tTndex), }, (err)=>{});
    }
    globalThis.collectIntervalNum++
    SPLogger.DEBUG(ProfilerTask.name,"profiler globalThis.collectIntervalNum " + globalThis.collectIntervalNum)

    SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask taskStart called');
  }

  taskStop() {
    SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask taskStop call');
    // t_index_info  入库
    if (globalThis.tTndexs.length > 2) {
      for (var index = 2; index < globalThis.tTndexs.length; index++) {
        const tTndex = globalThis.tTndexs[index];
        database.insertData("t_index_info", globalThis.dbTime, tTndex)
      }
    }
    createFilePath(globalThis.abilityContext.getApplicationContext().filesDir + "/" + globalThis.dbTime + "/t_index_info.csv", csvTIndexInfo(globalThis.tTndexs))
    // t_general_info  入库
    globalThis.endTime = new Date().getTime()
    let tGeneralInfo = new TGeneralInfo(
      globalThis.dbTime.toString(),
      "NA",
      globalThis.appName,
      globalThis.appVersion,
      globalThis.packageName,
      globalThis.startTime,
      globalThis.endTime,
      globalThis.collectIntervalNum,
      globalThis.testTaskName
    )
    tGeneralInfo.board = deviceInfo.brand
    tGeneralInfo.deviceTypeName = deviceInfo.deviceType
    tGeneralInfo.brand = deviceInfo.brand
    tGeneralInfo.version = deviceInfo.displayVersion
    tGeneralInfo.sn = deviceInfo.serial
    database.insertGeneraData("t_general_info", tGeneralInfo)
    createFilePath(globalThis.abilityContext.getApplicationContext().filesDir + "/" + globalThis.dbTime + "/t_general_info.csv", csvGeneralInfo(tGeneralInfo))

    SPLogger.DEBUG(ProfilerTask.name,'ProfilerTask taskStop called');
  }

  taskDestroy() {
    //数据库时间戳标记
    globalThis.dbTime = -1
    //开始时间
    globalThis.startTime = -1
    //结束时间
    globalThis.endTime = -1
    //入库数据
    globalThis.tTndexs = new Array<TIndexInfo>()

    //采集配置恢复fileOpen
    globalThis.collectConfigs = -1
    globalThis.collectPkg = -1

    //采集定时器 配置
    globalThis.collectInterval = -1
    globalThis.collectIntervalCollect = -1
    globalThis.collectIntervalNum = -1

    //socket采集队列 fps ram
    globalThis.fpsArr = new Array<String>()
    globalThis.fpsJitterArr = new Array<String>()
    globalThis.ramArr = new Array<String>()
  }
}















