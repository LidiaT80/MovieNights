package com.example.demo.controllers;

import com.example.demo.utilities.MovieDbHandler;
import com.example.demo.models.Movie;
import com.example.demo.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MovieController {
    @Autowired
    private MovieRepository movieRepository;
    private MovieDbHandler movieDbHandler = new MovieDbHandler();
    private RestTemplate restTemplate = new RestTemplate();
    private final String REQUEST_URL = "http://www.omdbapi.com/?apikey=340a04c&t=";

    @RequestMapping("/movie")
    public Movie movie(@RequestParam String title){
        Movie movie;
        if(movieDbHandler.isInDb(title, movieRepository))
            movie = movieDbHandler.findMovie(title, movieRepository);
        else{
            String url = REQUEST_URL + title;
            movie = restTemplate.getForObject(url, Movie.class);
            movieDbHandler.saveToDb(movie, movieRepository);
        }
        return movie;
    }
}
