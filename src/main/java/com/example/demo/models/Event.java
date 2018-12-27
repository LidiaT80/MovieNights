package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    @JsonProperty("creator")
    private User creator;
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("start")
    private EventTime start;
    @JsonProperty("end")
    private EventTime end;

    public Event(){
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public EventTime getStart() {
        return start;
    }

    public void setStart(EventTime start) {
        this.start = start;
    }

    public EventTime getEnd() {
        return end;
    }

    public void setEnd(EventTime end) {
        this.end = end;
    }
}
