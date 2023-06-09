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
import deviceInfo from '@ohos.deviceInfo';
import router from '@system.router';
import prompt from '@system.prompt';
import { TopComponent } from '../main/TopComponent';
import { HomeBottomPage } from './HomeBottomPage';
import { otherSupportList, OtherSupport, TestMode } from '../../data/LocalCommon';
import storage from '../../storage/StorageUtils';


/*
  home页
 */
@Component
export struct Home {
  @State private deviceStr: string = ''
  @State private versionStr: string = ''
  @State private circleHeight: string = '180vp'
  @State private circleWidth: string = '180vp'
  @State private circleRadius: string = '90vp'
  @State opacity: number = 0.6

  build() {
    Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Start, justifyContent: FlexAlign.Start }) {
      Column() {
        Column() {
          TopComponent({ title: "SmartPerf " })

          Text('性能/功耗测试')
            .fontSize('18fp')
            .fontColor($r("app.color.color_fff"))
            .textAlign(TextAlign.Start)
            .margin({ top: '20vp' })

          Stack() {
            Circle()
              .width('180vp')
              .height('180vp')
              .fill(Color.White)
              .fillOpacity(0)
              .border({ radius: '90vp', width: '0.5vp', color: $r("app.color.color_fff") })
              .linearGradient({
                angle: 135,
                direction: GradientDirection.Left,
                colors: [[$r("app.color.color_80B3193F"), 0.0], [$r("app.color.color_80fff"), 1.0]]
              })

            Text('开始测试')
              .fontColor($r('app.color.color_fff'))
              .fontSize('20fp')

            Column() {
            }.width('220vp')
            .height('220vp')

            Flex({ justifyContent: FlexAlign.Center, alignItems: ItemAlign.Center }) {
            }
            .width(this.circleWidth)
            .height(this.circleHeight)
            .border({ width: '1vp', radius: this.circleRadius, color: $r("app.color.color_80fff") })
            .opacity(this.opacity)
            .animation({
              duration: 1000,
              iterations: -1,
              curve: Curve.FastOutLinearIn })
            .onAppear(() => {
              this.circleWidth = '220vp'
              this.circleHeight = '220vp'
              this.circleRadius = '100vp'
              this.opacity = 0
            })
          }.onClick(() => {

            if(!globalThis.showFloatingWindow){
              router.push({ uri: 'pages/StartTestPage' })
            }else{
              prompt.showToast({ message: "已经有应用正在测试，请关闭测试再进行操作", duration: 1000 })
            }





          })

          Text(this.deviceStr)
            .fontSize('25fp')
            .fontColor($r("app.color.color_fff"))
            .fontWeight(FontWeight.Bold)
            .textAlign(TextAlign.Start)
            .margin({ top: '20vp', bottom: '40vp' })
        }.backgroundColor($r('app.color.colorPrimary')).width('100%')
        .alignItems(HorizontalAlign.Center)
        .onAppear(() => {
          this.deviceStr = deviceInfo.brand + " " + deviceInfo.productModel
          storage.getSyncValue("store", "userName").then((data) => {
            console.log("console: " + data)
          })
        })

        GridExample()
      }
    }.width('100%').height('100%')
  }
}

@Component
struct GridExample {
  @State support: Array<OtherSupport> = otherSupportList

  build() {
    Column({ space: 5 }) {
      Grid() {
        ForEach(this.support, (otherSupport) => {
          GridItem() {
            Column() {
              Image(otherSupport.resource).width(60)
                .height(60)
              Text(otherSupport.testName)
                .fontSize('13fp')
                .fontColor($r("app.color.color_333"))
                .height('30%')
                .textAlign(TextAlign.Center)

              Text(otherSupport.testSrc)
                .fontSize('10fp')
                .fontColor($r("app.color.color_999"))
                .height('30%')
                .textAlign(TextAlign.Center)
            }

          }
          .backgroundColor($r("app.color.color_fff"))
          .width('100%')
          .margin({ top: '10vp' })
          .padding({ top: '8vp', right: '20vp', bottom: '8vp', left: '20vp' })
          .onClick(() => {
            if (otherSupport.testMode == TestMode.ONLINE) {
              prompt.showToast({ message: "开发中" })
            } else if (otherSupport.testMode == TestMode.BRIGHTNESS) {
              router.push({ uri: 'pages/LightAdjust' })
            }
          })
        }, otherSupport => otherSupport.testName)
      }
      .columnsTemplate('1fr 1fr 1fr')
      .rowsTemplate('1fr 1fr 1fr')
      .width('100%')
      .height(450)
    }.width('100%').height('100%').backgroundColor('#EEEEEE')
  }
}
