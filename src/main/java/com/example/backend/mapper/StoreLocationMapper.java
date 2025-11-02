package com.example.backend.mapper;

import com.example.backend.domain.entity.StoreLocation;
import com.example.backend.dto.store.StoreLocationRequest;
import com.example.backend.dto.store.StoreLocationResponse;

public class StoreLocationMapper {

    public static StoreLocation toEntity(StoreLocationRequest req) {
        return StoreLocation.builder()
                .storeName(req.getStoreName()) // <-- THÊM DÒNG NÀY
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .build();
    }

    public static StoreLocationResponse toResponse(StoreLocation entity) {
        return StoreLocationResponse.builder()
                .id(entity.getId())
                .storeName(entity.getStoreName()) // <-- THÊM DÒNG NÀY
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    public static void updateEntity(StoreLocation entity, StoreLocationRequest req) {
        entity.setStoreName(req.getStoreName()); // <-- THÊM DÒNG NÀY
        entity.setAddress(req.getAddress());
        entity.setLatitude(req.getLatitude());
        entity.setLongitude(req.getLongitude());
    }
}
