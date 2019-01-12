package com.example.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getHomePage(){
        return "index";
    }

    @RequestMapping(value = "/movie", method = RequestMethod.GET)
    public String getMoviePage(){
        return "movie";
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getUserPage(){
        return "user";
    }

    @RequestMapping(value = "/date", method = RequestMethod.GET)
    public String getDatePage(){
        return "date";
    }


}