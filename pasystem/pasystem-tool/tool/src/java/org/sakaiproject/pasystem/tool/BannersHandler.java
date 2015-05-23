package org.sakaiproject.pasystem.tool;

import org.sakaiproject.pasystem.api.Banner;
import org.sakaiproject.pasystem.api.PASystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

public class BannersHandler extends CrudHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BannersHandler.class);
    private PASystem paSystem;

    public BannersHandler(PASystem pasystem) {
        this.paSystem = pasystem;
    }

    protected void handleDelete(HttpServletRequest request) {
        String uuid = extractId(request);
        paSystem.getBanners().deleteBanner(uuid);

        flash("info", "banner_deleted");
        sendRedirect("");
    }

    protected void handleEdit(HttpServletRequest request, Map<String, Object> context) {
        String uuid = extractId(request);
        context.put("subpage", "banner_form");

        Optional<Banner> banner = paSystem.getBanners().getForId(uuid);

        if (banner.isPresent()) {
            showEditForm(BannerForm.fromBanner(banner.get()), context, CrudMode.UPDATE);
        } else {
            LOG.warn("No banner found for UUID: " + uuid);
            sendRedirect("");
        }
    }

    protected void showNewForm(Map<String, Object> context) {
        context.put("subpage", "banner_form");
        context.put("mode", "new");
    }

    protected void handleCreateOrUpdate(HttpServletRequest request, Map<String, Object> context, CrudMode mode) {
        String uuid = extractId(request);
        BannerForm bannerForm = BannerForm.fromRequest(uuid, request);

        long startTime = bannerForm.getStartTime();
        long endTime = bannerForm.getEndTime();

        if (!bannerForm.hasValidStartTime()) {
            addError("start_time", "invalid_time");
        }

        if (!bannerForm.hasValidEndTime()) {
            addError("end_time", "invalid_time");
        }

        if (!bannerForm.startTimeBeforeEndTime()) {
            addError("start_time", "start_time_after_end_time");
            addError("end_time", "start_time_after_end_time");
        }

        if (hasErrors()) {
            showEditForm(bannerForm, context, mode);
            return;
        }

        if (CrudMode.CREATE.equals(mode)) {
            paSystem.getBanners().createBanner(bannerForm.getMessage(),
                    bannerForm.getHosts(),
                    bannerForm.isDismissible(),
                    bannerForm.isActive(),
                    startTime,
                    endTime);
            flash("info", "banner_created");
        } else {
            paSystem.getBanners().updateBanner(bannerForm.getUuid(),
                    bannerForm.getMessage(),
                    bannerForm.getHosts(),
                    bannerForm.isDismissible(),
                    bannerForm.isActive(),
                    startTime,
                    endTime);
            flash("info", "banner_updated");
        }

        sendRedirect("");
    }

    private void showEditForm(BannerForm bannerForm, Map<String, Object> context, CrudMode mode) {
        context.put("subpage", "banner_form");

        if (CrudMode.UPDATE.equals(mode)) {
            context.put("mode", "edit");
        } else {
            context.put("mode", "new");
        }

        context.put("banner", bannerForm);
    }
}
