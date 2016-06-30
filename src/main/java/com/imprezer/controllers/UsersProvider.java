package com.imprezer.controllers;

import com.imprezer.model.User;
import com.imprezer.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class UsersProvider {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping({"/user", "/me"})
    public User user(Principal principal) {
        try {
            return addIfNotExistsAndGet((OAuth2Authentication) principal);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User addIfNotExistsAndGet(OAuth2Authentication principal) {
        User user = null;
        if (principal != null) {
            user = userRepository.findByIdentification(principal.getName());
            if (user == null) {
                user = new User(principal.getName(),
                        ((Map<String, String>) (principal.getUserAuthentication().getDetails())).get("name"));
                userRepository.insert(user);
            }
        }
        return user;
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
