package com.example.demo.controllers;

import com.example.demo.models.Token;
import com.example.demo.models.User;
import com.example.demo.repositories.TokenRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utilities.CalenderHandler;
import com.example.demo.utilities.UserDbHandler;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

@RestController
public class CalenderController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;
    private UserDbHandler userDbHandler = new UserDbHandler();
    private CalenderHandler calenderHandler = new CalenderHandler();

    private final static Logger logger = LoggerFactory.getLogger(CalenderController.class);
    private static final String APPLICATION_NAME = "MovieNights";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private String userEmail;

    GoogleClientSecrets clientSecrets;
    GoogleAuthorizationCodeFlow flow;

    @Value("${google.client.client-id}")
    private String clientId;
    @Value("${google.client.client-secret}")
    private String clientSecret;
    @Value("${google.client.redirectUri}")
    private String redirectURI;
    @Value("${google.client.scope}")
    private String scope;

    @RequestMapping(value = "/userevents", method = RequestMethod.GET, params = "email")
    public RedirectView getCalenderWithParam(@RequestParam String email){
        RedirectView redirectView;
        User user = userDbHandler.findUserByEmail(userRepository, email);
        if (calenderHandler.hasToken(user, tokenRepository)){
            redirectView = new RedirectView("/events");
            redirectView.setPropagateQueryParams(true);
            return redirectView;
        }else {
            return new RedirectView("/calender");
        }
    }

    @RequestMapping(value = "/userevents", method = RequestMethod.GET)
    public RedirectView getCalender(){
        return new RedirectView("/events");
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET, params = "email")
    public List<Event> showEventsWithParam(@RequestParam String email){
        User user = userDbHandler.findUserByEmail(userRepository, email);
        return calenderHandler.getEvents(user, tokenRepository);
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public List<Event> showEvents(){
        User user = userDbHandler.findUserByEmail(userRepository, userEmail);
        return calenderHandler.getEvents(user, tokenRepository);
    }

    @RequestMapping(value = "/calender", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus() throws Exception {
        return new RedirectView(authorize());
    }

    @RequestMapping(value = "/calender", method = RequestMethod.GET, params = "code")
    public RedirectView oauth2Callback(@RequestParam(value = "code") String code) {
        String accessToken;
        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            accessToken = response.getAccessToken();
            String refreshToken = response.getRefreshToken();
            Long expiresAt = System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000);
            String userId = ((GoogleTokenResponse) response).parseIdToken().getPayload().getSubject();
            userEmail = ((GoogleTokenResponse) response).parseIdToken().getPayload().getEmail();
            User user = userDbHandler.findUserByEmail(userRepository, userEmail);
            Token token = new Token(userId, accessToken, refreshToken, expiresAt);
            token.setUser(user);
            tokenRepository.save(token);
        } catch (Exception e) {
            logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")");
        }
        return new RedirectView("/userevents");
    }


    private String authorize() throws Exception {
        AuthorizationCodeRequestUrl authorizationUrl;
        if (flow == null) {
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                    getScopes()).setAccessType("offline").setApprovalPrompt("force").build();
        }
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
        System.out.println("cal authorizationUrl->" + authorizationUrl);
        return authorizationUrl.build();
    }

    private Collection<String> getScopes(){
        List<String> scopes = new ArrayList<>();
        String[] scopeArray = scope.split(",");
        for (String s: scopeArray) {
            scopes.add(s);
        }
        return scopes;
    }

}
