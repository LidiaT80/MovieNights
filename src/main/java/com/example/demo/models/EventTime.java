package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventTime {
    @JsonProperty("dateTime")
    private DateTime dateTime;
    @JsonProperty("timeZone")
    private String timeZone;

    public EventTime(){

    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
