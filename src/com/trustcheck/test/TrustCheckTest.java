package com.trustcheck.test;

import com.trustcheck.model.ScamReport;
import com.trustcheck.model.VerificationResult;
import com.trustcheck.model.VerificationStatus;
import com.trustcheck.repository.EntityRepository;
import com.trustcheck.repository.ReportRepository;
import com.trustcheck.service.KeywordDetector;
import com.trustcheck.service.VerificationService;
import com.trustcheck.util.JsonUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Automated test suite covering:
 * 1. Verified entity scenario (Layer 1 starter database match).
 * 2. Suspicious offer scenario (Layer 2 scam keyword detection).
 * 3. Imposter brand scam scenario (Known entity with scam keywords).
 * 4. Unverified entity scenario (Neither in DB nor triggering scam keywords).
 * 5. Community report submission, persistence, and querying (Layer 3).
 * 6. Edge cases: whitespace normalization, case insensitivity.
 */
public class TrustCheckTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("         RUNNING TRUSTCHECK COMPREHENSIVE TEST SUITE              ");
        System.out.println("==================================================================");

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("trustcheck_test_data");

            EntityRepository entityRepo = new EntityRepository();
            KeywordDetector keywordDetector = new KeywordDetector();
            ReportRepository reportRepo = new ReportRepository(tempDir.toString());
            VerificationService service = new VerificationService(entityRepo, keywordDetector, reportRepo);

            // Run test cases
            testExactMatchVerified(service);
            testCaseInsensitiveAndWhitespace(service);
            testScamKeywordsSuspicious(service);
            testImposterBrandScam(service);
            testPhishingDomainSpoofing(service);
            testUnverifiedEntity(service);
            testCommunityReportPersistenceAndQuery(reportRepo, service);
            testKeywordDetectorPatterns(keywordDetector);
            testJsonUtilities();

        } catch (Exception e) {
            System.err.println("Fatal test runner exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                         .sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .forEach(File::delete);
                } catch (Exception ignored) {}
            }
        }

        System.out.println("==================================================================");
        System.out.println(" TEST RESULTS: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String errorDetail) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> " + errorDetail);
            testsFailed++;
        }
    }

    private static void testExactMatchVerified(VerificationService service) {
        VerificationResult res = service.verify("Google", "Company", "Official Google Summer of Code application.");
        assertTrue("Exact Match Verified: Google", 
            res.getStatus() == VerificationStatus.VERIFIED && res.isLayer1Match(),
            "Expected VERIFIED, got: " + res.getStatus() + "; note: " + res.getPrimaryReason());
    }

    private static void testCaseInsensitiveAndWhitespace(VerificationService service) {
        VerificationResult res = service.verify("   inFoSYs   ", "Company", "");
        assertTrue("Case Insensitivity & Whitespace: '   inFoSYs   '", 
            res.getStatus() == VerificationStatus.VERIFIED && "Infosys".equalsIgnoreCase(res.getLayer1MatchedName()),
            "Expected VERIFIED with matched entity Infosys, got: " + res.getStatus());
    }

    private static void testScamKeywordsSuspicious(VerificationService service) {
        String scamText = "Congratulations! You are selected. Please pay registration fee of 1500 INR to receive offer letter.";
        VerificationResult res = service.verify("Apex Virtual Academy", "Internship", scamText);
        assertTrue("Scam Keyword Triggered: SUSPICIOUS", 
            res.getStatus() == VerificationStatus.SUSPICIOUS && res.isLayer2Triggered(),
            "Expected SUSPICIOUS due to 'pay registration fee', got: " + res.getStatus());
        assertTrue("Scam Keyword Phrases Flagged", 
            res.getLayer2Keywords().contains("pay registration fee"),
            "Expected keywords to contain 'pay registration fee'");
    }

    private static void testImposterBrandScam(VerificationService service) {
        // High-risk scenario: fraudster uses "Microsoft" name but demands refundable security deposit
        String scamText = "Microsoft Internship Offer: Demands refundable security deposit of 3000 INR for laptop kit.";
        VerificationResult res = service.verify("Microsoft", "Internship", scamText);
        assertTrue("Imposter Brand Alert: Known brand with scam keyword must be SUSPICIOUS", 
            res.getStatus() == VerificationStatus.SUSPICIOUS,
            "Expected SUSPICIOUS despite Microsoft in DB, got: " + res.getStatus());
        assertTrue("Imposter Explanation Present",
            res.getPrimaryReason().contains("Imposter") || res.getPrimaryReason().contains("scam"),
            "Primary reason should alert about imposter scam: " + res.getPrimaryReason());
    }

    private static void testPhishingDomainSpoofing(VerificationService service) {
        // High-risk scenario: claims Google but application link is google-jobs.xyz
        VerificationResult res = service.verify("Google", "Company", "Apply now", "https://google-jobs.xyz/apply");
        assertTrue("Phishing Detection: Fake domain spoofing Google must be SUSPICIOUS",
            res.getStatus() == VerificationStatus.SUSPICIOUS && res.isUrlPhishingTriggered(),
            "Expected SUSPICIOUS due to domain spoofing, got: " + res.getStatus());
        assertTrue("Domain Spoofing Flagged",
            res.getUrlReasons().stream().anyMatch(r -> r.contains("Domain Mismatch") || r.contains("Spoofing")),
            "Expected domain spoofing reason");
    }

    private static void testUnverifiedEntity(VerificationService service) {
        // STRICT RULE: Never label an entity verified solely because no scam keywords were detected!
        VerificationResult res = service.verify("Unknown Startup Innovators Ltd", "Company", "Standard Python developer internship.");
        assertTrue("Unknown Entity must be UNVERIFIED", 
            res.getStatus() == VerificationStatus.UNVERIFIED && !res.isLayer1Match() && !res.isLayer2Triggered(),
            "Expected UNVERIFIED, got: " + res.getStatus() + "; reason: " + res.getPrimaryReason());
    }

    private static void testCommunityReportPersistenceAndQuery(ReportRepository repo, VerificationService service) {
        int initialCount = repo.getReportCount();
        ScamReport created = repo.addReport(
            "Fraudulent Academy 2026", 
            "Internship", 
            "WhatsApp HR @ +91 9999999999", 
            "Charged 2500 for training certificate before issuing internship appointment.", 
            "https://fake-link.example/proof"
        );

        assertTrue("Community Report Added", created != null && repo.getReportCount() == initialCount + 1,
            "Report count should increment");

        List<ScamReport> searchResults = repo.searchAndFilter("Fraudulent Academy", "Internship", "All");
        assertTrue("Community Report Searchable", !searchResults.isEmpty(),
            "Should find newly added report by query");

        // Now test that verifying this reported entity reflects community alert
        VerificationResult verifyRes = service.verify("Fraudulent Academy 2026", "Internship", "");
        assertTrue("Community Alert in Verification Result", 
            verifyRes.getLayer3ReportCount() >= 1,
            "Verification result should reflect community reports count");
    }

    private static void testKeywordDetectorPatterns(KeywordDetector detector) {
        assertTrue("Pattern: guaranteed placement", 
            detector.scan("We offer 100% guaranteed placement in top tier firms.").isTriggered(),
            "Expected pattern to trigger");
        assertTrue("Pattern: telegram task scam", 
            detector.scan("Daily telegram task like videos to earn daily stipend.").isTriggered(),
            "Expected telegram task to trigger");
        assertTrue("Clean text should not trigger", 
            !detector.scan("Apply via our official university campus placement cell.").isTriggered(),
            "Clean text should not trigger");
    }

    private static void testJsonUtilities() {
        String json = "{\"entityName\":\"Sample Corp\",\"category\":\"Internship\",\"details\":\"Clean offer\"}";
        java.util.Map<String, String> parsed = JsonUtil.parseSimpleJsonObject(json);
        assertTrue("JSON Parser: entityName", "Sample Corp".equals(parsed.get("entityName")),
            "Expected 'Sample Corp', got: " + parsed.get("entityName"));
        assertTrue("JSON Parser: category", "Internship".equals(parsed.get("category")),
            "Expected 'Internship', got: " + parsed.get("category"));
    }
}
