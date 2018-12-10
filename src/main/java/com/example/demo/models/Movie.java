package com.example.demo.models;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "movie")
@Table(name = "movie")
@DynamicUpdate
public class Movie {
    @Id
    @GeneratedValue
    private int id;
    @Column(name = "title")
    @NotNull
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "release_year")
    private int releaseYear;

    public Movie(String title, String description, int releaseYear){
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }
}
