package com.trustcheck.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete evaluation from all three verification layers.
 */
public class VerificationResult {
    private final String entityName;
    private final String category;
    private final VerificationStatus status;
    private final String primaryReason;
    private final List<String> detailedReasons;

    // Layer 1: Verified Database
    private final boolean layer1Match;
    private final String layer1MatchType; // "EXACT", "PARTIAL", "NONE"
    private final String layer1MatchedName;
    private final String layer1Domain;
    private final String layer1Note;

    // Layer 2: Scam Keyword Detection
    private final boolean layer2Triggered;
    private final List<String> layer2Keywords;
    private final String layer2RiskLevel; // "HIGH", "MEDIUM", "NONE"
    private final String layer2Note;

    // Layer 3: Community Reports
    private final int layer3ReportCount;
    private final List<ScamReport> layer3Reports;
    private final String layer3Note;

    // URL & Phishing Inspection
    private final String urlAnalyzed;
    private final boolean urlPhishingTriggered;
    private final String urlRiskLevel;
    private final String urlDomainMatchStatus;
    private final List<String> urlReasons;
    private final String urlNote;

    // Transparent Confidence & Limitations
    private final String confidenceBreakdown;
    private final String limitationsNotice;

    public VerificationResult(Builder builder) {
        this.entityName = builder.entityName;
        this.category = builder.category;
        this.status = builder.status;
        this.primaryReason = builder.primaryReason;
        this.detailedReasons = Collections.unmodifiableList(builder.detailedReasons);

        this.layer1Match = builder.layer1Match;
        this.layer1MatchType = builder.layer1MatchType;
        this.layer1MatchedName = builder.layer1MatchedName;
        this.layer1Domain = builder.layer1Domain;
        this.layer1Note = builder.layer1Note;

        this.layer2Triggered = builder.layer2Triggered;
        this.layer2Keywords = Collections.unmodifiableList(builder.layer2Keywords);
        this.layer2RiskLevel = builder.layer2RiskLevel;
        this.layer2Note = builder.layer2Note;

        this.layer3ReportCount = builder.layer3ReportCount;
        this.layer3Reports = Collections.unmodifiableList(builder.layer3Reports);
        this.layer3Note = builder.layer3Note;

        this.urlAnalyzed = builder.urlAnalyzed;
        this.urlPhishingTriggered = builder.urlPhishingTriggered;
        this.urlRiskLevel = builder.urlRiskLevel;
        this.urlDomainMatchStatus = builder.urlDomainMatchStatus;
        this.urlReasons = Collections.unmodifiableList(builder.urlReasons);
        this.urlNote = builder.urlNote;

        this.confidenceBreakdown = builder.confidenceBreakdown;
        this.limitationsNotice = builder.limitationsNotice;
    }

    public String getEntityName() { return entityName; }
    public String getCategory() { return category; }
    public VerificationStatus getStatus() { return status; }
    public String getPrimaryReason() { return primaryReason; }
    public List<String> getDetailedReasons() { return detailedReasons; }

    public boolean isLayer1Match() { return layer1Match; }
    public String getLayer1MatchType() { return layer1MatchType; }
    public String getLayer1MatchedName() { return layer1MatchedName; }
    public String getLayer1Domain() { return layer1Domain; }
    public String getLayer1Note() { return layer1Note; }

    public boolean isLayer2Triggered() { return layer2Triggered; }
    public List<String> getLayer2Keywords() { return layer2Keywords; }
    public String getLayer2RiskLevel() { return layer2RiskLevel; }
    public String getLayer2Note() { return layer2Note; }

    public int getLayer3ReportCount() { return layer3ReportCount; }
    public List<ScamReport> getLayer3Reports() { return layer3Reports; }
    public String getLayer3Note() { return layer3Note; }

    public String getUrlAnalyzed() { return urlAnalyzed; }
    public boolean isUrlPhishingTriggered() { return urlPhishingTriggered; }
    public String getUrlRiskLevel() { return urlRiskLevel; }
    public String getUrlDomainMatchStatus() { return urlDomainMatchStatus; }
    public List<String> getUrlReasons() { return urlReasons; }
    public String getUrlNote() { return urlNote; }

    public String getConfidenceBreakdown() { return confidenceBreakdown; }
    public String getLimitationsNotice() { return limitationsNotice; }

    public static class Builder {
        private String entityName = "";
        private String category = "Internship";
        private VerificationStatus status = VerificationStatus.UNVERIFIED;
        private String primaryReason = "";
        private List<String> detailedReasons = new ArrayList<>();

        private boolean layer1Match = false;
        private String layer1MatchType = "NONE";
        private String layer1MatchedName = "";
        private String layer1Domain = "";
        private String layer1Note = "";

        private boolean layer2Triggered = false;
        private List<String> layer2Keywords = new ArrayList<>();
        private String layer2RiskLevel = "NONE";
        private String layer2Note = "";

        private int layer3ReportCount = 0;
        private List<ScamReport> layer3Reports = new ArrayList<>();
        private String layer3Note = "";

        private String urlAnalyzed = "";
        private boolean urlPhishingTriggered = false;
        private String urlRiskLevel = "SAFE";
        private String urlDomainMatchStatus = "NONE";
        private List<String> urlReasons = new ArrayList<>();
        private String urlNote = "";

        private String confidenceBreakdown = "";
        private String limitationsNotice = "";

        public Builder entityName(String entityName) { this.entityName = entityName; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder status(VerificationStatus status) { this.status = status; return this; }
        public Builder primaryReason(String reason) { this.primaryReason = reason; return this; }
        public Builder addReason(String reason) { this.detailedReasons.add(reason); return this; }

        public Builder layer1Match(boolean match) { this.layer1Match = match; return this; }
        public Builder layer1MatchType(String type) { this.layer1MatchType = type; return this; }
        public Builder layer1MatchedName(String name) { this.layer1MatchedName = name; return this; }
        public Builder layer1Domain(String domain) { this.layer1Domain = domain; return this; }
        public Builder layer1Note(String note) { this.layer1Note = note; return this; }

        public Builder layer2Triggered(boolean triggered) { this.layer2Triggered = triggered; return this; }
        public Builder layer2Keywords(List<String> keywords) { this.layer2Keywords = keywords; return this; }
        public Builder layer2RiskLevel(String riskLevel) { this.layer2RiskLevel = riskLevel; return this; }
        public Builder layer2Note(String note) { this.layer2Note = note; return this; }

        public Builder layer3ReportCount(int count) { this.layer3ReportCount = count; return this; }
        public Builder layer3Reports(List<ScamReport> reports) { this.layer3Reports = reports; return this; }
        public Builder layer3Note(String note) { this.layer3Note = note; return this; }

        public Builder urlAnalyzed(String url) { this.urlAnalyzed = url; return this; }
        public Builder urlPhishingTriggered(boolean pt) { this.urlPhishingTriggered = pt; return this; }
        public Builder urlRiskLevel(String rl) { this.urlRiskLevel = rl; return this; }
        public Builder urlDomainMatchStatus(String dms) { this.urlDomainMatchStatus = dms; return this; }
        public Builder urlReasons(List<String> ur) { this.urlReasons = ur; return this; }
        public Builder urlNote(String un) { this.urlNote = un; return this; }

        public Builder confidenceBreakdown(String cb) { this.confidenceBreakdown = cb; return this; }
        public Builder limitationsNotice(String ln) { this.limitationsNotice = ln; return this; }

        public VerificationResult build() {
            return new VerificationResult(this);
        }
    }
}
