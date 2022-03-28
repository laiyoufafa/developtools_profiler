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

INCLUDEPATH +=$$PWD \
              $$PWD/../cfg
HEADERS += \
    $$PWD/clock_filter.h \
    $$PWD/cpu_filter.h \
    $$PWD/filter_base.h \
    $$PWD/filter_filter.h \
    $$PWD/measure_filter.h \
    $$PWD/process_filter.h \
    $$PWD/slice_filter.h \
    $$PWD/symbols_filter.h \
    $$PWD/stat_filter.h \
    $$PWD/binder_filter.h \
    $$PWD/args_filter.h \
    $$PWD/irq_filter.h \
    $$PWD/system_event_measure_filter.h
SOURCES += \
    $$PWD/clock_filter.cpp \
    $$PWD/cpu_filter.cpp \
    $$PWD/filter_base.cpp \
    $$PWD/filter_filter.cpp \
    $$PWD/measure_filter.cpp \
    $$PWD/process_filter.cpp \
    $$PWD/slice_filter.cpp \
    $$PWD/symbols_filter.cpp \
    $$PWD/stat_filter.cpp \
    $$PWD/binder_filter.cpp \
    $$PWD/args_filter.cpp \
    $$PWD/irq_filter.cpp \
    $$PWD/system_event_measure_filter.cpp
