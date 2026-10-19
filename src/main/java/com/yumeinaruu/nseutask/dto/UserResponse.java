package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role
) {
}
