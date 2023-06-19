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
rm -rf out/test
./build.sh test
./test.sh
cp -R out/test/obj/src/parser/htrace_pbreader_parser htrace_pbreader_parser
cp out/test/obj/src/parser/htrace_pbreader_parser_src.parser_base.gcda .
cp out/test/obj/src/parser/htrace_pbreader_parser_src.parser_base.gcno .
./build.sh testpb
./test_pbdecoder.sh
rm -rf out/test/obj/src/parser/htrace_pbreader_parser
cp -R htrace_pbreader_parser out/test/obj/src/parser
cp htrace_pbreader_parser_src.parser_base.gcda out/test/obj/src/parser
cp htrace_pbreader_parser_src.parser_base.gcno out/test/obj/src/parser
rm -rf htrace_pbreader_parser
rm htrace_pbreader_parser_src.parser_base.gcda
rm htrace_pbreader_parser_src.parser_base.gcno
./lcov_operator.sh
