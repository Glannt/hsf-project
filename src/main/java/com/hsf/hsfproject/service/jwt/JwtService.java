package com.hsf.hsfproject.service.jwt;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private SecretKey Key;

    private static final String SECRET_KEY = Dotenv.load().get("SECRET_KEY");

    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 86400000;   // A DAY
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 604800000; // A WEEK

    private SecretKey getSigningKey(){
        // decode secret key to signing key
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return Key;
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = exstractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }



    private Claims exstractAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSigningKey())    // create signing key to decoded token
                .build()
                .parseSignedClaims(token)
                .getPayload();   // get all Claims within token
    }


    public String generateRefreshToken(String username){
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_SECONDS))
                .signWith(getSigningKey())
                .compact();
    }

    public  String generateAccessToken(HashMap<String, Object> claims, String username ){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String userName = extractUsername(token);
        // make sure userName from token == userName from user input
        return (userName.equals(userDetails.getUsername()))&& !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
