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

#DEFINES +=_AIX
#DEFINES +=_GLIBCXX_BITS_STD_ABS_H
#DEFINES +=__CORRECT_ISO_CPP_STDLIB_H_PROTO
#DEFINES += __CORRECT_ISO_CPP11_MATH_H_PROTO_FP
#DEFINES +=__CORRECT_ISO_CPP_MATH_H_PROTO
#DEFINES +=_GLIBCXX_USE_C99_LONG_LONG_DYNAMIC

INCLUDEPATH +=$${ROOTSRCDIR}/include
INCLUDEPATH +=$${ROOTSRCDIR}/
CONFIG(debug, debug|release){
   BUILDVERSION = _debug
   message("debug")
}else{
   message("release")
   BUILDVERSION =
   DEFINES += NDEBUG
}
contains(QT_ARCH, i386) {
    message("qmake" $$TARGET "32-bit")
    BIT = x32
} else {
    message("qmake" $$TARGET "64-bit")
    BIT =
}
macx{
    PLATFORM = macx
    TOOL =
    BIT =
}
unix:!macx {
    PLATFORM = linux
    TOOL =
    BIT =
}
win32 {
    PLATFORM = windows
    TOOL =
}
DESTFOLDER =$${PLATFORM}$${BIT}$${TOOL}$${BUILDVERSION}
islib{
message("this is for lib")
DESTDIR = $${ROOTSRCDIR}/lib/$${DESTFOLDER}
} else {
message("this is for app")
DESTDIR = $${ROOTSRCDIR}/out/$${DESTFOLDER}
}
unix{
QMAKE_CXXFLAGS += -BigObj
INCLUDEPATH += $$DESTDIR/gen/build_config
INCLUDEPATH += $$DESTDIR/gen
INCLUDEPATH +=/usr/include/c++/7
INCLUDEPATH +=/usr/include/x86_64-linux-gnu/
} else {
INCLUDEPATH += $${GENDIR}/gen/build_config
INCLUDEPATH += $${GENDIR}/gen
DEFINES += WIN32
QMAKE_CXXFLAGS += -BigObj
staticlib{
QMAKE_CXXFLAGS += -Ofast -flto
}
}
OBJECTS_DIR = $${ROOTSRCDIR}/tmp_$${TARGET}_$${DESTFOLDER}
