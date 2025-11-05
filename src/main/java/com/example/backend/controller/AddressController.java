package com.example.backend.controller;

import com.example.backend.dto.address.CreateAddressRequest;
import com.example.backend.dto.address.UpdateAddressRequest;
import com.example.backend.dto.address.AddressResponse;
import com.example.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "APIs for managing addresses")
public class AddressController {

    private final AddressService addressService;

    // CREATE
    @PostMapping
    @Operation(summary = "Create address")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "409", description = "Address already exists")
    })
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody CreateAddressRequest request) {
        AddressResponse response = addressService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ BY USER ID
    @GetMapping("user/{userId}")
    @Operation(summary = "Get addresses by user ID")
    public ResponseEntity<List<AddressResponse>> getAddressByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(addressService.getAddressByUserId(userId));
    }

    // READ ONE
    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<AddressResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(addressService.getById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<AddressResponse> update(@PathVariable Integer id, @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        addressService.delete(id);
        return ResponseEntity.ok().build();
    }
}