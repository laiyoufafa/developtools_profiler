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
@Entry
@Component
struct FpsLineChartPage {
  private settings: RenderingContextSettings = new RenderingContextSettings(true)
  private context: CanvasRenderingContext2D = new CanvasRenderingContext2D(this.settings)
  private XPoint: number = 5  //x起始坐标
  private YPoint: number = 73   //Y起始坐标
  private XScale: number = 5 //刻度
  private YScale: number = 15 //刻度
  private XLength: number = 110 //X轴长度
  private YLength: number = 70  //Y轴长度
  data: number[]= [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]   //数据集合
  private MaxDataSize: number= this.XLength / this.XScale  //数据集合的最大长度
  @State LineFps: number= 0   //FPS数值
  offsetX: number = -1    //悬浮框移动触摸点 X
  offsetY: number = -1   //悬浮框移动触摸点 X

  MoveWindow(offsetX: number, offsetY: number) {
    globalThis.MoveFPSLineChartWindow(offsetX, offsetY)
  }

  SetWindowPosition(offsetX: number, offsetY: number) {
    globalThis.SetFPSLineChartWindowPosition(offsetX, offsetY)
  }

  aboutToAppear() {
    globalThis.LineChartCollect = setInterval(() => {
      if (this.data.length >= this.MaxDataSize) {
        console.log("GestureEvent--------------shift:" + this.data);
        this.data.shift()  //移除第一个元素
      }
      if (globalThis.timerFps == undefined) {
        this.LineFps = 0
        this.data.push(0) //在末尾填充一个元素
      } else {
        this.LineFps = globalThis.timerFps
        if(this.LineFps==0){
          this.data.push(0)
        }else{
          let lineCount: number = this.LineFps / 2
          this.data.push(lineCount)//在末尾填充一个元素
        }
      }

    }, 1000)
  }

  build() {
    Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Start, justifyContent: FlexAlign.Center }) {

      Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
        Text("FPS:" + this.LineFps).fontSize('6fp').fontColor($r("app.color.color_333")).margin({ left: 5, top: 3 })  //文本显示
        Image($r("app.media.icon_close_small")).width('15vp').height('15vp').onClick(() => {
          globalThis.HideFPSLineChartWindow()
        })

      }.height('15vp').width('100%')
      Canvas(this.context)
        .width('100%')
        .height('100%')
        .onReady(() => {
          //Y轴
          this.context.clearRect(this.XPoint + 0.5, this.YPoint - this.YLength, this.XLength, this.YLength)
          this.context.beginPath()
          this.context.moveTo(this.XPoint, this.YPoint - this.YLength)
          this.context.lineTo(this.XPoint, this.YPoint)
          this.context.stroke()
          //X轴
          this.context.beginPath()
          this.context.moveTo(this.XPoint, this.YPoint)
          this.context.lineTo(this.XPoint + this.XLength, this.YPoint)
          this.context.stroke()
          //K线绘制
          if (this.data.length > 1) {
            for (let i = 1; i < this.data.length; i++) {
              this.context.beginPath()
              console.log("GestureEvent--------------beginPath:" + this.data[i-1]);
              this.context.moveTo(this.XPoint + (i - 1) * this.XScale, this.YPoint - this.data[i-1])
              this.context.lineTo(this.XPoint + i * this.XScale, this.YPoint - this.data[i])
              this.context.stroke()
            }
          }
        })
    }
    .width('100%')
    .height('100%').gesture(
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