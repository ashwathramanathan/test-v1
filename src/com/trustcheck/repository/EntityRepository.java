package com.trustcheck.repository;

import com.trustcheck.model.Entity;

import java.util.*;

/**
 * Layer 1: Starter Database of Known Verified Entities.
 * 
 * IMPORTANT: This starter repository is explicitly maintained as a demonstration
 * dataset and does NOT claim to represent all legitimate companies or issuers worldwide.
 */
public class EntityRepository {

    public static class EntityMatch {
        private final String matchType; // "EXACT", "PARTIAL", "NONE"
        private final Entity entity;
        private final String explanation;

        public EntityMatch(String matchType, Entity entity, String explanation) {
            this.matchType = matchType;
            this.entity = entity;
            this.explanation = explanation;
        }

        public String getMatchType() { return matchType; }
        public Entity getEntity() { return entity; }
        public String getExplanation() { return explanation; }
        public boolean isFound() { return !"NONE".equals(matchType); }
    }

    private final Map<String, Entity> entityMap = new LinkedHashMap<>();

    public EntityRepository() {
        initStarterDataset();
    }

    private void initStarterDataset() {
        // Legitimate Technology & Global Enterprises
        addEntity(new Entity("ENT-001", "Google", "Company", "google.com",
            "Global technology and software enterprise. Official internships run through Google Careers and Summer of Code.",
            "Verified enterprise. Note: Google never charges application, interview, or equipment fees.",
            Arrays.asList("Google LLC", "Alphabet", "Google Careers", "Google Summer of Code", "GSoC")));

        addEntity(new Entity("ENT-002", "Microsoft", "Company", "microsoft.com",
            "Global technology corporation offering university student internships and technical certifications.",
            "Verified enterprise. Applications are accepted exclusively via careers.microsoft.com.",
            Arrays.asList("Microsoft Corporation", "Microsoft Learn", "MSFT")));

        addEntity(new Entity("ENT-003", "Tata Consultancy Services", "Company", "tcs.com",
            "Multinational information technology services and consulting company.",
            "Verified enterprise. Official hiring is conducted via TCS NextStep and official corporate emails.",
            Arrays.asList("TCS", "Tata Consultancy", "TCS iON", "TCS NextStep")));

        addEntity(new Entity("ENT-004", "Infosys", "Company", "infosys.com",
            "Global digital services and consulting leader (InStep Internship Program).",
            "Verified enterprise. Offers are dispatched from @infosys.com domains with zero registration charges.",
            Arrays.asList("Infosys Limited", "Infosys Springboard", "Infosys InStep")));

        addEntity(new Entity("ENT-005", "Amazon", "Company", "amazon.jobs",
            "Multinational technology company focusing on e-commerce, cloud computing (AWS), and AI.",
            "Verified enterprise. Amazon student programs never recruit via Telegram or demand security deposits.",
            Arrays.asList("Amazon Web Services", "AWS", "Amazon Development Center")));

        addEntity(new Entity("ENT-006", "Wipro", "Company", "wipro.com",
            "Multinational information technology, consulting and business process services company.",
            "Verified enterprise. Official communication originates strictly from @wipro.com.",
            Arrays.asList("Wipro Limited", "Wipro Elite")));

        addEntity(new Entity("ENT-007", "IBM", "Company", "ibm.com",
            "Global hybrid cloud, AI, and enterprise technology provider.",
            "Verified enterprise. Technical internships and digital badges issued through IBM SkillsBuild.",
            Arrays.asList("IBM India", "International Business Machines", "IBM SkillsBuild")));

        addEntity(new Entity("ENT-008", "Accenture", "Company", "accenture.com",
            "Global professional services company with leading capabilities in digital, cloud and security.",
            "Verified enterprise. Official onboarding requires direct portal access at accenture.com.",
            Arrays.asList("Accenture PLC", "Accenture Solutions")));

        addEntity(new Entity("ENT-009", "Cognizant", "Company", "cognizant.com",
            "Information technology services and consulting enterprise.",
            "Verified enterprise. Official offers are verified via Cognizant Careers candidate portal.",
            Arrays.asList("Cognizant Technology Solutions", "CTS")));

        addEntity(new Entity("ENT-010", "Cisco Systems", "Company", "cisco.com",
            "Worldwide leader in networking, cybersecurity, and hardware infrastructure.",
            "Verified enterprise. Certifications verifiable through Cisco Networking Academy portal.",
            Arrays.asList("Cisco", "Cisco Networking Academy", "NetAcad")));

        // Educational & Certification Issuers
        addEntity(new Entity("ENT-011", "Coursera", "Certificate", "coursera.org",
            "Global online learning platform offering university-backed courses and professional certificates.",
            "Verified credential issuer. Certificates feature cryptographic verification URLs (coursera.org/verify).",
            Arrays.asList("Coursera Inc", "Coursera Certificates")));

        addEntity(new Entity("ENT-012", "edX", "Certificate", "edx.org",
            "Higher-education learning platform founded by Harvard and MIT.",
            "Verified credential issuer. Verifiable certificates hosted directly under edx.org.",
            Arrays.asList("edX LLC", "HarvardX", "MITx")));

        addEntity(new Entity("ENT-013", "NPTEL", "Certificate", "nptel.ac.in",
            "National Programme on Technology Enhanced Learning by IITs and IISc (Govt. of India).",
            "Verified government academic body. Certificates verifiable via nptel.ac.in/noc.",
            Arrays.asList("SWAYAM NPTEL", "IIT NPTEL", "National Programme on Technology Enhanced Learning")));

        addEntity(new Entity("ENT-014", "ISRO", "Internship", "isro.gov.in",
            "Indian Space Research Organisation national space agency.",
            "Verified government agency. Student internships are arranged strictly through formal university MOUs.",
            Arrays.asList("Indian Space Research Organisation", "URSC ISRO", "VSSC ISRO")));

        addEntity(new Entity("ENT-015", "Oracle Academy", "Certificate", "oracle.com",
            "Oracle institutional computer science education program and certification authority.",
            "Verified certification provider. Official badges verified via CertView.",
            Arrays.asList("Oracle", "Oracle Certified Professional", "CertView")));

        addEntity(new Entity("ENT-016", "Linux Foundation", "Certificate", "linuxfoundation.org",
            "Non-profit consortium dedicated to fostering the growth of open source software.",
            "Verified open source credential issuer (LFCS, CKA, CKAD).",
            Arrays.asList("LF Training", "The Linux Foundation", "Cloud Native Computing Foundation", "CNCF")));

        addEntity(new Entity("ENT-017", "Google Cloud Certifications", "Certificate", "cloud.google.com/certification",
            "Professional and associate cloud architecture, data engineering, and security certificates.",
            "Verified credential provider. Digital badges verifiable via Credly/Accredible.",
            Arrays.asList("GCP Certifications", "Associate Cloud Engineer", "Professional Cloud Architect")));

        addEntity(new Entity("ENT-018", "DeepLearning.AI", "Certificate", "deeplearning.ai",
            "Leading AI and machine learning education platform founded by Andrew Ng.",
            "Verified certification provider. Issued in partnership with Coursera.",
            Arrays.asList("Deep Learning AI", "Andrew Ng ML")));
    }

    private void addEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
    }

    public List<Entity> getAllEntities() {
        return new ArrayList<>(entityMap.values());
    }

    public int getEntityCount() {
        return entityMap.size();
    }

    /**
     * Matches user input against starter database.
     * Accurately distinguishes Exact matches from Partial matches.
     */
    public EntityMatch match(String query, String category) {
        if (query == null || query.trim().isEmpty()) {
            return new EntityMatch("NONE", null, "No query provided.");
        }
        String cleanQuery = query.trim();

        // 1. Try Exact match first
        for (Entity e : entityMap.values()) {
            if (e.matchesExact(cleanQuery)) {
                return new EntityMatch("EXACT", e, 
                    "Exact match found in verified starter database: '" + e.getName() + "'.");
            }
        }

        // 2. Try Partial match
        for (Entity e : entityMap.values()) {
            if (e.matchesPartial(cleanQuery)) {
                return new EntityMatch("PARTIAL", e, 
                    "Partial name similarity match with verified starter entity: '" + e.getName() + 
                    "'. Please verify exact entity identity.");
            }
        }

        return new EntityMatch("NONE", null, 
            "Entity '" + cleanQuery + "' not found in the starter verified database. " +
            "Note: The starter dataset contains " + entityMap.size() + " known entities and does not cover every legitimate company.");
    }
}
