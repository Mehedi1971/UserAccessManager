package com.mahedi.useraccessmanager.controller;

import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;


    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user, Principal principal) {
        User users = userService.createUser(user, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(users);
    }

//    @PostMapping("/register")
//    public User register(@RequestBody User user) {
//        return userService.register(user);
//    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);

    }
}
