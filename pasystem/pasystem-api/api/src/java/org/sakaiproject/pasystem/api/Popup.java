package org.sakaiproject.pasystem.api;

import lombok.Getter;

import static org.sakaiproject.pasystem.api.ValidationHelper.*;


public class Popup {

    @Getter
    private String uuid;
    @Getter
    private String descriptor;
    @Getter
    private long startTime;
    @Getter
    private long endTime;
    @Getter
    private boolean isOpenCampaign;

    private String template;

    private Popup() {
        this.uuid = null;
    }

    private Popup(String uuid, String descriptor, long startTime, long endTime, boolean isOpenCampaign, String template) {
        this.uuid = uuid;
        this.descriptor = descriptor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.template = template;
        this.isOpenCampaign = isOpenCampaign;
    }

    public static Popup createNullPopup() {
        return new Popup();
    }

    public static Popup create(String descriptor, long startTime, long endTime, boolean isOpenCampaign) {
        return create(null, descriptor, startTime, endTime, isOpenCampaign);
    }

    public static Popup create(String uuid, String descriptor, long startTime, long endTime, boolean isOpenCampaign) {
        return create(uuid, descriptor, startTime, endTime, isOpenCampaign, null);
    }

    public static Popup create(String uuid, String descriptor, long startTime, long endTime, boolean isOpenCampaign, String template) {
        return new Popup(uuid, descriptor, startTime, endTime, isOpenCampaign, template);
    }

    public boolean isActive() {
        long now = System.currentTimeMillis();
        return (uuid != null) && startTime <= now && (endTime == 0 || now <= endTime);
    }

    public String getTemplate() {
        if (template == null) {
            throw new PASystemException("Template not loaded for Popup instance");
        }

        return template;
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
