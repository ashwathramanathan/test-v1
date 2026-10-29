package com.trustcheck.model;

/**
 * Three distinct verification statuses for TrustCheck.
 * Rules:
 * - VERIFIED: Entity exists in the curated verified database and contains no scam triggers.
 * - SUSPICIOUS: Triggered by scam keywords, imposter fee demands, or community scam flags.
 * - UNVERIFIED: Entity is not in the starter database and no scam keywords were detected.
 *   (Never label an entity verified solely because no scam keywords were detected).
 */
public enum VerificationStatus {
    VERIFIED("VERIFIED", "Confirmed legitimate entity in verified repository with no scam markers.", "#10B981"),
    SUSPICIOUS("SUSPICIOUS", "High-risk signals detected (scam keywords, fee demands, or fraud reports).", "#EF4444"),
    UNVERIFIED("UNVERIFIED", "Not found in starter database. Cannot confirm legitimacy or intent.", "#F59E0B");

    private final String label;
    private final String defaultDescription;
    private final String colorHex;

    VerificationStatus(String label, String defaultDescription, String colorHex) {
        this.label = label;
        this.defaultDescription = defaultDescription;
        this.colorHex = colorHex;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public String getColorHex() {
        return colorHex;
    }
}
