package com.trustcheck.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Represents a community-submitted report (Layer 3).
 * Allegations submitted by students/users to warn peers.
 */
public class ScamReport {
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final String id;
    private final String entityName;
    private final String category; // "Internship", "Certificate", "Company"
    private final String contactDetails; // Website, phone, WhatsApp, telegram
    private final String description;
    private final String evidenceUrl;
    private final long timestamp;
    private final String status; // "COMMUNITY_FLAGGED", "UNDER_REVIEW"

    public ScamReport(String id, String entityName, String category, String contactDetails,
                      String description, String evidenceUrl, long timestamp, String status) {
        this.id = id;
        this.entityName = entityName != null ? entityName.trim() : "";
        this.category = category != null ? category.trim() : "Internship";
        this.contactDetails = contactDetails != null ? contactDetails.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.evidenceUrl = evidenceUrl != null ? evidenceUrl.trim() : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.status = status != null ? status : "COMMUNITY_FLAGGED";
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getCategory() {
        return category;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public String getDescription() {
        return description;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedDate() {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    public String getStatus() {
        return status;
    }

    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        return entityName.toLowerCase().contains(q) ||
               description.toLowerCase().contains(q) ||
               contactDetails.toLowerCase().contains(q);
    }
}
