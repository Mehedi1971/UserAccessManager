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
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Response createUser(UserDto userDto) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User validUser = userMapper.toEntity(userDto);
        User creator = userRepository.findByUsernameAndIsActive(currentUsername, true);

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
        user.setCreatedBy(currentUsername);

        user= userRepository.save(user);

        if(user.getId()!=null){
            return ResponseBuilder.getSuccessResponse(HttpStatus.CREATED,"User created", user);
        }
        return ResponseBuilder.getFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "internal Server Error");
    }

    @Override
    public Response getUsers() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User requester = userRepository.findByUsernameAndIsActive(currentUsername, true);

        if (requester.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            List<User> userList= userRepository.findAllByCreatedByAndIsActive(requester.getUsername(),true);
            return ResponseBuilder.getSuccessResponse(HttpStatus.FOUND,"User Found", userList,userList.size());

        }

        if (requester.getRole().getName().equalsIgnoreCase("ADMIN")) {
            List<User> userList= userRepository.findAllByCreatedByAndIsActive(requester.getUsername(),true);
            return ResponseBuilder.getSuccessResponse(HttpStatus.FOUND,"User Found", userList,userList.size());
        }

        return ResponseBuilder.getFailureResponse(HttpStatus.NOT_FOUND,
                "User Not Found");
    }

    @Override
    public Response updateUser(UUID userId, User user) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User requester = userRepository.findByUsernameAndIsActive(currentUsername, true);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(existingUser.getId()==null){
            return ResponseBuilder.getFailureResponse(HttpStatus.NOT_FOUND,
                    "User Not Found");
        }

        if ("SUPER_ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            return updateAndSaveUser(existingUser, user);
        }

        if ("ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            if (!"USER".equalsIgnoreCase(existingUser.getRole().getName())) {
                return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                        "Unauthorized. ADMIN can only update USER accounts.");
            }

            if (!currentUsername.equals(existingUser.getCreatedBy())) {
                return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                        "Unauthorized. ADMIN can only update users they created.");
            }

            return updateAndSaveUser(existingUser, user);
        }

        return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                "Unauthorized action. Only ADMIN or SUPER_ADMIN can update accounts.");
    }

    private Response updateAndSaveUser(User existingUser, User user) {
        existingUser.setFirstname(user.getFirstname());
        existingUser.setLastname(user.getLastname());
        existingUser.setIsActive(user.getIsActive());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);

        return ResponseBuilder.getSuccessResponse(HttpStatus.OK, "User updated successfully", updatedUser);
    }

    @Override
    public Response deleteUser(UUID userId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User requester = userRepository.findByUsernameAndIsActive(currentUsername, true);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if ("SUPER_ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            userRepository.deleteById(userId);
            return ResponseBuilder.getSuccessResponse(HttpStatus.OK, "User deleted successfully.");
        }

        if ("ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            if (!"USER".equalsIgnoreCase(existingUser.getRole().getName())) {
                return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                        "Unauthorized. ADMIN can only delete USER accounts.");
            }

            if (!currentUsername.equals(existingUser.getCreatedBy())) {
                return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                        "Unauthorized. ADMIN can only delete users they created.");
            }

            userRepository.deleteById(userId);
            return ResponseBuilder.getSuccessResponse(HttpStatus.OK, "User deleted successfully.");
        }

        return ResponseBuilder.getFailureResponse(HttpStatus.UNAUTHORIZED,
                "Unauthorized action. Only ADMIN or SUPER_ADMIN can delete accounts.");

    }
}
