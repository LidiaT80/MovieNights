package com.example.demo.utilities;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

public class UserDbHandler {

    public boolean isInDb(User user, UserRepository userRepository){
        List<User> users = (List<User>) userRepository.findAll();
        if(users.stream().anyMatch(user1 -> user1.getEmail().equalsIgnoreCase(user.getEmail())))
            return true;
        return false;
    }

    public List<User> findUsersByName(UserRepository userRepository, String name){
        List<User> users = (List<User>) userRepository.findAll();
        List<User> userList = users.stream()
                                .filter(user -> user.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
        return userList;
    }

    public User findUserByEmail(UserRepository userRepository, String email){
        List<User> users = (List<User>) userRepository.findAll();
        User user = users.stream()
                    .filter(user1 -> user1.getEmail().equalsIgnoreCase(email)).collect(Collectors.toList()).get(0);
        return user;
    }
}
