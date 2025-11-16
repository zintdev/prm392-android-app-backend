package com.example.backend.controller;

import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.dto.user.UserSummary;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){ this.auth = auth; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary register(@RequestBody @Valid RegisterRequest req){
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req){
        return auth.login(req);
    }
    
}
