package com.trustcheck.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a verified entity in the starter database (Layer 1).
 */
public class Entity {
    private final String id;
    private final String name;
    private final String category; // "Company", "Internship", "Certificate"
    private final String officialDomain;
    private final String description;
    private final String verificationNote;
    private final List<String> aliases;

    public Entity(String id, String name, String category, String officialDomain, 
                  String description, String verificationNote, List<String> aliases) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.officialDomain = officialDomain;
        this.description = description;
        this.verificationNote = verificationNote;
        this.aliases = aliases != null ? new ArrayList<>(aliases) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getOfficialDomain() {
        return officialDomain;
    }

    public String getDescription() {
        return description;
    }

    public String getVerificationNote() {
        return verificationNote;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }

    /**
     * Checks if a query matches this entity name or aliases (exact or partial).
     */
    public boolean matchesExact(String query) {
        if (query == null) return false;
        String normalizedQuery = query.trim().toLowerCase();
        if (name.toLowerCase().equals(normalizedQuery)) return true;
        for (String alias : aliases) {
            if (alias.toLowerCase().equals(normalizedQuery)) return true;
        }
        return false;
    }

    public boolean matchesPartial(String query) {
        if (query == null) return false;
        String normalizedQuery = query.trim().toLowerCase();
        if (normalizedQuery.length() < 3) return false;
        if (name.toLowerCase().contains(normalizedQuery) || normalizedQuery.contains(name.toLowerCase())) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.toLowerCase().contains(normalizedQuery) || normalizedQuery.contains(alias.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
