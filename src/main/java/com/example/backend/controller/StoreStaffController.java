package com.example.backend.controller;

import com.example.backend.dto.store.AssignStaffRequest;
import com.example.backend.dto.user.UserResponse;
import com.example.backend.service.StoreStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-locations/{storeId}/staff")
@RequiredArgsConstructor
public class StoreStaffController {

    private final StoreStaffService storeStaffService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public UserResponse assignStaff(@PathVariable("storeId") Integer storeId,
                                    @Valid @RequestBody AssignStaffRequest request) {
        return storeStaffService.assignStaff(storeId, request);
    }

    @GetMapping
    public List<UserResponse> listStaff(@PathVariable("storeId") Integer storeId) {
        return storeStaffService.listStaff(storeId);
    }
}
