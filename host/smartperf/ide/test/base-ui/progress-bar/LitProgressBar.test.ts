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

// @ts-ignore
import { LitProgressBar } from '../../../dist/base-ui/progress-bar/LitProgressBar.js';

describe('LitProgressBar Test', () => {
  let litProgressBar = new LitProgressBar();
  litProgressBar.loading = '';
  litProgressBar.loading = 'load';

  it('LitProgressBarTest01', () => {
    expect(litProgressBar.loading).toBeTruthy();
  });

  it('LitProgressBarTest02', () => {
    expect(litProgressBar.initHtml()).toMatchInlineSnapshot(`
"
        <style>
            :host{
                width: 100%;
                height: 1px;
                display: flex;
                position: absolute;
                overflow: hidden;
            }
            .root{
                width: 100%;
                height: 100%;
                position:relative;
            }
            :host([loading]) .track1{
                position: absolute;
                width: 30%;
                height: 100%;
                background-image: linear-gradient(to right,transparent,  #535da6, #535da6, #535da6, #535da6,#535da6,transparent);
                left: -30%;
                animation: anim 1.7s linear 0s infinite;
            }
            :host([loading]) .track2{
                position: absolute;
                width: 30%;
                height: 100%;
                background-image: linear-gradient(to right,transparent,  #535da6, #535da6, #535da6, #535da6,#535da6,transparent);
                left: -30%;
                animation: anim 1.7s  ease-in-out  0.7s infinite;
            }
            @keyframes anim {
              0% {
                left:-30%;
              }

              100% {
                left:100%;
              }
            }

        </style>
        <div class="root">
            <div class="track1"></div>
            <div class="track2"></div>
        </div>
        "
`);
  });
});
