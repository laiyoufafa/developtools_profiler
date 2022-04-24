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

import dataRdb from '@ohos.data.rdb'
import { GPData,TIndexInfo,TGeneralInfo } from '../entity/DatabaseEntity';
import { sql_t_index_info, dbPath, dbVersion, dbName } from '../constant/ConstantSQL'
import SPLogger from '../utils/SPLogger'

const TAG = "DatabaseUtils"

export default {

    //创建表(T_INDEX_INFO T_GENERAL_INFO)
    async createTable(pathSuffix: number): Promise<void> {
        const STORE_CONFIG_Index = {
            name: pathSuffix + ".db"
        }
        dataRdb.getRdbStore(globalThis.abilityContext, STORE_CONFIG_Index, dbVersion)
            .then(rdbStore => {
                rdbStore.executeSql(sql_t_index_info, null).catch(err => {
                    SPLogger.DEBUG(TAG,"--> createTable t_index_info err:" + err)
                })
                SPLogger.DEBUG(TAG,"--> createTable start execute sql_t_index_info:" + sql_t_index_info)
                return rdbStore
            })
    },

    //插入表( T_GENERAL_INFO)
    insertGeneraData(tableName: string, tGeneralInfo: TGeneralInfo) {
        var strMap = new Map;
        for (let k of Object.keys(tGeneralInfo)) {
            strMap.set(k, tGeneralInfo[k])
        }
        const valueInsert = {
            "sessionId": strMap.get("sessionId"),
            "taskId": strMap.get("taskId"),
            "appName": strMap.get("appName"),
            "appVersion": strMap.get("appVersion"),
            "packageName": strMap.get("packageName"),
            "startTime": strMap.get("startTime"),
            "endTime": strMap.get("endTime"),
            "testDuration": strMap.get("testDuration"),
            "taskName": strMap.get("taskName")
            //      "testCase":strMap.get("testCase"),
            //      "testType":strMap.get("testType"),
            //      "user":strMap.get("user"),
            //      "userId":strMap.get("userId"),
            //      "projectId":strMap.get("projectId"),
            //      "dataSource":strMap.get("dataSource"),
            //      "spVersion":strMap.get("spVersion"),
            //      "deviceTypeName":strMap.get("deviceTypeName"),
            //      "brand":strMap.get("brand"),
            //      "deviceName":strMap.get("deviceName"),
            //      "board":strMap.get("board"),
            //      "version":strMap.get("version"),
            //      "plat":strMap.get("plat"),
            //      "cpuCluster":strMap.get("cpuCluster"),
            //      "sn":strMap.get("sn"),
            //      "resolution":strMap.get("resolution"),
            //      "screenBrightness":strMap.get("screenBrightness"),
            //      "volume":strMap.get("volume"),
            //      "batteryVolume":strMap.get("batteryVolume")
        }
        const STORE_CONFIG = {
            name: dbName
        }
        dataRdb.getRdbStore(globalThis.abilityContext, STORE_CONFIG, dbVersion, (err, rdbStore) => {
            SPLogger.DEBUG(TAG,"--> insert into  insertGeneraData :tableName:" + tableName + "| valueInsert:" + JSON.stringify(valueInsert))
            rdbStore.insert(tableName, valueInsert).catch(err => {
                SPLogger.DEBUG(TAG,"--> insert into  insertGeneraData err:" + err)
            }).then(() => {
                SPLogger.DEBUG(TAG,"--> insert into  insertGeneraData success:")
            })
        })
    },

    //插入表(T_INDEX_INFO)
    insertData(tableName: string, pathSuffix: number, tIndexInfo: TIndexInfo) {

        var strMap = new Map;
        for (let k of Object.keys(tIndexInfo)) {
            strMap.set(k, tIndexInfo[k]);
        }
        const valueInsert = {
            "timestamp": strMap.get("timestamp"),
            "taskId": strMap.get("taskId"),
            "ddrFrequency": strMap.get("ddrFrequency"),
            "cpu0Frequency": strMap.get("cpu0Frequency"),
            "cpu1Frequency": strMap.get("cpu1Frequency"),
            "cpu2Frequency": strMap.get("cpu2Frequency"),
            "cpu3Frequency": strMap.get("cpu3Frequency"),
            "cpu4Frequency": strMap.get("cpu4Frequency"),
            "cpu5Frequency": strMap.get("cpu5Frequency"),
            "cpu6Frequency": strMap.get("cpu6Frequency"),
            "cpu7Frequency": strMap.get("cpu7Frequency"),
            "cpu0Load": strMap.get("cpu0Load"),
            "cpu1Load": strMap.get("cpu1Load"),
            "cpu2Load": strMap.get("cpu2Load"),
            "cpu3Load": strMap.get("cpu3Load"),
            "cpu4Load": strMap.get("cpu4Load"),
            "cpu5Load": strMap.get("cpu5Load"),
            "cpu6Load": strMap.get("cpu6Load"),
            "cpu7Load": strMap.get("cpu7Load"),
            "gpuFrequency": strMap.get("gpuFrequency"),
            "gpuLoad": strMap.get("gpuLoad"),
            "currentNow": strMap.get("currentNow"),
            "voltageNow": strMap.get("voltageNow"),
            "shellFrameTemp": strMap.get("shellFrameTemp"),
            "shellFrontTemp": strMap.get("shellFrontTemp"),
            "shellBackTemp": strMap.get("shellBackTemp"),
            "socThermalTemp": strMap.get("socThermalTemp"),
            "systemHTemp": strMap.get("systemHTemp"),
            "gpuTemp": strMap.get("gpuTemp"),
            "ambientTemp": strMap.get("ambientTemp"),
            "batteryTemp": strMap.get("batteryTemp"),
            "pss": strMap.get("pss"),
            "fps": strMap.get("fps"),
            "fpsJitters": strMap.get("fpsJitters")
        }
        const STORE_CONFIG = {
            name: pathSuffix + ".db"
        }
        dataRdb.getRdbStore(globalThis.abilityContext, STORE_CONFIG, dbVersion, (err, rdbStore) => {
            rdbStore.insert(tableName, valueInsert)
        })
    },

    //查询表( T_GENERAL_INFO)
    async queryGeneralData(): Promise<Array<TGeneralInfo>> {
        const STORE_CONFIG = {
            name: dbName
        }
        let results = Array<TGeneralInfo>()
        try {
            return await dataRdb.getRdbStore(globalThis.abilityContext, STORE_CONFIG, 1)
                .then(rdbStore => {
                    let predicates = new dataRdb.RdbPredicates('t_general_info')
                    predicates.orderByDesc("startTime")
                    return rdbStore.query(predicates, ["sessionId",
                    "startTime", "taskId", "appName", "appVersion", "packageName", "endTime", "testDuration", "taskName"
                    ])
                })
                .then(resultSet => {
                    while (resultSet.goToNextRow()) {
                        let sessionId = resultSet.getString(resultSet.getColumnIndex("sessionId"))
                        let taskId = resultSet.getString(resultSet.getColumnIndex("taskId"))
                        let appName = resultSet.getString(resultSet.getColumnIndex("appName"))
                        let appVersion = resultSet.getString(resultSet.getColumnIndex("appVersion"))
                        let packageName = resultSet.getString(resultSet.getColumnIndex("packageName"))
                        let startTime = resultSet.getString(resultSet.getColumnIndex("startTime"))
                        let endTime = resultSet.getString(resultSet.getColumnIndex("endTime"))
                        let testDuration = resultSet.getLong(resultSet.getColumnIndex("testDuration"))
                        let taskName = resultSet.getString(resultSet.getColumnIndex("taskName"))
                        let board = resultSet.getString(resultSet.getColumnIndex("board"))
                        let deviceTypeName = resultSet.getString(resultSet.getColumnIndex("deviceTypeName"))
                        let brand = resultSet.getString(resultSet.getColumnIndex("brand"))
                        let version = resultSet.getString(resultSet.getColumnIndex("version"))
                        let sn = resultSet.getString(resultSet.getColumnIndex("sn"))

                        let tGeneralInfo = new TGeneralInfo(
                            sessionId, taskId, appName, appVersion, packageName, Number(startTime).valueOf(), Number(endTime).valueOf(), testDuration, taskName
                        )
                        tGeneralInfo.board = board
                        tGeneralInfo.deviceTypeName = deviceTypeName
                        tGeneralInfo.brand = brand
                        tGeneralInfo.version = version
                        tGeneralInfo.sn = sn
                        results.push(tGeneralInfo)
                    }
                    return results
                })

        } catch (err) {
            SPLogger.ERROR(TAG,"resultSet queryGeneralData:err" + err)
        }
    },

    //查询表( T_INDEX_INFO) 2022-02-23 改为传时间戳
    async queryData(dbPath: string): Promise<Array<TIndexInfo>> {
        const STORE_CONFIG = {
            name: dbPath
        }
        let results = Array<TIndexInfo>()
        try {
            return await dataRdb.getRdbStore(globalThis.abilityContext, STORE_CONFIG, 1)
                .then(rdbStore => {
                    let predicates = new dataRdb.RdbPredicates('t_index_info')
                    predicates.orderByDesc("timestamp")
                    return rdbStore.query(predicates, [
                        "timestamp",
                        "taskId",
                        "shellBackTemp", "shellFrameTemp", "shellFrontTemp", "socThermalTemp", "systemHTemp", "gpuTemp", "ambientTemp", "batteryTemp",
                        "ddrFrequency",
                        "cpu0Frequency", "cpu1Frequency", "cpu2Frequency", "cpu3Frequency", "cpu4Frequency", "cpu5Frequency", "cpu6Frequency", "cpu7Frequency",
                        "cpu0Load", "cpu1Load", "cpu2Load", "cpu3Load", "cpu4Load", "cpu5Load", "cpu6Load", "cpu7Load",
                        "gpuLoad", "gpuFrequency",
                        "currentNow", "voltageNow",
                        "pss",
                        "fps", "fpsJitters"
                    ])
                })
                .then(resultSet => {
                    while (resultSet.goToNextRow()) {
                        let timestamp = resultSet.getString(resultSet.getColumnIndex("timestamp"))
                        let shellBackTemp = resultSet.getString(resultSet.getColumnIndex("shellBackTemp"))
                        let shellFrameTemp = resultSet.getString(resultSet.getColumnIndex("shellFrameTemp"))
                        let shellFrontTemp = resultSet.getString(resultSet.getColumnIndex("shellFrontTemp"))
                        let socThermalTemp = resultSet.getString(resultSet.getColumnIndex("socThermalTemp"))
                        let systemHTemp = resultSet.getString(resultSet.getColumnIndex("systemHTemp"))
                        let gpuTemp = resultSet.getString(resultSet.getColumnIndex("gpuTemp"))
                        let ambientTemp = resultSet.getString(resultSet.getColumnIndex("ambientTemp"))
                        let batteryTemp = resultSet.getString(resultSet.getColumnIndex("batteryTemp"))
                        let ddrFrequency = resultSet.getString(resultSet.getColumnIndex("ddrFrequency"))
                        let cpu0Frequency = resultSet.getString(resultSet.getColumnIndex("cpu0Frequency"))
                        let cpu1Frequency = resultSet.getString(resultSet.getColumnIndex("cpu1Frequency"))
                        let cpu2Frequency = resultSet.getString(resultSet.getColumnIndex("cpu2Frequency"))
                        let cpu3Frequency = resultSet.getString(resultSet.getColumnIndex("cpu3Frequency"))
                        let cpu4Frequency = resultSet.getString(resultSet.getColumnIndex("cpu4Frequency"))
                        let cpu5Frequency = resultSet.getString(resultSet.getColumnIndex("cpu5Frequency"))
                        let cpu6Frequency = resultSet.getString(resultSet.getColumnIndex("cpu6Frequency"))
                        let cpu7Frequency = resultSet.getString(resultSet.getColumnIndex("cpu7Frequency"))

                        let cpu0Load = resultSet.getString(resultSet.getColumnIndex("cpu0Load"))
                        let cpu1Load = resultSet.getString(resultSet.getColumnIndex("cpu1Load"))
                        let cpu2Load = resultSet.getString(resultSet.getColumnIndex("cpu2Load"))
                        let cpu3Load = resultSet.getString(resultSet.getColumnIndex("cpu3Load"))
                        let cpu4Load = resultSet.getString(resultSet.getColumnIndex("cpu4Load"))
                        let cpu5Load = resultSet.getString(resultSet.getColumnIndex("cpu5Load"))
                        let cpu6Load = resultSet.getString(resultSet.getColumnIndex("cpu6Load"))
                        let cpu7Load = resultSet.getString(resultSet.getColumnIndex("cpu7Load"))

                        let gpuLoad = resultSet.getString(resultSet.getColumnIndex("gpuLoad"))
                        let gpuFrequency = resultSet.getString(resultSet.getColumnIndex("gpuFrequency"))
                        let currentNow = resultSet.getString(resultSet.getColumnIndex("currentNow"))
                        let voltageNow = resultSet.getString(resultSet.getColumnIndex("voltageNow"))

                        let pss = resultSet.getString(resultSet.getColumnIndex("pss"))
                        let fps = resultSet.getString(resultSet.getColumnIndex("fps"))
                        let fpsJitters = resultSet.getString(resultSet.getColumnIndex("fpsJitters"))

                        results.push(new TIndexInfo(
                            timestamp,
                            "18",
                            cpu0Frequency, cpu1Frequency, cpu2Frequency, cpu3Frequency, cpu4Frequency, cpu5Frequency, cpu6Frequency, cpu7Frequency,
                            cpu0Load, cpu1Load, cpu2Load, cpu3Load, cpu4Load, cpu5Load, cpu6Load, cpu7Load,
                            gpuFrequency, gpuLoad,
                            ddrFrequency,
                            shellFrameTemp, shellFrontTemp, shellBackTemp, socThermalTemp, systemHTemp, gpuTemp, ambientTemp, batteryTemp,
                            currentNow, voltageNow,
                            pss,
                            fps,
                            fpsJitters
                        ))
                        console.log("resultSet column names:results:index:" + resultSet.rowIndex)
                    }
                    console.log("resultSet column names:results length:" + results.length)
                    console.log("resultSet column names:results:" + JSON.stringify(results))
                    return results
                })
        } catch (err) {
            SPLogger.ERROR(TAG,"resultSet queryIndexInfo Data:err" + err)
        }
    },
    /**
     * Array<GPData> 封装为TIndexInfo
     * @param gpDatas
     */
    gpArray2Index(gpDatas: Array<GPData>): TIndexInfo{
        var tIndexInfo: TIndexInfo = new TIndexInfo()
        tIndexInfo.setTaskId("18")
        tIndexInfo.setTimeStamp(new Date().getTime().toString())
        if (gpDatas != null) {
            for (var index = 0; index < gpDatas.length; index++) {
                let curGPData: GPData = gpDatas[index]
                let map = curGPData.values
                switch (curGPData.moduleType) {
                    case "CPU":
                        tIndexInfo.setCPUData(
                        map.get("cpu0Freq"),
                        map.get("cpu1Freq"),
                        map.get("cpu2Freq"),
                        map.get("cpu3Freq"),
                        map.get("cpu4Freq"),
                        map.get("cpu5Freq"),
                        map.get("cpu6Freq"),
                        map.get("cpu7Freq")
                        )
                        break;
                    case "CPULoad":
                        tIndexInfo.setCPULoadData(
                        map.get("cpu0Load"),
                        map.get("cpu1Load"),
                        map.get("cpu2Load"),
                        map.get("cpu3Load"),
                        map.get("cpu4Load"),
                        map.get("cpu5Load"),
                        map.get("cpu6Load"),
                        map.get("cpu7Load")
                        )
                        break;
                    case "GPU":
                        tIndexInfo.setGPUData(
                        map.get("gpuFreq"),
                        map.get("gpuLoad")
                        )
                        break;
                    case "DDR":
                        tIndexInfo.setDDRData(
                        map.get("ddrFreq")
                        )
                        break;
                    case "Temp":
                        tIndexInfo.setTempData(
                        map.get("shell_frame"),
                        map.get("shell_front"),
                        map.get("shell_back"),
                        map.get("soc-thermal"),
                        map.get("system_h"),
                        map.get("gpu-thermal"),
                        map.get("ambient"),
                        map.get("Battery")
                        )
                        break;
                    case "Power":
                        tIndexInfo.setPowerData(
                        map.get("current_now"),
                        map.get("voltage_now")
                        )
                        break;
                    case "RAM":
                        tIndexInfo.setRamData(
                        map.get("pss")
                        )
                        break;
                    case "FPS":
                        tIndexInfo.setFpsData(
                        map.get("fps"), map.get("fpsJitters"))
                        break;
                }
            }
        }
        return tIndexInfo
    }
}
