/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "network_plugin.h"
#include "buffer_splitter.h"
#include "securec.h"

#include <string>

namespace {
constexpr size_t READ_BUFFER_SIZE = 1024 * 16;
const std::string DEFAULT_NET_PATH("/proc/net/xt_qtaguid/stats");
} // namespace

NetworkPlugin::NetworkPlugin()
    : buffer_(new (std::nothrow) uint8_t[READ_BUFFER_SIZE]), fp_(nullptr, nullptr)
{
    pidUid_.clear();
}

int NetworkPlugin::Start(const uint8_t* configData, uint32_t configSize)
{
    CHECK_NOTNULL(buffer_, -1, "%s:NetworkPlugin, buffer_ is null", __func__);

    if (protoConfig_.ParseFromArray(configData, configSize) <= 0) {
        HILOG_ERROR(LOG_CORE, "%s:NetworkPlugin, parseFromArray failed!", __func__);
        return -1;
    }

    for (int i = 0; i < protoConfig_.pid().size(); i++) {
        int32_t pid = protoConfig_.pid(i);
        pidUid_.emplace(pid, GetUid(pid));
    }

    HILOG_INFO(LOG_CORE, "%s:NetworkPlugin, start success!", __func__);

    return 0;
}

int NetworkPlugin::Report(uint8_t* data, uint32_t dataSize)
{
    NetworkDatas dataProto;
    std::string file = GetRateNodePath();
    if (protoConfig_.test_file() != "") {
        file = protoConfig_.test_file();
    }

    char realPath[PATH_MAX + 1] = {0};
    if ((file.length() >= PATH_MAX) || (realpath(file.c_str(), realPath) == nullptr)) {
        HILOG_ERROR(LOG_CORE, "%s:path is invalid: %s, errno=%d", __func__, file.c_str(), errno);
        return -1;
    }
    fp_ = std::unique_ptr<FILE, int (*)(FILE*)>(fopen(realPath, "r"), fclose);
    CHECK_NOTNULL(fp_, -1, "%s:NetworkPlugin, open(%s) Failed, errno(%d)", __func__, file.c_str(), errno);

    if (protoConfig_.pid().size() > 0) {
        for (int i = 0; i < protoConfig_.pid().size(); i++) {
            auto* info = dataProto.add_networkinfo();
            int32_t pid = protoConfig_.pid(i);
            NetworkCell dataCell = {0};
            ReadTxRxBytes(pid, dataCell);
            // set proto
            for (auto& it : dataCell.details) {
                NetworkDetails* data = info->add_details();
                data->set_tx_bytes(it.tx);
                data->set_rx_bytes(it.rx);
                data->set_type(it.type);
            }
            info->set_pid(pid);
            info->set_tx_bytes(dataCell.tx);
            info->set_rx_bytes(dataCell.rx);
            info->set_tv_sec(dataCell.ts.tv_sec);
            info->set_tv_nsec(dataCell.ts.tv_nsec);
        }
    } else if (protoConfig_.test_file() != "") { // test data
        NetSystemData systemData = {};
        ReadSystemTxRxBytes(systemData);
        static int randNum = 0;
        randNum++;
        auto* systemInfo = dataProto.mutable_network_system_info();
        for (auto& it : systemData.details) {
            NetworkSystemDetails* data = systemInfo->add_details();
            data->set_rx_bytes(it.rx_bytes + randNum * RX_BYTES_INDEX);
            data->set_rx_packets(it.rx_packets + randNum * RX_PACKETS_INDEX);
            data->set_tx_bytes(it.tx_bytes + randNum * TX_BYTES_INDEX);
            data->set_tx_packets(it.tx_packets + randNum * TX_PACKETS_INDEX);
            data->set_type(it.type);
        }
        systemInfo->set_tv_sec(systemData.ts.tv_sec);
        systemInfo->set_tv_nsec(systemData.ts.tv_nsec);
        systemInfo->set_rx_bytes(systemData.rx_bytes + (randNum * RX_BYTES_INDEX * systemData.details.size()));
        systemInfo->set_rx_packets(systemData.rx_packets + (randNum * RX_PACKETS_INDEX * systemData.details.size()));
        systemInfo->set_tx_bytes(systemData.tx_bytes + (randNum * TX_BYTES_INDEX * systemData.details.size()));
        systemInfo->set_tx_packets(systemData.tx_packets + (randNum * TX_PACKETS_INDEX * systemData.details.size()));
    } else { // real data
        NetSystemData systemData = {};
        ReadSystemTxRxBytes(systemData);
        auto* systemInfo = dataProto.mutable_network_system_info();
        for (auto& it : systemData.details) {
            NetworkSystemDetails* data = systemInfo->add_details();
            data->set_rx_bytes(it.rx_bytes);
            data->set_rx_packets(it.rx_packets);
            data->set_tx_bytes(it.tx_bytes);
            data->set_tx_packets(it.tx_packets);
            data->set_type(it.type);
        }
        systemInfo->set_tv_sec(systemData.ts.tv_sec);
        systemInfo->set_tv_nsec(systemData.ts.tv_nsec);
        systemInfo->set_rx_bytes(systemData.rx_bytes);
        systemInfo->set_rx_packets(systemData.rx_packets);
        systemInfo->set_tx_bytes(systemData.tx_bytes);
        systemInfo->set_tx_packets(systemData.tx_packets);
    }

    uint32_t length = dataProto.ByteSizeLong();
    if (length > dataSize) {
        return -length;
    }
    if (dataProto.SerializeToArray(data, length) > 0) {
        return length;
    }
    return 0;
}

int NetworkPlugin::Stop()
{
    buffer_ = nullptr;
    fp_ = nullptr;
    pidUid_.clear();

    HILOG_INFO(LOG_CORE, "%s:NetworkPlugin, stop success!", __func__);
    return 0;
}

std::string NetworkPlugin::GetRateNodePath()
{
    std::string name = "";

    if (!fileForTest_.empty()) {
        name = fileForTest_ + DEFAULT_NET_PATH;
        return name;
    }
    if (access(DEFAULT_NET_PATH.c_str(), F_OK) == 0) {
        name = DEFAULT_NET_PATH;
    }
    return name;
}

int32_t NetworkPlugin::GetUid(int32_t pid)
{
    if (pid <= 0) {
        HILOG_ERROR(LOG_CORE, "%s:NetworkPlugin, check param fail, pid less than 0!", __func__);
        return -1;
    }

    char* end = nullptr;
    std::string path = std::string("/proc/") + std::to_string(pid) + std::string("/status");
    if (!fileForTest_.empty()) {
        path = fileForTest_ + std::string("/proc/") + std::to_string(pid) + std::string("/status");
    }
    std::ifstream input(path, std::ios::in);
    if (input.fail()) {
        const int bufSize = 256;
        char buf[bufSize] = { 0 };
        strerror_r(errno, buf, bufSize);
        HILOG_ERROR(LOG_CORE, "%s:NetworkPlugin, open %s failed, errno(%s)", __func__, path.c_str(), buf);
        return -1;
    }
    do {
        if (!input.good()) {
            return -1;
        }
        std::string line;
        getline(input, line);
        if (!strncmp(line.c_str(), "Uid:", strlen("Uid:"))) {
            std::string str = line.substr(strlen("Uid:\t"));
            HILOG_INFO(LOG_CORE, "%s:NetworkPlugin, line(%s), str(%s)", __func__, line.c_str(), str.c_str());
            return strtol(str.c_str(), &end, DEC_BASE);
        }
    } while (!input.eof());
    input.close();

    return -1;
}

bool NetworkPlugin::ReadTxRxBytes(int32_t pid, NetworkCell &cell)
{
    int32_t uid = pidUid_.at(pid);
    CHECK_NOTNULL(fp_.get(), false, "%s:NetworkPlugin, fp_ is null", __func__);
    int ret = fseek(fp_.get(), 0, SEEK_SET);
    CHECK_TRUE(ret == 0, false, "%s:NetworkPlugin, fseek failed, error(%d)!", __func__, errno);
    size_t rsize = static_cast<size_t>(fread(buffer_.get(), sizeof(char), READ_BUFFER_SIZE - 1, fp_.get()));
    buffer_.get()[rsize] = '\0';
    CHECK_TRUE(rsize >= 0, false, "%s:NetworkPlugin, read failed, errno(%d)", __func__, errno);
    char* end = nullptr;
    BufferSplitter totalbuffer((const char*)buffer_.get(), rsize + 1);
    do {
        int index = 0;
        NetDetails cache = {0};
        char tmp[TX_BYTES_INDEX + 1] = {0};
        while (totalbuffer.NextWord(' ')) {
            index++;
            if (totalbuffer.CurWord() == nullptr) {
                continue;
            }
            if (index == IFACE_INDEX && !strncmp(totalbuffer.CurWord(), "lo", strlen("lo"))) {
                break;
            }
            if (index == IFACE_INDEX &&
                strncpy_s(tmp, sizeof(tmp), totalbuffer.CurWord(), totalbuffer.CurWordSize()) == EOK) {
                cache.type = tmp;
            }
            uint64_t value = static_cast<uint64_t>(strtoull(totalbuffer.CurWord(), &end, DEC_BASE));
            CHECK_TRUE(value >= 0, false, "%s:NetworkPlugin, strtoull value failed", __func__);
            if ((index == UID_INDEX) && (uid != static_cast<int32_t>(value))) {
                break;
            }
            if (index == RX_BYTES_INDEX) {
                uint64_t rx_bytes = value;
                cache.rx = rx_bytes;
                cell.rx += rx_bytes;
            }
            if (index == TX_BYTES_INDEX) {
                uint64_t tx_bytes = value;
                cache.tx = tx_bytes;
                cell.tx += tx_bytes;
                AddNetDetails(cell, cache);
            }
        }
    } while (totalbuffer.NextLine());

    clock_gettime(CLOCK_REALTIME, &cell.ts);

    return true;
}

void NetworkPlugin::AddNetDetails(NetworkCell& cell, NetDetails& data)
{
    bool finded = false;

    // 处理重复数据
    for (auto it = cell.details.begin(); it != cell.details.end(); it++) {
        if (it->type == data.type) {
            it->tx += data.tx;
            it->rx += data.rx;
            finded = true;
        }
    }

    if (!finded) {
        cell.details.push_back(data);
    }
}

bool NetworkPlugin::ReadSystemTxRxBytes(NetSystemData &systemData)
{
    CHECK_NOTNULL(fp_.get(), false, "%s:NetworkPlugin, fp_ is null", __func__);
    int ret = fseek(fp_.get(), 0, SEEK_SET);
    CHECK_TRUE(ret == 0, false, "%s:NetworkPlugin, fseek failed, error(%d)!", __func__, errno);
    size_t rsize = static_cast<size_t>(fread(buffer_.get(), sizeof(char), READ_BUFFER_SIZE - 1, fp_.get()));
    buffer_.get()[rsize] = '\0';
    CHECK_TRUE(rsize >= 0, false, "%s:NetworkPlugin, read failed, errno(%d)", __func__, errno);
    char* end = nullptr;
    BufferSplitter totalbuffer((const char*)buffer_.get(), rsize + 1);
    do {
        int index = 0;
        NetSystemDetails systemCache = {};
        char tmp[TX_BYTES_INDEX + 1] = "";
        while (totalbuffer.NextWord(' ')) {
            index++;
            if (totalbuffer.CurWord() == nullptr) {
                continue;
            }
            if (index == IFACE_INDEX && !strncmp(totalbuffer.CurWord(), "lo", strlen("lo"))) {
                break;
            }
            if (index == IFACE_INDEX &&
                strncpy_s(tmp, sizeof(tmp), totalbuffer.CurWord(), totalbuffer.CurWordSize()) == EOK) {
                systemCache.type = tmp;
            }
            if (strcmp(systemCache.type.c_str(), "iface") == 0) {
                break;
            }
            uint64_t value = static_cast<uint64_t>(strtoull(totalbuffer.CurWord(), &end, DEC_BASE));
            CHECK_TRUE(value >= 0, false, "%s:NetworkPlugin, strtoull value failed", __func__);
            if (index == RX_BYTES_INDEX) {
                uint64_t rx_bytes = value;
                systemCache.rx_bytes = rx_bytes;
                systemData.rx_bytes += rx_bytes;
            } else if (index == RX_PACKETS_INDEX) {
                uint64_t rx_packets = value;
                systemCache.rx_packets = rx_packets;
                systemData.rx_packets += rx_packets;
            } else if (index == TX_BYTES_INDEX) {
                uint64_t tx_bytes = value;
                systemCache.tx_bytes = tx_bytes;
                systemData.tx_bytes += tx_bytes;
            } else if (index == TX_PACKETS_INDEX) {
                uint64_t tx_packets = value;
                systemCache.tx_packets = tx_packets;
                systemData.tx_packets += tx_packets;
                AddNetSystemDetails(systemData, systemCache);
            }
        }
    } while (totalbuffer.NextLine());

    clock_gettime(CLOCK_REALTIME, &systemData.ts);

    return true;
}

void NetworkPlugin::AddNetSystemDetails(NetSystemData& systemData, NetSystemDetails& data)
{
    bool finded = false;

    // 处理重复数据
    for (auto it = systemData.details.begin(); it != systemData.details.end(); it++) {
        if (it->type == data.type) {
            it->rx_bytes += data.rx_bytes;
            it->rx_packets += data.rx_packets;
            it->tx_bytes += data.tx_bytes;
            it->tx_packets += data.tx_packets;
            finded = true;
        }
    }

    if (!finded) {
        systemData.details.push_back(data);
    }
}
