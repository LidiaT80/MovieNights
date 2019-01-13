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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private String chosenMovie;

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

    @RequestMapping(value = "/events", method = RequestMethod.GET, params = "email")
    public ResponseEntity getEventsWithParam(@RequestParam String email, HttpServletResponse servletResponse) throws Exception {
        User user = userDbHandler.findUserByEmail(userRepository, email);
        if (tokenHandler.hasToken(user, tokenRepository)){
            List<Event> response = calenderHandler.getEvents(user, tokenRepository);
            return new ResponseEntity(response, HttpStatus.OK);
        }else {
            servletResponse.sendRedirect(authorize());
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getEvents(){
        User user = userDbHandler.findUserByEmail(userRepository, userEmail);
        List<Event> response = calenderHandler.getEvents(user, tokenRepository);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @RequestMapping(value = "/calender", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus() throws Exception {
        return new RedirectView(authorize());
    }

    @CrossOrigin
    @RequestMapping(value = "/calender", params = "code")
    public RedirectView oauth2Callback(@RequestParam(value = "code") String code){
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
        return new RedirectView("/events");
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

    @RequestMapping(value = "/dates/all", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getAvailableDates(){
        return new ResponseEntity<>(calenderHandler.getAvailableDates(), HttpStatus.OK);
    }

    @RequestMapping(value = "/booking/movie/{title}", method = RequestMethod.POST)
    public ResponseEntity bookMovie(@PathVariable String title){
        chosenMovie = title;
        return new ResponseEntity("Booked movie: " + title, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/booking", method = RequestMethod.POST)
    public ResponseEntity bookEvent(@RequestParam String startDate){
        Movie movie = movieDbHandler.findMovie(chosenMovie, movieRepository);
        DateTime startDateTime = new DateTime(startDate + ":00.000");
        calenderHandler.bookEvent(movie, startDateTime, tokenRepository);
        return new ResponseEntity("Movie night booked", HttpStatus.CREATED);
    }

}
