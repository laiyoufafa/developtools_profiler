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
set -e
gn_path="$1"
if [ ! -d "tools" ];then
    mkdir tools
fi
cd tools
git clone git@gitee.com:su_ze1688/public_tools.git
cd ..
mv tools/public_tools/gn/$gn_path/gn prebuilts/$gn_path
mv tools/public_tools/gn/$gn_path/ninja prebuilts/$gn_path/ninja
rm -rf tools