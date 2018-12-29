package com.example.demo.utilities;

import com.example.demo.models.Token;
import com.example.demo.models.User;
import com.example.demo.repositories.TokenRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CalenderHandler {
    private static com.google.api.services.calendar.Calendar calender;
    private final DateTime startDate = new DateTime(new Date());
    private static final String APPLICATION_NAME = "MovieNights";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Credential credential;

    private final String CLIENT_ID = "466030073988-nrlmchmiekrosrkusar31v4l5qp6n21r.apps.googleusercontent.com";
    private final String CLIENT_SECRET = "sIKmcpJDRhZQ2gB1Fmi0sFkw";

    public List<Event> getEvents(User user, TokenRepository tokenRepository){
        Calendar.Events events;
        com.google.api.services.calendar.model.Events eventList;
        List<Event> calenderEvents = null;
        String accessToken;
        Token token = getToken(user, tokenRepository);
        if(isTokenValid(token)){
            accessToken = token.getAccessToken();
        }else {
            accessToken = getRefreshToken(token, tokenRepository).getAccessToken();
        }
        credential = new GoogleCredential().setAccessToken(accessToken);

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            calender = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
            events = calender.events();
            eventList = events.list("primary").setTimeMin(startDate).execute();
            calenderEvents = eventList.getItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calenderEvents;
    }

    public Token getToken(User user, TokenRepository tokenRepository){
        Token token = null;
        int id = user.getId();
        Optional<Token> tokenOptional = tokenRepository.findById(id);
        if(tokenOptional.isPresent()){
            token = tokenOptional.get();
        }
        return token;
    }

    public boolean hasToken(User user, TokenRepository tokenRepository){
        int id = user.getId();
        List<Token> tokens = (List<Token>) tokenRepository.findAll();
        return tokens.stream().anyMatch(token -> token.getId() == id);
    }

    public boolean isTokenValid(Token token){
        if(token.getExpiresAt() > System.currentTimeMillis())
            return true;
        return false;
    }

    public Token getRefreshToken(Token token, TokenRepository tokenRepository){
        String refreshToken = token.getRefreshToken();
        GoogleTokenResponse response = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            response= new GoogleRefreshTokenRequest(
                     httpTransport, JSON_FACTORY, refreshToken,
                    CLIENT_ID, CLIENT_SECRET).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        token.setAccessToken(response.getAccessToken());
        token.setExpiresAt(System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000));
        tokenRepository.save(token);
        return token;
    }
}
