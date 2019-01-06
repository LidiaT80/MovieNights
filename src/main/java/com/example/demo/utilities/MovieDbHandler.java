package com.example.demo.utilities;

import com.example.demo.models.Movie;
import com.example.demo.repositories.MovieRepository;

import java.util.List;
import java.util.stream.Collectors;

public class MovieDbHandler {

    private String chosenMovie;

    public void saveToDb(Movie movie, MovieRepository movieRepository){
        movieRepository.save(movie);
    }

    public boolean isInDb(String title, MovieRepository movieRepository){
        List<Movie> movies = (List<Movie>) movieRepository.findAll();
        if(movies.stream().anyMatch(m -> m.getTitle().equalsIgnoreCase(title)))
            return true;
        return false;
    }

    public Movie findMovie(String title, MovieRepository movieRepository){
        List<Movie> movies = (List<Movie>) movieRepository.findAll();
        Movie movie = (movies.stream().filter(m -> m.getTitle().equalsIgnoreCase(title)).collect(Collectors.toList())).get(0);
        return movie;
    }

    public String getChosenMovie() {
        return chosenMovie;
    }

    public void setChosenMovie(String chosenMovie) {
        this.chosenMovie = chosenMovie;
    }
}
