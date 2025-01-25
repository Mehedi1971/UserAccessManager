package com.mahedi.useraccessmanager.service;

import com.mahedi.useraccessmanager.dto.LoginRequestDto;
import com.mahedi.useraccessmanager.dto.Response;
import com.mahedi.useraccessmanager.dto.UserDto;
import com.mahedi.useraccessmanager.entity.User;

import java.util.UUID;


public interface UserService {

    Response createUser(UserDto userDto, String name);

    Response verify(LoginRequestDto loginRequestDto);

    Response getUsers(String name);

    Response updateUser(UUID userId, User userDto, String name);

    Response deleteUser(UUID userId, String name);
}
