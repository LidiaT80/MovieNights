package com.example.demo.utilities;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;


@Component
public class JWTHandler {

    private static final Logger logger = LoggerFactory.getLogger(JWTHandler.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication, HttpServletResponse response){

        UserDetails userPrincipal = (User) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime()+jwtExpirationInMs);

        String token = JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC512(jwtSecret.getBytes()));
        response.addHeader("Authorization", "Bearer " + token);
        return token;
    }

    public String validateToken(String token){

        return JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(token.replace("Bearer ",""))
                .getSubject();
    }
}