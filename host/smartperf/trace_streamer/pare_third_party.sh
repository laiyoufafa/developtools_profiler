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
patch='patch'
sed='sed'
cp='cp'

if [ ! -d "third_party" ];then
    mkdir third_party
fi
cd third_party

if [ ! -f "sqlite/BUILD.gn" ];then
    rm -rf sqlite
    git clone git@gitee.com:openharmony/third_party_sqlite.git
    if [ -d "third_party_sqlite" ];then
        mv third_party_sqlite sqlite
        $cp ../prebuilts/patch_sqlite/sqlite3build.gn ../third_party/sqlite/BUILD.gn 
    else
        echo 'third_party_sqlite not exist'
    fi
fi
if [ ! -f "protobuf/BUILD.gn" ];then
    rm -rf protobuf
    git clone git@gitee.com:openharmony/third_party_protobuf.git
    if [ -d "third_party_protobuf" ];then
        mv third_party_protobuf protobuf
        $cp ../prebuilts/patch_protobuf/protobufbuild.gn ../third_party/protobuf/BUILD.gn
    else
        echo 'third_party_protobuf not exist'
    fi
fi

if [ ! -f "googletest/BUILD.gn" ];then
    rm -rf googletest
    git clone git@gitee.com:openharmony/third_party_googletest.git
    if [ -d "third_party_googletest" ];then
        mv third_party_googletest googletest
        $cp ../prebuilts/patch_googletest/googletestbuild.gn ../third_party/googletest/BUILD.gn
        $patch -p0 ../third_party/googletest/googletest/include/gtest/internal/gtest-internal.h ../prebuilts/patch_googletest/gtest_internal.h.patch
        $patch -p0 ../third_party/googletest/googletest/include/gtest/internal/gtest-port.h ../prebuilts/patch_googletest/gtest_port.h.patch
        $patch -p0 ../third_party/googletest/googletest/include/gtest/gtest-message.h ../prebuilts/patch_googletest/gtest-message.h.patch
        $sed -i "/using ::std::string/s/^\(.*\)$/\/\/\1/g" ../third_party/googletest/googletest/include/gtest/hwext/gtest-tag.h

    else
        echo 'third_party_googletest not exist'
    fi
fi

if [ ! -f "json-master/BUILD.gn" ];then
    rm -rf json-master
    git clone git@gitee.com:openharmony/third_party_json.git
    if [ -d "third_party_json" ];then
        mv third_party_json json-master
    else
        echo 'third_party_json not exist'
    fi
fi

if [ ! -f "libunwind/BUILD.gn" ];then
    rm -rf libunwind
    git clone git@gitee.com:openharmony/third_party_libunwind.git
    if [ -d "third_party_libunwind" ];then
        mv third_party_libunwind libunwind
        $cp ../prebuilts/patch_libunwind/libunwindbuild.gn libunwind/BUILD.gn
    else
        echo 'third_party_libunwind not exist'
    fi
fi

if [ ! -f "perf_include/libbpf/linux/perf_event.h" ];then
   mkdir -p perf_include/libbpf/linux
   rm -rf perf_event.h
   curl https://gitee.com/openharmony/third_party_libbpf/raw/master/include/uapi/linux/perf_event.h > perf_event.h
   mv perf_event.h perf_include/libbpf/linux/perf_event.h
   $patch -p0 perf_include/libbpf/linux/perf_event.h ../prebuilts/patch_perf_event/perf_event.h.patch
fi

if [ ! -f "perf_include/musl/elf.h" ];then
   mkdir -p perf_include/musl
   rm -rf elf.h
   curl https://gitee.com/openharmony/third_party_musl/raw/master/include/elf.h > elf.h
   mv elf.h perf_include/musl/elf.h
fi