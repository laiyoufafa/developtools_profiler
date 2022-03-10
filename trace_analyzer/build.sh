#! /bin/bash

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

echo "begin to check input"
target_os='linux'
is_debug='false'
if [ "$#" -eq "2" ];then
    if [ "$1" != 'windows' ] && [ "$1" != "linux" ];then
	echo "failed"
    	echo "Usage: `basename $0` windows/linux debug/release"
	exit
    fi
    if [ "$2" != "debug" -a "$2" != "release" ];then
	echo "failed"
    	echo "Usage: `basename $0` windows/linux debug/release"
	exit
    fi
    if [ $2 == "debug" ];then
	is_debug='true'
    else
	is_debug='false'
    fi
    target_os=$1
    echo "platform is $target_os"
    echo "isdebug: $is_debug"
else
    echo "Usage: `basename $0` windows/linux debug/release"
    echo "You provided $# parameters,but 2 are required."
    echo "use default input paramter"
    echo "platform is $target_os"
    echo "is_debug:$is_debug"
fi
echo "gen ..."
ext=""
if [ "$is_debug" != 'false'  ];then
       	ext="_debug"
fi
echo "the output file will be at ""$prefix""$target_os"
buildtools/linux64/gn gen out/"$target_os""$ext" --args='is_debug='"$is_debug"' target_os="'"$target_os"'"'
echo "begin to build ..."
touch out/windows/trace_streamer.exe
target_os='linux'
buildtools/linux64/ninja -C out/"$target_os""$ext"
