package com.example.demo.controllers;

import com.example.demo.utilities.MovieDbHandler;
import com.example.demo.models.Movie;
import com.example.demo.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class MovieController {
    @Autowired
    private MovieRepository movieRepository;
    private MovieDbHandler movieDbHandler = new MovieDbHandler();
    private RestTemplate restTemplate = new RestTemplate();
    private final String REQUEST_URL = "http://www.omdbapi.com/?apikey=340a04c&t=";

    @RequestMapping(value = "/movie/{title}", method = RequestMethod.GET)
    public ResponseEntity getMovie(@PathVariable String title){
        Movie movie;
        if(movieDbHandler.isInDb(title, movieRepository))
            movie = movieDbHandler.findMovie(title, movieRepository);
        else{
            String url = REQUEST_URL + title;
            movie = restTemplate.getForObject(url, Movie.class);
            movieDbHandler.saveToDb(movie, movieRepository);
        }
        if(movie == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(movie, HttpStatus.OK);
    }

    @RequestMapping(value = "/movie/{title}/save", method = RequestMethod.POST)
    public ResponseEntity saveChosenMovie(@PathVariable String title){
        movieDbHandler.setChosenMovie(title);
        return new ResponseEntity("Saved", HttpStatus.CREATED);
    }
}
