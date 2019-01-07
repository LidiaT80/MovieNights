package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping(value = "/")
    public String getHomePage(){
        return "index";
    }

    @RequestMapping(value = "/movie")
    public String getMoviePage(){
        return "movie";
    }

    @RequestMapping(value = "/users")
    public String getUserPage(){
        return "user";
    }

    @RequestMapping(value = "/dates")
    public String getDatePage(){
        return "date";
    }


}
