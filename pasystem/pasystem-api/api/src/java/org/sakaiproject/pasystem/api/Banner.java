/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.api;

import lombok.Getter;
import java.util.Arrays;
import java.util.Date;

import static org.sakaiproject.pasystem.api.ValidationHelper.*;

public class Banner implements Comparable<Banner> {
    @Getter
    private String uuid;
    @Getter
    private String message;
    @Getter
    private long startTime;
    @Getter
    private long endTime;
    @Getter
    private String hosts;
    private BannerType type;

    private boolean isActive;

    @Getter
    private boolean isDismissed;

    enum BannerType {
        HIGH,
        MEDIUM,
        LOW
    }

    public Banner(String message, String hosts, boolean active, long startTime, long endTime, String type) {
        this(null, message, hosts, active, startTime, endTime, type, false);
    }

    public Banner(String uuid, String message, String hosts, boolean active, long startTime, long endTime, String type) {
        this(uuid, message, hosts, active, startTime, endTime, type, false);
    }

    public Banner(String uuid, String message, String hosts, boolean active, long startTime, long endTime, String type, boolean isDismissed) {
        this.uuid = uuid;
        this.message = message;
        this.hosts = hosts;
        this.isActive = active;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = BannerType.valueOf(type.toUpperCase());
        this.isDismissed = isDismissed;
    }

    public String getType() {
        return this.type.toString().toLowerCase();
    }

    public String calculateAcknowledgementType() {
        if (type.equals(BannerType.MEDIUM)) {
            return Acknowledger.TEMPORARY;
        } else {
            return Acknowledger.PERMANENT;
        }
    }

    public int compareTo(Banner other) {
        return getSeverityScore() - other.getSeverityScore();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Banner)) {
            return false;
        }

        return uuid.equals(((Banner)obj).getUuid());
    }

    public int hashCode() {
        return uuid.hashCode();
    }

    public int getSeverityScore() {
        return type.ordinal();
    }

    public boolean isActiveNow() {
        if (!isActive()) {
            return false;
        }

        if (startTime == 0 && endTime == 0) {
            return isActive();
        }

        Date now = new Date();

        return (now.after(new Date(startTime))
                && (endTime == 0 || now.before(new Date(endTime))));
    }

    public boolean isDismissible() {
        return !BannerType.HIGH.equals(type);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isActiveForHost(String hostname) {
        // are we active?
        if (!isActiveNow()) {
            return false;
        }

        // if no hosts then assume active for any host
        if (hosts == null || hosts.isEmpty()) {
            return true;
        }

        // we have some hosts defined, so check if the current is listed
        return Arrays.asList(hosts.split(",")).contains(hostname);
    }

    public Errors validate() {
        Errors errors = new Errors();

        if (!startTimeBeforeEndTime(startTime, endTime)) {
            errors.addError("start_time", "start_time_after_end_time");
            errors.addError("end_time", "start_time_after_end_time");
        }

        return errors;
    }
}
