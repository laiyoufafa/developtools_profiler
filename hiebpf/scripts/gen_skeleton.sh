#!/bin/bash

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
# limitations under the License

CWD=$(pwd)
BASE_DIR=${CWD}"/"$1
DEST_DIR=${CWD}"/../../"$2

echo ${BASE_DIR}
echo ${DEST_DIR}

if [[ ! -d "${BASE_DIR}" ]]
then
    echo ${BASE_DIR}": no such directory"
    exit -1
fi

if [[ ! -d "${DEST_DIR}" ]]
then
    echo ${DEST_DIR}": no such directory"
    exit -1
fi

BPF_OBJ_SETS=`ls ${BASE_DIR} | grep '\.bpf'`
echo "bpf object directories: "${BPF_OBJ_SETS}

for obj_set in ${BPF_OBJ_SETS}
do 
    echo "current bpf object directory name: "${obj_set}
    BPF_OBJ_DIR=${BASE_DIR}"/"${obj_set}
    echo "current bpf object directory path: "${BPF_OBJ_DIR}
    BPF_OBJECTS=`ls ${BPF_OBJ_DIR} | grep '\.bpf\.o'`
    echo "bpf object file names: "${BPF_OBJECTS}
    for obj in ${BPF_OBJECTS}
    do
        echo "current bpf object file name: "${obj}
        OBJ_PATH=${BPF_OBJ_DIR}"/"${obj}
        echo "current bpf object file path: "${OBJ_PATH}
        SKEL_NAME=${obj/bpf.o/skel.h}
        echo "current skeleton name: "${SKEL_NAME}
        SKEL_PATH=${DEST_DIR}"/"${SKEL_NAME}
        echo "current skeleton path: "${SKEL_PATH}
        bpftool gen skeleton ${OBJ_PATH} > ${SKEL_PATH}
        replacement="#include \"libbpf.h\""
        sed -i "s/#include <bpf\/libbpf.h>/${replacement}/g" ${SKEL_PATH}
    done
done