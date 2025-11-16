package com.example.backend.controller;

import com.example.backend.dto.store.*;
import com.example.backend.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/store-locations")
public class StoreLocationController {

    private final StoreService service;

    public StoreLocationController(StoreService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreLocationResponse create(@Valid @RequestBody StoreLocationRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public StoreLocationResponse get(@PathVariable("id") Integer id) {
        return service.get(id);
    }

    @GetMapping
    public Page<StoreLocationResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    public StoreLocationResponse update(@PathVariable("id") Integer id,
            @Valid @RequestBody StoreLocationRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id) {
        service.delete(id);
    }

     @GetMapping("/nearby")
    public List<StoreNearbyDto> nearby(
 @RequestParam(name = "lat")  @NotNull Double lat,
            @RequestParam(name = "lng")  @NotNull Double lng,
            @RequestParam(name = "radiusKm", required = false) Double radiusKm,
            @RequestParam(name = "limit",    defaultValue = "50") Integer limit,
            @RequestParam(name = "productId", required = false) Integer productId,
            @RequestParam(name = "inStockOnly", defaultValue = "false") boolean inStockOnly
    ){
        if (productId != null) {
            return service.getNearbyForProduct(lat, lng, radiusKm, limit, productId, inStockOnly);
        }
        return service.getNearby(lat, lng, radiusKm, limit);
    }
}
