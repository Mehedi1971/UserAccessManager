package com.mahedi.useraccessmanager.service;

import com.mahedi.useraccessmanager.entity.Role;
import com.mahedi.useraccessmanager.entity.User;
import com.mahedi.useraccessmanager.repository.RoleRepository;
import com.mahedi.useraccessmanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetaDataService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void initialize() {
        createDefaultRoles();
        createDefaultSuperAdmin();
    }

    private void createDefaultRoles() {
        if (roleRepository.count() == 0) {
            Role superAdminRole = new Role();
            superAdminRole.setName("SUPER_ADMIN");

            Role adminRole = new Role();
            adminRole.setName("ADMIN");

            Role userRole = new Role();
            userRole.setName("USER");

            roleRepository.saveAll(List.of(superAdminRole, adminRole, userRole));
        }
    }

    public void createDefaultSuperAdmin() {
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
                superadmin.setRole(superAdminRole);
                superadmin.setCreatedBy("superadmin");

                userRepository.save(superadmin);

                log.warn("Default SuperAdmin user created with username: 'superadmin' and default password.");
            }
        }
    }
}
