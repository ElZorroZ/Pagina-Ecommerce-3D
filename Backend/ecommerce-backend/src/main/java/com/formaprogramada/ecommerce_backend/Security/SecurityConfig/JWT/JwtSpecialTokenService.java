package com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtSpecialTokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long RESET_PASSWORD_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30 min
    private static final long CONFIRM_EMAIL_TOKEN_EXPIRATION = 1000 * 60 * 60;  // 1 hora

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateResetPasswordToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "reset_password");
        return generateToken(claims, username, RESET_PASSWORD_TOKEN_EXPIRATION);
    }

    public String generateConfirmEmailToken(String nuevoEmail, String gmailActual) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "confirm_email");
        claims.put("gmailActual", gmailActual);  // agrego el email actual como claim extra
        return generateToken(claims, nuevoEmail, CONFIRM_EMAIL_TOKEN_EXPIRATION);
    }

    private String generateToken(Map<String, Object> extraClaims, String subject, long expirationMillis) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubjectFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateResetPasswordToken(String token, String username) {
        return validateToken(token, username, "reset_password");
    }

    public boolean validateConfirmEmailToken(String token, String username) {
        return validateToken(token, username, "confirm_email");
    }

    private boolean validateToken(String token, String username, String expectedPurpose) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            String purpose = claims.get("purpose", String.class);

            return subject.equals(username) && purpose.equals(expectedPurpose)
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    public <T> T getClaimFromToken(String token, String claimKey, Class<T> clazz) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get(claimKey, clazz);
        } catch (Exception e) {
            return null;
        }
    }

}