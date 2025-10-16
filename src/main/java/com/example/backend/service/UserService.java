package com.example.backend.service;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.UserRole;
import com.example.backend.dto.user.UserRequest;
import com.example.backend.dto.user.UserResponse;
import com.example.backend.exception.custom.UserAlreadyExistsException;
import com.example.backend.exception.custom.UserNotFoundException;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // CREATE - Tạo user mới
    public UserResponse createUser(UserRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.valueOf(request.getRole() != null ? request.getRole() : "USER"))
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    // READ - Lấy tất cả users
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // READ - Lấy user theo ID
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    // READ - Lấy user theo username
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    // UPDATE - Cập nhật user
    public UserResponse updateUser(Integer id, UserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Kiểm tra username có thay đổi không và đã tồn tại chưa
        if (!existingUser.getUsername().equalsIgnoreCase(request.getUsername())
                && userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Kiểm tra email có thay đổi không và đã tồn tại chưa
        if (!existingUser.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Cập nhật thông tin user
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setPhoneNumber(request.getPhoneNumber());

        // Chỉ cập nhật password nếu có trong request
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật role nếu có trong request
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            existingUser.setRole(UserRole.valueOf(request.getRole()));
            try {
                existingUser.setRole(UserRole.valueOf(request.getRole().toUpperCase())); // <-- chuyển String -> enum
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole() +
                        " (allowed: ADMIN, STAFF, CUSTOMER)");
            }
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToResponse(updatedUser);
    }

    // DELETE - Xóa user
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // Utility method để map User entity sang Response DTO
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(String.valueOf(user.getRole()))
                .build();
    }
}
