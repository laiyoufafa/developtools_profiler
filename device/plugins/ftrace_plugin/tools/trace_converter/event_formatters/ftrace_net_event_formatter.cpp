/* THIS FILE IS GENERATE BY ftrace_cpp_generator.py, PLEASE DON'T EDIT IT!
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <sstream>
#include "event_formatter.h"

FTRACE_NS_BEGIN
namespace {
REGISTER_FTRACE_EVENT_FORMATTER(
    napi_gro_frags_entry,
    [](const FtraceEvent& event) -> bool { return event.has_napi_gro_frags_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.napi_gro_frags_entry_format();
        std::stringstream sout;
        sout << "napi_gro_frags_entry:";
        sout << " name=" << msg.name();
        sout << " napi_id=" << msg.napi_id();
        sout << " queue_mapping=" << msg.queue_mapping();
        sout << " skbaddr=" << msg.skbaddr();
        sout << " vlan_tagged=" << msg.vlan_tagged();
        sout << " vlan_proto=" << msg.vlan_proto();
        sout << " vlan_tci=" << msg.vlan_tci();
        sout << " protocol=" << msg.protocol();
        sout << " ip_summed=" << msg.ip_summed();
        sout << " hash=" << msg.hash();
        sout << " l4_hash=" << msg.l4_hash();
        sout << " len=" << msg.len();
        sout << " data_len=" << msg.data_len();
        sout << " truesize=" << msg.truesize();
        sout << " mac_header_valid=" << msg.mac_header_valid();
        sout << " mac_header=" << msg.mac_header();
        sout << " nr_frags=" << msg.nr_frags();
        sout << " gso_size=" << msg.gso_size();
        sout << " gso_type=" << msg.gso_type();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    napi_gro_receive_entry,
    [](const FtraceEvent& event) -> bool { return event.has_napi_gro_receive_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.napi_gro_receive_entry_format();
        std::stringstream sout;
        sout << "napi_gro_receive_entry:";
        sout << " name=" << msg.name();
        sout << " napi_id=" << msg.napi_id();
        sout << " queue_mapping=" << msg.queue_mapping();
        sout << " skbaddr=" << msg.skbaddr();
        sout << " vlan_tagged=" << msg.vlan_tagged();
        sout << " vlan_proto=" << msg.vlan_proto();
        sout << " vlan_tci=" << msg.vlan_tci();
        sout << " protocol=" << msg.protocol();
        sout << " ip_summed=" << msg.ip_summed();
        sout << " hash=" << msg.hash();
        sout << " l4_hash=" << msg.l4_hash();
        sout << " len=" << msg.len();
        sout << " data_len=" << msg.data_len();
        sout << " truesize=" << msg.truesize();
        sout << " mac_header_valid=" << msg.mac_header_valid();
        sout << " mac_header=" << msg.mac_header();
        sout << " nr_frags=" << msg.nr_frags();
        sout << " gso_size=" << msg.gso_size();
        sout << " gso_type=" << msg.gso_type();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    net_dev_queue,
    [](const FtraceEvent& event) -> bool { return event.has_net_dev_queue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.net_dev_queue_format();
        std::stringstream sout;
        sout << "net_dev_queue:";
        sout << " skbaddr=" << msg.skbaddr();
        sout << " len=" << msg.len();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    net_dev_start_xmit,
    [](const FtraceEvent& event) -> bool { return event.has_net_dev_start_xmit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.net_dev_start_xmit_format();
        std::stringstream sout;
        sout << "net_dev_start_xmit:";
        sout << " name=" << msg.name();
        sout << " queue_mapping=" << msg.queue_mapping();
        sout << " skbaddr=" << msg.skbaddr();
        sout << " vlan_tagged=" << msg.vlan_tagged();
        sout << " vlan_proto=" << msg.vlan_proto();
        sout << " vlan_tci=" << msg.vlan_tci();
        sout << " protocol=" << msg.protocol();
        sout << " ip_summed=" << msg.ip_summed();
        sout << " len=" << msg.len();
        sout << " data_len=" << msg.data_len();
        sout << " network_offset=" << msg.network_offset();
        sout << " transport_offset_valid=" << msg.transport_offset_valid();
        sout << " transport_offset=" << msg.transport_offset();
        sout << " tx_flags=" << msg.tx_flags();
        sout << " gso_size=" << msg.gso_size();
        sout << " gso_segs=" << msg.gso_segs();
        sout << " gso_type=" << msg.gso_type();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    net_dev_xmit,
    [](const FtraceEvent& event) -> bool { return event.has_net_dev_xmit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.net_dev_xmit_format();
        std::stringstream sout;
        sout << "net_dev_xmit:";
        sout << " skbaddr=" << msg.skbaddr();
        sout << " len=" << msg.len();
        sout << " rc=" << msg.rc();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    netif_receive_skb,
    [](const FtraceEvent& event) -> bool { return event.has_netif_receive_skb_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.netif_receive_skb_format();
        std::stringstream sout;
        sout << "netif_receive_skb:";
        sout << " skbaddr=" << msg.skbaddr();
        sout << " len=" << msg.len();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    netif_receive_skb_entry,
    [](const FtraceEvent& event) -> bool { return event.has_netif_receive_skb_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.netif_receive_skb_entry_format();
        std::stringstream sout;
        sout << "netif_receive_skb_entry:";
        sout << " name=" << msg.name();
        sout << " napi_id=" << msg.napi_id();
        sout << " queue_mapping=" << msg.queue_mapping();
        sout << " skbaddr=" << msg.skbaddr();
        sout << " vlan_tagged=" << msg.vlan_tagged();
        sout << " vlan_proto=" << msg.vlan_proto();
        sout << " vlan_tci=" << msg.vlan_tci();
        sout << " protocol=" << msg.protocol();
        sout << " ip_summed=" << msg.ip_summed();
        sout << " hash=" << msg.hash();
        sout << " l4_hash=" << msg.l4_hash();
        sout << " len=" << msg.len();
        sout << " data_len=" << msg.data_len();
        sout << " truesize=" << msg.truesize();
        sout << " mac_header_valid=" << msg.mac_header_valid();
        sout << " mac_header=" << msg.mac_header();
        sout << " nr_frags=" << msg.nr_frags();
        sout << " gso_size=" << msg.gso_size();
        sout << " gso_type=" << msg.gso_type();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    netif_rx,
    [](const FtraceEvent& event) -> bool { return event.has_netif_rx_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.netif_rx_format();
        std::stringstream sout;
        sout << "netif_rx:";
        sout << " skbaddr=" << msg.skbaddr();
        sout << " len=" << msg.len();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    netif_rx_entry,
    [](const FtraceEvent& event) -> bool { return event.has_netif_rx_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.netif_rx_entry_format();
        std::stringstream sout;
        sout << "netif_rx_entry:";
        sout << " name=" << msg.name();
        sout << " napi_id=" << msg.napi_id();
        sout << " queue_mapping=" << msg.queue_mapping();
        sout << " skbaddr=" << msg.skbaddr();
        sout << " vlan_tagged=" << msg.vlan_tagged();
        sout << " vlan_proto=" << msg.vlan_proto();
        sout << " vlan_tci=" << msg.vlan_tci();
        sout << " protocol=" << msg.protocol();
        sout << " ip_summed=" << msg.ip_summed();
        sout << " hash=" << msg.hash();
        sout << " l4_hash=" << msg.l4_hash();
        sout << " len=" << msg.len();
        sout << " data_len=" << msg.data_len();
        sout << " truesize=" << msg.truesize();
        sout << " mac_header_valid=" << msg.mac_header_valid();
        sout << " mac_header=" << msg.mac_header();
        sout << " nr_frags=" << msg.nr_frags();
        sout << " gso_size=" << msg.gso_size();
        sout << " gso_type=" << msg.gso_type();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    netif_rx_ni_entry,
    [](const FtraceEvent& event) -> bool { return event.has_netif_rx_ni_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.netif_rx_ni_entry_format();
        std::stringstream sout;
        sout << "netif_rx_ni_entry:";
        sout << " name=" << msg.name();
        sout << " napi_id=" << msg.napi_id();
        sout << " queue_mapping=" << msg.queue_mapping();
        sout << " skbaddr=" << msg.skbaddr();
        sout << " vlan_tagged=" << msg.vlan_tagged();
        sout << " vlan_proto=" << msg.vlan_proto();
        sout << " vlan_tci=" << msg.vlan_tci();
        sout << " protocol=" << msg.protocol();
        sout << " ip_summed=" << msg.ip_summed();
        sout << " hash=" << msg.hash();
        sout << " l4_hash=" << msg.l4_hash();
        sout << " len=" << msg.len();
        sout << " data_len=" << msg.data_len();
        sout << " truesize=" << msg.truesize();
        sout << " mac_header_valid=" << msg.mac_header_valid();
        sout << " mac_header=" << msg.mac_header();
        sout << " nr_frags=" << msg.nr_frags();
        sout << " gso_size=" << msg.gso_size();
        sout << " gso_type=" << msg.gso_type();
        return sout.str();
    });
} // namespace
FTRACE_NS_END
