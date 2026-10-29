package com.trustcheck.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.trustcheck.model.Entity;
import com.trustcheck.model.ScamReport;
import com.trustcheck.model.VerificationResult;
import com.trustcheck.repository.EntityRepository;
import com.trustcheck.repository.ReportRepository;
import com.trustcheck.service.KeywordDetector;
import com.trustcheck.service.VerificationService;
import com.trustcheck.util.JsonUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Built-in HTTP Server for TrustCheck using Java Standard Library.
 * No external web frameworks (Spring, Spark, Javalin, etc.) required.
 */
public class HttpServerApp {

    private final int port;
    private final String dataDir;
    private final String webDir;
    private final VerificationService verificationService;
    private final long startTimeMillis;
    private HttpServer server;

    public HttpServerApp(int port, String dataDir, String webDir) {
        this.port = port;
        this.dataDir = dataDir;
        this.webDir = webDir;
        this.startTimeMillis = System.currentTimeMillis();

        EntityRepository entityRepo = new EntityRepository();
        KeywordDetector keywordDetector = new KeywordDetector();
        ReportRepository reportRepo = new ReportRepository(dataDir);
        this.verificationService = new VerificationService(entityRepo, keywordDetector, reportRepo);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        // Register Web Interface Route
        server.createContext("/", new StaticWebHandler());

        // Register REST API Routes
        server.createContext("/api/verify", new VerifyHandler());
        server.createContext("/api/reports", new ReportsHandler());
        server.createContext("/api/report", new CreateReportHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/entities", new EntitiesHandler());

        server.start();

        System.out.println("==================================================================");
        System.out.println("  TRUSTCHECK — Fake Internship & Certificate Verification Engine  ");
        System.out.println("==================================================================");
        System.out.println(" [✓] Engine Status: ACTIVE (Java SE Built-in HTTP Server)");
        System.out.println(" [✓] Web Application: http://localhost:" + port + "/");
        System.out.println(" [✓] Local Data Directory: " + Paths.get(dataDir).toAbsolutePath());
        System.out.println(" [✓] Layer 1 Starter Entities: " + verificationService.getEntityRepository().getEntityCount());
        System.out.println(" [✓] Layer 2 Scam Rules: " + verificationService.getKeywordDetector().getRuleCount());
        System.out.println(" [✓] Layer 3 Community Reports: " + verificationService.getReportRepository().getReportCount());
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("[TrustCheck] Server stopped gracefully.");
        }
    }

    public VerificationService getVerificationService() {
        return verificationService;
    }

    // --- HTTP HANDLERS ---

    /**
     * Serves the single-page responsive web application (index.html).
     */
    private class StaticWebHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
                    return;
                }

                if (path.equals("/") || path.equals("/index.html")) {
                    Path htmlFile = Paths.get(webDir, "index.html");
                    if (!Files.exists(htmlFile)) {
                        htmlFile = Paths.get("index.html");
                    }
                    byte[] bytes;
                    if (Files.exists(htmlFile)) {
                        bytes = Files.readAllBytes(htmlFile);
                    } else {
                        // Fallback message if web folder was moved
                        String fallback = "<html><body><h1>TrustCheck</h1><p>index.html not found.</p></body></html>";
                        bytes = fallback.getBytes(StandardCharsets.UTF_8);
                    }
                    sendResponse(exchange, 200, "text/html; charset=UTF-8", bytes);
                } else {
                    sendResponse(exchange, 404, "text/plain", "404 Not Found");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "text/plain", "Internal Server Error");
            }
        }
    }

    /**
     * Handles POST /api/verify
     */
    private class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            try {
                String requestBody = readBody(exchange);
                Map<String, String> bodyMap = JsonUtil.parseSimpleJsonObject(requestBody);

                String entityName = bodyMap.get("entityName");
                String category = bodyMap.getOrDefault("category", "Internship");
                String offerText = bodyMap.getOrDefault("offerText", "");
                String offerUrl = bodyMap.getOrDefault("offerUrl", "");

                if (entityName == null || entityName.trim().isEmpty()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Entity name is required\"}");
                    return;
                }

                VerificationResult result = verificationService.verify(entityName, category, offerText, offerUrl);
                String jsonResponse = JsonUtil.resultToJson(result);

                sendResponse(exchange, 200, "application/json; charset=UTF-8", jsonResponse);
            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"Internal verification error\"}");
            }
        }
    }

    /**
     * Handles GET /api/reports (with search and filtering)
     */
    private class ReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParams = parseQueryParams(query);

                String q = queryParams.getOrDefault("q", "");
                String category = queryParams.getOrDefault("category", "All");
                String status = queryParams.getOrDefault("status", "All");

                List<ScamReport> list = verificationService.getReportRepository().searchAndFilter(q, category, status);
                String jsonResponse = JsonUtil.toJson(list);

                sendResponse(exchange, 200, "application/json; charset=UTF-8", jsonResponse);
            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"Failed to retrieve reports\"}");
            }
        }
    }

    /**
     * Handles POST /api/report
     */
    private class CreateReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            try {
                String requestBody = readBody(exchange);
                Map<String, String> bodyMap = JsonUtil.parseSimpleJsonObject(requestBody);

                String entityName = bodyMap.get("entityName");
                String category = bodyMap.getOrDefault("category", "Internship");
                String contactDetails = bodyMap.getOrDefault("contactDetails", "");
                String description = bodyMap.get("description");
                String evidenceUrl = bodyMap.getOrDefault("evidenceUrl", "");

                if (entityName == null || entityName.trim().isEmpty() || 
                    description == null || description.trim().isEmpty()) {
                    sendResponse(exchange, 400, "application/json", 
                        "{\"error\":\"Entity name and scam description are required fields.\"}");
                    return;
                }

                ScamReport report = verificationService.getReportRepository().addReport(
                    entityName, category, contactDetails, description, evidenceUrl
                );

                String jsonResponse = JsonUtil.reportToJson(report);
                sendResponse(exchange, 201, "application/json; charset=UTF-8", jsonResponse);
            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"Failed to save report\"}");
            }
        }
    }

    /**
     * Handles GET /api/stats
     */
    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            long uptimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
            int verifiedCount = verificationService.getEntityRepository().getEntityCount();
            int reportCount = verificationService.getReportRepository().getReportCount();
            int keywordsCount = verificationService.getKeywordDetector().getRuleCount();

            String json = String.format(
                "{\"verifiedEntitiesCount\":%d,\"communityReportsCount\":%d,\"scamKeywordsCount\":%d,\"uptimeSeconds\":%d}",
                verifiedCount, reportCount, keywordsCount, uptimeSeconds
            );

            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    /**
     * Handles GET /api/entities (starter dataset list)
     */
    private class EntitiesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            List<Entity> list = verificationService.getEntityRepository().getAllEntities();
            String jsonResponse = JsonUtil.toJson(list);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", jsonResponse);
        }
    }

    // --- UTILITIES ---

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    map.put(key, value);
                } else if (!pair.isEmpty()) {
                    String key = URLDecoder.decode(pair, "UTF-8");
                    map.put(key, "");
                }
            } catch (UnsupportedEncodingException ignored) {}
        }
        return map;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        sendResponse(exchange, statusCode, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.flush();
        os.close();
    }

    // --- MAIN METHOD ---

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        } else {
            String envPort = System.getenv("PORT");
            if (envPort != null) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException ignored) {}
            }
        }

        String projectRoot = System.getProperty("user.dir");
        String dataDir = Paths.get(projectRoot, "data").toString();
        String webDir = Paths.get(projectRoot, "web").toString();

        try {
            HttpServerApp app = new HttpServerApp(port, dataDir, webDir);
            app.start();

            // Register JVM shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        } catch (IOException e) {
            System.err.println("Fatal: Could not start TrustCheck HTTP server: " + e.getMessage());
            System.exit(1);
        }
    }
}
