package com.trustcheck.service;

import com.trustcheck.model.*;
import com.trustcheck.repository.EntityRepository;
import com.trustcheck.repository.EntityRepository.EntityMatch;
import com.trustcheck.repository.ReportRepository;

import java.util.*;

/**
 * Three-Layer Verification Engine:
 * Layer 1: Curated starter database of known entities.
 * Layer 2: Scam keyword & pattern detection.
 * Layer 3: Community feedback & user allegations.
 */
public class VerificationService {

    private final EntityRepository entityRepository;
    private final KeywordDetector keywordDetector;
    private final ReportRepository reportRepository;
    private final UrlPhishingDetector urlPhishingDetector;

    public VerificationService(EntityRepository entityRepository, 
                               KeywordDetector keywordDetector, 
                               ReportRepository reportRepository) {
        this.entityRepository = entityRepository;
        this.keywordDetector = keywordDetector;
        this.reportRepository = reportRepository;
        this.urlPhishingDetector = new UrlPhishingDetector();
    }

    public EntityRepository getEntityRepository() {
        return entityRepository;
    }

    public KeywordDetector getKeywordDetector() {
        return keywordDetector;
    }

    public ReportRepository getReportRepository() {
        return reportRepository;
    }

    public UrlPhishingDetector getUrlPhishingDetector() {
        return urlPhishingDetector;
    }

    public VerificationResult verify(String entityName, String category, String offerText) {
        return verify(entityName, category, offerText, "");
    }

    /**
     * Executes the comprehensive verification check across all layers + Phishing URL inspection.
     *
     * @param entityName Target company, internship provider, or certificate issuer.
     * @param category   "Internship", "Company", or "Certificate"
     * @param offerText  Optional text snippet (offer letter text, email body, JD, or chat notes)
     * @param offerUrl   Optional job/offer link or application form URL
     */
    public VerificationResult verify(String entityName, String category, String offerText, String offerUrl) {
        String cleanName = entityName != null ? entityName.trim() : "";
        String cleanCat = (category != null && !category.trim().isEmpty()) ? category.trim() : "Internship";
        String combinedScanText = cleanName + " " + (offerText != null ? offerText : "") + " " + (offerUrl != null ? offerUrl : "");

        VerificationResult.Builder builder = new VerificationResult.Builder()
            .entityName(cleanName)
            .category(cleanCat);

        // --- LAYER 1: Verified Database Match ---
        EntityMatch match = entityRepository.match(cleanName, cleanCat);
        builder.layer1Match(match.isFound())
               .layer1MatchType(match.getMatchType());

        if (match.isFound()) {
            Entity matchedEntity = match.getEntity();
            builder.layer1MatchedName(matchedEntity.getName())
                   .layer1Domain(matchedEntity.getOfficialDomain())
                   .layer1Note(match.getExplanation() + " Official domain: " + matchedEntity.getOfficialDomain() + 
                               ". " + matchedEntity.getVerificationNote());
        } else {
            builder.layer1MatchedName("")
                   .layer1Domain("")
                   .layer1Note(match.getExplanation());
        }

        // --- LAYER 2: Scam Keyword Detection ---
        KeywordDetector.KeywordAnalysisResult keywordResult = keywordDetector.scan(combinedScanText);
        builder.layer2Triggered(keywordResult.isTriggered())
               .layer2Keywords(keywordResult.getFlaggedPhrases())
               .layer2RiskLevel(keywordResult.getRiskLevel())
               .layer2Note(keywordResult.getSummaryNote());

        // --- LAYER 3: Community Feedback ---
        List<ScamReport> communityReports = reportRepository.findByEntityName(cleanName);
        builder.layer3ReportCount(communityReports.size())
               .layer3Reports(communityReports);

        if (!communityReports.isEmpty()) {
            builder.layer3Note("Found " + communityReports.size() + " community fraud/scam report(s) referencing '" + 
                               cleanName + "'. User allegations advise caution.");
        } else {
            builder.layer3Note("No community scam reports found on file for '" + cleanName + "'.");
        }

        // --- URL & PHISHING INSPECTION ---
        UrlPhishingDetector.PhishingAnalysisResult phishingResult = urlPhishingDetector.analyze(
            offerUrl != null && !offerUrl.trim().isEmpty() ? offerUrl : extractFirstUrl(offerText),
            match.isFound() ? match.getEntity() : null
        );

        builder.urlAnalyzed(phishingResult.getAnalyzedUrl())
               .urlPhishingTriggered(phishingResult.isTriggered())
               .urlRiskLevel(phishingResult.getRiskLevel())
               .urlDomainMatchStatus(phishingResult.getDomainMatchStatus())
               .urlReasons(phishingResult.getFlaggedReasons())
               .urlNote(phishingResult.getSummaryNote());

        // --- SYNTHESIS & FINAL DETERMINATION LOGIC ---

        // Case A: High Phishing Risk detected (Domain Spoofing / Malicious Link)
        if ("HIGH".equals(phishingResult.getRiskLevel())) {
            builder.status(VerificationStatus.SUSPICIOUS);
            builder.primaryReason("Phishing & Domain Alert: " + phishingResult.getSummaryNote() + " " +
                (phishingResult.getFlaggedReasons().isEmpty() ? "" : phishingResult.getFlaggedReasons().get(0)));
            for (String r : phishingResult.getFlaggedReasons()) {
                builder.addReason("Phishing Signal: " + r);
            }
            if (keywordResult.isTriggered()) {
                builder.addReason("Keyword warning: " + String.join(", ", keywordResult.getFlaggedPhrases()));
            }
        }
        // Case B: Scam Keywords detected OR Multiple Community Allegations
        else if (keywordResult.isTriggered() || communityReports.size() >= 2) {
            builder.status(VerificationStatus.SUSPICIOUS);

            if (match.isFound() && keywordResult.isTriggered()) {
                // Critical Imposter Alert: Known company name, but scam keywords detected in offer text!
                builder.primaryReason("Imposter Scam Warning: Matched legitimate brand '" + match.getEntity().getName() + 
                    "', but the offer text contains dangerous scam phrases (" + String.join(", ", keywordResult.getFlaggedPhrases()) + "). " +
                    "Legitimate organizations never ask candidates for fees or guaranteed placements.");
                builder.addReason("High risk of brand impersonation: Fraudsters often use legitimate corporate names to collect fake fees.");
            } else if (keywordResult.isTriggered()) {
                builder.primaryReason("High-risk scam signatures detected: Found warning phrases (" + 
                    String.join(", ", keywordResult.getFlaggedPhrases()) + ").");
            } else {
                builder.primaryReason("Multiple student scam reports on record (" + communityReports.size() + 
                    " community reports) flagging suspicious recruitment activities.");
            }

            for (String exp : keywordResult.getExplanations()) {
                builder.addReason(exp);
            }
            if (!communityReports.isEmpty()) {
                builder.addReason("Community feedback: " + communityReports.size() + " student allegation(s) documented.");
            }
            for (String r : phishingResult.getFlaggedReasons()) {
                builder.addReason("Link observation: " + r);
            }
        } 
        // Case C: In Verified Starter Database & No Scam Keywords & No Community Reports & Safe/Matching URL
        else if ("EXACT".equals(match.getMatchType()) && communityReports.isEmpty() && !"SPOOFED_MISMATCH".equals(phishingResult.getDomainMatchStatus())) {
            builder.status(VerificationStatus.VERIFIED);
            builder.primaryReason("Confirmed match in curated verified starter database with no scam triggers or fraud reports.");
            builder.addReason("Database Verification: " + match.getEntity().getName() + " is an authenticated entity.");
            builder.addReason("Legitimate Domain: Official communication must strictly come from *" + match.getEntity().getOfficialDomain() + "*.");
            builder.addReason("Safety Check: No fee demands or suspicious recruitment signatures detected.");
            if (phishingResult.isTriggered()) {
                for (String r : phishingResult.getFlaggedReasons()) {
                    builder.addReason("URL Warning: " + r);
                }
            }
        }
        else if ("PARTIAL".equals(match.getMatchType()) && communityReports.isEmpty()) {
            builder.status(VerificationStatus.UNVERIFIED);
            builder.primaryReason("Partial match with '" + match.getEntity().getName() + "', but exact identity could not be established.");
            builder.addReason("Partial Name Match: Matches verified entity '" + match.getEntity().getName() + 
                "', but name differs from the registered record. Verify if this is an official subsidiary.");
            builder.addReason("Unconfirmed Issuer: Not an exact match in the starter verified dataset.");
            builder.addReason("Keyword Check Passed: No scam phrases detected, but this alone does not verify legitimacy.");
        }
        // Case D: Neither in Verified Database nor triggering scam keywords
        else {
            builder.status(VerificationStatus.UNVERIFIED);
            builder.primaryReason("Entity '" + cleanName + "' was not found in the starter verified database. No scam keywords were detected.");
            builder.addReason("Starter Dataset Limitation: Not listed in our starter repository of " + entityRepository.getEntityCount() + " verified entities.");
            builder.addReason("Heuristic Limitation: No scam keywords detected, but lack of keywords does not prove authenticity.");
            builder.addReason("Recommendation: Contact the company via their official domain and verify with your college placement cell.");
        }

        // Transparent Confidence Breakdown
        int rulesChecked = keywordDetector.getRuleCount();
        int dbSize = entityRepository.getEntityCount();
        int reportPool = reportRepository.getReportCount();

        String confidenceCalc = String.format(
            "Evaluation evaluated against %d verified entities, %d scam keyword heuristics, and %d community records. " +
            "Determined purely through deterministic rule evaluation without synthetic scores.",
            dbSize, rulesChecked, reportPool
        );
        builder.confidenceBreakdown(confidenceCalc);

        String limitations = "TrustCheck's starter dataset is intentionally limited for demonstration. " +
            "Automated keyword detection cannot catch novel scam formats, and community reports represent student allegations. " +
            "Always verify offer letters through registered corporate domains.";
        builder.limitationsNotice(limitations);

        return builder.build();
    }

    private static String extractFirstUrl(String text) {
        if (text == null) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("https?://\\S+").matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "";
    }
}
