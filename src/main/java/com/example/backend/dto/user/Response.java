package com.example.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    
    private Integer id;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
}
