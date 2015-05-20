package org.sakaiproject.pasystem.api;

public interface Popup {
    public boolean isActive();
    public String getUuid();
    public String getTemplate();

    public String getDescriptor();
    public long getStartTime();
    public long getEndTime();
}
