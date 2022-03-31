#!/bin/bash
# Copyright (c) 2021 Huawei Device Co., Ltd.
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

THIS_DIR=$(dirname ${BASH_SOURCE[0]})
PROJECT_TOP=$(realpath $THIS_DIR/../../..)

OHOS_X64_OUT=$PROJECT_TOP/$2/
LIBCXX_X64_OUT=$PROJECT_TOP/$1/ndk/libcxx/linux_x86_64
SUBSYS_X64_OUT=$PROJECT_TOP/$2/developtools/profiler
PROTOC=$PROJECT_TOP/$2/developtools/profiler/protoc

echo "1 = $1"
echo "2 = $2"
PARAMS=$*
PARAMS_FILTER="$1 $2"
echo "EXEC: LD_LIBRARY_PATH=$LIBCXX_X64_OUT:$SUBSYS_X64_OUT $PROTOC ${PARAMS[@]:${#PARAMS_FILTER}}"
LD_LIBRARY_PATH=$LIBCXX_X64_OUT:$SUBSYS_X64_OUT exec $PROTOC ${PARAMS[@]:${#PARAMS_FILTER}}
