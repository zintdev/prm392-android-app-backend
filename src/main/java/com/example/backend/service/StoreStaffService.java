package com.example.backend.service;

import com.example.backend.domain.entity.StoreLocation;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.UserRole;
import com.example.backend.dto.store.AssignStaffRequest;
import com.example.backend.dto.user.UserResponse;
import com.example.backend.repository.StoreRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreStaffService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public UserResponse assignStaff(Integer storeId, AssignStaffRequest request) {
        StoreLocation store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store location not found: " + storeId));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new IllegalArgumentException("Actor user not found: " + request.getActorUserId()));

        if (actor.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only administrators can assign staff to stores");
        }

        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + request.getStaffId()));

        if (staff.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("User is not registered as staff");
        }

        staff.setStoreLocation(store);
        User saved = userRepository.save(staff);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listStaff(Integer storeId) {
        return userRepository.findByRoleAndStoreLocationId(UserRole.STAFF, storeId).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(UserResponse::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .storeLocationId(user.getStoreLocation() != null ? user.getStoreLocation().getId() : null)
                .storeName(user.getStoreLocation() != null ? user.getStoreLocation().getStoreName() : null)
                .build();
    }
}
