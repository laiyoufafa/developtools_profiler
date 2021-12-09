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
PROTOGEN = $$PWD/../../third_party/protogen
INCLUDEPATH += $${PROTOGEN}/types/plugins/ftrace_data \
        $${PROTOGEN}/types/plugins/memory_data \
        $${PROTOGEN}
SOURCES +=$${PROTOGEN}/services/common_types.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/trace_plugin_result.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/ftrace_event.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/irq.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/vmscan.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/workqueue.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/task.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/power.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/sched.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/filemap.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/i2c.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/kmem.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/block.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/ipi.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/ftrace.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/ext4.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/oom.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/compaction.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/clk.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/cgroup.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/binder.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/signal.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/sunrpc.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/net.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/cpuhp.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/writeback.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/v4l2.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/pagemap.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/dma_fence.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/printk.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/filelock.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/gpio.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/timer.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/raw_syscalls.pb.cc \
    $${PROTOGEN}/types/plugins/ftrace_data/rcu.pb.cc \
    \
    $${PROTOGEN}/types/plugins/memory_data/memory_plugin_common.pb.cc \
    $${PROTOGEN}/types/plugins/memory_data/memory_plugin_config.pb.cc \
    $${PROTOGEN}/types/plugins/memory_data/memory_plugin_result.pb.cc \
    $${PROTOGEN}/types/plugins/hilog_data/hilog_plugin_result.pb.cc

HEADERS += $${PROTOGEN}/services/common_types.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/trace_plugin_result.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/ftrace_event.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/irq.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/vmscan.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/workqueue.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/task.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/power.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/sched.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/filemap.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/i2c.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/kmem.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/block.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/ipi.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/ftrace.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/ext4.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/oom.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/compaction.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/clk.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/cgroup.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/signal.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/binder.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/net.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/v4l2.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/writeback.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/cpuhp.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/pagemap.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/dma_fence.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/printk.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/filelock.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/gpio.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/timer.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/raw_syscalls.pb.h \
    $${PROTOGEN}/types/plugins/ftrace_data/rcu.pb.h \
    \
    $${PROTOGEN}/types/plugins/memory_data/memory_plugin_common.pb.h \
    $${PROTOGEN}/types/plugins/memory_data/memory_plugin_config.pb.h \
    $${PROTOGEN}/types/plugins/memory_data/memory_plugin_result.pb.h \
    $${PROTOGEN}/types/plugins/hilog_data/hilog_plugin_result.pb.h
