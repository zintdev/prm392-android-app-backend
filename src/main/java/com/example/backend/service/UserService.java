package com.example.backend.service;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.UserRole;
import com.example.backend.dto.user.UserRequest;
import com.example.backend.dto.user.UserResponse;
import com.example.backend.dto.user.UserUpdateRequest;
import com.example.backend.exception.custom.UserAlreadyExistsException;
import com.example.backend.exception.custom.UserNotFoundException;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Integer adminUserId;

    public UserService(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.user-id}") Integer adminUserId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUserId = adminUserId;
    }

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
                .role(request.getRole() != null ?
                        UserRole.valueOf(request.getRole().toString().toUpperCase()) : UserRole.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    // READ - Lấy tất cả users
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByRoleNot(UserRole.ADMIN).stream()
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
    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
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
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            existingUser.setUsername(request.getUsername());
        }


        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            existingUser.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            existingUser.setPhoneNumber(request.getPhoneNumber());
        }

        // ✅ Kiểm tra mật khẩu cũ trước khi đổi sang mật khẩu mới
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new IllegalArgumentException("Old password is required to update password.");
            }

            // So sánh mật khẩu cũ với hash trong DB
            if (!passwordEncoder.matches(request.getOldPassword(), existingUser.getPasswordHash())) {
                throw new IllegalArgumentException("Old password is incorrect.");
            }

            // Nếu đúng thì mã hóa và lưu mật khẩu mới
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
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
        .storeLocationId(user.getStoreLocation() != null ? user.getStoreLocation().getId() : null)
    .storeName(user.getStoreLocation() != null ? user.getStoreLocation().getStoreName() : null)
    .storeAddress(user.getStoreLocation() != null ? user.getStoreLocation().getAddress() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Integer userId) {
        if (userId == null) return false;
        if (adminUserId != null && adminUserId.equals(userId)) return true;

        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.map(u -> u.getRole() == UserRole.ADMIN).orElse(false);
    }

    public Integer getAdminUserId() {
        return adminUserId;
    }


}
