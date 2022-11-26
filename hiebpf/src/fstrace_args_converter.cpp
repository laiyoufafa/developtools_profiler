/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
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

#include "type_headers.h"


static int Openat2ArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->openat2_args.dfd;
    args[1] = (__u64) start_event->openat2_args.filename;
    args[2] = (__u64) start_event->openat2_args.how;
    args[3] = 0xFFFFFFFFFFFFFFFF;
    return 0;
}

static int ReadArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->read_args.fd;
    args[1] = (__u64) start_event->read_args.buf;
    args[2] = (__u64) start_event->read_args.count;
    args[3] = 0xFFFFFFFFFFFFFFFF;
    return 0;
}

static int WriteArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->write_args.fd;
    args[1] = (__u64) start_event->write_args.buf;
    args[2] = (__u64) start_event->write_args.count;
    args[3] = 0xFFFFFFFFFFFFFFFF;
    return 0;
}

static int Pread64ArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->pread64_args.fd;
    args[1] = (__u64) start_event->pread64_args.buf;
    args[2] = (__u64) start_event->pread64_args.count;
    args[3] = (__u64) start_event->pread64_args.pos;
    return 0;
}

static int Pwrite64ArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->pwrite64_args.fd;
    args[1] = (__u64) start_event->pwrite64_args.buf;
    args[2] = (__u64) start_event->pwrite64_args.count;
    args[3] = (__u64) start_event->pwrite64_args.pos;
    return 0;
}

static int ReadvArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->readv_args.fd;
    args[1] = (__u64) start_event->readv_args.vec;
    args[2] = (__u64) start_event->readv_args.vlen;
    args[3] = (__u64) start_event->readv_args.flags;
    return 0;
}

static int WritevArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->writev_args.fd;
    args[1] = (__u64) start_event->writev_args.vec;
    args[2] = (__u64) start_event->writev_args.vlen;
    args[3] = (__u64) start_event->writev_args.flags;
    return 0;
}

static int PreadvArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->preadv_args.fd;
    args[1] = (__u64) start_event->preadv_args.vec;
    args[2] = (__u64) start_event->preadv_args.vlen;
    args[3] = (__u64) start_event->preadv_args.pos;
    return 0;
}

static int PwritevArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->pwritev_args.fd;
    args[1] = (__u64) start_event->pwritev_args.vec;
    args[2] = (__u64) start_event->pwritev_args.vlen;
    args[3] = (__u64) start_event->pwritev_args.pos;
    return 0;
}

static int CloseArgsConverter(__u64* args, const struct fstrace_start_event_t* start_event)
{
    args[0] = (__u64) start_event->close_args.files;
    args[1] = (__u64) start_event->close_args.fd;
    args[2] = 0xFFFFFFFFFFFFFFFF;
    args[3] = 0xFFFFFFFFFFFFFFFF;
    return 0;
}

using ConverterType = int (*) (__u64*, const struct fstrace_start_event_t *);
ConverterType gArgsConverterTable[11] = {
    nullptr,
    &Openat2ArgsConverter,
    &ReadArgsConverter,
    &WriteArgsConverter,
    &Pread64ArgsConverter,
    &Pwrite64ArgsConverter,
    &ReadvArgsConverter,
    &WritevArgsConverter,
    &PreadvArgsConverter,
    &PwritevArgsConverter,
    &CloseArgsConverter
};
