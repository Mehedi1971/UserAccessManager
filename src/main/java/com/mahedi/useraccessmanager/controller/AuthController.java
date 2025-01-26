package com.mahedi.useraccessmanager.controller;

import com.mahedi.useraccessmanager.dto.LoginRequestDto;
import com.mahedi.useraccessmanager.dto.Response;
import com.mahedi.useraccessmanager.dto.UserDto;
import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    /**
     * Handles the creation of a new user.
     *
     * @param userDto the data transfer object containing user details.
     * @return a Response object containing the status and details of the user creation operation.
     */
    @PostMapping("/create")
    public Response createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    /**
     * a Response object containing the status and details of the user creation operation.
     *
     * @param loginRequestDto loginRequestDto the data transfer object containing login credentials (e.g., username and password).
     * @return a Response object containing jwt Token.
     */
    @PostMapping("/login")
    public Response login(@RequestBody LoginRequestDto loginRequestDto) {
        return userService.verify(loginRequestDto);

    }

    /**
     * Retrieves a Role wise list of all registered users.
     *
     * @return a Response object containing a list of users or an error message if no users are found.
     */
    @GetMapping("/users")
    public Response getAllUsers() {
        return userService.getUsers();
    }

    /**
     * Updates the details of an existing user.
     *
     * @param userId the unique identifier of the user to be updated.
     * @param user the data transfer object containing updated user details.
     * @return a Response object indicating the result of the update operation.
     */
    @PutMapping("/users/{userId}")
    public Response updateUser(@PathVariable UUID userId, @RequestBody User user) {
        return userService.updateUser(userId, user);
    }

    /**
     * Deletes an existing user based on the provided user ID.
     *
     * @param userId the unique identifier of the user to be deleted.
     * @return a Response object indicating the result of the delete operation.
     */
    @DeleteMapping("/users/{userId}")
    public Response deleteUser(@PathVariable UUID userId){
        return userService.deleteUser(userId);
    }
}
