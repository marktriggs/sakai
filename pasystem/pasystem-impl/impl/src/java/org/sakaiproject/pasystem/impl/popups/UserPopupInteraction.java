package org.sakaiproject.pasystem.impl.popups;

import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.component.cover.ServerConfigurationService;

import java.sql.Connection;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.sakaiproject.pasystem.impl.common.DB;
import org.sakaiproject.pasystem.impl.common.DBAction;
import org.sakaiproject.pasystem.impl.common.DBConnection;
import org.sakaiproject.pasystem.impl.common.DBResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPopupInteraction {

    private static final Logger LOG = LoggerFactory.getLogger(UserPopupInteraction.class);

    private User user;
    private String eid;

    public UserPopupInteraction(User currentUser) {
        user = currentUser;

        if (user != null && user.getEid() != null) {
            eid = user.getEid().toLowerCase();
        } else {
            user = null;
        }
    }

    public Popup getPopup() {
        if (user == null) {
            // No user.
            return Popup.createNullPopup();
        }

        String sql = ("SELECT splash.uuid, content.template_content " +

                // Find a splash screen
                " FROM PASYSTEM_SPLASH_SCREENS splash" +

                // And its content
                " INNER JOIN PASYSTEM_SPLASH_CONTENT content on content.uuid = splash.uuid" +

                // That is either assigned to the current user, or open to all
                " LEFT OUTER JOIN PASYSTEM_SPLASH_ASSIGN assign " +
                " on assign.uuid = splash.uuid AND (lower(assign.user_eid) = ? OR assign.open_campaign = 1)" +

                // Which the current user hasn't yet dismissed
                " LEFT OUTER JOIN PASYSTEM_SPLASH_DISMISSED dismissed " +
                " on dismissed.uuid = splash.uuid AND lower(dismissed.user_eid) = ?" +

                " WHERE " +

                // It's assigned to us
                " assign.uuid IS NOT NULL AND " +

                // And currently active
                " splash.start_time <= ? AND " +
                " splash.end_time > ? AND " +

                // And either hasn't been dismissed yet
                " (dismissed.state is NULL OR" +

                // Or was dismissed temporarily, but some time has passed
                "  (dismissed.state = 'temporary' AND" +
                "   (? - dismissed.dismiss_time) >= ?))");

        try {
            long now = System.currentTimeMillis();

            return DB.transaction
                    ("Find a popup for the current user",
                            new DBAction<Popup>() {
                                public Popup call(DBConnection db) throws SQLException {
                                    try (DBResults results = db.run(sql)
                                            .param(eid).param(eid)
                                            .param(now).param(now).param(now)
                                            .param(getTemporaryTimeoutMilliseconds())
                                            .executeQuery()) {
                                        for (ResultSet result : results) {
                                            Clob contentClob = result.getClob(2);
                                            String templateContent = contentClob.getSubString(1, (int)contentClob.length());

                                            // Got one!
                                            return Popup.createPopup(result.getString(1), templateContent);
                                        }

                                        // Otherwise, no suitable popup was found
                                        return Popup.createNullPopup();
                                    }
                                }
                            });
        } catch (Exception e) {
            LOG.error("Error determining active popup", e);
            return Popup.createNullPopup();
        }
    }

    public void acknowledge(String campaign, String acknowledgement) {
        if (user == null) {
            return;
        }

        final String mappedAcknowledgement = ("permanent".equals(acknowledgement) ? "permanent" : "temporary");

        try {
            DB.transaction
                    ("Acknowledge campaign for user",
                            new DBAction<Void>() {
                                public Void call(DBConnection db) throws SQLException {
                                    deleteExistingEntry(db, campaign);
                                    insertNewEntry(db, campaign, mappedAcknowledgement);
                                    db.commit();

                                    return null;
                                }
                            }
                    );
        } catch (Exception e) {
            LOG.error("Error acknowledging popup", e);
        }
    }

    private int getTemporaryTimeoutMilliseconds() {
        return ServerConfigurationService.getInt("popup.temporary-timeout-ms", (24 * 60 * 60 * 1000));
    }

    private boolean deleteExistingEntry(DBConnection db, String campaign) throws SQLException {
        int updatedRows = db.run("DELETE FROM PASYSTEM_SPLASH_DISMISSED where lower(user_eid) = ? AND campaign = ?")
                .param(eid)
                .param(campaign)
                .executeUpdate();

        return (updatedRows > 0);
    }

    private boolean insertNewEntry(DBConnection db, String campaign, String acknowledgement) throws SQLException {
        long now = System.currentTimeMillis();
        int updatedRows = db.run("INSERT INTO PASYSTEM_SPLASH_DISMISSED (user_eid, campaign, state, dismiss_time) VALUES (?, ?, ?, ?)")
                .param(eid)
                .param(campaign)
                .param(acknowledgement)
                .param(now)
                .executeUpdate();

        return (updatedRows > 0);
    }
}
