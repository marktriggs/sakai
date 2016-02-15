package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.SessionManager;

public class GenerateBugReportHandler extends BasePortalHandler
{

  private static final String URL_FRAGMENT = "generatebugreport";
  private static final Log log = LogFactory.getLog(GenerateBugReportHandler.class);


  public GenerateBugReportHandler()
  {
    setUrlFragment(GenerateBugReportHandler.URL_FRAGMENT);
  }


  @Override
  public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
    throws PortalHandlerException
  {
    return NEXT;
  }

  @Override
  public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
    Session session) throws PortalHandlerException
  {
    if ((parts.length == 2) && (parts[1].equals(GenerateBugReportHandler.URL_FRAGMENT))) {
      if (!ServerConfigurationService.getBoolean("generatebugreport.enabled", false)) {
        log.info("No bug report generated because generatebugreport.enabled isn't set");
      } else if (SessionManager.getCurrentSessionUserId() == null) {
        log.info("No bug report generated because user isn't logged in");
      } else {
        throw new RuntimeException("Free bug report!");
      }
    }

    return NEXT;
  }
}
