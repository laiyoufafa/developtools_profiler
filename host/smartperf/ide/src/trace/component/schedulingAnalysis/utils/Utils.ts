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

import {Utils} from "../../trace/base/Utils.js";

export const getFormatData=(data:Array<any>)=>{
    let arrData:Array<any> = [];
    data.forEach((item,idx)=>{
        arrData.push({
            index:idx+1,
            ...item,
            avg:Utils.getProbablyTime(item.avg),
            max:Utils.getProbablyTime(item.max),
            min:Utils.getProbablyTime(item.min),
            sum:Utils.getProbablyTime(item.sum),
        })
    })
    return arrData;
};

export const getDataNo=(data:Array<any>)=>{
    let arrData:Array<any> = [];
    data.forEach((item,idx)=>{
        arrData.push({
            index:idx+1,
            ...item,
        })
    })
    return arrData;
};
