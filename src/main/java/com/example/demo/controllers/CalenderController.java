package com.example.demo.controllers;

import com.example.demo.models.Movie;
import com.example.demo.models.Token;
import com.example.demo.models.User;
import com.example.demo.repositories.MovieRepository;
import com.example.demo.repositories.TokenRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utilities.CalenderHandler;
import com.example.demo.utilities.MovieDbHandler;
import com.example.demo.utilities.TokenHandler;
import com.example.demo.utilities.UserDbHandler;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
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
    @Autowired
    MovieRepository movieRepository;

    private UserDbHandler userDbHandler = new UserDbHandler();
    private CalenderHandler calenderHandler = new CalenderHandler();
    private TokenHandler tokenHandler = new TokenHandler();
    private MovieDbHandler movieDbHandler = new MovieDbHandler();

    private final static Logger logger = LoggerFactory.getLogger(CalenderController.class);
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
        if (tokenHandler.hasToken(user, tokenRepository)){
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
    public ResponseEntity<List<Event>> getEventsWithParam(@RequestParam String email){
        User user = userDbHandler.findUserByEmail(userRepository, email);
        List<Event> response = calenderHandler.getEvents(user, tokenRepository);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getEvents(){
        User user = userDbHandler.findUserByEmail(userRepository, userEmail);
        List<Event> response = calenderHandler.getEvents(user, tokenRepository);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/calender", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus() throws Exception {
        return new RedirectView(authorize());
    }

    @RequestMapping(value = "/calender", method = RequestMethod.GET, params = "code")
    public RedirectView oauth2Callback(@RequestParam(value = "code") String code) {
        TokenResponse response = null;
        String userId = null;
        String accessToken;
        String refreshToken;
        Long expiresAt;
        try {
            response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            userId = ((GoogleTokenResponse) response).parseIdToken().getPayload().getSubject();
            userEmail = ((GoogleTokenResponse) response).parseIdToken().getPayload().getEmail();

        } catch (Exception e) {
            logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")");
        }
        accessToken = response.getAccessToken();
        refreshToken = response.getRefreshToken();
        expiresAt = System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000);
        User user = userDbHandler.findUserByEmail(userRepository, userEmail);
        Token token = new Token(userId, accessToken, refreshToken, expiresAt);
        token.setUser(user);
        tokenRepository.save(token);
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

    @RequestMapping(value = "/dates", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getAvailableDates(){
        return new ResponseEntity<>(calenderHandler.getAvailableDates(), HttpStatus.OK);
    }

    @RequestMapping(value = "/booking")
    public ResponseEntity bookEvent(@RequestParam DateTime startDate){
        String movieTitle = movieDbHandler.getChosenMovie();
        Movie movie = movieDbHandler.findMovie(movieTitle, movieRepository);
        calenderHandler.bookEvent(movie, startDate, tokenRepository);
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
