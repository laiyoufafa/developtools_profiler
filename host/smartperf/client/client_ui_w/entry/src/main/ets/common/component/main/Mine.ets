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
import router from '@system.router';
import { TopComponent } from '../main/TopComponent';
import { secToTime } from '../../utils/TimeUtils';

/**
 * 我的页面
 */
@Component
export struct Mine {
  build() {
    Column({ space: '15vp' }) {
      TopComponent({ title: "我的" })

      Image($r('app.media.person'))
        .width('60vp')
        .height('60vp')
        .margin({ top: '30vp' })

      Text('test')
        .height('20vp')
        .fontSize('14vp')
        .fontColor(0xeeeeee)
        .alignSelf(ItemAlign.Center)

      Flex({ direction: FlexDirection.Row, justifyContent: FlexAlign.SpaceAround }) {
        Column() {
          Image($r('app.media.test_apps_count'))
            .width('40vp')
            .height('40vp')

          Text('测试总app数')
            .margin('20vp')
            .fontColor(0xeeeeee)

          Text(String(globalThis.sumTestApp))
            .fontColor(0xeeeeee)
        }

        Column() {
          Image($r('app.media.test_session_count'))
            .width('40vp')
            .height('40vp')

          Text('测试总局数')
            .margin('20vp')
            .fontColor(0xeeeeee)

          Text(String(globalThis.sumTest))
            .fontColor(0xeeeeee)
        }

        Column() {
          Image($r('app.media.test_times_count'))
            .width('40vp')
            .height('40vp')

          Text('测试总时长')
            .margin('20vp')
            .fontColor(0xeeeeee)

          Text(secToTime(globalThis.sumTestTime).toString())
            .fontColor(0xeeeeee)
        }

      }.margin({ top: '15vp' })

      Column({ space: '10vp' }) {
        Row() {
          Image($r('app.media.icon_about_we'))
            .width('20vp').height('20vp')
            .margin({ left: '10vp' })

          Text('关于我们')
            .margin('5vp')
        }
        .width('100%')
        .backgroundColor(0xeeeeee)
        .height('35vp')
        .borderRadius('5vp')
        .onClick(
          res => {
            router.push({ uri: 'pages/AboutMePage' })
          }
        )

        Row() {
          Image($r('app.media.question')).width('20vp').height('20vp').margin({ left: '10vp' })

          Text('常见问题').margin('5vp')

        }
        .width('100%')
        .backgroundColor(0xeeeeee)
        .height('35vp')
        .borderRadius('5vp')
        .onClick(
          res => {
            router.push({ uri: 'pages/Question' })
          }
        )

        Row() {
          Image($r('app.media.settings'))
            .width('20vp')
            .height('20vp')
            .margin({ left: '10vp' })

          Text('设置').margin('5vp')
        }
        .width('100%')
        .height('35vp')
        .backgroundColor(0xeeeeee)
        .borderRadius('5vp')
        .onClick(
          res => {
            router.push({ uri: 'pages/SettingsPage' })
          }
        )
      }.width('95%')

    }.backgroundColor($r('app.color.colorPrimary'))
    .width('100%')
    .height('100%')
  }
}
