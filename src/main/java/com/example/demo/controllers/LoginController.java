package com.example.demo.controllers;

import com.example.demo.models.JwtAuthenticationResponse;
import com.example.demo.models.LoginData;
import com.example.demo.utilities.JWTHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


@RestController
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    JWTHandler jwtHandler;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity logIn(@RequestBody LoginData loginData, HttpServletResponse response){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginData.getEmail(), loginData.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtHandler.generateToken(authentication, response);
        return new ResponseEntity (new JwtAuthenticationResponse(jwt), HttpStatus.OK);
    }
}
