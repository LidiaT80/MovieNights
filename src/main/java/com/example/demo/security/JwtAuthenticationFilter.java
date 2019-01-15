package com.example.demo.security;

import com.example.demo.models.RequestDetails;
import com.example.demo.repositories.RequestDetailsRepository;
import com.example.demo.utilities.CustomUserDetailsService;
import com.example.demo.utilities.JWTHandler;
import com.google.api.client.util.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JWTHandler jwtHandler;

    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @Autowired
    RequestDetailsRepository requestDetailsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        int respStatus;
        String reqMethod = request.getMethod();
        String reqURI = request.getRequestURI();
        String ip = request.getRemoteAddr();
        Long time = new DateTime(new Date()).getValue();
        String user = "guest";
        String header = request.getHeader("Authorization");
        if (header ==  null) {
            chain.doFilter(request,response);
            respStatus = response.getStatus();
            //requestDetailsRepository.save(new RequestDetails(reqMethod, reqURI, ip, user, time, respStatus));
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(authentication != null)
            user = authentication.getName();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request,response);
        respStatus = response.getStatus();
        //requestDetailsRepository.save(new RequestDetails(reqMethod, reqURI, ip, user, time, respStatus));
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token != null) {
            String tokenUser = jwtHandler.validateToken(token);
            if (tokenUser != null){
                return new UsernamePasswordAuthenticationToken(tokenUser, null, new ArrayList<>());
            }
            return null;
        }
        return null;
    }
}
