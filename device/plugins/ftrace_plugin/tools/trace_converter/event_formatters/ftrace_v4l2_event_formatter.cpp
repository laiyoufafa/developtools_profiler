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
    v4l2_dqbuf,
    [](const FtraceEvent& event) -> bool { return event.has_v4l2_dqbuf_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.v4l2_dqbuf_format();
        std::stringstream sout;
        sout << "v4l2_dqbuf:";
        sout << " minor=" << msg.minor();
        sout << " index=" << msg.index();
        sout << " type=" << msg.type();
        sout << " bytesused=" << msg.bytesused();
        sout << " flags=" << msg.flags();
        sout << " field=" << msg.field();
        sout << " timestamp=" << msg.timestamp();
        sout << " timecode_type=" << msg.timecode_type();
        sout << " timecode_flags=" << msg.timecode_flags();
        sout << " timecode_frames=" << msg.timecode_frames();
        sout << " timecode_seconds=" << msg.timecode_seconds();
        sout << " timecode_minutes=" << msg.timecode_minutes();
        sout << " timecode_hours=" << msg.timecode_hours();
        sout << " timecode_userbits0=" << msg.timecode_userbits0();
        sout << " timecode_userbits1=" << msg.timecode_userbits1();
        sout << " timecode_userbits2=" << msg.timecode_userbits2();
        sout << " timecode_userbits3=" << msg.timecode_userbits3();
        sout << " sequence=" << msg.sequence();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    v4l2_qbuf,
    [](const FtraceEvent& event) -> bool { return event.has_v4l2_qbuf_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.v4l2_qbuf_format();
        std::stringstream sout;
        sout << "v4l2_qbuf:";
        sout << " minor=" << msg.minor();
        sout << " index=" << msg.index();
        sout << " type=" << msg.type();
        sout << " bytesused=" << msg.bytesused();
        sout << " flags=" << msg.flags();
        sout << " field=" << msg.field();
        sout << " timestamp=" << msg.timestamp();
        sout << " timecode_type=" << msg.timecode_type();
        sout << " timecode_flags=" << msg.timecode_flags();
        sout << " timecode_frames=" << msg.timecode_frames();
        sout << " timecode_seconds=" << msg.timecode_seconds();
        sout << " timecode_minutes=" << msg.timecode_minutes();
        sout << " timecode_hours=" << msg.timecode_hours();
        sout << " timecode_userbits0=" << msg.timecode_userbits0();
        sout << " timecode_userbits1=" << msg.timecode_userbits1();
        sout << " timecode_userbits2=" << msg.timecode_userbits2();
        sout << " timecode_userbits3=" << msg.timecode_userbits3();
        sout << " sequence=" << msg.sequence();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    vb2_v4l2_buf_done,
    [](const FtraceEvent& event) -> bool { return event.has_vb2_v4l2_buf_done_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.vb2_v4l2_buf_done_format();
        std::stringstream sout;
        sout << "vb2_v4l2_buf_done:";
        sout << " minor=" << msg.minor();
        sout << " flags=" << msg.flags();
        sout << " field=" << msg.field();
        sout << " timestamp=" << msg.timestamp();
        sout << " timecode_type=" << msg.timecode_type();
        sout << " timecode_flags=" << msg.timecode_flags();
        sout << " timecode_frames=" << msg.timecode_frames();
        sout << " timecode_seconds=" << msg.timecode_seconds();
        sout << " timecode_minutes=" << msg.timecode_minutes();
        sout << " timecode_hours=" << msg.timecode_hours();
        sout << " timecode_userbits0=" << msg.timecode_userbits0();
        sout << " timecode_userbits1=" << msg.timecode_userbits1();
        sout << " timecode_userbits2=" << msg.timecode_userbits2();
        sout << " timecode_userbits3=" << msg.timecode_userbits3();
        sout << " sequence=" << msg.sequence();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    vb2_v4l2_buf_queue,
    [](const FtraceEvent& event) -> bool { return event.has_vb2_v4l2_buf_queue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.vb2_v4l2_buf_queue_format();
        std::stringstream sout;
        sout << "vb2_v4l2_buf_queue:";
        sout << " minor=" << msg.minor();
        sout << " flags=" << msg.flags();
        sout << " field=" << msg.field();
        sout << " timestamp=" << msg.timestamp();
        sout << " timecode_type=" << msg.timecode_type();
        sout << " timecode_flags=" << msg.timecode_flags();
        sout << " timecode_frames=" << msg.timecode_frames();
        sout << " timecode_seconds=" << msg.timecode_seconds();
        sout << " timecode_minutes=" << msg.timecode_minutes();
        sout << " timecode_hours=" << msg.timecode_hours();
        sout << " timecode_userbits0=" << msg.timecode_userbits0();
        sout << " timecode_userbits1=" << msg.timecode_userbits1();
        sout << " timecode_userbits2=" << msg.timecode_userbits2();
        sout << " timecode_userbits3=" << msg.timecode_userbits3();
        sout << " sequence=" << msg.sequence();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    vb2_v4l2_dqbuf,
    [](const FtraceEvent& event) -> bool { return event.has_vb2_v4l2_dqbuf_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.vb2_v4l2_dqbuf_format();
        std::stringstream sout;
        sout << "vb2_v4l2_dqbuf:";
        sout << " minor=" << msg.minor();
        sout << " flags=" << msg.flags();
        sout << " field=" << msg.field();
        sout << " timestamp=" << msg.timestamp();
        sout << " timecode_type=" << msg.timecode_type();
        sout << " timecode_flags=" << msg.timecode_flags();
        sout << " timecode_frames=" << msg.timecode_frames();
        sout << " timecode_seconds=" << msg.timecode_seconds();
        sout << " timecode_minutes=" << msg.timecode_minutes();
        sout << " timecode_hours=" << msg.timecode_hours();
        sout << " timecode_userbits0=" << msg.timecode_userbits0();
        sout << " timecode_userbits1=" << msg.timecode_userbits1();
        sout << " timecode_userbits2=" << msg.timecode_userbits2();
        sout << " timecode_userbits3=" << msg.timecode_userbits3();
        sout << " sequence=" << msg.sequence();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    vb2_v4l2_qbuf,
    [](const FtraceEvent& event) -> bool { return event.has_vb2_v4l2_qbuf_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.vb2_v4l2_qbuf_format();
        std::stringstream sout;
        sout << "vb2_v4l2_qbuf:";
        sout << " minor=" << msg.minor();
        sout << " flags=" << msg.flags();
        sout << " field=" << msg.field();
        sout << " timestamp=" << msg.timestamp();
        sout << " timecode_type=" << msg.timecode_type();
        sout << " timecode_flags=" << msg.timecode_flags();
        sout << " timecode_frames=" << msg.timecode_frames();
        sout << " timecode_seconds=" << msg.timecode_seconds();
        sout << " timecode_minutes=" << msg.timecode_minutes();
        sout << " timecode_hours=" << msg.timecode_hours();
        sout << " timecode_userbits0=" << msg.timecode_userbits0();
        sout << " timecode_userbits1=" << msg.timecode_userbits1();
        sout << " timecode_userbits2=" << msg.timecode_userbits2();
        sout << " timecode_userbits3=" << msg.timecode_userbits3();
        sout << " sequence=" << msg.sequence();
        return sout.str();
    });
} // namespace
FTRACE_NS_END
