package com.example.backend.service;

import com.example.backend.domain.entity.Address;
import com.example.backend.domain.entity.User;
import com.example.backend.dto.address.UpdateAddressRequest;
import com.example.backend.dto.address.CreateAddressRequest;
import com.example.backend.dto.address.AddressResponse;
import com.example.backend.exception.custom.AddressAlreadyExistsException;
import com.example.backend.exception.custom.AddressNotFoundException;
import com.example.backend.repository.AddressRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // CREATE
    public AddressResponse create(CreateAddressRequest req) {
        // Validate user exists
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if address already exists for this user
        if (addressRepository.existsByUserIdAndShippingAddressLine1AndShippingCityState(
                req.getUserId(), req.getShippingAddressLine1(), req.getShippingCityState())) {
            throw new AddressAlreadyExistsException("Address already exists for this user");
        }

        Address address = Address.builder()
                .user(user)
                .shippingAddressLine1(req.getShippingAddressLine1())
                .shippingAddressLine2(req.getShippingAddressLine2())
                .shippingCityState(req.getShippingCityState())
                .createdAt(OffsetDateTime.now())
                .build();

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    // READ BY USER ID
    public List<AddressResponse> getAddressByUserId(Integer userId) {
        return addressRepository.findByUserId(userId)
                .stream().map(this::mapToResponse).toList();
    }

    // READ ONE
    public AddressResponse getById(Integer id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
        return mapToResponse(address);
    }

    // UPDATE
    public AddressResponse update(Integer id, UpdateAddressRequest req) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));

        // Update user if provided
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            address.setUser(user);
        }

        if (req.getShippingAddressLine1() != null) {
            address.setShippingAddressLine1(req.getShippingAddressLine1());
        }

        if (req.getShippingAddressLine2() != null) {
            address.setShippingAddressLine2(req.getShippingAddressLine2());
        }

        if (req.getShippingCityState() != null) {
            address.setShippingCityState(req.getShippingCityState());
        }

        return mapToResponse(addressRepository.save(address));
    }

    // DELETE
    public void delete(Integer id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException(id);
        }
        addressRepository.deleteById(id);
    }

    // MAPPER
    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .shippingAddressLine1(address.getShippingAddressLine1())
                .shippingAddressLine2(address.getShippingAddressLine2())
                .shippingCityState(address.getShippingCityState())
                .createdAt(address.getCreatedAt())
                .build();
    }
}