package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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

    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 1000L))
                .signWith(secretKey)
                .compact();
    }

    public TokenUserInfo validateAndTokenUserInfo(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return TokenUserInfo.builder()
                .email(claims.getSubject())
                .role(Role.valueOf(claims.get("role", String.class)))
                .build();
    }

    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
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
}
