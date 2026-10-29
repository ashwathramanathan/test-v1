package com.trustcheck.repository;

import com.trustcheck.model.ScamReport;
import com.trustcheck.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Layer 3: Community Feedback Repository.
 * Persistently stores and retrieves user-submitted scam allegations using Java standard library.
 */
public class ReportRepository {
    private final Path storagePath;
    private final List<ScamReport> reports = new CopyOnWriteArrayList<>();

    public ReportRepository(String dataDirectoryPath) {
        this.storagePath = Paths.get(dataDirectoryPath, "community_reports.json");
        initStorage();
    }

    private void initStorage() {
        try {
            if (!Files.exists(storagePath.getParent())) {
                Files.createDirectories(storagePath.getParent());
            }

            if (Files.exists(storagePath) && Files.size(storagePath) > 0) {
                loadFromFile();
            } else {
                seedInitialReports();
                saveToFile();
            }
        } catch (IOException e) {
            System.err.println("[ReportRepository] Error initializing file storage: " + e.getMessage());
            seedInitialReports();
        }
    }

    private void seedInitialReports() {
        reports.clear();
        long now = System.currentTimeMillis();
        // Seed initial student-submitted community reports for testing and realism
        reports.add(new ScamReport("REP-101", "Apex Tech Global Innovations", "Internship", 
            "Telegram: @apex_hr_recruitment / +91 98765 43210", 
            "Offered remote Python internship without technical interview. Required payment of ₹1,800 as refundable security deposit for onboarding kit.", 
            "https://t.me/apex_hr_sample", now - 86400000L * 3, "COMMUNITY_FLAGGED"));

        reports.add(new ScamReport("REP-102", "EduCert International Academy", "Certificate", 
            "contact@educert-verify.online / WhatsApp", 
            "Promised instant AI Specialist certificate from Stanford without exam or coursework if paid $49 within 24 hours.", 
            "https://educert-verify.online/offer", now - 86400000L * 7, "COMMUNITY_FLAGGED"));

        reports.add(new ScamReport("REP-103", "FastTrack Placements & Internships", "Internship", 
            "fasttrackcareers24@gmail.com", 
            "Guaranteed 100% placement post 1-week internship. Demanded ₹3,500 registration fee before sending appointment letter.", 
            "https://drive.google.com/sample_offer", now - 86400000L * 12, "COMMUNITY_FLAGGED"));

        reports.add(new ScamReport("REP-104", "FutureEdge Solutions", "Internship", 
            "HR Sneha on WhatsApp (+91 87654 32109)", 
            "Asked student to like YouTube videos and rate hotel reviews as daily internship tasks, then demanded deposit for VIP withdrawal tier.", 
            "https://wa.me/sample_chat", now - 86400000L * 15, "COMMUNITY_FLAGGED"));
    }

    private synchronized void loadFromFile() {
        try {
            String content = new String(Files.readAllBytes(storagePath), StandardCharsets.UTF_8).trim();
            if (content.isEmpty() || !content.startsWith("[") || !content.endsWith("]")) {
                seedInitialReports();
                return;
            }

            // Parse list of reports
            List<ScamReport> loaded = new ArrayList<>();
            // Split by JSON object boundaries
            int depth = 0;
            int objStart = -1;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') {
                    if (depth == 0) objStart = i;
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && objStart != -1) {
                        String objStr = content.substring(objStart, i + 1);
                        Map<String, String> map = JsonUtil.parseSimpleJsonObject(objStr);
                        if (!map.isEmpty() && map.containsKey("entityName")) {
                            long ts = System.currentTimeMillis();
                            try {
                                if (map.containsKey("timestamp")) {
                                    ts = Long.parseLong(map.get("timestamp"));
                                }
                            } catch (NumberFormatException ignored) {}
                            loaded.add(new ScamReport(
                                map.getOrDefault("id", "REP-" + (100 + loaded.size() + 1)),
                                map.getOrDefault("entityName", "Unknown"),
                                map.getOrDefault("category", "Internship"),
                                map.getOrDefault("contactDetails", ""),
                                map.getOrDefault("description", ""),
                                map.getOrDefault("evidenceUrl", ""),
                                ts,
                                map.getOrDefault("status", "COMMUNITY_FLAGGED")
                            ));
                        }
                        objStart = -1;
                    }
                }
            }

            reports.clear();
            reports.addAll(loaded);
        } catch (Exception e) {
            System.err.println("[ReportRepository] Error parsing storage file: " + e.getMessage());
            seedInitialReports();
        }
    }

    private synchronized void saveToFile() {
        try {
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < reports.size(); i++) {
                if (i > 0) sb.append(",\n");
                sb.append("  ").append(JsonUtil.reportToJson(reports.get(i)));
            }
            sb.append("\n]");
            Files.write(storagePath, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("[ReportRepository] Failed to persist reports: " + e.getMessage());
        }
    }

    public synchronized ScamReport addReport(String entityName, String category, String contactDetails, 
                                            String description, String evidenceUrl) {
        String id = "REP-" + (reports.size() + 101);
        ScamReport report = new ScamReport(
            id,
            entityName,
            category,
            contactDetails,
            description,
            evidenceUrl,
            System.currentTimeMillis(),
            "COMMUNITY_FLAGGED"
        );
        // Add to front of list
        reports.add(0, report);
        saveToFile();
        return report;
    }

    public List<ScamReport> getAllReports() {
        return new ArrayList<>(reports);
    }

    public int getReportCount() {
        return reports.size();
    }

    public List<ScamReport> searchAndFilter(String query, String category, String status) {
        List<ScamReport> results = new ArrayList<>();
        for (ScamReport r : reports) {
            boolean matchesQ = query == null || query.trim().isEmpty() || r.matchesQuery(query);
            boolean matchesCat = category == null || category.equalsIgnoreCase("All") || category.trim().isEmpty() 
                                 || r.getCategory().equalsIgnoreCase(category.trim());
            boolean matchesStat = status == null || status.equalsIgnoreCase("All") || status.trim().isEmpty() 
                                  || r.getStatus().equalsIgnoreCase(status.trim());

            if (matchesQ && matchesCat && matchesStat) {
                results.add(r);
            }
        }
        return results;
    }

    public List<ScamReport> findByEntityName(String entityName) {
        List<ScamReport> matches = new ArrayList<>();
        if (entityName == null || entityName.trim().isEmpty()) return matches;
        String q = entityName.trim().toLowerCase();
        for (ScamReport r : reports) {
            if (r.getEntityName().toLowerCase().contains(q) || q.contains(r.getEntityName().toLowerCase())) {
                matches.add(r);
            }
        }
        return matches;
    }
}
