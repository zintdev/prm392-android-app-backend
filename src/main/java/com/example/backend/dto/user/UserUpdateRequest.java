package com.example.backend.dto.user;

import com.example.backend.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateRequest", description = "Update User request body")
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(min = 6, message = "Old password must be at least 6 characters")
    private String oldPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;

    private UserRole role;
}
