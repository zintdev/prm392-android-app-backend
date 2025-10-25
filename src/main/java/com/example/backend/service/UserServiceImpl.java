package com.example.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.UserRole;
import com.example.backend.dto.user.UserRequest;
import com.example.backend.dto.user.UserResponse;
import com.example.backend.exception.custom.UserAlreadyExistsException;
import com.example.backend.exception.custom.UserNotFoundException;
import com.example.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Integer adminUserId;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.user-id}") Integer adminUserId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUserId = adminUserId;
    }

    // CREATE
    @Override
    public UserResponse createUser(UserRequest request) {
        if (request == null) throw new IllegalArgumentException("UserRequest is required");

        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        UserRole role = UserRole.CUSTOMER;
        if (request.getRole() != null) {
            try {
                role = UserRole.valueOf(request.getRole());
            } catch (IllegalArgumentException ignored) {
                role = UserRole.CUSTOMER;
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .build();

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    // READ ALL
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // READ by ID
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    // READ by username
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    // UPDATE
    @Override
    public UserResponse updateUser(Integer id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getUsername() != null && !existing.getUsername().equalsIgnoreCase(request.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
            }
            existing.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !existing.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            }
            existing.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            existing.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            try {
                UserRole newRole = UserRole.valueOf(request.getRole());
                existing.setRole(newRole);
            } catch (IllegalArgumentException ignored) {
                // ignore invalid role
            }
        }

        User updated = userRepository.save(existing);
        return mapToResponse(updated);
    }

    // DELETE
    @Override
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // MAPPER
    @Override
    public UserResponse mapToResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    // isAdmin: ưu tiên config-based adminUserId, fallback reading DB role
    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(Integer userId) {
        if (userId == null) return false;
        if (adminUserId != null && adminUserId.equals(userId)) return true;

        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.map(u -> u.getRole() == UserRole.ADMIN).orElse(false);
    }

    @Override
    public Integer getAdminUserId() {
        return adminUserId;
    }
}
