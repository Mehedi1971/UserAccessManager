package com.mahedi.useraccessmanager.controller;

import com.mahedi.useraccessmanager.dto.LoginRequestDto;
import com.mahedi.useraccessmanager.dto.Response;
import com.mahedi.useraccessmanager.dto.UserDto;
import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;


    @PostMapping("/create")
    public Response createUser(@RequestBody UserDto userDto, Principal principal) {
        return userService.createUser(userDto, principal.getName());
    }


    @PostMapping("/login")
    public Response login(@RequestBody LoginRequestDto loginRequestDto) {
        return userService.verify(loginRequestDto);

    }

    @GetMapping("/users")
    public Response getAllUsers(Principal principal) {
        return userService.getUsers(principal.getName());
    }

    @PutMapping("/users/{userId}")
    public Response updateUser(@PathVariable UUID userId, @RequestBody User userDto, Principal principal) {
        return userService.updateUser(userId, userDto, principal.getName());
    }

    @DeleteMapping("/users/{userId}")
    public Response deleteUser(@PathVariable UUID userId, Principal principal){
        return userService.deleteUser(userId,principal.getName());
    }
}
