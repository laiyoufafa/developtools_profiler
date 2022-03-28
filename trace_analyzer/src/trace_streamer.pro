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
QT -= gui core
TEMPLATE = app
CONFIG += c++17 console
#CONFIG += c++14
TARGET = trace_streamer

#CONFIG += release

DEFINES += HAVE_PTHREAD
DEFINES += _LIBCPP_DISABLE_AVAILABILITY

DEFINES += HAVE_PTHREAD
ROOTSRCDIR = $$PWD/../

include($$PWD/multi_platform/global.pri)
INCLUDEPATH += $$PWD/include \
    $$PWD/../third_party/protobuf/src \
    $$PWD/../third_party/sqlite/include \
    $$PWD/../third_party/protogen/gen \
    $$PWD/../third_party/protogen/gen/types/plugins/memory_data \
    $$PWD/../third_party/protogen/gen/types/plugins/ftrace_data \
    $$PWD/../third_party/protogen/gen/types/plugins/hilog_data


include($$PWD/trace_streamer/trace_streamer.pri)
include($$PWD/base/base.pri)
include($$PWD/filter/filter.pri)
include($$PWD/parser/parser.pri)
include($$PWD/multi_platform/protogen.pri)
include($$PWD/table/table.pri)
include($$PWD/trace_data/trace_data.pri)
include($$PWD/cfg/cfg.pri)

#LIBS += -lrt -ldl
unix{
LIBS += -L$$DESTDIR/ -lstdc++ \
        -L$${ROOTSRCDIR}/lib/$${DESTFOLDER} -lprotobuf -lsqlite -ldl
} else {
LIBS += -L$$DESTDIR/ -lstdc++ \
        -L$${ROOTSRCDIR}/lib/$${DESTFOLDER} -lprotobuf -lsqlite
}
INCLUDEPATH +=$$PWD/include

SOURCES += \
    main.cpp
