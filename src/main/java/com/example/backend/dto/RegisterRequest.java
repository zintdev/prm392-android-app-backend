package com.example.backend.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    public String username;

    @NotBlank
    @Email
    @Size(max = 100)
    public String email;

    @NotBlank
    @Size(min = 6, max = 100)
    public String password;

    @Size(max = 15)
    public String phoneNumber;

    @Size(max = 255)
    public String address;
}
