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
PARAMS=$*
echo $PARAMS
echo "begin to check input"
target_os='linux'
is_debug='false'
is_clean='false'
if [ "$#" -ne "0" ];then
    if [ $1 == "wasm" ];then
        if [ ! -d "prebuilts/emsdk" ];then
            echo "you need emsdk to compile wasm"
            mv emsdk.tar.gz prebuilts/
            mv ../emsdk.tar.gz prebuilts/
            if [ ! -f "prebuilts/emsdk.tar.gz" ];then
                # consider <compile_trace_streamer.md>
                # you need to get emsdk.tar.gz some where
                mv emsdk.tar.gz prebuilts/
                tar -zxvf prebuilts/emsdk.tar.gz -C prebuilts/
            else
                tar -zxvf prebuilts/emsdk.tar.gz -C prebuilts/
            fi
        fi
        target_os='wasm'
    fi
    if [ $1 == "test" ];then
        target_os='test'
    fi
fi
if [ "$#" -eq "2" ];then
    if [ "$1" != 'windows' ] && [ $1 != "linux" ] && [ $1 != "wasm" ] && [ $1 != "test" ];then
	echo "failed"
    	echo "Usage: `basename $0` windows/linux/wasm/test debug/release/clean"
	exit
    fi
    if [ $2 != "debug" -a $2 != "release" -a $2 != "clean" ];then
	echo "failed"
    	echo "Usage: `basename $0` windows/linux debug/release/clean"
	exit
    fi
    if [ $2 == "debug" ];then
	is_debug='true'
    elif [ $2 == "clean" ];then
	is_clean='true'
    else
	is_debug='false'
    fi
    target_os=$1
    if [ $target_os == "windows" ];then
        echo "gn only support linux and wasm build currently"
        mkdir out/windows
        touch out/windows/trace_streamer.exe
        exit
    fi
    echo "platform is $target_os"
    echo "isdebug: $is_debug"
else
    echo "Usage: `basename $0` windows/linux/wasm debug/release wasm[optional]"
    echo "You provided $# parameters,but 2 are required."
    echo "use default input paramter"
    echo "platform is $target_os"
    echo "is_debug:$is_debug"
fi
echo "gen ..."
ext=""
if [ "$is_debug" != 'false' ];then
       	ext="_debug"
fi
#exec "protogen.sh"
echo "the output file will be at ""$prefix""$target_os"
echo ""
echo ""
echo "-------------tips-------------"
echo ""
echo "if you are compiling first time, or your proto has changed, you need to run ./src/protos/protogen.sh"
echo ""
echo ""
echo 
#./src/protos/protogen.sh
mkdir prebuilts/$target_os
if [ ! -f "prebuilts/$target_os/gn" ];then
	echo "you may get gn for $target_os and place it in prebuilts/$target_os"
	ehco "the file can be get at https://gitee.com/su_fu/public_tools/raw/master/gn/$target_os/gn, you need to download it manually"
    #wget https://gitee.com/su_fu/public_tools/raw/master/gn/$target_os/gn
    #mv gn prebuilts/$target_os/
    #chmod +x prebuilts/$target_os/gn
	exit
fi
if [ ! -f "prebuilts/$target_os/ninja" ];then
	echo "you may get ninja for $target_os and place it in prebuilts/$target_os"
	ehco "the file can be get at https://gitee.com/su_fu/public_tools/raw/master/gn/$target_os/ninja, you need to download it manually"
    #wget "https://gitee.com/su_fu/public_tools/raw/master/gn/$target_os/ninja"
	#wget https://gitee.com/su_fu/public_tools/raw/master/gn/$target_os/ninja
    #mv ninja prebuilts/$target_os/
    #chmod +x prebuilts/$target_os/*
	exit
fi
echo "$is_clean"
if [ "$is_clean" == 'true'  ];then
    prebuilts/$target_os/gn gen out/"$target_os""$ext" --clean
    prebuilts/$target_os/ninja -C out/"$target_os""$ext" -t clean
else
    prebuilts/$target_os/gn gen out/"$target_os""$ext" --args='is_debug='"$is_debug"' target_os="'"$target_os"'"'
    echo "begin to build ..."
    mkdir -p out/windows
    touch out/windows/trace_streamer.exe
    prebuilts/$target_os/ninja -v -C out/"$target_os""$ext"
fi
