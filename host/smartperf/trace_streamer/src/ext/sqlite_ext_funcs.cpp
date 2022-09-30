/*
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
#include "sqlite_ext_funcs.h"
#include <cmath>
#include "log.h"
#include "sqlite3.h"
/*
** Return a stdev value
*/
static void sqliteExtStdevFinalize(sqlite3_context* context)
{
    StdevCtx* ptr = static_cast<StdevCtx*>(sqlite3_aggregate_context(context, 0));
    if (ptr && ptr->cntValue > 1) {
        sqlite3_result_double(context, sqrt(ptr->rSValue / (ptr->cntValue - 1)));
    } else {
        sqlite3_result_double(context, 0.0);
    }
}
/*
** called each value received during a calculation of stdev or variance
*/
static void sqliteExtStdevNextStep(sqlite3_context* context, int argc, sqlite3_value** argv)
{
    double deltaValue;
    double x;

    TS_ASSERT(argc == 1);
    StdevCtx* ptr = static_cast<StdevCtx*>(sqlite3_aggregate_context(context, sizeof(StdevCtx)));
    if (SQLITE_NULL != sqlite3_value_numeric_type(argv[0])) {
        ptr->cntValue++;
        x = sqlite3_value_double(argv[0]);
        deltaValue = (x - ptr->rMValue);
        ptr->rMValue += deltaValue / ptr->cntValue;
        ptr->rSValue += deltaValue * (x - ptr->rMValue);
    }
}

void CreateExtendFunction(sqlite3* db)
{
    sqlite3_create_function(db, "stdev", 1, SQLITE_UTF8, nullptr, 0, sqliteExtStdevNextStep,
                            sqliteExtStdevFinalize);
}