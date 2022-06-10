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
import { StartTestTitleComponent } from '../common/component/StartTestTitleComponent';

/**
 * 关于我们页面
 */
@Entry
@Component
struct AboutMePage {
  build() {
    Column() {
      StartTestTitleComponent({ title: "关于我们" })

      Image($r('app.media.logo')).width('72vp').height('72vp').margin({ top: '56vp' })

      Text('SmartPerf')
        .fontSize('16fp')
        .fontColor($r('app.color.color_333'))
        .width('300vp')
        .height('45vp')
        .textAlign(TextAlign.Center)

      Row({ space: '20vp' }) {
        Flex({ justifyContent: FlexAlign.SpaceBetween, alignItems: ItemAlign.Center }) {
          Column() {

            Text('官方网站').fontSize('16fp').fontColor('#E6000000').alignSelf(ItemAlign.Start)

            Text('https://devecotesting.huawei.com').fontSize('15fp').fontColor('#99000000')
              .alignSelf(ItemAlign.Start)
          }

          Image($r('app.media.icon_enter')).width('15vp').height('15vp').margin({ left: '25vp' })
        }.height('47vp').width('90%').margin({ left: 20 })

      }.height('47vp').width('95%').backgroundColor($r("app.color.color_fff")).margin({ top: '10vp' })

      Divider().layoutWeight(1).visibility(Visibility.Hidden)

      Text('版权所有@2018-2021 华为终端有限公司保留一切权力')
        .fontColor('#000000')
        .fontSize('10fp')
        .margin({ bottom: '25vp' })
        .width('100%')
        .height('40vp')
        .textAlign(TextAlign.Center)
    }
    .width('100%')
    .height('100%')
  }
}