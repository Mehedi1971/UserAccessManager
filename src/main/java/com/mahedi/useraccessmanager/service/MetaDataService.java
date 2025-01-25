package com.mahedi.useraccessmanager.service;

import com.mahedi.useraccessmanager.entity.Role;
import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.repository.RoleRepository;
import com.mahedi.useraccessmanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetaDataService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @PostConstruct
    public void run() {
        if (roleRepository.count() == 0) {
            Role superAdminRole = new Role();
            superAdminRole.setName("SUPER_ADMIN");

            Role adminRole = new Role();
            adminRole.setName("ADMIN");

            Role userRole = new Role();
            userRole.setName("USER");

            roleRepository.saveAll(List.of(superAdminRole, adminRole, userRole));
        }

        if (userRepository.count() == 0) {
            if (userRepository.count() == 0) {
                Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                        .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

                User superadmin = new User();
                superadmin.setUsername("superadmin");
                superadmin.setFirstname("Super");
                superadmin.setLastname("Admin");
                superadmin.setPassword(passwordEncoder.encode("SuperSecurePassword123"));
                superadmin.setIsActive(true);
                superadmin.setRole(superAdminRole); // Assign properly fetched Role

                userRepository.save(superadmin);

                System.out.println("SuperAdmin user created with default password.");
            }
        }
    }
}
