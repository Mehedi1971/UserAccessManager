package com.mahedi.useraccessmanager.service;

import com.mahedi.useraccessmanager.dto.LoginRequestDto;
import com.mahedi.useraccessmanager.dto.Response;
import com.mahedi.useraccessmanager.dto.UserDto;
import com.mahedi.useraccessmanager.entity.User;

import java.util.UUID;


public interface UserService {

    Response createUser(UserDto userDto);

    Response verify(LoginRequestDto loginRequestDto);

    Response getUsers();

    Response updateUser(UUID userId, User user);

    Response deleteUser(UUID userId);
}
