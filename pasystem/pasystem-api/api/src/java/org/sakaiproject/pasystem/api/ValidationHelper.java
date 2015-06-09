package org.sakaiproject.pasystem.api;

public class ValidationHelper {

    public static boolean startTimeBeforeEndTime(long startTime, long endTime) {
        if (startTime <= 0 || endTime <= 0) {
            return true;
        } else {
            return startTime <= endTime;
        }
    }

}
