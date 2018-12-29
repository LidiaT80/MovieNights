package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utilities.UserDbHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private UserDbHandler userDbHandler = new UserDbHandler();

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> showUsers(){
        List<User> users = (List<User>) userRepository.findAll();
        return users;
    }

    @RequestMapping(value = "users/{name}")
    public List<User> findUsersByName(@PathVariable("name") String name){
        List<User> users = userDbHandler.findUsersByName(userRepository, name);
        return users;
    }

    @RequestMapping(value = "/saveUser")
    public RedirectView saveUser(@RequestParam String name, @RequestParam String email, @RequestParam String password){
        User newUser = new User(name, email, password);
        if(!userDbHandler.isInDb(newUser, userRepository)){
            userRepository.save(newUser);
        }
        return new RedirectView("/users");
    }
}
