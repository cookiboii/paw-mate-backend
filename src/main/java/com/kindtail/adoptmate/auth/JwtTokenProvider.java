package com.kindtail.adoptmate.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.kindtail.adoptmate.member.domain.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRtString;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    private SecretKey secretKey;
    private SecretKey secretKeyRt;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        this.secretKeyRt = Keys.hmacShaKeyFor(secretKeyRtString.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long id, String email, String role) {
        return createToken(id, email, role, 0L);
    }

    public String createToken(Long id, String email, String role, long tokenVersion) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("tokenVersion", tokenVersion);
        if (id != null) {
            claims.put("id", id);
        }
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 1000L))
                .signWith(secretKey)
                .compact();
    }

    public String createToken(String email, String role) {
        return createToken(null, email, role, 0L);
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public TokenPrincipal getTokenPrincipal(String token) {
        Claims claims = getClaims(token);
        Number id = claims.get("id", Number.class);
        String role = claims.get("role", String.class);
        if (id == null || role == null) {
            throw new IllegalArgumentException("JWT is missing required authentication claims");
        }
        return new TokenPrincipal(id.longValue(), claims.getSubject(), Role.valueOf(role));
    }

    public long getTokenVersion(String token) {
        Number tokenVersion = getClaims(token).get("tokenVersion", Number.class);
        return tokenVersion == null ? 0L : tokenVersion.longValue();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 1000L))
                .signWith(secretKeyRt)
                .compact();
    }

    public String validateRefreshToken(String refreshToken) throws Exception {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        return claims.getSubject();
    }

    public long getRemainingExpirationMillis(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date exp = claims.getExpiration();
            long now = System.currentTimeMillis();
            return Math.max(0, exp.getTime() - now);
        } catch (ExpiredJwtException e) {
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public int getExpirationRt() {
        return expirationRt;
    }

    public record TokenPrincipal(Long id, String email, Role role) { }
}
