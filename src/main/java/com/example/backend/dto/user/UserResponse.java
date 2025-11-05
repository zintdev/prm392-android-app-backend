package com.example.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserResponse", description = "User response body")
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
}