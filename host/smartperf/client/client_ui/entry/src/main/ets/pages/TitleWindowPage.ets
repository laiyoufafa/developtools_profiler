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
import { TIndexInfo } from '../common/entity/DatabaseEntity'
import CheckEmptyUtils from '../common/utils/CheckEmptyUtils'

//展示配置项
export enum ShowItemConfig {
  ITEM_CPU = 0X01,
  ITEM_GPU = 0X01,
  ITEM_DDR = 0X01,
  ITEM_FPS = 0X00,
  ITEM_POWER = 0X01,
  ITEM_SOC_TEMP = 0X01,
  ITEM_SHELL_FRAME = 0X01,
  ITEM_SHELL_BACK = 0X01,
  ITEM_SHELL_FRONT = 0X01,
  ITEM_RAM = 0X01,
  ITEM_UNKNOWN = 0X00,
}
;

@Component
export struct ItemContent {
  private icon
  private tittle: string
  @State  value: string = "-1"

  build() {
    Flex({ justifyContent: FlexAlign.Center }) {
      Row() {
        Image(this.icon).width('10vp').height('10vp')
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text(this.tittle).fontSize(10).fontColor(Color.Black)
          Text(this.value).fontSize(10).fontColor(Color.Black)
        }.width('88%').height("100%")
      }
      .height('15vp')
      .width('95%')
    }
  }
}

@Entry
@Component
struct TitleWindowPage {
  aboutToAppear() {
    globalThis.floatIntervalCollect = setInterval(() => {
      if (CheckEmptyUtils.isEmptyArr(globalThis.tTndex)) {
        this.tIndexInfo.setDefaultValue()
        console.log("TitleWindowPage : this.tIndexInfo isEmpty" + JSON.stringify(this.tIndexInfo))
      } else {
        this.tIndexInfo = globalThis.tTndex
        console.log("TitleWindowPage : this.tIndexInfo isNotEmpty" + JSON.stringify(this.tIndexInfo))
      }

    }, 999)
  }
  //计时器
  @State timerNum: number= 0
  @State tIndexInfo: TIndexInfo = new TIndexInfo()
  offsetX: number = -1
  offsetY: number = -1

  MoveWindow(offsetX: number, offsetY: number) {
    globalThis.MoveTitleWindow(offsetX, offsetY)
  }

  SetWindowPosition(offsetX: number, offsetY: number) {
    globalThis.SetTitleWindowPosition(offsetX, offsetY)
  }

  build() {
    Column({ space: 2 }) {
      ItemContent({
        icon: $r("app.media.icon_counter"),
        value: (parseInt(this.tIndexInfo.cpu0Frequency.toString()) / 1e3).toString() + "MHz",
        tittle: "CPU0频率"
      })
      ItemContent({
        icon: $r("app.media.icon_counter"),
        value: (parseInt(this.tIndexInfo.cpu1Frequency.toString()) / 1e3).toString() + "MHz",
        tittle: "CPU1频率"
      })
      ItemContent({
        icon: $r("app.media.icon_counter"),
        value: (parseInt(this.tIndexInfo.cpu2Frequency.toString()) / 1e3).toString() + "MHz",
        tittle: "CPU2频率"
      })
      ItemContent({
        icon: $r("app.media.icon_frame_score"),
        value: (parseInt(this.tIndexInfo.gpuFrequency.toString()) / 1e6).toString() + "MHz",
        tittle: "GPU频点"
      })
      ItemContent({ icon: $r("app.media.icon_jank_each_hour"), value: this.tIndexInfo.pss + "KB", tittle: "RAM" })
      ItemContent({
        icon: $r("app.media.icon_max_temperature"),
        value: (parseInt(this.tIndexInfo.socThermalTemp.toString()) / 1e3).toString() + "℃",
        tittle: "SOC温度"
      })
//      ItemContent({
//        icon: $r("app.media.icon_max_temperature"),
//        value: (ParseIntValue(this.tIndexInfo.shellBackTemp.toString()) / 1e3).toString() + "℃",
//        tittle: "后壳温"
//      })
//      ItemContent({
//        icon: $r("app.media.icon_max_temperature"),
//        value: (ParseIntValue(this.tIndexInfo.shellFrontTemp.toString()) / 1e3).toString() + "℃",
//        tittle: "前壳温"
//      })
//      ItemContent({
//        icon: $r("app.media.icon_max_temperature"),
//        value: (ParseIntValue(this.tIndexInfo.shellFrameTemp.toString()) / 1e3).toString() + "℃",
//        tittle: "壳温"
//      })
//      ItemContent({
//        icon: $r("app.media.icon_normalized_current"),
//        value: this.tIndexInfo.currentNow + "mA",
//        tittle: "电流"
//      })
//      ItemContent({
//        icon: $r("app.media.icon_counter"),
//        value: (ParseIntValue(this.tIndexInfo.ddrFrequency.toString()) / 1e6).toString() + "MHz",
//        tittle: "DDR频点"
//      })
    }.width('100%')
    .gesture(
    GestureGroup(GestureMode.Exclusive,
    PanGesture({})
      .onActionStart((event: GestureEvent) => {
      })
      .onActionUpdate((event: GestureEvent) => {
        this.offsetX = event.offsetX
        this.offsetY = event.offsetY
      })
      .onActionEnd(() => {
        this.MoveWindow(this.offsetX, this.offsetY)
        this.SetWindowPosition(this.offsetX, this.offsetY)
      })
    ))
  }
}