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
import { hideFloatWindow } from '../common/FloatWindowUtils'
import { FloatWindowComponent } from '../common/FloatWindowComponent'
import { task_running } from '../common/ProfilerUtils';

@Entry
@Component
struct CPU2LineChartPage {
  data: number[]= [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] //数据集合
  @State random: number= 0 //用于刷新的随机数
  @State cpu2Frequency: number= 0 //数值

  offsetX: number = -1 //悬浮框移动触摸点 X
  offsetY: number = -1 //悬浮框移动触摸点 X
  private floatName: string= "cpu2Frequency"
  task_state = 1

  aboutToAppear() {
    globalThis.LineChartCollect = setInterval(() => {
      this.task_state = globalThis.task_status
      if (this.task_state == task_running) {
        if (this.data.length >= 22) {

          this.data.shift() //移除第一个元素
        }
        if (globalThis.tTndex.cpu7Frequency == undefined) {
          this.cpu2Frequency = 0
        } else {
          this.cpu2Frequency = parseInt(globalThis.tTndex.cpu7Frequency) / 1e3

          if (this.cpu2Frequency == 0) {
            this.data.push(0)
          } else {
            let lineCount: number = this.cpu2Frequency / 50
            this.data.push(Math.abs(lineCount)) //在末尾填充一个元素
          }
          this.random = Math.random()
        }
      } else {
        //暂停
      }
    }, 1000)
  }

  build() {
    Stack({ alignContent: Alignment.Top }) {
      FloatWindowComponent({ title: `cpu2Frequency`, data: this.data })
      Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
        Text(this.floatName + ":" + this.cpu2Frequency + "MHz")
          .fontSize('6fp')
          .fontColor($r("app.color.color_fff"))
          .margin({ left: 5, top: 3 }) //文本显示
        Text(this.random + "")
          .fontSize('1fp')
          .fontColor($r("app.color.color_fff")).visibility(Visibility.None)
        Image($r("app.media.icon_close_small")).width('15vp').height('15vp').onClick(() => {
          hideFloatWindow(this.floatName)
          console.log("hideFloatWindow---------------------" + this.floatName)
        }).align(Alignment.TopEnd)
      }.height('15vp').width('100%')

    }
  }
}