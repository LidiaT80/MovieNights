package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {
    @Autowired
    CalenderController calenderController;

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
    public String getDatePage(HttpServletResponse servletResponse) throws Exception {
        if(calenderController.getChosenUsers() != null)
            calenderController.getCalEvents(servletResponse);
        return "date";
    }


}