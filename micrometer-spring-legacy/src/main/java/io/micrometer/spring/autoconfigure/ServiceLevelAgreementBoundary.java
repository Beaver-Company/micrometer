/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.spring.autoconfigure;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.spring.autoconfigure.export.StringToDurationConverter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A service level agreement boundary. Can be specified as either a {@link Long}
 * (applicable to timers and distribution summaries) or a {@link Long} (applicable to only timers).
 *
 * @author Phillip Webb
 */
public final class ServiceLevelAgreementBoundary {
    private static final StringToDurationConverter durationConverter = new StringToDurationConverter();
    private final Object value;

    ServiceLevelAgreementBoundary(long value) {
        this.value = value;
    }

    ServiceLevelAgreementBoundary(Duration value) {
        this.value = value;
    }

    /**
     * Return the underlying value of the SLA in form suitable to apply to the given meter
     * type.
     *
     * @param meterType the meter type
     * @return the value or {@code null} if the value cannot be applied
     */
    public Long getValue(Meter.Type meterType) {
        if (meterType == Type.DISTRIBUTION_SUMMARY) {
            return getDistributionSummaryValue();
        }
        if (meterType == Type.TIMER) {
            return getTimerValue();
        }
        return null;
    }

    private Long getDistributionSummaryValue() {
        if (this.value instanceof Long) {
            return (Long) this.value;
        }
        return null;
    }

    private Long getTimerValue() {
        if (this.value instanceof Long) {
            return TimeUnit.MILLISECONDS.toNanos((long) this.value);
        }
        if (this.value instanceof Duration) {
            return ((Duration) this.value).toNanos();
        }
        return null;
    }

    public static ServiceLevelAgreementBoundary valueOf(String value) {
        if (isNumber(value)) {
            return new ServiceLevelAgreementBoundary(Long.parseLong(value));
        }
        return new ServiceLevelAgreementBoundary(
                durationConverter.convert(value));
    }

    /**
     * Return a new {@link ServiceLevelAgreementBoundary} instance for the given long
     * value.
     *
     * @param value the source value
     * @return a {@link ServiceLevelAgreementBoundary} instance
     */
    public static ServiceLevelAgreementBoundary valueOf(long value) {
        return new ServiceLevelAgreementBoundary(value);
    }

    /**
     * Return a new {@link ServiceLevelAgreementBoundary} instance for the given String
     * value. The value may contain a simple number, or a {@link StringToDurationConverter}
     * duration formatted value.
     *
     * @param value the source value
     * @return a {@link ServiceLevelAgreementBoundary} instance
     */
    private static boolean isNumber(String value) {
        return value.chars().allMatch(Character::isDigit);
    }

}
