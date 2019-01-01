package com.example.demo.utilities;

import com.example.demo.models.User;
import com.example.demo.repositories.TokenRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.util.*;

public class CalenderHandler {
    private static com.google.api.services.calendar.Calendar calender;
    private static final String APPLICATION_NAME = "MovieNights";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Credential credential;
    private TokenHandler tokenHandler = new TokenHandler();
    private Map<String,List<Event>> userEvents = new HashMap<>();
    private final int MSEC_PER_DAY = 1000*60*60*24;

    public List<Event> getEvents(User user, TokenRepository tokenRepository){
        Calendar.Events events;
        com.google.api.services.calendar.model.Events eventList;
        List<Event> calenderEvents = null;
        String accessToken = tokenHandler.getAccessToken(user, tokenRepository);
        credential = new GoogleCredential().setAccessToken(accessToken);
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            calender = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
            events = calender.events();
            eventList = events.list("primary").setTimeMin(new DateTime(new Date())).execute();
            calenderEvents = eventList.getItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        userEvents.put(user.getEmail(), calenderEvents);
        return calenderEvents;
    }

    public List<String> getAvailableDates(){
        List<String> unavailableDates = new ArrayList<>();
        List<String> allDates = new ArrayList<>();
        List <String> availableDates = new ArrayList<>();
        DateTime now;
        DateTime threeWeeksfromNow;
        DateTime startDate;
        DateTime endDate;
        for (List<Event> eventList: userEvents.values()) {
            for (Event event: eventList) {
                startDate = event.getStart().getDateTime();
                endDate = event.getEnd().getDateTime();
                addDatesToList(startDate, endDate, unavailableDates);
            }
        }
        now = new DateTime(new Date());
        threeWeeksfromNow = new DateTime(now.getValue()+21*MSEC_PER_DAY);
        addDatesToList(now, threeWeeksfromNow, allDates);
        for (String dateString: allDates) {
            if(!unavailableDates.contains(dateString))
                availableDates.add(dateString);
        }
        return availableDates;
    }

    public void addDatesToList(DateTime start, DateTime end, List<String> dateList){
        String startStr = getDateString(start);
        String endStr = getDateString(end);
        addDate(startStr, dateList);
        if(!startStr.equals(endStr)){
            List<DateTime> datesBetween= getDatesBetween(start, end);
            for (DateTime date: datesBetween) {
                addDate(getDateString(date), dateList);
            }
            addDate(endStr, dateList);
        }
    }

    public String getDateString(DateTime dateTime){
        String dateString = dateTime.toString().substring(0, 10);
        return dateString;
    }

    public void addDate(String dateString, List<String> dateTimeList){
        if(!dateTimeList.contains(dateString))
            dateTimeList.add(dateString);
    }

    public List<DateTime> getDatesBetween(DateTime startDate, DateTime endDate){
        List<DateTime> datesBetween = new ArrayList<>();
        Long interval = endDate.getValue()-startDate.getValue();
        Long nrOfDays = interval/ MSEC_PER_DAY;
        if(nrOfDays > 1){
            for (int i = 1; i < nrOfDays; i++) {
                datesBetween.add(new DateTime(startDate.getValue()+i* MSEC_PER_DAY));
            }
        }
        return datesBetween;
    }


}
