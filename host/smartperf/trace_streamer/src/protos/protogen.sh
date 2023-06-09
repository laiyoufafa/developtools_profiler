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
SOURCE="${BASH_SOURCE[0]}"
cd $(dirname ${SOURCE})
echo "begin to generate proto based files"
SOURCE=$(dirname ${SOURCE})
proto_dir="."
services_dir="$proto_dir/services"
ftrace_data_dir="$proto_dir/types/plugins/ftrace_data"
memory_data_dir="$proto_dir/types/plugins/memory_data"
hilog_data_dir="$proto_dir/types/plugins/hilog_data"
native_hook_dir="$proto_dir/types/plugins/native_hook"
hidump_data_dir="$proto_dir/types/plugins/hidump_data"
arrayWen=("${services_dir}/common_types.proto"
    "$ftrace_data_dir/trace_plugin_result.proto"
    "$ftrace_data_dir/ftrace_event.proto"
    "$ftrace_data_dir/irq.proto"
    "$ftrace_data_dir/vmscan.proto"
    "$ftrace_data_dir/workqueue.proto"
    "$ftrace_data_dir/task.proto"
    "$ftrace_data_dir/power.proto"
    "$ftrace_data_dir/sched.proto"
    "$ftrace_data_dir/filemap.proto"
    "$ftrace_data_dir/i2c.proto"
    "$ftrace_data_dir/kmem.proto"
    "$ftrace_data_dir/block.proto"
    "$ftrace_data_dir/ipi.proto"
    "$ftrace_data_dir/ftrace.proto"
    "$ftrace_data_dir/ext4.proto"
    "$ftrace_data_dir/oom.proto"
    "$ftrace_data_dir/compaction.proto"
    "$ftrace_data_dir/clk.proto"
    "$ftrace_data_dir/cgroup.proto"
    "$ftrace_data_dir/binder.proto"
    "$ftrace_data_dir/signal.proto"
    "$ftrace_data_dir/sunrpc.proto"
    "$ftrace_data_dir/net.proto"
    "$ftrace_data_dir/cpuhp.proto"
    "$ftrace_data_dir/writeback.proto"
    "$ftrace_data_dir/v4l2.proto"
    "$ftrace_data_dir/pagemap.proto"
    "$ftrace_data_dir/dma_fence.proto"
    "$ftrace_data_dir/printk.proto"
    "$ftrace_data_dir/filelock.proto"
    "$ftrace_data_dir/gpio.proto"
    "$ftrace_data_dir/timer.proto"
    "$ftrace_data_dir/raw_syscalls.proto"
    "$ftrace_data_dir/rcu.proto"
    "$memory_data_dir/memory_plugin_common.proto"
    "$memory_data_dir/memory_plugin_config.proto"
    "$memory_data_dir/memory_plugin_result.proto"
    "$hilog_data_dir/hilog_plugin_result.proto"
    "$native_hook_dir/native_hook_result.proto"
    "$native_hook_dir/native_hook_config.proto"
    "$hidump_data_dir/hidump_plugin_result.proto"
    "$hidump_data_dir/hidump_plugin_config.proto")

export LD_LIBRARY_PATH=../../out/linux
for ((i = 0; i < ${#arrayWen[@]}; i ++))
do
   newpath=$(dirname ${arrayWen[$i]})
   newpath=${newpath:2}
   cppout=../../third_party/protogen/$newpath
   mkdir -p $cppout
   ../../out/linux/protoc --proto_path=$memory_data_dir:$native_hook_dir:$hidump_data_dir:$hilog_data_dir:$ftrace_data_dir:$services_dir --cpp_out=$cppout ${arrayWen[$i]}
done
echo "generate proto based files over"