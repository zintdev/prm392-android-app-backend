package com.example.backend.service;

import com.example.backend.domain.entity.Product;
import com.example.backend.domain.entity.StoreInventory;
import com.example.backend.domain.entity.StoreLocation;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.UserRole;
import com.example.backend.dto.store.StoreInventoryResponse;
import com.example.backend.dto.store.StoreInventoryUpdateRequest;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.StoreInventoryRepository;
import com.example.backend.repository.StoreRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreInventoryService {

    private final StoreInventoryRepository storeInventoryRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public StoreInventoryResponse upsertInventory(Integer storeId, StoreInventoryUpdateRequest request) {
        StoreLocation store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store location not found: " + storeId));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new IllegalArgumentException("Actor user not found: " + request.getActorUserId()));

        validateActor(store, actor);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));

        StoreInventory inventory = storeInventoryRepository
                .findByStoreLocationIdAndProductId(storeId, request.getProductId())
                .orElseGet(() -> StoreInventory.builder()
                        .storeLocation(store)
                        .product(product)
                        .quantity(0)
                        .build());
        int previousQuantity = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        int requestedQuantity = request.getQuantity() == null ? 0 : Math.max(0, request.getQuantity());
        int productQuantity = product.getQuantity() == null ? 0 : Math.max(0, product.getQuantity());

        Integer totalAllocatedBeforeObj = storeInventoryRepository.sumQuantityByProductId(product.getId());
        int totalAllocatedBefore = totalAllocatedBeforeObj != null ? totalAllocatedBeforeObj : 0;
        int totalAllocatedOther = totalAllocatedBefore;
        if (inventory.getId() != null) {
            totalAllocatedOther -= previousQuantity;
        }
        if (totalAllocatedOther < 0) {
            totalAllocatedOther = 0;
        }

        int maxAllowedForStore = Math.max(0, productQuantity - totalAllocatedOther);
        int finalQuantity = Math.min(requestedQuantity, maxAllowedForStore);

        inventory.setQuantity(finalQuantity);

        StoreInventory saved = storeInventoryRepository.save(inventory);

        int totalAllocated = totalAllocatedOther + finalQuantity;
        int remainingQuantity = Math.max(0, productQuantity - totalAllocated);
        int availableForStore = Math.max(0, productQuantity - totalAllocatedOther);

        return buildResponse(store, product, saved.getQuantity(), productQuantity, totalAllocated, remainingQuantity, availableForStore);
    }

    @Transactional(readOnly = true)
    public List<StoreInventoryResponse> listInventory(Integer storeId) {
    StoreLocation store = storeRepository.findById(storeId)
        .orElseThrow(() -> new IllegalArgumentException("Store location not found: " + storeId));

    Map<Integer, StoreInventory> inventoryByProduct = storeInventoryRepository
        .findByStoreLocationId(storeId)
        .stream()
        .collect(Collectors.toMap(inv -> inv.getProduct().getId(), inv -> inv, (existing, replacement) -> replacement));

    Map<Integer, Integer> totalByProduct = new HashMap<>();
    storeInventoryRepository.sumQuantityGroupedByProductId().forEach(row -> {
        if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
            totalByProduct.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }
    });

    return productRepository.findAll().stream()
        .sorted(Comparator.comparing(Product::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
        .map(product -> {
            StoreInventory inventory = inventoryByProduct.get(product.getId());
            int quantity = inventory != null ? inventory.getQuantity() : 0;
            int productQuantity = product.getQuantity() == null ? 0 : Math.max(0, product.getQuantity());
            int totalAllocated = totalByProduct.getOrDefault(product.getId(), 0);
            int totalAllocatedExcludingStore = totalAllocated - quantity;
            if (totalAllocatedExcludingStore < 0) {
                totalAllocatedExcludingStore = 0;
            }
            int remainingQuantity = Math.max(0, productQuantity - totalAllocated);
            int availableForStore = Math.max(0, productQuantity - totalAllocatedExcludingStore);
            return buildResponse(store, product, quantity, productQuantity, totalAllocated, remainingQuantity, availableForStore);
        })
        .collect(Collectors.toList());
    }

    private void validateActor(StoreLocation store, User actor) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }

        if (actor.getRole() == UserRole.STAFF && actor.getStoreLocation() != null
                && actor.getStoreLocation().getId().equals(store.getId())) {
            return;
        }

        throw new IllegalArgumentException("User is not allowed to update inventory for this store");
    }

    private StoreInventoryResponse buildResponse(StoreLocation store,
                         Product product,
                         int quantity,
                         int productQuantity,
                         int totalAllocated,
                         int remainingQuantity,
                         int availableForStore) {
    return StoreInventoryResponse.builder()
        .storeLocationId(store.getId())
        .storeName(store.getStoreName())
        .productId(product.getId())
        .productName(product.getName())
        .productImageUrl(product.getImageUrl())
        .quantity(quantity)
        .productQuantity(productQuantity)
        .totalAllocatedQuantity(totalAllocated)
        .remainingQuantity(remainingQuantity)
        .availableForStore(availableForStore)
        .build();
    }
}
