package com.example.demo.utilities;

import com.example.demo.models.Movie;
import com.example.demo.models.User;
import com.example.demo.repositories.MovieRepository;
import com.example.demo.repositories.TokenRepository;
import com.example.demo.repositories.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class CalenderHandler {
    private static com.google.api.services.calendar.Calendar calender;
    private static final String APPLICATION_NAME = "MovieNights";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Credential credential;
    private TokenHandler tokenHandler = new TokenHandler();
    private Map<User,List<Event>> userEvents = new HashMap<>();

    public List<Event> getEvents(User user, TokenRepository tokenRepository){
        Calendar.Events events;
        com.google.api.services.calendar.model.Events eventList;
        List<Event> calenderEvents = null;
        try {
            events = getCalender(user, tokenRepository);
            eventList = events.list("primary").setTimeMin(new DateTime(new Date())).execute();
            calenderEvents = eventList.getItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        userEvents.put(user, calenderEvents);
        return calenderEvents;
    }

    public List<String> getAvailableDates(){
        String date;
        List <String> availableDates = new ArrayList<>();
        for (LocalDateTime localDateTime: getAvailableTimes()) {
            date = localDateTime.toString().replace("T", " -- ");
            availableDates.add(date);
        }
        return availableDates;
    }

    public List<LocalDateTime> getAvailableTimes(){
        List<LocalDateTime> availableTimes = new ArrayList<>();
        for (LocalDateTime time: getAllTimes()) {
            if(!getUnavailableTimes().contains(time))
                availableTimes.add(time);
        }
        return availableTimes;
    }

    public List<LocalDateTime> getAllTimes(){
        LocalDate today = LocalDate.now();
        int nrOfDays = 14;
        List<LocalDateTime> allTimes = new ArrayList<>();
        for (int i = 0; i < nrOfDays; i++) {
            for (int j = 18; j < 22; j++) {
                allTimes.add(LocalDateTime.of(today.plusDays(i), LocalTime.of(j, 0)));
            }
        }
        return allTimes;
    }

    public List<LocalDateTime> getUnavailableTimes(){
        List<LocalDateTime> unavailableTimes = new ArrayList<>();
        DateTime startDateTime;
        DateTime endDateTime;
        LocalDateTime localStartDateTime;
        LocalDateTime localEndDateTime;
        LocalDate localStartDate;
        LocalDate localEndDate;
        for (List<Event> eventList: userEvents.values()) {
            for (Event event: eventList) {
                startDateTime = event.getStart().getDateTime();
                endDateTime = event.getEnd().getDateTime();
                localStartDateTime = getLocal(startDateTime);
                localStartDate = localStartDateTime.toLocalDate();
                localEndDateTime = getLocal(endDateTime);
                localEndDate = localEndDateTime.toLocalDate();
                if(!localEndDate.equals(localStartDate)) {
                    int startHour = localStartDateTime.getHour();
                    if(startHour < 21)
                        for (int i = 18; i < 22; i++) {
                            unavailableTimes.add(LocalDateTime.of(localStartDate, LocalTime.of(i, 0)));
                        }
                        if(startHour >= 21)
                            for (int i = 19; i < 22; i++) {
                                unavailableTimes.add(LocalDateTime.of(localStartDate, LocalTime.of(i, 0)));
                            }
                    for (LocalDate date: getDatesBetween(localStartDate, localEndDate)) {
                        for (int i = 18; i < 22; i++) {
                            unavailableTimes.add(LocalDateTime.of(date, LocalTime.of(i, 0)));
                        }
                    }
                }
                if(localEndDateTime.getHour() >= 18){
                    for (int i = 18; i < localEndDateTime.getHour()+1 && i < 22; i++) {
                        unavailableTimes.add(LocalDateTime.of(localEndDate, LocalTime.of(i, 0)));
                    }
                }
            }
        }
        return unavailableTimes;
    }

    public LocalDateTime getLocal(DateTime dateTime){
        return LocalDateTime.parse(
                dateTime.toString().substring(0, dateTime.toString().indexOf("."))
        );
    }

    public List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate){
        List<LocalDate> datesBetween = new ArrayList<>();
        int interval = endDate.getDayOfYear()-startDate.getDayOfYear();
        int nrOfDaysBetween = interval-1;
            for (int i = 1; i <= nrOfDaysBetween; i++) {
                datesBetween.add(startDate.plusDays(1));
            }
        return datesBetween;
    }

    public void bookEvent(String chosenMovie, String startDate, List<String> chosenUsers, MovieRepository movieRepository, UserRepository userRepository, TokenRepository tokenRepository){
        Movie movie = movieRepository.findByTitle(chosenMovie);
        DateTime startDateTime = new DateTime(startDate + ":00.000");
        Event event = createNewEvent(movie, startDateTime);
        for (String email: chosenUsers) {
            try {
                User user = userRepository.findByEmail(email);
                Calendar.Events events = getCalender(user, tokenRepository);
                events.insert("primary", event).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Event createNewEvent(Movie movie, DateTime dateTime){
        Event event = new Event();
        event.setSummary("Movie night with: " + movie.getTitle());
        event.setStart(new EventDateTime().setDateTime(dateTime));
        DateTime end = new DateTime(dateTime.getValue()+calculateMovieRuntime(movie));
        event.setEnd(new EventDateTime().setDateTime(end));
        return event;
    }

    public int calculateMovieRuntime(Movie movie){
        int runtime = Integer.parseInt(movie.getRuntime().split(" ")[0])*60*1000;
        return runtime;
    }

    public Calendar.Events getCalender(User user, TokenRepository tokenRepository){
        Calendar.Events events;
        String accessToken = tokenHandler.getAccessToken(user, tokenRepository);
        credential = new GoogleCredential().setAccessToken(accessToken);
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        calender = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
        events = calender.events();
        return events;
    }


}
