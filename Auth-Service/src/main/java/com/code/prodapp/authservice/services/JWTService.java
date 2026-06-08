package com.code.prodapp.authservice.services;


import com.code.prodapp.authservice.entities.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.SecretKey}")
    private String key ;

    private SecretKey HMACGeneratedKey(){
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    protected String generateAccessToken(UserEntity user){
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username",user.getUsername())
                .claim("email",user.getEmail())
                .claim("authorities",user.getAuthorities())
                .issuedAt(new Date(System.currentTimeMillis()))
                // Expires after 10 Minutes
                .expiration(new  Date(System.currentTimeMillis() + 60*1000*10))
                .signWith(HMACGeneratedKey())
                .compact();
    }

    protected String generateRefreshToken(UserEntity user){
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username",user.getUsername())
                .claim("email",user.getEmail())
                .claim("authorities",user.getAuthorities())
                .issuedAt(new Date(System.currentTimeMillis()))
                // Refresh Token Expires after 1 Day
                .expiration(new  Date(System.currentTimeMillis() + 60*1000*60*24))
                .signWith(HMACGeneratedKey())
                .compact();
    }

    protected Long getUserIdFromToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(HMACGeneratedKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }






}
