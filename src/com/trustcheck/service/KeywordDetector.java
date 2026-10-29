package com.trustcheck.service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Layer 2: Scam Keyword & Heuristic Pattern Detection Engine.
 * Scans internship offers, job descriptions, and communication texts for known scam signatures.
 * 
 * IMPORTANT: Keyword matches are warning signals (heuristics), not conclusive proof of fraud.
 */
public class KeywordDetector {

    public static class DetectionRule {
        private final String phrase;
        private final String category; // "FEE_DEMAND", "FAKE_OFFER", "TASK_SCAM", "UNPROFESSIONAL_COMMUNICATION"
        private final String severity; // "HIGH", "MEDIUM"
        private final String explanation;
        private final Pattern pattern;

        public DetectionRule(String phrase, String category, String severity, String explanation) {
            this.phrase = phrase.trim().toLowerCase();
            this.category = category;
            this.severity = severity;
            this.explanation = explanation;

            String[] words = this.phrase.split("\\s+");
            StringBuilder sb = new StringBuilder("(?i)(?<=^|\\W)");
            for (int i = 0; i < words.length; i++) {
                if (i > 0) sb.append("\\s+");
                sb.append(Pattern.quote(words[i]));
            }
            sb.append("(?=\\W|$)");
            this.pattern = Pattern.compile(sb.toString());
        }

        public String getPhrase() { return phrase; }
        public String getCategory() { return category; }
        public String getSeverity() { return severity; }
        public String getExplanation() { return explanation; }

        public boolean matches(String text) {
            if (text == null) return false;
            return pattern.matcher(text).find();
        }
    }

    public static class KeywordAnalysisResult {
        private final boolean triggered;
        private final List<String> flaggedPhrases;
        private final List<String> explanations;
        private final String riskLevel; // "HIGH", "MEDIUM", "NONE"
        private final String summaryNote;

        public KeywordAnalysisResult(boolean triggered, List<String> flaggedPhrases, 
                                     List<String> explanations, String riskLevel, String summaryNote) {
            this.triggered = triggered;
            this.flaggedPhrases = flaggedPhrases != null ? flaggedPhrases : Collections.emptyList();
            this.explanations = explanations != null ? explanations : Collections.emptyList();
            this.riskLevel = riskLevel != null ? riskLevel : "NONE";
            this.summaryNote = summaryNote != null ? summaryNote : "";
        }

        public boolean isTriggered() { return triggered; }
        public List<String> getFlaggedPhrases() { return flaggedPhrases; }
        public List<String> getExplanations() { return explanations; }
        public String getRiskLevel() { return riskLevel; }
        public String getSummaryNote() { return summaryNote; }
    }

    private final List<DetectionRule> rules = new ArrayList<>();

    public KeywordDetector() {
        initRules();
    }

    private void initRules() {
        // High-severity fee demands
        rules.add(new DetectionRule("pay registration fee", "FEE_DEMAND", "HIGH",
            "Legitimate companies never require candidates to pay a registration or application fee."));
        rules.add(new DetectionRule("registration fee", "FEE_DEMAND", "HIGH",
            "Requesting registration fees for interviews or internships is a primary indicator of recruitment fraud."));
        rules.add(new DetectionRule("pay to receive an offer letter", "FEE_DEMAND", "HIGH",
            "Official employment/internship offer letters are never sold or conditional upon upfront payment."));
        rules.add(new DetectionRule("pay for offer letter", "FEE_DEMAND", "HIGH",
            "Demanding payment prior to issuing an appointment letter is a classic fraudulent practice."));
        rules.add(new DetectionRule("refundable security deposit", "FEE_DEMAND", "HIGH",
            "Scammers commonly promise 'refundable' laptop, onboarding, or training deposits that are never returned."));
        rules.add(new DetectionRule("security deposit", "FEE_DEMAND", "HIGH",
            "Demanding a security deposit for intern onboarding or equipment is illegal under standard employment laws."));
        rules.add(new DetectionRule("processing fee", "FEE_DEMAND", "HIGH",
            "Charging students a 'file processing' or 'HR administrative' fee is indicative of an advance-fee scam."));
        rules.add(new DetectionRule("pay for certificate", "FEE_DEMAND", "HIGH",
            "Offers that sell completion certificates without rigorous evaluation or coursework undermine authenticity."));
        rules.add(new DetectionRule("crypto payment", "FEE_DEMAND", "HIGH",
            "Requests for cryptocurrency or untraceable payments are virtually always fraudulent."));
        rules.add(new DetectionRule("upi to personal number", "FEE_DEMAND", "HIGH",
            "Directing payments to individual personal UPI IDs instead of registered corporate escrow accounts is suspicious."));

        // High-severity fake selection guarantees
        rules.add(new DetectionRule("guaranteed placement", "FAKE_OFFER", "HIGH",
            "No credible enterprise guarantees job placement without rigorous evaluation and competitive assessment."));
        rules.add(new DetectionRule("100% placement guarantee", "FAKE_OFFER", "HIGH",
            "100% guaranteed placement claims are deceptive marketing tactics used by fraudulent placement agencies."));
        rules.add(new DetectionRule("guaranteed job without an interview", "FAKE_OFFER", "HIGH",
            "Legitimate technical organizations always conduct technical screening or interviews prior to selection."));
        rules.add(new DetectionRule("direct selection without interview", "FAKE_OFFER", "HIGH",
            "Direct selection without formal interviews or tests is a tactic to rush students into paying onboarding fees."));
        rules.add(new DetectionRule("no interview required", "FAKE_OFFER", "HIGH",
            "Lack of any interview process indicates indiscriminate mass-recruitment fraud."));

        // Unofficial communication & Task scams
        rules.add(new DetectionRule("telegram task", "TASK_SCAM", "HIGH",
            "Telegram-based task scams pay small amounts for rating products before stealing large sums in 'recharge traps'."));
        rules.add(new DetectionRule("daily rating task", "TASK_SCAM", "HIGH",
            "Assigning YouTube likes or Google map reviews as 'internship tasks' is characteristic of cyber-crime syndicates."));
        rules.add(new DetectionRule("whatsapp hr", "UNPROFESSIONAL_COMMUNICATION", "MEDIUM",
            "Official corporate recruitment does not conduct initial hiring solely through unofficial personal WhatsApp contacts."));
        rules.add(new DetectionRule("contact on telegram", "UNPROFESSIONAL_COMMUNICATION", "MEDIUM",
            "Conducting formal enterprise recruitment exclusively over Telegram is a strong warning indicator."));
        rules.add(new DetectionRule("training fee deduction", "FEE_DEMAND", "HIGH",
            "Mandating upfront training fees or selling training modules disguised as an internship offer."));
    }

    public List<DetectionRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public int getRuleCount() {
        return rules.size();
    }

    /**
     * Analyzes any arbitrary input text (entity name, offer description, email excerpt).
     */
    public KeywordAnalysisResult scan(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new KeywordAnalysisResult(false, Collections.emptyList(), 
                Collections.emptyList(), "NONE", "No text provided for scam keyword scanning.");
        }

        List<String> triggeredPhrases = new ArrayList<>();
        List<String> explanations = new ArrayList<>();
        boolean hasHigh = false;

        for (DetectionRule rule : rules) {
            if (rule.matches(text)) {
                triggeredPhrases.add(rule.getPhrase());
                explanations.add("Triggered '" + rule.getPhrase() + "': " + rule.getExplanation());
                if ("HIGH".equals(rule.getSeverity())) {
                    hasHigh = true;
                }
            }
        }

        if (triggeredPhrases.isEmpty()) {
            return new KeywordAnalysisResult(false, Collections.emptyList(), Collections.emptyList(), "NONE",
                "No known scam warning phrases detected. (Note: Lack of keywords does not automatically guarantee authenticity).");
        }

        String risk = hasHigh ? "HIGH" : "MEDIUM";
        String summary = String.format("Found %d high-risk keyword warning signature(s). These patterns frequently appear in student recruitment scams.",
            triggeredPhrases.size());

        return new KeywordAnalysisResult(true, triggeredPhrases, explanations, risk, summary);
    }
}
