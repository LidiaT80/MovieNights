package com.example.demo.utilities;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;

import java.util.List;

public class UserDbHandler {
    public boolean isInDb(User user, UserRepository userRepository){
        List<User> users = (List<User>) userRepository.findAll();
        if(users.stream().anyMatch(user1 -> user1.getEmail().equalsIgnoreCase(user.getEmail())))
            return true;
        return false;
    }
}
