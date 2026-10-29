package com.trustcheck.service;

import com.trustcheck.model.Entity;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Phishing & Malicious Link Detection Engine.
 * Analyzes offer links, application URLs, and domains for spoofing,
 * suspicious TLDs, anonymous channels, and credential harvesting.
 */
public class UrlPhishingDetector {

    public static class PhishingAnalysisResult {
        private final boolean triggered;
        private final String analyzedUrl;
        private final String extractedDomain;
        private final String riskLevel; // "HIGH", "MEDIUM", "SAFE"
        private final String domainMatchStatus; // "MATCHES_OFFICIAL", "SPOOFED_MISMATCH", "UNVERIFIED_DOMAIN"
        private final List<String> flaggedReasons;
        private final String summaryNote;

        public PhishingAnalysisResult(boolean triggered, String analyzedUrl, String extractedDomain,
                                      String riskLevel, String domainMatchStatus,
                                      List<String> flaggedReasons, String summaryNote) {
            this.triggered = triggered;
            this.analyzedUrl = analyzedUrl != null ? analyzedUrl : "";
            this.extractedDomain = extractedDomain != null ? extractedDomain : "";
            this.riskLevel = riskLevel != null ? riskLevel : "SAFE";
            this.domainMatchStatus = domainMatchStatus != null ? domainMatchStatus : "UNVERIFIED_DOMAIN";
            this.flaggedReasons = flaggedReasons != null ? flaggedReasons : Collections.emptyList();
            this.summaryNote = summaryNote != null ? summaryNote : "";
        }

        public boolean isTriggered() { return triggered; }
        public String getAnalyzedUrl() { return analyzedUrl; }
        public String getExtractedDomain() { return extractedDomain; }
        public String getRiskLevel() { return riskLevel; }
        public String getDomainMatchStatus() { return domainMatchStatus; }
        public List<String> getFlaggedReasons() { return flaggedReasons; }
        public String getSummaryNote() { return summaryNote; }
    }

    private static final Set<String> HIGH_RISK_TLDS = new HashSet<>(Arrays.asList(
        "xyz", "top", "online", "site", "buzz", "club", "tk", "ml", "ga", "cf", "work", "cc", "fit"
    ));

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    public PhishingAnalysisResult analyze(String urlStr, Entity matchedEntity) {
        if (urlStr == null || urlStr.trim().isEmpty()) {
            return new PhishingAnalysisResult(false, "", "", "SAFE", "NO_URL_PROVIDED", 
                Collections.emptyList(), "No URL submitted for phishing inspection.");
        }

        String rawUrl = urlStr.trim();
        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            rawUrl = "https://" + rawUrl;
        }

        List<String> reasons = new ArrayList<>();
        boolean isHighRisk = false;
        String domain = "";

        try {
            URI uri = new URI(rawUrl);
            domain = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
        } catch (Exception e) {
            reasons.add("Malformed URL format: unable to parse host domain.");
            return new PhishingAnalysisResult(true, rawUrl, "", "MEDIUM", "MALFORMED_URL", reasons, "Malformed URL.");
        }

        // Check 1: Insecure HTTP
        if (rawUrl.startsWith("http://")) {
            reasons.add("Insecure Connection: The link uses unencrypted HTTP instead of secure HTTPS.");
        }

        // Check 2: Raw IP address hostname
        if (IP_PATTERN.matcher(domain).matches()) {
            reasons.add("Raw IP Address: The link points directly to a raw numeric IP address rather than a registered corporate domain.");
            isHighRisk = true;
        }

        // Check 3: Free / Generic form platforms for enterprise recruitment
        if (domain.contains("forms.gle") || domain.contains("docs.google.com") && rawUrl.contains("forms")) {
            reasons.add("Generic Form Platform: Link is a free Google Form. Established enterprises use authenticated applicant tracking systems (e.g. Workday, Greenhouse, Taleo), not anonymous free forms.");
        }

        // Check 4: Anonymous Chat / Messaging redirects
        if (domain.contains("t.me") || domain.contains("telegram.me")) {
            reasons.add("Telegram Channel Redirect: Official hiring is never routed through anonymous Telegram links.");
            isHighRisk = true;
        }
        if (domain.contains("wa.me") || domain.contains("api.whatsapp.com")) {
            reasons.add("WhatsApp Chat Link: Job application routes directly to a personal WhatsApp chat.");
        }

        // Check 5: Suspicious TLD detection
        int lastDot = domain.lastIndexOf('.');
        if (lastDot > 0 && lastDot < domain.length() - 1) {
            String tld = domain.substring(lastDot + 1);
            if (HIGH_RISK_TLDS.contains(tld)) {
                reasons.add("High-Risk Domain Extension: Domain uses ." + tld + ", an inexpensive TLD frequently abused in recruitment phishing campaigns.");
            }
        }

        // Check 6: Domain Spoofing against Layer 1 Matched Entity
        String matchStatus = "UNVERIFIED_DOMAIN";
        if (matchedEntity != null && matchedEntity.getOfficialDomain() != null) {
            String official = matchedEntity.getOfficialDomain().toLowerCase().replace("www.", "");
            if (domain.equals(official) || domain.endsWith("." + official)) {
                matchStatus = "MATCHES_OFFICIAL";
            } else {
                matchStatus = "SPOOFED_MISMATCH";
                reasons.add("Domain Mismatch / Spoofing Alert: You selected '" + matchedEntity.getName() + 
                    "', whose official authenticated domain is '" + official + 
                    "'. The submitted link points to '" + domain + "'. This is a strong indicator of brand impersonation.");
                isHighRisk = true;
            }
        }

        boolean triggered = !reasons.isEmpty();
        String risk = isHighRisk ? "HIGH" : (triggered ? "MEDIUM" : "SAFE");
        String summary = triggered ? 
            String.format("Flagged %d phishing/spoofing warning signal(s) on domain '%s'.", reasons.size(), domain) :
            "Link domain '" + domain + "' passed preliminary phishing and spoofing checks.";

        return new PhishingAnalysisResult(triggered, rawUrl, domain, risk, matchStatus, reasons, summary);
    }
}
