package com.example.backend.service;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.UserRole;
import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.dto.user.UserSummary;
import com.example.backend.repository.UserRepository;
import com.example.backend.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public UserSummary register(RegisterRequest req) {
        if (users.existsByUsernameIgnoreCase(req.username))
            throw new IllegalArgumentException("Username already exists");
        if (users.existsByEmailIgnoreCase(req.email))
            throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(req.username.trim());
        u.setEmail(req.email.trim());
        u.setPasswordHash(encoder.encode(req.password));
        u.setPhoneNumber(req.phoneNumber);
        u.setRole(UserRole.CUSTOMER);

        users.save(u);

        UserSummary s = new UserSummary();
        s.id = u.getId();
        s.username = u.getUsername();
        s.email = u.getEmail();
        s.role = u.getRole().toString();
        return s;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = users.findByUsernameIgnoreCase(req.usernameOrEmail)
                .or(() -> users.findByEmailIgnoreCase(req.usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password, u.getPasswordHash()))
            throw new IllegalArgumentException("Wrong password!");

        String token = jwt.generateToken(u.getId(), u.getUsername(), u.getRole().toString());

        UserSummary s = new UserSummary();
        s.id = u.getId();
        s.username = u.getUsername();
        s.email = u.getEmail();
        s.role = u.getRole().toString();


        AuthResponse res = new AuthResponse();
        res.token = token;
        res.user = s;
        return res;
    }
}
