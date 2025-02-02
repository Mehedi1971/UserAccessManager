package com.mahedi.useraccessmanager.dto;

import com.mahedi.useraccessmanager.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private UUID id;

    private String username;

    private String firstname;

    private String lastname;

    private String password;

    private Boolean isActive;

    private RoleDto role;
}
