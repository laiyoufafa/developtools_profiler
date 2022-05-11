#!/bin/bash
# Copyright (C) 2021 Huawei Device Co., Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
cp out/macx/trace_streamer prebuilts/dist/trace_streamer_mac
cp out/linux/trace_streamer prebuilts/dist/trace_streamer_linux
cp out/windows/trace_streamer.exe prebuilts/dist/trace_streamer_windows.exe
cp out/wasm/trace_streamer_builtin.js prebuilts/dist/
cp out/wasm/trace_streamer_builtin.wasm prebuilts/dist/
