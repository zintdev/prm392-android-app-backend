package com.example.backend.dto;

import jakarta.validation.constraints.*;

public class LoginRequest {
    @NotBlank
    public String usernameOrEmail;

    @NotBlank
    public String password;
}
