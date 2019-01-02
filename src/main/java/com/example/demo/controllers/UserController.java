package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utilities.UserDbHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private UserDbHandler userDbHandler = new UserDbHandler();

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<List<User>> showUsers(){
        List<User> users = (List<User>) userRepository.findAll();
        if(users.size() == 0)
            return new ResponseEntity<>(users, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @RequestMapping(value = "users/{name}")
    public ResponseEntity<List<User>> findUsersByName(@PathVariable("name") String name){
        List<User> users = userDbHandler.findUsersByName(userRepository, name);
        if(users.size() == 0)
            return new ResponseEntity<>(users, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @RequestMapping(value = "/saveUser")
    public ResponseEntity<User> saveUser(@RequestParam String name, @RequestParam String email, @RequestParam String password){
        User newUser = new User(name, email, password);
        if(!userDbHandler.isInDb(newUser, userRepository)){
            userRepository.save(newUser);
            return new ResponseEntity(newUser, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(newUser,HttpStatus.OK);
    }
}
