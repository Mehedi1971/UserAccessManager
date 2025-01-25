package com.mahedi.useraccessmanager.repository;

import com.mahedi.useraccessmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsernameAndIsActive(String username, Boolean isActive);

    List<User> findAllByCreatedByAndIsActive(String createdBy, Boolean isActive);
}
