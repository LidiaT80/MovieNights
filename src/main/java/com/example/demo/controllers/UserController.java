package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utilities.UserDbHandler;
import com.example.demo.utilities.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private UserDbHandler userDbHandler = new UserDbHandler();
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @JsonView(Views.Public.class)
    @RequestMapping(value = "/users/all", method = RequestMethod.GET)
    public ResponseEntity showUsers(){
        List<User> users = (List<User>) userRepository.findAll();
        if(users.size() == 0)
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @JsonView(Views.Public.class)
    @RequestMapping(value = "users/{name}", method = RequestMethod.GET)
    public ResponseEntity findUsersByName(@PathVariable("name") String name){
        List<User> users = userDbHandler.findUsersByName(userRepository, name);
        if(users.size() == 0)
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @JsonView(Views.Public.class)
    @RequestMapping(value = "/new-user", method = RequestMethod.POST)
    public ResponseEntity saveUser(@RequestBody User user){
        String password = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(password);
        if(!userDbHandler.isInDb(user, userRepository)){
            userRepository.save(user);
            return new ResponseEntity("Registered new user", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("User account already exists",HttpStatus.OK);
    }
}
