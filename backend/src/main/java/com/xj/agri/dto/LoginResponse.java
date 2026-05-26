package com.xj.agri.dto;

public record LoginResponse(
        String token,
        String username,
        String realName,
        String role
) {
}
