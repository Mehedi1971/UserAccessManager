package com.mahedi.useraccessmanager.service.impl;

import com.mahedi.useraccessmanager.dto.LoginRequestDto;
import com.mahedi.useraccessmanager.dto.Response;
import com.mahedi.useraccessmanager.dto.TokenDto;
import com.mahedi.useraccessmanager.dto.UserDto;
import com.mahedi.useraccessmanager.entity.Role;
import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.mapper.UserMapper;
import com.mahedi.useraccessmanager.repository.RoleRepository;
import com.mahedi.useraccessmanager.repository.UserRepository;
import com.mahedi.useraccessmanager.service.JwtService;
import com.mahedi.useraccessmanager.service.UserService;
import com.mahedi.useraccessmanager.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public Response verify(LoginRequestDto loginRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),loginRequestDto.getPassword()
                )
        );
        if (authenticate.isAuthenticated()) {
            TokenDto loginResponseDto = new TokenDto();

            loginResponseDto.setUsername(loginRequestDto.getUsername());
            loginResponseDto.setToken(jwtService.generateToken(loginRequestDto));

            return ResponseBuilder.getSuccessResponse(HttpStatus.CREATED,"Token created", loginResponseDto);
        }
        return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                "Unauthorized Access");
    }

    @Override
    public Response createUser(UserDto userDto, String creatorUsername) {
        User validUser = userMapper.toEntity(userDto);
        User creator = userRepository.findByUsernameAndIsActive(creatorUsername, true);

        if(creator.getId()== null){
            return ResponseBuilder.getFailureResponse(HttpStatus.NOT_FOUND,
                    "User not found.");
        }

        Role role = roleRepository.findByName(validUser.getRole().getName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getName().equalsIgnoreCase("ADMIN") && !creator.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Only SUPER_ADMIN can create ADMIN and USER.");
        }

        if (role.getName().equalsIgnoreCase("USER") &&
                !(creator.getRole().getName().equalsIgnoreCase("ADMIN") || creator.getRole().getName().equalsIgnoreCase("SUPER_ADMIN"))) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Unauthorized to create USER accounts.");
        }

        User user = new User();
        user.setUsername(validUser.getUsername());
        user.setFirstname(validUser.getFirstname());
        user.setLastname(validUser.getLastname());
        user.setPassword(bCryptPasswordEncoder.encode(validUser.getPassword()));
        user.setIsActive(validUser.getIsActive());
        user.setRole(role);
        user.setCreatedBy(creatorUsername);

        user= userRepository.save(user);

        if(user.getId()!=null){
            return ResponseBuilder.getSuccessResponse(HttpStatus.CREATED,"User created", user);
        }
        return ResponseBuilder.getFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "internal Server Error");
    }

    @Override
    public Response getUsers(String name) {
        User requester = userRepository.findByUsernameAndIsActive(name, true);

        if (requester.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            List<User> userList= userRepository.findAllByCreatedByAndIsActive(requester.getUsername(),true);
            return ResponseBuilder.getSuccessResponse(HttpStatus.FOUND,"User Found", userList,userList.size());

        }

        if (requester.getRole().getName().equalsIgnoreCase("ADMIN")) {
            List<User> userList= userRepository.findAllByCreatedByAndIsActive(requester.getUsername(),true);
            return ResponseBuilder.getSuccessResponse(HttpStatus.FOUND,"User Found", userList,userList.size());
        }

        return ResponseBuilder.getFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "internal Server Error");
    }

    @Override
    public Response updateUser(UUID userId, User userDto, String name) {
        User requester = userRepository.findByUsernameAndIsActive(name, true);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(userDto.getRole().getName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (requester.getId().equals(existingUser.getId()) &&
                "ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Admins are not allowed to update their own accounts.");
        }

        if (role.getName().equalsIgnoreCase("ADMIN") && !requester.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Unauthorized. Only SUPER_ADMIN can create ADMIN and USER.");
        }

        if (role.getName().equalsIgnoreCase("USER") &&
                !(requester.getRole().getName().equalsIgnoreCase("ADMIN") || requester.getRole().getName().equalsIgnoreCase("SUPER_ADMIN"))) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Unauthorized to create USER accounts.");
        }

        existingUser.setFirstname(userDto.getFirstname());
        existingUser.setLastname(userDto.getLastname());
        existingUser.setIsActive(userDto.getIsActive());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        }

        existingUser= userRepository.save(existingUser);

        if(existingUser.getId()!=null){
            return ResponseBuilder.getSuccessResponse(HttpStatus.CREATED,"User updated", existingUser);
        }
        return ResponseBuilder.getFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "internal Server Error");
    }

    @Override
    public Response deleteUser(UUID userId,String name) {
        User requester = userRepository.findByUsernameAndIsActive(name, true);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(existingUser.getRole().getName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (requester.getId().equals(existingUser.getId()) &&
                "ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Admins are not allowed to update their own accounts.");
        }
        if (role.getName().equalsIgnoreCase("ADMIN") && !requester.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Unauthorized. Only SUPER_ADMIN can create ADMIN and USER.");
        }

        if (role.getName().equalsIgnoreCase("USER") &&
                !(requester.getRole().getName().equalsIgnoreCase("ADMIN") || requester.getRole().getName().equalsIgnoreCase("SUPER_ADMIN"))) {
            return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                    "Unauthorized to create USER accounts.");
        }

        userRepository.deleteById(userId);

        return ResponseBuilder.getSuccessResponse(HttpStatus.OK, "User deleted successfully");

    }
}
