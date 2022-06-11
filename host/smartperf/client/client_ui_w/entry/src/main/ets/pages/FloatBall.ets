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
import { taskSave } from '../common/ProfilerUtils';
import { TIndexInfo } from '../common/data/DatabaseEntity';
import {catch_trace_times, catch_trace_start, catch_trace_finish, catch_trace_first_running, catch_trace_running} from '../common/CatchTraceUtils';
import {secToTime} from '../common/utils/TimeUtils';
import {taskStart, taskDestroy, taskInit, task_running, task_pause, task_init} from '../common/ProfilerUtils';
import SPLogger from "../common/utils/SPLogger"
import {MainWorker} from "../common/MainWorkProfiler"
import {initFloatWindow, destoryAllFloatWindow} from '../common/FloatWindowUtils'

const TAG = "FloatBall"
let traceTime = 0
let socketCollectItems
let isCollectRam = false

@Entry
@Component
struct FloatBall {
  @State playerState: number = task_init
  @State timerNum: number = 0
  @State FpsTimer: number = -1
  timerId: number = -1
  @State windShowState: boolean = false
  @State windFpsShowState: boolean = false
  offsetX: number = -1
  offsetY: number = -1
  //解決手势失效的问题
  COUNTS = 2 // 点击次数
  DURATION: number = 300 // 规定有效时间
  mHits = Array(this.COUNTS) // 数组
  isDoubleFlag = false // 是否是双击
  singleClickId = 0 // 单击事件ID

  aboutToAppear() {
    taskInit()
    //创建TiTile窗
    globalThis.CreateTitleWindow()


    initFloatWindow()
    globalThis.task_status = task_init

    if (globalThis.collectConfigs.trace) {
      globalThis.catchTraceState = catch_trace_start
      globalThis.jitterTrace = false
    }
  }

  initAllCollect() {
    SPLogger.DEBUG(TAG, "initAllCollect");
    if (globalThis.collectConfigs != -1 && globalThis.collectPkg != -1) {

      globalThis.collectIntervalCollect = setInterval(() => {

        if (globalThis.timerFps == undefined) {
        } else {
          this.FpsTimer = globalThis.timerFps
        }
        if (this.playerState == task_running) {
          this.timerNum++
          taskStart(globalThis.collectConfigs, globalThis.collectPkg)
        }
        SPLogger.DEBUG(TAG, "this.timerNum:" + this.timerNum)
        SPLogger.DEBUG(TAG, "taskStatus:" + this.playerState);
      }, 999)
      globalThis.collectSocketCollect = setInterval(() => {
        if (this.playerState == task_running) {
          isCollectRam = false
          if ((globalThis.collectIntervalNum % 5 == 0) && globalThis.collectConfigs.RAM) {
            isCollectRam = true
          } else {
            isCollectRam = false
          }

          if ((globalThis.collectIntervalNum == 0) && globalThis.collectConfigs.RAM) {
            isCollectRam = true
          }

          let curPkg = globalThis.collectPkg  
          let curFPS = globalThis.collectConfigs.FPS
          let curScreenCapture = globalThis.collectConfigs.screen_capture
          let isVideo = globalThis.collectConfigs.is_video
          let isCamera = globalThis.collectConfigs.is_camera
          socketCollectItems = {
            "pkg": curPkg,
            "ram": isCollectRam,
            "fps": curFPS,
            "screen_capture": curScreenCapture,
            "is_video": isVideo,
            "is_camera": isCamera
          }

          if (globalThis.collectConfigs.trace) {
            this.taskTrace(socketCollectItems)
          }
          MainWorker.postMessage(socketCollectItems)
        }
      }, 1000)
    }
    globalThis.task_status = task_running
    this.playerState = task_running
  }

  getJitterTrace() {
    if (globalThis.fpsJitterArr != undefined && globalThis.fpsJitterArr != null && globalThis.fpsJitterArr != "") {
      SPLogger.DEBUG(TAG, "getTrace--------------222222this.jitterTrace" + globalThis.jitterTrace)
      let tempQueue: Array<String> = globalThis.fpsJitterArr
      let curJitter = tempQueue.pop()
      let tempJitterArr = curJitter.split("==")
      for (var i = 0; i < tempJitterArr.length; i++) {
        let tmp = tempJitterArr[i]
        let jitter = parseInt(tmp) / 1e6
        if (jitter > 100) {
          SPLogger.DEBUG(TAG, "getTrace--------------3333333this.jitterTrace" + globalThis.jitterTrace)
          globalThis.jitterTrace = true
          return
        }
      }
    }
  }

  taskTrace(defaultSocketCollectItems) {
    if (globalThis.catchTraceState == catch_trace_start) { //开始
      defaultSocketCollectItems["catch_trace_start"] = true
      globalThis.catchTraceState = catch_trace_first_running
    } else if (globalThis.catchTraceState == catch_trace_finish) { //抓取
      defaultSocketCollectItems["catch_trace_finish"] = true
      defaultSocketCollectItems["traceName"] = this.timerNum + ""
      globalThis.catchTraceState = catch_trace_running;
      traceTime = 0
    } else if (globalThis.catchTraceState == catch_trace_first_running) { //第一次运行
      if (globalThis.jitterTrace) { //抓取
        defaultSocketCollectItems["catch_trace_finish"] = true
        defaultSocketCollectItems["traceName"] = this.timerNum + ""
        globalThis.catchTraceState = catch_trace_running;
        traceTime = 0
        globalThis.jitterTrace = false
      }
    } else if (globalThis.catchTraceState == catch_trace_running) { //有时间间隔的运行
      traceTime++
      if (traceTime > catch_trace_times) {
        globalThis.catchTraceState = catch_trace_start
      }
    }
    socketCollectItems = defaultSocketCollectItems
  }

  singleEvent() {
    if (this.playerState == task_running) {
      globalThis.task_status = task_pause
      this.playerState = task_pause
    }
    else if (this.playerState == task_pause) {
      globalThis.task_status = task_running
      this.playerState = task_running
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
    clearInterval(globalThis.collectIntervalCollect)
    clearInterval(globalThis.collectSocketCollect)
    clearInterval(globalThis.collectOtherSocket)
    let data = new Array<TIndexInfo>()
    let result = data.concat(globalThis.tTndexs)
    globalThis.tTndexs.length = 0;
    taskSave(result)
    taskDestroy()
    globalThis.DestroyFloatingWindow()
    globalThis.DestroyTitleWindow()
    destoryAllFloatWindow()
  }

  MoveWindow(offsetX: number, offsetY: number) {
    globalThis.MoveFloatingWindow(offsetX, offsetY)
  }

  SetWindowPosition(offsetX: number, offsetY: number) {
    globalThis.SetFloatingWindowPosition(offsetX, offsetY)
  }

  build() {
    Stack({ alignContent: Alignment.Center }) {

      if (this.playerState == task_init) {
        Circle()
          .width('40vp')
          .height('40vp')
          .fill(Color.White)
          .fillOpacity(0)
          .opacity(0.8)
          .border({ radius: '90vp', width: '0.5vp', color: $r("app.color.colorPrimary") })
          .linearGradient({
            angle: 135,
            direction: GradientDirection.Left,
            colors: [[$r("app.color.colorPrimary"), 1.0], [$r("app.color.colorPrimary"), 1.0]]
          })
        Text('start')
          .fontSize(10)
          .textAlign(TextAlign.Center)
          .fontColor($r("app.color.color_fff"))
          .width('100%')
          .height('100%')
          .onClick(() => {
            this.initAllCollect()
          })
          .gesture(
          GestureGroup(GestureMode.Exclusive,
          TapGesture({ count: 2 })
            .onAction(() => {
              SPLogger.DEBUG(TAG, "TapGesture");
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

      if (this.playerState == task_running || this.playerState == task_pause) {
        if (this.playerState == task_pause) {
          Circle()
            .width('40vp')
            .height('40vp')
            .fill(Color.White)
            .fillOpacity(0)
            .opacity(0.8)
            .border({ radius: '90vp', width: '0.5vp', color: $r("app.color.color_666") })
            .linearGradient({
              angle: 135,
              direction: GradientDirection.Left,
              colors: [[$r("app.color.color_666"), 0.7], [$r("app.color.color_666"), 0.7]]
            })
        }else{
          Circle()
            .width('40vp')
            .height('40vp')
            .fill(Color.White)
            .fillOpacity(0)
            .opacity(0.5)
            .border({ radius: '90vp', width: '0.5vp', color: $r("app.color.colorPrimary") })
            .linearGradient({
              angle: 135,
              direction: GradientDirection.Left,
              colors: [[$r("app.color.colorPrimary"), 0.7], [$r("app.color.colorPrimary"), 0.7]]
            })
        }

        Text(secToTime(this.timerNum).toString())
          .fontSize(10)
          .textAlign(TextAlign.Center)
          .fontColor($r("app.color.color_fff"))
          .width('100%')
          .height('100%')
          .onClick(res => {
            this.isDoubleFlag = false
            for(let i = 0; i < this.mHits.length - 1; i++) {
              this.mHits[i] = this.mHits[i + 1]
            }
            this.mHits[this.mHits.length - 1] = new Date().getTime()
            if(this.mHits[0] >= new Date().getTime() - this.DURATION) {
              this.doubleEvent()
              this.isDoubleFlag = true
              this.mHits = Array(this.COUNTS)
            } else {
              this.singleClickId = setTimeout(()=>{
                if(!this.isDoubleFlag) {
                  this.singleEvent()
                }
              }, 300)
            }
          })
          .gesture(
          GestureGroup(GestureMode.Exclusive,
          LongPressGesture({ fingers: 1, repeat: false, duration: 1000 })
            .onAction(() => {
              SPLogger.DEBUG(TAG, "LongPressGesture");
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



    }.width('100%').height('100%')

  }
}