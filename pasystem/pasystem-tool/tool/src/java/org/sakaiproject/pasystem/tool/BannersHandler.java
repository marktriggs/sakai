package org.sakaiproject.pasystem.tool;

import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.pasystem.api.PASystem;


public class BannersHandler implements Handler {

    private PASystem paSystem;

    public BannersHandler(PASystem pasystem) {
        this.paSystem = pasystem;
    }

    public boolean willHandle(HttpServletRequest request) {
        return (request.getPathInfo() == null);
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        context.put("subpage", "banners");
        context.put("banners", paSystem.getBanners().getAll());
        context.put("popups", paSystem.getPopups().getAll());

    }
}
