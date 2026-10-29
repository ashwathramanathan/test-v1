package com.trustcheck.util;

import com.trustcheck.model.Entity;
import com.trustcheck.model.ScamReport;
import com.trustcheck.model.VerificationResult;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON serializer and parser for Java standard library.
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Entity) return entityToJson((Entity) obj);
        if (obj instanceof ScamReport) return reportToJson((ScamReport) obj);
        if (obj instanceof VerificationResult) return resultToJson((VerificationResult) obj);
        if (obj instanceof List<?>) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map<?, ?>) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":")
                  .append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    public static String entityToJson(Entity e) {
        if (e == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"id\":\"").append(escape(e.getId())).append("\",")
          .append("\"name\":\"").append(escape(e.getName())).append("\",")
          .append("\"category\":\"").append(escape(e.getCategory())).append("\",")
          .append("\"officialDomain\":\"").append(escape(e.getOfficialDomain())).append("\",")
          .append("\"description\":\"").append(escape(e.getDescription())).append("\",")
          .append("\"verificationNote\":\"").append(escape(e.getVerificationNote())).append("\",")
          .append("\"aliases\":").append(toJson(e.getAliases()))
          .append("}");
        return sb.toString();
    }

    public static String reportToJson(ScamReport r) {
        if (r == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"id\":\"").append(escape(r.getId())).append("\",")
          .append("\"entityName\":\"").append(escape(r.getEntityName())).append("\",")
          .append("\"category\":\"").append(escape(r.getCategory())).append("\",")
          .append("\"contactDetails\":\"").append(escape(r.getContactDetails())).append("\",")
          .append("\"description\":\"").append(escape(r.getDescription())).append("\",")
          .append("\"evidenceUrl\":\"").append(escape(r.getEvidenceUrl())).append("\",")
          .append("\"timestamp\":").append(r.getTimestamp()).append(",")
          .append("\"formattedDate\":\"").append(escape(r.getFormattedDate())).append("\",")
          .append("\"status\":\"").append(escape(r.getStatus())).append("\"")
          .append("}");
        return sb.toString();
    }

    public static String resultToJson(VerificationResult res) {
        if (res == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"entityName\":\"").append(escape(res.getEntityName())).append("\",")
          .append("\"category\":\"").append(escape(res.getCategory())).append("\",")
          .append("\"status\":\"").append(res.getStatus().name()).append("\",")
          .append("\"statusColor\":\"").append(res.getStatus().getColorHex()).append("\",")
          .append("\"statusDescription\":\"").append(escape(res.getStatus().getDefaultDescription())).append("\",")
          .append("\"primaryReason\":\"").append(escape(res.getPrimaryReason())).append("\",")
          .append("\"detailedReasons\":").append(toJson(res.getDetailedReasons())).append(",")
          .append("\"layer1Match\":").append(res.isLayer1Match()).append(",")
          .append("\"layer1MatchType\":\"").append(escape(res.getLayer1MatchType())).append("\",")
          .append("\"layer1MatchedName\":\"").append(escape(res.getLayer1MatchedName())).append("\",")
          .append("\"layer1Domain\":\"").append(escape(res.getLayer1Domain())).append("\",")
          .append("\"layer1Note\":\"").append(escape(res.getLayer1Note())).append("\",")
          .append("\"layer2Triggered\":").append(res.isLayer2Triggered()).append(",")
          .append("\"layer2Keywords\":").append(toJson(res.getLayer2Keywords())).append(",")
          .append("\"layer2RiskLevel\":\"").append(escape(res.getLayer2RiskLevel())).append("\",")
          .append("\"layer2Note\":\"").append(escape(res.getLayer2Note())).append("\",")
          .append("\"layer3ReportCount\":").append(res.getLayer3ReportCount()).append(",")
          .append("\"layer3Reports\":").append(toJson(res.getLayer3Reports())).append(",")
          .append("\"layer3Note\":\"").append(escape(res.getLayer3Note())).append("\",")
          .append("\"urlAnalyzed\":\"").append(escape(res.getUrlAnalyzed())).append("\",")
          .append("\"urlPhishingTriggered\":").append(res.isUrlPhishingTriggered()).append(",")
          .append("\"urlRiskLevel\":\"").append(escape(res.getUrlRiskLevel())).append("\",")
          .append("\"urlDomainMatchStatus\":\"").append(escape(res.getUrlDomainMatchStatus())).append("\",")
          .append("\"urlReasons\":").append(toJson(res.getUrlReasons())).append(",")
          .append("\"urlNote\":\"").append(escape(res.getUrlNote())).append("\",")
          .append("\"confidenceBreakdown\":\"").append(escape(res.getConfidenceBreakdown())).append("\",")
          .append("\"limitationsNotice\":\"").append(escape(res.getLimitationsNotice())).append("\"")
          .append("}");
        return sb.toString();
    }

    /**
     * Minimal JSON Object string-to-map parser for handling incoming requests.
     */
    public static Map<String, String> parseSimpleJsonObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return map;

        int i = 1;
        int len = trimmed.length() - 1;
        while (i < len) {
            // Find key start quote
            while (i < len && trimmed.charAt(i) != '"') i++;
            if (i >= len) break;
            i++; // skip open quote
            int keyStart = i;
            while (i < len && trimmed.charAt(i) != '"') {
                if (trimmed.charAt(i) == '\\') i++;
                i++;
            }
            if (i >= len) break;
            String key = unescape(trimmed.substring(keyStart, i));
            i++; // skip close quote

            // Find colon
            while (i < len && trimmed.charAt(i) != ':') i++;
            if (i >= len) break;
            i++; // skip colon

            // Skip whitespace
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) i++;
            if (i >= len) break;

            // Value can be string, boolean, number, or null
            if (trimmed.charAt(i) == '"') {
                i++; // skip open quote
                int valStart = i;
                while (i < len && trimmed.charAt(i) != '"') {
                    if (trimmed.charAt(i) == '\\') i++;
                    i++;
                }
                String val = unescape(trimmed.substring(valStart, Math.min(i, len)));
                map.put(key, val);
                if (i < len) i++; // skip quote
            } else {
                int valStart = i;
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') {
                    i++;
                }
                String val = trimmed.substring(valStart, i).trim();
                map.put(key, val);
            }

            // Skip to comma
            while (i < len && trimmed.charAt(i) != ',') i++;
            if (i < len && trimmed.charAt(i) == ',') i++;
        }
        return map;
    }

    public static String unescape(String s) {
        if (s == null || !s.contains("\\")) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            try {
                                int code = Integer.parseInt(s.substring(i + 1, i + 5), 16);
                                sb.append((char) code);
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append("\\u");
                            }
                        } else {
                            sb.append("\\u");
                        }
                        break;
                    default:
                        sb.append(next);
                        break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
