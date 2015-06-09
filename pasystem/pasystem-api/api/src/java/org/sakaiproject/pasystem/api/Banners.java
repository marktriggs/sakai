package org.sakaiproject.pasystem.api;

import java.util.List;
import java.util.Optional;

public interface Banners extends Acknowledger {

    public List<Banner> getRelevantAlerts(String serverId, String userEid);

    public String createBanner(Banner banner);

    public void updateBanner(String uuid, Banner banner);

    public void deleteBanner(String uuid);

    public List<Banner> getAll();

    public void clearTemporaryDismissedForUser(String userEid);

    public Optional<Banner> getForId(String uuid);
}
    
