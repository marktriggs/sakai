package org.sakaiproject.pasystem.tool.forms;

import lombok.Data;
import org.sakaiproject.pasystem.api.Errors;
import org.sakaiproject.pasystem.api.Banner;
import javax.servlet.http.HttpServletRequest;

@Data
public class BannerForm extends BaseForm {

    private String message;
    private String hosts;
    private String type;
    private boolean active;

    private BannerForm(String uuid, String message, String hosts, long startTime, long endTime, boolean active, String type) {
        this.uuid = uuid;
        this.message = message;
        this.hosts = hosts;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
        this.type = type;
    }

    public static BannerForm fromBanner(Banner existingBanner) {
        String uuid = existingBanner.getUuid();

        return new BannerForm(uuid,
                existingBanner.getMessage(),
                existingBanner.getHosts(),
                existingBanner.getStartTime(),
                existingBanner.getEndTime(),
                existingBanner.isActive(),
                existingBanner.getType());
    }

    public static BannerForm fromRequest(String uuid, HttpServletRequest request) {
        String message = request.getParameter("message");
        String hosts = request.getParameter("hosts");
        String type = request.getParameter("type");

        long startTime = "".equals(request.getParameter("start_time")) ? 0 : parseTime(request.getParameter("start_time_selected_datetime"));
        long endTime = "".equals(request.getParameter("end_time")) ? 0 : parseTime(request.getParameter("end_time_selected_datetime"));

        boolean active = "on".equals(request.getParameter("active"));

        return new BannerForm(uuid, message, hosts, startTime, endTime, active, type);
    }

    public Errors validate() {
        Errors errors = new Errors();

        if (!hasValidStartTime()) {
            errors.addError("start_time", "invalid_time");
        }

        if (!hasValidEndTime()) {
            errors.addError("end_time", "invalid_time");
        }

        Errors modelErrors = toBanner().validate();

        return errors.merge(modelErrors);
    }

    public Banner toBanner() {
        return new Banner(uuid, message, hosts, active, startTime, endTime, type);
    }

}

