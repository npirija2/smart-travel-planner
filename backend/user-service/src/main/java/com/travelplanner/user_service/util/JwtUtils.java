package com.travelplanner.user_service.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.travelplanner.user_service.model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {

    private final PrivateKey privateKey;

    public JwtUtils() throws Exception {
        this.privateKey = loadPrivateKey();
    }

    private PrivateKey loadPrivateKey() throws Exception {

        ClassPathResource resource =
                new ClassPathResource("keys/private.pem");

        String key = new String(
                resource.getInputStream().readAllBytes());

        key = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(decoded);

        return KeyFactory
                .getInstance("RSA")
                .generatePrivate(spec);
    }

    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getRole());
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + 1000L * 60 * 15))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + 1000L * 60 * 60 * 24 * 7))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}