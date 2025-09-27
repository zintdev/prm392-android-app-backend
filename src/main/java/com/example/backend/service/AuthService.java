package com.example.backend.service;

import com.example.backend.domain.User;
import com.example.backend.dto.*;
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
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @Transactional
    public UserSummary register(RegisterRequest req) {
        if (users.existsByUsernameIgnoreCase(req.username)) throw new IllegalArgumentException("Username already exists");
        if (users.existsByEmailIgnoreCase(req.email)) throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(req.username.trim());
        u.setEmail(req.email.trim());
        u.setPasswordHash(encoder.encode(req.password));
        u.setPhoneNumber(req.phoneNumber);
        u.setAddress(req.address);
        u.setRole("USER");

        users.save(u);

        UserSummary s = new UserSummary();
        s.id = u.getId(); s.username = u.getUsername(); s.email = u.getEmail(); s.role = u.getRole();
        return s;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = users.findByUsernameIgnoreCase(req.usernameOrEmail)
                .or(() -> users.findByEmailIgnoreCase(req.usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password, u.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");

        String token = jwt.generateToken(u.getId(), u.getUsername(), u.getRole());

        UserSummary s = new UserSummary();
        s.id = u.getId(); s.username = u.getUsername(); s.email = u.getEmail(); s.role = u.getRole();

        AuthResponse res = new AuthResponse();
        res.token = token; res.user = s;
        return res;
    }
}
