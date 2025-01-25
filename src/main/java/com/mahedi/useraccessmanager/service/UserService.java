package com.mahedi.useraccessmanager.service;

import com.mahedi.useraccessmanager.entity.Role;
import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.repository.RoleRepository;
import com.mahedi.useraccessmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleInfo;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    public User register(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        if (user.getRole() != null) {
            Role existingRole = roleRepository.findByName(user.getRole().getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + user.getRole().getName()));

            user.setRole(existingRole);
        }

        return userRepository.save(user);
    }



    public String verify(User user) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),user.getPassword()
                )
        );
        if (authenticate.isAuthenticated()) {
            return jwtService.generateToken(user);
        }
        return "failure";
    }

    public User createUser(User userDto, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername);

        Role role = roleRepository.findByName(userDto.getRole().getName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Check if creator is Superadmin when creating Admin users
        if (role.getName().equalsIgnoreCase("ADMIN") && !creator.getRole().getName().equalsIgnoreCase("SUPERADMIN")) {
            throw new RuntimeException("Only SUPER_ADMIN can create ADMIN users.");
        }

        // Check if creator is allowed to create Regular users
        if (role.getName().equalsIgnoreCase("USER") &&
                !(creator.getRole().getName().equalsIgnoreCase("ADMIN") || creator.getRole().getName().equalsIgnoreCase("SUPERADMIN"))) {
            throw new RuntimeException("Unauthorized to create USER accounts.");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        user.setIsActive(userDto.getIsActive());
        user.setRole(role);
//        user.setCreatedBy(creator);

        return userRepository.save(user);
    }
}
