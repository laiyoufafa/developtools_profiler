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
import { secToTime } from '../common/utils/TimeUtils';
import { TaskStatus,CatchTraceStatus}  from '../common/profiler/base/ProfilerConstant';
import { MainWorker } from "../common/profiler/MainWorkTask"
import { ProfilerTask } from "../common/profiler/ProfilerTask"

let traceTime = 0
let socketCollectItems
let isCollectRam = false

@Entry
@Component
struct FloatBall {
  @State playerState: number = TaskStatus.task_init
  @State timerNum: number = 0
  @State FpsTimer: number = -1
  timerId: number = -1
  @State windShowState: boolean = false
  offsetX: number = -1
  offsetY: number = -1

  aboutToAppear() {

    ProfilerTask.getInstance().initModule()
    ProfilerTask.getInstance().taskInit()
    //创建TiTile窗
    globalThis.CreateTitleWindow()
    globalThis.task_status = TaskStatus.task_init

    if (globalThis.collectConfigs.trace) {
      globalThis.catchTraceState = CatchTraceStatus.catch_trace_start
      globalThis.jitterTrace = false
    }

  }

  initAllCollect() {
    if (globalThis.collectConfigs != -1 && globalThis.collectPkg != -1) {
      globalThis.collectIntervalCollect = setInterval(() => {
        if (globalThis.timerFps == undefined) {
        } else {
          this.FpsTimer = globalThis.timerFps
        }
        if (this.playerState == TaskStatus.task_running) {
          ProfilerTask.getInstance().taskStart()
        }
      }, 1000)
      globalThis.collectSocketCollect = setInterval(() => {
        if (this.playerState == TaskStatus.task_running) {
          this.timerNum++
          isCollectRam = false
          if ((globalThis.collectIntervalNum % 5 == 0) && globalThis.collectConfigs.RAM) {
            isCollectRam = true
          } else {
            isCollectRam = false
          }

          socketCollectItems = {
            "pkg": globalThis.collectPkg,
            "ram": isCollectRam,
            "fps": globalThis.collectConfigs.FPS,
            "screen_capture": globalThis.collectConfigs.screen_capture,
            "is_video": globalThis.collectConfigs.is_video,
            "is_camera": globalThis.collectConfigs.is_camera
          }

          if (globalThis.collectConfigs.trace) {
            this.taskTrace(socketCollectItems)
          }
          MainWorker.postMessage(socketCollectItems)
        }
      }, 1000)
    }
    globalThis.task_status = TaskStatus.task_running
    this.playerState = TaskStatus.task_running
  }

  getJitterTrace() {
    if (globalThis.fpsJitterArr != undefined && globalThis.fpsJitterArr != null && globalThis.fpsJitterArr != "") {
      let tempQueue: Array<String> = globalThis.fpsJitterArr
      let curJitter = tempQueue.pop()
      let tempJitterArr = curJitter.split("==")
      for (var i = 0; i < tempJitterArr.length; i++) {
        let tmp = tempJitterArr[i]
        let jitter = parseInt(tmp) / 1e6
        if (jitter > 100) {
          globalThis.jitterTrace = true
          return
        }
      }
    }
  }

  taskTrace(defaultSocketCollectItems) {
    if (globalThis.catchTraceState == CatchTraceStatus.catch_trace_start) { //开始
      defaultSocketCollectItems["catch_trace_start"] = true
      globalThis.catchTraceState = CatchTraceStatus.catch_trace_first_running
    } else if (globalThis.catchTraceState == CatchTraceStatus.catch_trace_finish) { //抓取
      defaultSocketCollectItems["catch_trace_finish"] = true
      defaultSocketCollectItems["traceName"] = this.timerNum + ""
      globalThis.catchTraceState = CatchTraceStatus.catch_trace_running;
      traceTime = 0
    } else if (globalThis.catchTraceState == CatchTraceStatus.catch_trace_first_running) { //第一次运行
      if (globalThis.jitterTrace) { //抓取
        defaultSocketCollectItems["catch_trace_finish"] = true
        defaultSocketCollectItems["traceName"] = this.timerNum + ""
        globalThis.catchTraceState = CatchTraceStatus.catch_trace_running;
        traceTime = 0
        globalThis.jitterTrace = false
      }
    } else if (globalThis.catchTraceState == CatchTraceStatus.catch_trace_running) { //有时间间隔的运行
      traceTime++
      if (traceTime > CatchTraceStatus.catch_trace_times) {
        globalThis.catchTraceState = CatchTraceStatus.catch_trace_start
      }
    }
    socketCollectItems = defaultSocketCollectItems
  }

  singleEvent() {
    if (this.playerState == TaskStatus.task_running) {
      globalThis.task_status = TaskStatus.task_pause
      this.playerState = TaskStatus.task_pause
    }
    else if (this.playerState == TaskStatus.task_pause) {
      globalThis.task_status = TaskStatus.task_running
      this.playerState = TaskStatus.task_running
    }
  }

  doubleEvent() {
    // 双击启动悬浮TITLE
    if (this.windShowState) {
      globalThis.HideTitleWindow()
      this.windShowState = false
    } else {
      globalThis.ShowTitleWindow()
      this.windShowState = true
    }
  }

  longEvent() {
    ProfilerTask.getInstance().taskStop()
    this.destroyAllWindow()
    this.clearAllInterVal()
  }

  destroyAllWindow(){
    globalThis.DestroyFloatingWindow()
    globalThis.DestroyTitleWindow()
  }

  clearAllInterVal(){
    clearInterval(globalThis.collectIntervalCollect)
    clearInterval(globalThis.collectSocketCollect)
    clearInterval(globalThis.collectOtherSocket)
  }

  MoveWindow(offsetX: number, offsetY: number) {
    globalThis.MoveFloatingWindow(offsetX, offsetY)
  }

  SetWindowPosition(offsetX: number, offsetY: number) {
    globalThis.SetFloatingWindowPosition(offsetX, offsetY)
  }

  build() {
    Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Center }) {
      if (this.playerState == TaskStatus.task_init) {
        Text('start')
          .textAlign(TextAlign.Center)
          .fontSize("20fp")
          .fontColor($r("app.color.colorPrimary"))
          .border({ width: 1 })
          .padding("10vp")
          .width('100%')
          .height('100%')
          .onClick(() => {
            this.initAllCollect()
          })
          .gesture(
          GestureGroup(GestureMode.Exclusive,
          TapGesture({ count: 2 })
            .onAction(() => {
              this.doubleEvent()
            }),
          PanGesture({})
            .onActionStart(() => {
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

      if (this.playerState == TaskStatus.task_running || this.playerState == TaskStatus.task_pause) {
        Text(secToTime(this.timerNum).toString())
          .fontSize("20fp")
          .textAlign(TextAlign.Center)
          .fontColor($r("app.color.colorPrimary"))
          .border({ width: 1 })
          .padding("10vp")
          .width('100%')
          .height('100%')
          .onClick(() => {
            this.singleEvent()
          })
          .gesture(
          GestureGroup(GestureMode.Exclusive,
          TapGesture({ count: 2 })
            .onAction(() => {
              this.doubleEvent()
            }),
          LongPressGesture({ fingers: 1, repeat: false, duration: 1000 })
            .onAction(() => {
              this.longEvent()
            }),
          PanGesture({})
            .onActionStart(() => {
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

      Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Center }) {
        Text("FPS:" + this.FpsTimer)
          .textAlign(TextAlign.Center)
          .fontSize("10fp")
          .fontColor($r("app.color.colorPrimary"))
          .border({ width: 1 })
          .padding("5vp")
          .width('100%')
          .height('100%')
      }.width('100%').height('50%')

    }.width('100%').height('100%')

  }
}