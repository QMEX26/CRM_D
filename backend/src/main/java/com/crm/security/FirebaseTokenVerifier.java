package com.crm.security;

import com.crm.config.FirebaseConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FirebaseTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseTokenVerifier.class);
    private static final String GOOGLE_CERTS_URL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";

    private final FirebaseConfig firebaseConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private Instant cacheExpiry = Instant.MIN;

    @Autowired
    public FirebaseTokenVerifier(FirebaseConfig firebaseConfig,
                                 ObjectMapper objectMapper) {
        this.firebaseConfig = firebaseConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public FirebaseTokenInfo verifyToken(String token) {
        // 1. Try Firebase Admin SDK if initialized
        FirebaseAuth firebaseAuth = firebaseConfig.getFirebaseAuth();
        if (firebaseAuth != null) {
            try {
                FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
                return FirebaseTokenInfo.builder()
                        .uid(decoded.getUid())
                        .email(decoded.getEmail())
                        .name(decoded.getName())
                        .build();
            } catch (Exception e) {
                logger.debug("Firebase Admin SDK token verification failed: {}, trying JWKS verifier", e.getMessage());
            }
        }

        // 2. Direct cryptographic verification using Google's public certificates
        return verifyViaGooglePublicKeys(token);
    }

    private FirebaseTokenInfo verifyViaGooglePublicKeys(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            // Extract kid from header
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            JsonNode headerNode = objectMapper.readTree(headerJson);
            String kid = headerNode.path("kid").asText();
            if (kid == null || kid.isEmpty()) {
                throw new IllegalArgumentException("JWT header missing 'kid'");
            }

            PublicKey publicKey = getPublicKey(kid);
            if (publicKey == null) {
                // Refresh cache once and retry
                refreshKeyCache();
                publicKey = getPublicKey(kid);
                if (publicKey == null) {
                    throw new IllegalStateException("Google public key not found for kid: " + kid);
                }
            }

            String expectedIssuer = "https://securetoken.google.com/" + firebaseConfig.getProjectId();
            String expectedAudience = firebaseConfig.getProjectId();

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(expectedIssuer)
                    .requireAudience(expectedAudience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.get("email", String.class);
            String uid = claims.getSubject();
            String name = claims.get("name", String.class);

            return FirebaseTokenInfo.builder()
                    .uid(uid)
                    .email(email)
                    .name(name)
                    .build();
        } catch (Exception e) {
            logger.warn("Firebase ID token verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Firebase ID token: " + e.getMessage(), e);
        }
    }

    private PublicKey getPublicKey(String kid) {
        if (Instant.now().isAfter(cacheExpiry)) {
            refreshKeyCache();
        }
        return keyCache.get(kid);
    }

    private synchronized void refreshKeyCache() {
        if (Instant.now().isBefore(cacheExpiry) && !keyCache.isEmpty()) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_CERTS_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch Google public certificates, status: " + response.statusCode());
            }

            Map<String, String> certs = objectMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            keyCache.clear();
            for (Map.Entry<String, String> entry : certs.entrySet()) {
                String certPem = entry.getValue();
                X509Certificate cert = (X509Certificate) certFactory.generateCertificate(
                        new ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8)));
                keyCache.put(entry.getKey(), cert.getPublicKey());
            }

            // Parse Cache-Control header if available
            long maxAge = 3600; // default 1 hour
            String cacheControl = response.headers().firstValue("cache-control").orElse("");
            for (String part : cacheControl.split(",")) {
                part = part.trim().toLowerCase();
                if (part.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(part.substring(8));
                    } catch (NumberFormatException ignored) {}
                }
            }

            cacheExpiry = Instant.now().plusSeconds(maxAge);
            logger.info("Successfully refreshed Google public certificates cache ({} keys, expires in {}s)", keyCache.size(), maxAge);
        } catch (Exception e) {
            logger.error("Failed to refresh Google public keys: {}", e.getMessage(), e);
            // Retry sooner if failed
            cacheExpiry = Instant.now().plusSeconds(60);
        }
    }
}
