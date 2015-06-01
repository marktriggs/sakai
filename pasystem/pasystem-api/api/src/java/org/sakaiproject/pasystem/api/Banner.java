package org.sakaiproject.pasystem.api;

import lombok.Getter;
import java.util.Arrays;
import java.util.Date;

public class Banner {
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
    @Getter
    private String type;

    private boolean isActive;
    private boolean hasBeenDismissed;

    public Banner(String uuid, String message, String hosts, int active, long startTime, long endTime, String type) {
        this(uuid, message, hosts, active, startTime, endTime, type, false);
    }

    public Banner(String uuid, String message, String hosts, int active, long startTime, long endTime, String type, boolean hasBeenDismissed) {
        this.uuid = uuid;
        this.message = message;
        this.hosts = hosts;
        this.isActive = (active == 1);
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.hasBeenDismissed = hasBeenDismissed;
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
        // FIXME: Create an enum for this
        return "high".equals(type);
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
}
