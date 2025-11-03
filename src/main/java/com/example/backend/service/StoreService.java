package com.example.backend.service;

import com.example.backend.domain.entity.StoreLocation;
import com.example.backend.dto.store.*;
import com.example.backend.mapper.StoreLocationMapper;
import com.example.backend.repository.StoreRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import java.util.List;

@Service
public class StoreService {

    private final StoreRepository repo;

    public StoreService(StoreRepository repo) {
        this.repo = repo;
    }

    public StoreLocationResponse create(StoreLocationRequest req) {
        if (repo.existsByStoreNameIgnoreCase(req.getStoreName()) ||
            repo.existsByAddressIgnoreCase(req.getAddress())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Tên cửa hàng hoặc địa chỉ này đã tồn tại");
        }
        StoreLocation saved = repo.save(StoreLocationMapper.toEntity(req));
        return StoreLocationMapper.toResponse(saved);
    }

    public StoreLocationResponse get(Integer id) {
        StoreLocation found = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("StoreLocation not found: " + id));
        return StoreLocationMapper.toResponse(found);
    }

    public Page<StoreLocationResponse> list(Pageable pageable) {
        var safePage = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), sanitizeSort(pageable.getSort()));
        return repo.findAll(safePage).map(StoreLocationMapper::toResponse);
    }

    public StoreLocationResponse update(Integer id, StoreLocationRequest req) {
        StoreLocation entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("StoreLocation not found: " + id));
        // conflict checks only if changed
        boolean nameChanged = req.getStoreName() != null && !req.getStoreName().equalsIgnoreCase(entity.getStoreName());
        boolean addrChanged = req.getAddress() != null && !req.getAddress().equalsIgnoreCase(entity.getAddress());
        if ((nameChanged && repo.existsByStoreNameIgnoreCase(req.getStoreName())) ||
            (addrChanged && repo.existsByAddressIgnoreCase(req.getAddress()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Tên cửa hàng hoặc địa chỉ này đã tồn tại");
        }
        StoreLocationMapper.updateEntity(entity, req);
        return StoreLocationMapper.toResponse(repo.save(entity));
    }

    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("StoreLocation not found: " + id);
        }
        repo.deleteById(id);
    }

    private Sort sanitizeSort(Sort sort) {
        var allowed = java.util.Set.of("id", "storeName", "latitude", "longitude", "address");
        var cleaned = sort.stream().filter(o -> allowed.contains(o.getProperty())).toList();
        return cleaned.isEmpty() ? Sort.by(Sort.Order.asc("address")) : Sort.by(cleaned);
    }

     public List<StoreNearbyDto> getNearby(double lat, double lng, Double radiusKm, int limit) {
        return repo.findNearby(lat, lng, radiusKm, limit)
                .stream().map(this::mapRow).collect(Collectors.toList());
    }

    public List<StoreNearbyDto> getNearbyForProduct(double lat, double lng, Double radiusKm,
                                                    int limit, int productId, boolean inStockOnly) {
        return repo.findNearbyWithInventory(lat, lng, radiusKm, limit, productId, inStockOnly)
                .stream().map(this::mapRowWithQty).collect(Collectors.toList());
    }

    private StoreNearbyDto mapRow(Object[] r) {
        return StoreNearbyDto.builder()
                .storeId(((Number) r[0]).intValue())
                .name((String) r[1])
                .address((String) r[2])
                .latitude(((Number) r[3]).doubleValue())
                .longitude(((Number) r[4]).doubleValue())
                .distanceKm(((Number) r[5]).doubleValue())
                .build();
    }

    private StoreNearbyDto mapRowWithQty(Object[] r) {
        return StoreNearbyDto.builder()
                .storeId(((Number) r[0]).intValue())
                .name((String) r[1])
                .address((String) r[2])
                .latitude(((Number) r[3]).doubleValue())
                .longitude(((Number) r[4]).doubleValue())
                .distanceKm(((Number) r[5]).doubleValue())
                .quantity(((Number) r[6]).intValue())
                .build();
    }

}
