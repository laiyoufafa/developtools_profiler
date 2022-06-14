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
import {createFloatWindow,showFloatWindow,hideFloatWindow,destoryFloatWindow,moveFloatWindow,setFloatWindow} from '../common/FloatWindowUtils'
import {CURRENTNOW,SHELLBACKTEMP,DDRFREQUENCY,GPUFREQUENCY,CPU0FREQUENCY,CPU1FREQUENCY,CPU2FREQUENCY,RAM,FPS} from '../common/CatchTraceUtils'
@Entry
@Component
struct TitleWindowPage {
  aboutToAppear() {
    globalThis.floatIntervalCollect = setInterval(() => {
      this.shellBackTemp = globalThis.tTndex.shellBackTemp
       if (globalThis.timerFps == undefined) {
        } else {
          this.FpsTimer = globalThis.timerFps
        }
      this.currentNow = globalThis.tTndex.currentNow
      this.gpuFrequency = Number((globalThis.tTndex.gpuFrequency / 1e6).toFixed(2)).valueOf()
      this.gpuLoad = parseInt(globalThis.tTndex.gpuLoad)
      this.ddrFrequency = Number(globalThis.tTndex.ddrFrequency / 1e6).valueOf()

      if (globalThis.tTndex.pss != "" && globalThis.tTndex.pss !== undefined) {
        if(!isNaN(globalThis.tTndex.pss)) {
          this.pss = globalThis.tTndex.pss
        }
      }

      // 0 4 7 小中大核
      this.cpu0Frequency = parseInt(globalThis.tTndex.cpu0Frequency) / 1e3
      this.cpu0Load = parseInt(globalThis.tTndex.cpu0Load)
      this.cpu1Frequency = parseInt(globalThis.tTndex.cpu4Frequency) / 1e3
      this.cpu1Load = parseInt(globalThis.tTndex.cpu4Load)
      this.cpu2Frequency = parseInt(globalThis.tTndex.cpu7Frequency) / 1e3
      this.cpu2Load = parseInt(globalThis.tTndex.cpu7Load)
    }, 999)
  }

  //计时器
  @State timerNum: number= 0
  //当前电流
  @State  currentNow: number= 0
  //壳温
  @State  shellBackTemp: number= 0
  //GPU帧率
  @State  gpuFrequency: number= 0
  //gpu负载
  @State  gpuLoad: number= 0
  //ddr帧率
  @State  ddrFrequency: number= 0
  //FPS
   @State FpsTimer: number = -1
  //RAM
  @State  pss: number= -1
  @State  cpu0Frequency: number= 0
  @State  cpu0Load: number= 0
  @State  cpu1Frequency: number= 0
  @State  cpu1Load: number= 0
  @State  cpu2Frequency: number= 0
  @State  cpu2Load: number= 0
  offsetX: number = -1
  offsetY: number = -1
  data: boolean[]= [false, false, false, false,false, false, false, false,false]

  MoveWindow(offsetX:number,offsetY:number){
    globalThis.MoveTitleWindow(offsetX,offsetY)
  }
  SetWindowPosition(offsetX:number,offsetY:number){
    globalThis.SetTitleWindowPosition(offsetX,offsetY)
  }
  floatWindowEvent(floatName:string,flag:number){
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
      Rect({ width: '100%', height: '100%' }).radius(10).opacity(0.5)

    Column({ space: 1 }) {
      Row() {
        Image($r("app.media.logo")).width('10vp').height('10vp').margin({ left: '2%' })
        Text( "SmartPerf")
          .fontSize('10fp')
          .fontColor($r("app.color.color_fff")).margin({ left: '2%' })
        Image($r("app.media.icon_close_small")).height('15vp').width('15vp').margin({ left: '44%' }).onClick(() => {
           //关闭实时悬浮框
          globalThis.HideTitleWindow()
        })
      } .height('15vp')
      .width('95%')

      Row() {
        Image($r("app.media.icon_normalized_current")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("FPS").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.FpsTimer + "fps").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })

        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("FPS",FPS)
          console.log("FPS--------------onClick")
        })
      }
      .height('15vp')
      .width('95%')
      Row() {
        Image($r("app.media.icon_normalized_current")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("当前电流").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.currentNow + "mA").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })

        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("currentNow",CURRENTNOW)
        })
      }
      .height('15vp')
      .width('95%')


      Row() {
        Image($r("app.media.icon_max_temperature")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("壳温").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.shellBackTemp / 1e3 + "℃").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })
        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("shellBackTemp",SHELLBACKTEMP)
        })
      }
      .height('15vp')
      .width('95%')


      Row() {
        Image($r("app.media.icon_counter")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("DDR").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.ddrFrequency + "MHz").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })

        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("ddrFrequency",DDRFREQUENCY)
        })
      }
      .height('15vp')
      .width('95%')


      Row() {
        Image($r("app.media.icon_frame_score")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("GPU").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.gpuFrequency + "MHz" + " " + this.gpuLoad + "%")
            .fontSize('10fp')
            .fontColor($r("app.color.color_fff"))
            .onClick(() => {
            })
        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("gpuFrequency",GPUFREQUENCY)
        })
      }
      .height('15vp')
      .width('95%')


      Row() {
        Image($r("app.media.icon_counter")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("CPU0频率").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.cpu0Frequency + "MHz"+ " " + this.cpu0Load + "%").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })
        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("cpu0Frequency",CPU0FREQUENCY)
        })

      }
      .height('15vp')
      .width('95%')

      Row() {
        Image($r("app.media.icon_counter")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("CPU1频率").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.cpu1Frequency + "MHz"+ " " + this.cpu1Load + "%").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })

        }.height('15vp').width('88%') .onClick(() => {
          this.floatWindowEvent("cpu1Frequency",CPU1FREQUENCY)
        })
      }
      .height('15vp')
      .width('95%')


      Row() {
        Image($r("app.media.icon_average_frame_b")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("CPU2频率").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.cpu2Frequency + "MHz"+ " " + this.cpu2Load + "%").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })
        }.height('15vp').width('88%').onClick(() => {
          this.floatWindowEvent("cpu2Frequency",CPU2FREQUENCY)
        })

      }
      .height('15vp')
      .width('95%')


      Row() {
        Image($r("app.media.icon_jank_each_hour")).width('10vp').height('10vp').margin({ left: '2%' })
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Text("RAM").fontSize('10fp').fontColor($r("app.color.color_fff"))
          Text(this.pss + "KB").fontSize('10fp').fontColor($r("app.color.color_fff")).onClick(() => {
          })
        }.height('15vp').width('88%') .onClick(() => {
          this.floatWindowEvent("RAM",RAM)
        })
      }
      .height('15vp')
      .width('95%')

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
        this.MoveWindow(this.offsetX,this.offsetY)
        this.SetWindowPosition(this.offsetX,this.offsetY)
      })
    ))
    } .width('100%')
    .height('100%')
  }
}