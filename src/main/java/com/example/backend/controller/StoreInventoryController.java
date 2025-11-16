package com.example.backend.controller;

import com.example.backend.dto.store.StoreInventoryResponse;
import com.example.backend.dto.store.StoreInventoryUpdateRequest;
import com.example.backend.service.StoreInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-locations/{storeId}/inventory")
@RequiredArgsConstructor
public class StoreInventoryController {

    private final StoreInventoryService storeInventoryService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public StoreInventoryResponse updateInventory(@PathVariable("storeId") Integer storeId,
                                                  @Valid @RequestBody StoreInventoryUpdateRequest request) {
        return storeInventoryService.upsertInventory(storeId, request);
    }

    @GetMapping
    public List<StoreInventoryResponse> listInventory(@PathVariable("storeId") Integer storeId) {
        return storeInventoryService.listInventory(storeId);
    }
}
