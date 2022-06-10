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
import { TIndexInfo } from '../../data/DatabaseEntity';
import { SummaryItem } from '../../data/LocalCommon'
import { getCpuCoreInfo, } from '../../utils/SystemUtils';
import CalculationUtils from '../../utils/CalculationUtils';

/*
 * 报告详情概览页
 */
@Component
@Preview
export struct Summary {
  @State private gpData: Array<TIndexInfo> = new Array()
  @State private summaryItems: Array<SummaryItem> = new Array()

  aboutToAppear() {
    console.log("Summary: aboutToAppear");
    var cpuCoreArr = getCpuCoreInfo()
    console.log("Summary:aboutToAppear: cpuCoreArr:" + cpuCoreArr);

    var cpu0FreqSum: number = 0
    var cpu1FreqSum: number = 0
    var cpu2FreqSum: number = 0
    var cpu3FreqSum: number = 0
    var cpu4FreqSum: number = 0
    var cpu5FreqSum: number = 0
    var cpu6FreqSum: number = 0
    var cpu7FreqSum: number = 0
    var cpuFreqMap = new Map

    var cpuMin: number = 0
    var cpuMid: number = 0
    var cpuMax: number = 0

    var currentNowSum: number = 0
    var curVoltageSum: number = 0
    var normalCurrentNow: number = 0


    var shellFrameSum: number = 0
    var gpuLoadSum: number = 0
    var ddrFreqSum: number = 0

    // fps和ram 为空时 过滤掉脏数据 0和空
    var fpsNullSum = 0
    var ramNullSum = 0

    var pssSum: number = 0
    var fpsSum: number = 0
    let fpsMax: number = 0
    let fpsList = []
    let fpsJitters = []

    for (var index = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      console.log("Summary s:" + JSON.stringify(gpDataCur));
      let currentNow = Number(gpDataCur.currentNow).valueOf()
      currentNowSum += Math.abs(currentNow)

      let curVoltage = Number(gpDataCur.voltageNow).valueOf() / 1e6
      curVoltageSum += Math.abs(curVoltage)
      normalCurrentNow += Math.abs(currentNow) * Math.abs(curVoltage) / 3.8

      shellFrameSum += Number(gpDataCur.shellFrameTemp).valueOf()
      gpuLoadSum += Number(gpDataCur.gpuLoad).valueOf()
      ddrFreqSum += Number(gpDataCur.ddrFrequency).valueOf()
      cpu0FreqSum += Number(gpDataCur.cpu0Frequency).valueOf()
      cpu1FreqSum += Number(gpDataCur.cpu1Frequency).valueOf()
      cpu2FreqSum += Number(gpDataCur.cpu2Frequency).valueOf()
      cpu3FreqSum += Number(gpDataCur.cpu3Frequency).valueOf()
      cpu4FreqSum += Number(gpDataCur.cpu4Frequency).valueOf()
      cpu5FreqSum += Number(gpDataCur.cpu5Frequency).valueOf()
      cpu6FreqSum += Number(gpDataCur.cpu6Frequency).valueOf()
      cpu7FreqSum += Number(gpDataCur.cpu7Frequency).valueOf()

      if (gpDataCur.pss == "") {
        ramNullSum++
      }
      if (gpDataCur.fps == "" || gpDataCur.fps == "0") {
        fpsNullSum++
      }

      pssSum += Number(gpDataCur.pss).valueOf()
      let fpxCur = Number(gpDataCur.fps).valueOf()
      fpsSum += fpxCur
      if (fpsMax < fpxCur) {
        fpsMax = fpxCur
      }
      fpsList.push(Number(gpDataCur.fps).valueOf())
      fpsJitters.push(gpDataCur.fpsJitters.toString().replace("\"", ""))
    }

    cpuFreqMap.set("cpu0FreqSum", cpu0FreqSum)
    cpuFreqMap.set("cpu1FreqSum", cpu1FreqSum)
    cpuFreqMap.set("cpu2FreqSum", cpu2FreqSum)
    cpuFreqMap.set("cpu3FreqSum", cpu3FreqSum)
    cpuFreqMap.set("cpu4FreqSum", cpu4FreqSum)
    cpuFreqMap.set("cpu5FreqSum", cpu5FreqSum)
    cpuFreqMap.set("cpu6FreqSum", cpu6FreqSum)
    cpuFreqMap.set("cpu7FreqSum", cpu7FreqSum)

    console.log("Summary cpu0FreqSum" + cpu0FreqSum);
    console.log("Summary cpu1FreqSum" + cpu1FreqSum);
    console.log("Summary cpu2FreqSum" + cpu2FreqSum);
    console.log("Summary cpu3FreqSum" + cpu3FreqSum);
    console.log("Summary cpu4FreqSum" + cpu4FreqSum);
    console.log("Summary cpu5FreqSum" + cpu5FreqSum);
    console.log("Summary cpu6FreqSum" + cpu6FreqSum);
    console.log("Summary cpu7FreqSum" + cpu7FreqSum);
    console.log("Summary FpsJitters" + JSON.stringify(fpsJitters));

    console.log("Summary:aboutToAppear currentNowSum:" + currentNowSum);
    console.log("Summary:aboutToAppear shellFrameSum:" + shellFrameSum);
    console.log("Summary:aboutToAppear gpuLoadSum:" + gpuLoadSum);
    console.log("Summary:aboutToAppear ddrFreqSum:" + ddrFreqSum);
    console.log("Summary:aboutToAppear cpuFreqMap:" + JSON.stringify(cpuFreqMap));


    cpuMin = cpuFreqMap.get("cpu" + cpuCoreArr[0] + "FreqSum") / 1e3
    cpuMid = cpuFreqMap.get("cpu" + cpuCoreArr[1] + "FreqSum") / 1e3
    cpuMax = cpuFreqMap.get("cpu" + cpuCoreArr[2] + "FreqSum") / 1e3

    let calculationTest = new CalculationUtils(
      fpsList,
    CalculationUtils.calculateFPSNew(fpsList))
    this.summaryItems.push(
      new SummaryItem($r("app.media.icon_normalized_current"), "归一化电流", (normalCurrentNow / this.gpData.length).toFixed(0) + "mA", ""),
      new SummaryItem($r("app.media.icon_max_temperature"), "壳温", shellFrameSum / this.gpData.length / 1000 + "℃", "#fff8f8"),

      new SummaryItem($r("app.media.icon_counter"), "平均帧率/最高帧率",
        (fpsSum / (this.gpData.length - fpsNullSum)).toFixed(0) + "Hz" + "/" + (fpsMax).toFixed(0) + "HZ", "#fcf4ee"),
      new SummaryItem($r("app.media.icon_jank_score"), "低帧率",
        (calculationTest.Low_Frame_Rate()).toFixed(2) + "%", "#fcf4ee"),

      new SummaryItem($r("app.media.icon_jank_score"), "抖动率", (calculationTest.Jitter_rate()).toFixed(2) + "%", "#fcf4ee"),

      new SummaryItem($r("app.media.icon_jank_score"), "卡顿次数", (calculationTest.calculateCaton(fpsJitters)).toFixed(0) + "次", "#fcf4ee"),


      new SummaryItem($r("app.media.icon_frame_score"), "GPU负载", (gpuLoadSum / this.gpData.length).toFixed(0) + "%", "#fcf9f2"),
      new SummaryItem($r("app.media.icon_average_frame_b"), "DDR频率", (ddrFreqSum / this.gpData.length / 1e6).toFixed(0) + "MHZ", "#fcf9f2"),

      new SummaryItem($r("app.media.icon_jank_each_hour"), "CPU0频率", (cpuMin / this.gpData.length).toFixed(0) + "MHZ", "#fcf9f2"),
      new SummaryItem($r("app.media.icon_frame_score"), "CPU1频率", (cpuMid / this.gpData.length).toFixed(0) + "MHZ", "#fcf9f2"),
      new SummaryItem($r("app.media.icon_average_frame_b"), "CPU2频率", (cpuMax / this.gpData.length).toFixed(0) + "MHZ", "#fcf9f2"),
      new SummaryItem($r("app.media.icon_jank_each_hour"), "RAM", (pssSum / (this.gpData.length - ramNullSum)).toFixed(0) + "KB", "#f0faff")
    )
  }

  build() {
    Scroll() {
      Column() {
        Grid() {
          ForEach(this.summaryItems, item => {
            GridItem() {
              Row({ space: "8vp" }) {
                Image(item.icon).width('30vp').height('30vp')

                Text(item.content).fontSize('14fp').textAlign(TextAlign.Start)

                Text(item.value).fontSize('11fp').textAlign(TextAlign.Start)
              }.alignItems(VerticalAlign.Center).width('100%')
            }
            .width("90%")
            .align(Alignment.Center)
            .backgroundColor(item.backColor)
            .border({ radius: '5vp', color: '#ffffff' }).shadow({radius : 5})
            .margin({top: '30vp', left:'30vp'})
            .padding("10vp")
          }, item => item.content)
        }.margin({ bottom: "20%", left: "15%", right: "15%" }).width('70%')
        .columnsTemplate('1fr 1fr 1fr')
      }
    }.height('100%').width('100%')
  }
}