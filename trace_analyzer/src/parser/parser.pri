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

INCLUDEPATH += $$PWD \
    $$PWD/bytrace_parser \
    $$PWD/htrace_parser/htrace_cpu_parser \
    $$PWD/htrace_parser/htrace_event_parser \
    $$PWD/htrace_parser/htrace_symbol_parser
HEADERS += \
    $$PWD/common_types.h \
    $$PWD/event_parser_base.h \
    $$PWD/thread_state.h \
    $$PWD/bytrace_parser/bytrace_parser.h \
    $$PWD/bytrace_parser/bytrace_event_parser.h \
    $$PWD/htrace_parser/htrace_cpu_parser/htrace_cpu_detail_parser.h \
    $$PWD/htrace_parser/htrace_event_parser/htrace_event_parser.h \
    $$PWD/htrace_parser/htrace_parser.h \
    $$PWD/htrace_parser/htrace_mem_parser.h \
    $$PWD/htrace_parser/htrace_clock_detail_parser.h \
    $$PWD/htrace_parser/htrace_symbols_detail_parser.h

SOURCES += \
    $$PWD/parser_base.cpp \
    $$PWD/event_parser_base.cpp \
    $$PWD/thread_state.cpp \
    $$PWD/bytrace_parser/bytrace_parser.cpp \
    $$PWD/bytrace_parser/bytrace_event_parser.cpp \
    $$PWD/htrace_parser/htrace_cpu_parser/htrace_cpu_detail_parser.cpp \
    $$PWD/htrace_parser/htrace_event_parser/htrace_event_parser.cpp \
    $$PWD/htrace_parser/htrace_parser.cpp \
    $$PWD/htrace_parser/htrace_mem_parser.cpp \
    $$PWD/htrace_parser/htrace_clock_detail_parser.cpp \
    $$PWD/htrace_parser/htrace_symbols_detail_parser.cpp
