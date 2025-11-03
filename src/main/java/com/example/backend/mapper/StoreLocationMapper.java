package com.example.backend.mapper;

import com.example.backend.domain.entity.StoreLocation;
import com.example.backend.dto.store.*;

public final class StoreLocationMapper {
    private StoreLocationMapper(){}

    public static StoreLocation toEntity(StoreLocationRequest r){
        return StoreLocation.builder()
                .storeName(r.getStoreName())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .address(r.getAddress())
                .build();
    }

    public static void updateEntity(StoreLocation e, StoreLocationRequest r){
        e.setStoreName(r.getStoreName());
        e.setLatitude(r.getLatitude());
        e.setLongitude(r.getLongitude());
        e.setAddress(r.getAddress());
    }

    public static StoreLocationResponse toResponse(StoreLocation e){
        return StoreLocationResponse.builder()
                .id(e.getId())
                .storeName(e.getStoreName())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .address(e.getAddress())
                .build();
    }
}
