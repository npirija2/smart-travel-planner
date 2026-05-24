package com.travelplanner.shared.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtValidator {

    private final PublicKey publicKey;

    public JwtValidator() throws Exception {
        this.publicKey = loadPublicKey();
    }

    private PublicKey loadPublicKey() throws Exception {

        ClassPathResource resource =
                new ClassPathResource("keys/public.pem");

        String key = new String(
                resource.getInputStream().readAllBytes());

        key = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(decoded);

        return KeyFactory
                .getInstance("RSA")
                .generatePublic(spec);
    }

    public Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateToken(String token) {
        getClaims(token);
    }
    
    public void validateAuthorizationHeader(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);

        validateToken(token);
    }
    public String extractRole(String token) {
        return getClaims(token)
                .get("role", String.class);
    }

    public String extractType(String token) {
        return getClaims(token)
                .get("type", String.class);
    }

    public Long extractUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public HttpHeaders createAuthHeaders(String authHeader) {

        HttpHeaders headers = new HttpHeaders();

        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        return headers;
    }
}