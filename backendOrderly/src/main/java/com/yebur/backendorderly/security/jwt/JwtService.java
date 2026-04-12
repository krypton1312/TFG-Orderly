package com.yebur.backendorderly.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expMinutes;
    private final long refreshExpDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expMinutes,
            @Value("${app.jwt.refresh-expiration-days}") long refreshExpDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expMinutes = expMinutes;
        this.refreshExpDays = refreshExpDays;
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(user.getUsername()) // email
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expMinutes * 60)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpDays * 24L * 60 * 60)))
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        Claims c = parseClaims(token);
        return c.getSubject().equals(user.getUsername())
                && c.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

