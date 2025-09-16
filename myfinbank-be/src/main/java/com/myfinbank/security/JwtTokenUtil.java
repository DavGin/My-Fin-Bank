package com.myfinbank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    private static final String SECRET = "supersecretkeymysupersecurekeysupersecret";
    private static final long EXPIRATION_MS = 3600_000; // 1h

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String username, Set<String> ruoli) {
        return Jwts.builder()
                .setSubject(username)
                .claim("ruoli", ruoli)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public Set<String> getRuoli(String token) {
        return ((java.util.List<String>) parseClaims(token).getBody().get("ruoli"))
                .stream().collect(Collectors.toSet());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
