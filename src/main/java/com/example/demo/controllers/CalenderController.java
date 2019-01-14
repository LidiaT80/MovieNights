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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
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

    private final static Logger logger = LoggerFactory.getLogger(CalenderController.class);
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private String userEmail;
    private String chosenMovie;
    private String chosenDate;
    private List<String> chosenUsers;

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

    public void getCalEvents (HttpServletResponse servletResponse) throws Exception {
        for (String email: chosenUsers) {
            User user = userDbHandler.findUserByEmail(userRepository, email);
            if (tokenHandler.hasToken(user, tokenRepository)){
                calenderHandler.getEvents(user, tokenRepository);
            }else {
                servletResponse.sendRedirect(authorize());
            }
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/calender", params = "code", method = RequestMethod.GET)
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
        calenderHandler.getEvents(user, tokenRepository);
        return new RedirectView("/date");
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
        if(chosenUsers == null)
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(calenderHandler.getAvailableDates(), HttpStatus.OK);
    }

    @RequestMapping(value = "/booking/movie/{title}", method = RequestMethod.POST)
    public ResponseEntity bookMovie(@PathVariable String title){
        chosenMovie = title;
        return new ResponseEntity("Booked movie: " + title, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/booking/users", method = RequestMethod.POST)
    public ResponseEntity saveChosenUsers(@RequestBody List<String> userList){
        chosenUsers = userList;
        return new ResponseEntity("Saved", HttpStatus.CREATED);
    }

    @RequestMapping(value = "/booking/dates/{date}", method = RequestMethod.POST)
    public ResponseEntity bookDate(@PathVariable String date){
        chosenDate = date;
        return new ResponseEntity("Booked date: " + date, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/booking", method = RequestMethod.POST)
    public ResponseEntity bookEvent(){
        if(chosenMovie == null || chosenUsers == null || chosenDate == null){
            return new ResponseEntity("Could not complete booking. Missing some details.", HttpStatus.ACCEPTED);
        }
        calenderHandler.bookEvent(chosenMovie, chosenDate, chosenUsers, movieRepository, userRepository, tokenRepository);
        chosenMovie = null;
        chosenDate = null;
        chosenUsers = null;
        return new ResponseEntity("Movie night booked", HttpStatus.CREATED);
    }

    public List<String> getChosenUsers(){
        return chosenUsers;
    }

}
