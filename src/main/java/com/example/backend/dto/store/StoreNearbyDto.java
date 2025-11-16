package com.example.backend.dto.store;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreNearbyDto {
    private Integer storeId;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private double distanceKm;
    private Integer quantity; // nullable nếu không filter product
}
