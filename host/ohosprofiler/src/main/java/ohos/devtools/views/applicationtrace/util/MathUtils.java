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

package ohos.devtools.views.applicationtrace.util;

import java.util.List;

/**
 * The MathUtils
 *
 * @date 2021/04/22 12:25
 */
public class MathUtils {
    /**
     * Incoming a series x calculates the average
     *
     * @param doubles List<Double> calculates x
     * @return average value
     */
    public static double average(List<Double> doubles) {
        int size = doubles.size(); // The number of column elements
        double sum = doubles.stream().mapToDouble(it -> it).sum();
        return sum / size;
    }

    /**
     * Pass in a series x to calculate variance
     * Variance s^2=[（x1-x）^2+（x2-x）^2+......（xn-x）^2]/（n） (x is average)
     *
     * @param doubles List<Double> The number of columns to calculate
     * @return variance variance
     */
    public static double variance(List<Double> doubles) {
        int size = doubles.size(); // The number of column elements
        double avg = average(doubles); // Average
        double varianceD = doubles.stream().mapToDouble(it -> (it - avg) * (it + avg)).sum();
        return varianceD / size;
    }

    /**
     * Pass in a number of columns x to calculate the standard deviation
     * The standard deviation σ sqrt (s)2), which is the square root of the standard deviation s/a variance
     *
     * @param doubles List<Double> The number of columns to calculate
     * @return standard deviation
     */
    public static double standardDiviation(List<Double> doubles) {
        return Math.sqrt(variance(doubles));
    }
}
