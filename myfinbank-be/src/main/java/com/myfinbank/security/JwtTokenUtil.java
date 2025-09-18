package com.myfinbank.security;

import com.myfinbank.entity.User;
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
    private static final long EXPIRATION_MS = 15 * 60 * 1000; // 1h.
    private static final long REFRESH_TOKEN = 7 * 24 * 60 * 60 * 1000; //.


    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String username, String ruoli) {
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

    public String refreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("ruoli", user.getRuolo())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
