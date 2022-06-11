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
import { showFloatWindow, hideFloatWindow } from '../common/ui/floatwindow/utils/FloatWindowUtils'
import { FPS, GPUFREQUENCY, CPU0FREQUENCY, CPU1FREQUENCY, CPU2FREQUENCY, RAM } from '../common/ui/floatwindow/FloatWindowConstant'
import CommonEvent from '@ohos.commonEvent'
@Component
export struct ItemContent {
  private icon
  private tittle: string
  @State  value: string = "-1"
  private onClickCallBack: () => void

  build() {
    Row() {
      Image(this.icon).width('20vp').height('20vp').margin({ right: '2%' })
      Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
        Text(this.tittle).fontSize(20).fontColor(Color.White)
        Text(this.value).fontSize(20).fontColor(Color.White)
      }.width('88%').height('25vp')
    }
    .height('26vp')
    .width('88%')
    .onClick(() => {
      this.onClickCallBack()
    })
  }
}

@Entry
@Component
struct TitleWindowPage {
  private data: boolean[]= [false, false, false, false, false, false, false, false]

  @State tIndexInfo: TIndexInfo = new TIndexInfo()
  offsetX: number = -1
  offsetY: number = -1

  aboutToAppear() {

    let that = this
    var subscriber
    //订阅者信息
    var subscribeInfo = {
      events: ["event"]
    };
    //订阅公共事件回调
    function SubscribeCallBack(err, data) {
      if (data.data == "") {
      } else {
        console.error("subscriberCurData:"+ data.data);
        that.tIndexInfo = JSON.parse(data.data)
        globalThis.cpu0Frequency=that.tIndexInfo.cpu0Frequency
        globalThis.cpu1Frequency=that.tIndexInfo.cpu1Frequency
        globalThis.cpu2Frequency=that.tIndexInfo.cpu2Frequency
        globalThis.currentNow=that.tIndexInfo.currentNow
        globalThis.ddrFrequency=that.tIndexInfo.ddrFrequency
        globalThis.lineFps=that.tIndexInfo.fps
        globalThis.gpuFrequency=that.tIndexInfo.gpuFrequency
        globalThis.pss=that.tIndexInfo.pss
        globalThis.shellBackTemp=that.tIndexInfo.socThermalTemp
      }
    }
    //创建订阅者回调
    function CreateSubscriberCallBack(err, data) {
      subscriber = data;
      //订阅公共事件
      CommonEvent.subscribe(subscriber, SubscribeCallBack);
    }
    //创建订阅者
    CommonEvent.createSubscriber(subscribeInfo, CreateSubscriberCallBack);
  }

  MoveWindow(offsetX: number, offsetY: number) {
    globalThis.MoveTitleWindow(offsetX, offsetY)
  }

  SetWindowPosition(offsetX: number, offsetY: number) {
    globalThis.SetTitleWindowPosition(offsetX, offsetY)
  }

  floatWindowEvent(floatName: string, flag: number) {
    if (this.data[flag]) {
      hideFloatWindow(floatName)
      this.data[flag] = false
    } else {
      showFloatWindow(floatName)
      this.data[flag] = true
    }
  }

  build() {
    Stack({ alignContent: Alignment.Center }) {
      Rect({ width: '100%', height: '100%' }).radius(20).opacity(0.4)
      Column({ space: 2 }) {

        Row() {
          Image($r("app.media.logo")).width('20vp').height('20vp').margin({ left: '2%' })
          Text( "SmartPerf")
            .fontSize('20fp')
            .fontColor($r("app.color.color_fff")).margin({ left: '2%' })
          Image($r("app.media.icon_close_small")).height('18vp').width('18vp').margin({ left: '55%' }).onClick(() => {
            //关闭实时悬浮框
            globalThis.HideTitleWindow()
          })
        } .height('26vp')
        .width('95%')

        ItemContent({
          icon: $r("app.media.icon_average_frame_b"),
          value: (this.tIndexInfo.fps.toString()) + "FPS",
          tittle: "帧率",
          onClickCallBack: () => {
            this.floatWindowEvent("FPS",FPS)
          }
        })

        ItemContent({
          icon: $r("app.media.icon_counter"),
          value: (parseInt(this.tIndexInfo.cpu0Frequency.toString()) / 1e3).toString() + "MHz",
          tittle: "CPU0频率",
          onClickCallBack: () => {
            this.floatWindowEvent("cpu0Frequency", CPU0FREQUENCY)
          }
        })
        ItemContent({
          icon: $r("app.media.icon_counter"),
          value: (parseInt(this.tIndexInfo.cpu1Frequency.toString()) / 1e3).toString() + "MHz",
          tittle: "CPU1频率",
          onClickCallBack: () => {
            this.floatWindowEvent("cpu1Frequency", CPU1FREQUENCY)
          }
        })
        ItemContent({
          icon: $r("app.media.icon_counter"),
          value: (parseInt(this.tIndexInfo.cpu2Frequency.toString()) / 1e3).toString() + "MHz",
          tittle: "CPU2频率",
          onClickCallBack: () => {
            this.floatWindowEvent("cpu2Frequency", CPU2FREQUENCY)
          }
        })
        ItemContent({
          icon: $r("app.media.icon_frame_score"),
          value: (parseInt(this.tIndexInfo.gpuFrequency.toString()) / 1e6).toString() + "MHz",
          tittle: "GPU频点",
          onClickCallBack: () => {
            this.floatWindowEvent("gpuFrequency", GPUFREQUENCY)
          }
        })
        ItemContent({
          icon: $r("app.media.icon_jank_each_hour"),
          value: this.tIndexInfo.pss + "KB",
          tittle: "RAM",
          onClickCallBack: () => {
            this.floatWindowEvent("RAM", RAM)
          }
        })
        ItemContent({
          icon: $r("app.media.icon_max_temperature"),
          value: (parseInt(this.tIndexInfo.socThermalTemp.toString()) / 1e3).toString() + "℃",
          tittle: "SOC温度"
        })
        //ItemContent({
        // icon: $r("app.media.icon_counter"),
        //value: this.tIndexInfo.networkUpSpeed.toString() + "kbps" + " " +this.tIndexInfo.networkDownSpeed.toString() + "kbps",
        // tittle: "带宽"
        //})
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
}