/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef GP_CONSTANT_H
#define GP_CONSTANT_H
enum NumberConstant {
  ZERO = 0X00,
  ONE = 0X01,
  TWO = 0X02,
  THREE = 0X03
};
enum FunConstant {
  SUCCESS_ONE = 0X01,
  SUCCESS_ZERO = 0X00,
  ERROR_ZERO = 0X00,
  ERROR_MINUX = 0xFFFFFFFF
};
#endif