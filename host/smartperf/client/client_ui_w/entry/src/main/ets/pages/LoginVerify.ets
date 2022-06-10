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
import prompt from '@system.prompt';


@Entry
@Component
struct W3Login {
  @State private tips: string= '短信验证码将发送到以下手机号'
  @State private title: string = '手机号验证'
  @State private account: string = '15535456915'
  private password: string = ''
  @State private isSending: boolean= false
  @State private timeSends: number= 10
  //手机0 邮箱1 身份验证码2
  @State private curType: number= 0
  private catchIntervalId: number= null

  build() {
    Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Center, justifyContent: FlexAlign.Start }) {
      Row() {
        //开始测试title
        Row({ space: '15vp' }) {
          Image($r('app.media.icon_back'))
            .width('25vp')
            .height('15%')
            .margin({ left: '5%', top: '50vp' })
            .alignSelf(ItemAlign.Center)
            .onClick(() => {
              router.back()
            })
          Text(this.title)
            .fontSize('20fp')
            .fontWeight(FontWeight.Bold)
            .fontColor($r("app.color.color_fff"))
            .margin({ top: '50vp' })
        }.backgroundColor($r("app.color.colorPrimary")).width('100%').height('12%')
      }

      Row() {
        Text(this.tips).align(Alignment.Start).fontSize(12).margin({ top: 20, left: 10 }).fontColor('#AAAAAA')
      }.width('100%')


      Row() {
        if (this.curType != 2) {
          TextInput({ placeholder: '155****6915', text: this.account })
            .type(InputType.Normal)
            .width('80%').height('50vp').margin({ top: '20vp' })
        }
      }

      Row() {
        TextInput({ placeholder: '请输入验证码', text: this.password }).width('70%').height('50vp')
          .onChange((value) => {
            this.password = value
          })
        if (!this.isSending && this.timeSends == 10) {
          Button('发送验证码')
            .backgroundColor($r("app.color.colorPrimary")).onClick(
            res => {
              prompt.showToast({ message: "发送验证码", duration: 2000 })
              this.isSending = true
              var that = this
              this.catchIntervalId = setInterval(function () {
                that.timeSends = that.timeSends - 1
                console.log('cur time :' + that.timeSends);
                if (that.timeSends == 0) {
                  that.isSending = false
                  that.timeSends = 10
                  if (that.catchIntervalId != null) {
                    clearInterval(that.catchIntervalId)
                  }
                }
              }, 1000)
            }
          ).width('30%')
        } else {
          Button(this.timeSends + 's')
            .backgroundColor($r("app.color.colorPrimary"))
            .width('30%')
        }

      }.margin({ top: '15vp' }).width('80%')

      Text('确认')
        .fontSize('15fp')
        .fontColor($r('app.color.color_fff'))
        .margin({ top: '15vp' })
        .border({ radius: '20vp' })
        .backgroundColor($r("app.color.colorPrimary"))
        .width('80%')
        .height('40vp')
        .textAlign(TextAlign.Center)
        .onClick(() => {
          router.push({ uri: 'pages/MainPage', params: {
            title: "123"
          } })
        })

      Row() {

        Text('手机号验证') {
          if (this.curType == 0) {
            Span('手机号验证').fontSize(12).textCase(TextCase.Normal).fontColor(Color.Red)
          }
        }
        .align(Alignment.Start).fontSize(12).margin(10).onClick(res => {
          this.tips = '手机号验证即将发送到以下邮箱'
          this.title = '手机号验证'
          this.account = '15535456915'
          this.curType = 0
        })

        Divider().vertical(true).margin(5).height(15)
        Text('邮箱验证') {
          if (this.curType == 1) {
            Span('邮箱验证').fontSize(12).textCase(TextCase.Normal).fontColor(Color.Red)
          }
        }
        .align(Alignment.Start).fontSize(12).margin(10).onClick(res => {
          this.tips = '邮箱验证码即将发送到以下邮箱'
          this.title = '邮箱验证'
          this.account = 'lixin4126@qq.com'
          this.curType = 1
        })

        Divider().vertical(true).margin(5).height(15)
        Text('身份识别码验证') {
          if (this.curType == 2) {
            Span('身份识别码验证').fontSize(12).textCase(TextCase.Normal).fontColor(Color.Red)
          }
        }
        .align(Alignment.Start).fontSize(12).margin(10).onClick(res => {
          this.tips = '请输入身份识别码'
          this.title = '身份识别码验证'
          this.account = ''
          this.curType = 2
        })
      }

      Column() {
        Text('提示：根据公司安全策略要求,需要自行进行身份验证').fontSize('10fp').margin({ top: 20 }).fontColor('#C0C0C0')
        Text('提示：如果您没有有效的验证方式,请联系12345客服联盟去设置')
          .fontSize('10fp').margin({ top: 200 }).fontColor('#C0C0C0')
      }
    }
    .width('100%')
    .height('100%')
  }
}
