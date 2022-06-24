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
import { csvGeneralInfo, csvTIndexInfo } from './utils/CSVUtils';
import { createFilePath } from './utils/IOUtils';
import { GPData, TIndexInfo, TGenneralInfo } from './data/DatabaseEntity';
import { AppFileDir } from '../common/storage/ConstantsPath';
import fileio from '@ohos.fileio';
import database from '../common/storage/DatabaseUtils';
import deviceInfo from '@ohos.deviceInfo';
import util from '@ohos.util';


const TAG = "service.ts "
const dbName = "gp.db"
const tableIndex = "t_index_info"
const tableGeneral = "t_general_info"
const saveInterval = 60

/**
 * 构造GPData
 */
export function createGPData(moduleType: string, values: Map<String, String>): GPData {
  var now = new Date();
  return new GPData(moduleType, now.getTime(), values)
}

/**
 * 通用文件节点打开
 * @param path
 */
export function fileOpen(path: string): string {
  try {
    var fd = -1
    fd = fileio.openSync(path, 0o0)
    let buf = new ArrayBuffer(1024);
    fileio.readSync(fd, buf);
    var result: String = String.fromCharCode.apply(null, new Uint8Array(buf))
    console.log("fileOpen path data:" + result);
    return util.printf("%s", result.substring(0, lastIndex(result))).toString()
  } catch (err) {
    console.log("fileOpen path:" + path + " error:" + err);
  } finally {
    fileio.closeSync(fd)
  }
  return "null"
}

/**
 * 返回字符结尾符坐标
 * @param str
 */
function lastIndex(str) {
  var index = -1;
  for (var i = 0; i < str.length; i++) {
    var temp = str.charCodeAt(i).toString(16);
    if (temp == 'a') {
      return i;
    }
  }
  return index;
}


/**
 * 睡眠函数
 * @param numberMillis
 */
export function sleep(numberMillis) {
  var now = new Date();
  var exitTime = now.getTime() + numberMillis;
  while (true) {
    now = new Date();
    if (now.getTime() > exitTime)
    return;
  }
}
/**
 * 读取ddr频率
 */
export function readDDRFreq(): GPData {

  const path = "/sys/class/devfreq/ddrfreq/cur_freq"
  var ddrfreq = fileOpen(path)
  console.log("profile_readDDRFreq freq:" + ddrfreq)
  let map = new Map
  map.set("ddrFreq", ddrfreq)
  return createGPData("DDR", map)
}
/**
 * 读取cpu频率
 */
export function readCPUFreq(): GPData {
  let map = new Map
  let cpuLength = 8
  for (var i = 0;i < cpuLength; i++) {
    const path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq"
    var cpu = fileOpen(path)
    map.set("cpu" + i + "Freq", cpu)
    console.log("profile_readCpu cpu" + i + "freq:" + cpu);
  }
  return createGPData("CPU", map)
}
/**
 * 读取cpu整机负载
 */

let prebufferArr = ["", "", "", "", "", "", "", "", ""]
export function readCPULoad(): GPData {
  console.log("readCPULoad called");
  const path = "/proc/stat"
  var workLoadArr = []

  try {
    var fd = -1
    fd = fileio.openSync(path, 0o0)
    let buf = new ArrayBuffer(2048);
    fileio.readSync(fd, buf);
    let cpuStr: String = String.fromCharCode.apply(null, new Uint8Array(buf))
    let cpuStrArr = []
    cpuStr = cpuStr.substring(0, cpuStr.indexOf("intr"))
    let nextj = 0;
    let j
    for (var i = 1;i < cpuStr.length; i++) {
      if (cpuStr.charAt(i) == 'c') {
        j = nextj
        nextj = i
        cpuStrArr.push(cpuStr.substring(j, nextj))
      }
    }
    cpuStrArr.push(cpuStr.substring(nextj, nextj + 50))

    console.log("cpuStrArrLength:" + cpuStrArr.length);
    let buffer = ""
    for (let index = 1;index < cpuStrArr.length; index++) {
      console.log("cpuStrArr:" + cpuStrArr[index]);
      buffer = cpuStrArr[index]
      let load = calCPULoad(buffer, prebufferArr[index])
      workLoadArr.push(load)
      console.log("cpuStr load:" + load);
      prebufferArr[index] = buffer
    }
  } catch (err) {
    console.log("readCPULoad path:" + path + " error:" + err);
  } finally {
    fileio.closeSync(fd)
  }

  let map = new Map
  for (let index = 0; index < workLoadArr.length; index++) {
    const element = workLoadArr[index];
    map.set("cpu" + index + "Load", element)
  }
  return createGPData("CPULoad", map)
}
/**
 * calculate cpu load
 * @param buffer
 * @param pre_buffer
 */
export function calCPULoad(buffer: string, pre_buffer: string): number {

  if (pre_buffer.length == 0) {
    return -1
  }
  let timeArr: string[] = buffer.split(" ");
  let pre_timeArr: string[] = pre_buffer.split(" ");

  timeArr.reverse().pop()
  pre_timeArr.reverse().pop()
  timeArr.reverse()
  pre_timeArr.reverse()

  console.log("cpuStr profile_readCpuLoad cur_time" + JSON.stringify(timeArr));
  console.log("cpuStr profile_readCpuLoad pre_time" + JSON.stringify(pre_timeArr));
  let time = ArrStr2Number(timeArr)
  let pre_time = ArrStr2Number(pre_timeArr)

  let user = time[0] + time[1] - pre_time[0] - pre_time[1];
  let sys = time[2] - pre_time[2];
  let idle = time[3] - pre_time[3];
  let iowait = time[4] - pre_time[4];
  let irq = time[5] + time[6] - pre_time[5] - pre_time[6];
  let total = user + sys + idle + iowait + irq;
  if (user < 0 || sys < 0 || idle < 0 || iowait < 0 || irq < 0) {
    return 0;
  }
  let per_user = user * 100.0 / total;
  console.log("calc user * 100.0 / total" + user + "/" + total + "=" + per_user);
  let per_sys = sys * 100.0 / total;
  console.log("calc sys * 100.0 / total" + +sys + "/" + total + "=" + per_sys);
  let per_iowait = iowait * 100.0 / total;
  console.log("calc iowait * 100.0 / total" + +iowait + "/" + total + "=" + per_iowait);
  let per_irq = irq * 100.0 / total;
  console.log("calc irq * 100.0 / total;" + +irq + "/" + total + "=" + per_irq);

  let workload = per_user + per_sys + per_iowait + per_irq;
  return Number(workload.toFixed(2)).valueOf()
}

export function ArrStr2Number(arr: Array<String>): Array<number> {
  let result = new Array<number>()
  for (var index = 0; index < arr.length; index++) {
    const element = arr[index].replace("\n", "");
    result.push(new Number(element).valueOf())
  }
  return result
}


/**
 * read gpu
 */
export function readGPU(): GPData {
  let map = new Map

  const path = "/sys/class/devfreq/gpufreq/cur_freq"
  var gpufreq = fileOpen(path)
  map.set("gpuFreq", gpufreq)
  console.log("profile_readGPUFreq freq:" + gpufreq);

  const path1 = "/sys/class/devfreq/gpufreq/gpu_scene_aware/utilisation"
  var gpuload = fileOpen(path1)
  map.set("gpuLoad", gpuload)

  console.log("profile_readGPULoad Load:" + gpuload);
  return createGPData("GPU", map)
}


/**
 * read power [current_now, voltage_now, charge_now, temp, status]
 */
export function readPower(): GPData {
  let map = new Map
  var index = ["current_now", "voltage_now", "charge_now", "temp", "status"]
  let pathPower = "/sys/class/power_supply/Battery/"
  for (var i in index) {
      var value = fileOpen(pathPower + index[i])
      map.set(index[i], value)
      console.log("profile_readPower::" + index[i] + " " + value)
  }
  return createGPData("Power", map)
}

/**
 * read temperature [shell_frame,shell_front,shell_back]
 */
export function readTemp(): GPData {
  let map = new Map
  let zoneMap = new Map
  var defaultArr = [
    "shell_back",
    "shell_frame",
    "shell_front",
    "soc_thermal",
    "system_h",
    "gpu",
    "ambient",
    "Battery"
  ]
  for (var i = 0; i < 50; i++) {
    const pathtemp = "/sys/devices/virtual/thermal/thermal_zone" + i + "/temp"
    try {
      fileio.accessSync(pathtemp);
      var temp = fileOpen(pathtemp)
      const pathtype = "/sys/devices/virtual/thermal/thermal_zone" + i + "/type"
      var tptype = fileOpen(pathtype)
      zoneMap.set(tptype, temp)
    } catch (e) {
      break
    }
  }
  for (var k = 0; k < defaultArr.length; k++) {
    map.set(defaultArr[k], zoneMap.get(defaultArr[k]))
  }
  return createGPData("Temp", map)
}

/**
 * read ram[pss]
 */

export function readRam(): GPData {
  let map = new Map
  let pss = ""
  console.info("  messageQueue for ram " + globalThis.ramArr.length)
  if (globalThis.ramArr.length > 0) {
    let ramQueue: Array<String> = globalThis.ramArr
    let curRAM = ramQueue.pop()
    for (var index = 0; index < curRAM.length; index++) {
      const element: string = curRAM[index];
      if (element.match("^[0-9]*$")) {
        pss += element
      }
    }
    map.set("pss", pss)
  }
  return createGPData("RAM", map)
}

/**
 * read fps[fps]
 */
export function readFPS(): GPData {
  let map = new Map
  let fps = ""
  console.info("  messageQueue for fps " + globalThis.fpsArr.length)
  if (globalThis.fpsArr.length > 0) {
    let fpsQueue: Array<String> = globalThis.fpsArr
    let fpsJitterQueue: Array<String> = globalThis.fpsJitterArr
    let curFPS = fpsQueue.pop()
    let curFPSJitter = fpsJitterQueue.pop()

    for (var index = 0; index < curFPS.length; index++) {
      const element: string = curFPS[index];
      if (element.match("^[0-9]*$")) {
        fps += element
      }
    }
    let fpsJitters = "\"" + curFPSJitter.split("==").join(",") + "\""
    map.set("fpsJitters", fpsJitters)
    map.set("fps", fps)
  }
  return createGPData("FPS", map)
}


//采集初始化
export const task_init = 1
//采集运行中
export const task_running = 2
//采集暂停
export const task_pause = 3
//采集结束
export const task_stop = 4

export function taskInit() {
  console.info(TAG + 'ServiceAbility taskInit call');
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
  console.info(TAG + 'ServiceAbility taskInit called');
}

export function taskStart(configItems, curSelectPkg) {

  console.info(TAG + 'ServiceAbility taskStart call');

  var gpDataArr: Array<GPData> = new Array<GPData>()
  console.info(TAG + 'profiler ServiceAbility taskRunning..');
  if (configItems.CPU) {

    gpDataArr.push(readCPUFreq())

    gpDataArr.push(readCPULoad())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..1');
  if (configItems.DDR) {
    gpDataArr.push(readDDRFreq())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..2');
  if (configItems.POWER) {
    gpDataArr.push(readPower())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..3');
  if (configItems.TEMP) {
    gpDataArr.push(readTemp())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..4');
  if (configItems.GPU) {
    gpDataArr.push(readGPU())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..5');
  if (configItems.RAM) {
    gpDataArr.push(readRam())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..6');
  if (configItems.FPS) {
    gpDataArr.push(readFPS())
  }
  console.info(TAG + 'profiler ServiceAbility taskRunning..7');


  for (var j = 0;j < gpDataArr.length; j++) {
    var curGPData: GPData = gpDataArr[j]
    console.info('profiler gpDataArrItem:' + curGPData.toString())
  }


  //防止cpuLoad -1
  if (globalThis.collectIntervalNum > 0) {
    let tTndex = database.gpArray2Index(gpDataArr)
    globalThis.tTndexs.push(tTndex)
    globalThis.tTndex = tTndex;

  }
  globalThis.collectIntervalNum++
  if (globalThis.collectIntervalNum != 0 && globalThis.collectIntervalNum % saveInterval == 0) {
    console.info("zhaohui" + "...................saveData  start")
    let data = new Array<TIndexInfo>()
    let result = data.concat(globalThis.tTndexs)
    globalThis.tTndexs.length = 0;
    taskSave(result)
  }
  console.info("profiler globalThis.collectIntervalNum " + globalThis.collectIntervalNum)

  console.info(TAG + 'ServiceAbility taskStart called');
}

export function taskPause() {
  //todo
  console.info(TAG + 'ServiceAbility taskPause called');
}

export function taskSave(data: Array<TIndexInfo>) {
  console.info(TAG + 'ServiceAbility taskStop call');
  // t_index_info  入库
  if (data.length > 0) {
    if (globalThis.collectIntervalNum == saveInterval && data.length > 2) {
      for (var index = 2; index < data.length; index++) {
        const tTndex = data[index];
        database.insertData(tableIndex, globalThis.dbTime, tTndex)
      }
    } else {
      for (var index = 0; index < data.length; index++) {
        const tTndex = data[index];
        database.insertData(tableIndex, globalThis.dbTime, tTndex)
      }
    }
  }
  console.info(TAG + 'ServiceAbility taskStop call1');
  createFilePath(globalThis.abilityContext.getApplicationContext().filesDir + "/" + globalThis.dbTime + "/t_index_info.csv", csvTIndexInfo(data))
  console.info(TAG + 'ServiceAbility taskStop call2');
  // t_general_info  入库
  globalThis.endTime = new Date().getTime()
  let tGenneralInfo = new TGenneralInfo(
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
  tGenneralInfo.board = deviceInfo.brand
  tGenneralInfo.deviceTypeName = deviceInfo.deviceType
  tGenneralInfo.brand = deviceInfo.brand
  tGenneralInfo.version = deviceInfo.displayVersion
  tGenneralInfo.sn = deviceInfo.serial
  database.insertGeneraData(tableGeneral, tGenneralInfo, globalThis.dbTime.toString())
  console.info(TAG + 'ServiceAbility taskStop call3');
  createFilePath(globalThis.abilityContext.getApplicationContext().filesDir + "/" + globalThis.dbTime + "/t_general_info.csv", csvGeneralInfo(tGenneralInfo))
  console.info(TAG + 'ServiceAbility taskStop call4');

  console.info(TAG + 'ServiceAbility taskStop called');
}

export function taskDestroy() {
  //数据库时间戳标记
  globalThis.dbTime = -1
  //开始时间
  globalThis.startTime = -1
  //结束时间
  globalThis.endTime = -1
  //时长
  //  globalThis.duration=10
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














