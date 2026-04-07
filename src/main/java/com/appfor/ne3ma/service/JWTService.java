package com.appfor.ne3ma.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.util.Base64;
import java.util.function.Function;

@Service
public class JWTService {
    private String secretjwt;
    public JWTService(){
        try{
            KeyGenerator keygen= KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk= keygen.generateKey();
            secretjwt=Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(Keys.hmacShaKeyFor(secretjwt.getBytes()))
                .compact();
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretjwt.getBytes(StandardCharsets.UTF_8));
    }
    public boolean validatetoken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


}

