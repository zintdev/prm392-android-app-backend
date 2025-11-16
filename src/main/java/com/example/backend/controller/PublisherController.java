package com.example.backend.controller;

import com.example.backend.dto.publisher.CreatePublisherRequest;
import com.example.backend.dto.publisher.PublisherResponse;
import com.example.backend.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
@Tag(name = "Publishers", description = "APIs for managing publishers")
public class PublisherController {
    
    private final PublisherService publisherService;
    
    /**
     * Tạo mới một publisher
     */
    @PostMapping
    @Operation(summary = "Create a new publisher", description = "Create a new publisher with name and founded year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Publisher created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Publisher name already exists")
    })
    public ResponseEntity<PublisherResponse> createPublisher(@Valid @RequestBody CreatePublisherRequest request) {
        PublisherResponse response = publisherService.createPublisher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lấy tất cả publishers
     */
    @GetMapping
    @Operation(summary = "Get all publishers", description = "Retrieve a list of all publishers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of publishers")
    })
    public ResponseEntity<List<PublisherResponse>> getAllPublishers() {
        List<PublisherResponse> publishers = publisherService.getAllPublishers();
        return ResponseEntity.ok(publishers);
    }
    
    /**
     * Lấy publisher theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get publisher by ID", description = "Retrieve a specific publisher by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publisher found"),
            @ApiResponse(responseCode = "404", description = "Publisher not found")
    })
    public ResponseEntity<PublisherResponse> getPublisherById(
            @Parameter(description = "Publisher ID") @PathVariable Integer id) {
        PublisherResponse publisher = publisherService.getPublisherById(id);
        return ResponseEntity.ok(publisher);
    }
    
    /**
     * Cập nhật publisher
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update publisher", description = "Update an existing publisher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publisher updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Publisher not found")
    })
    public ResponseEntity<PublisherResponse> updatePublisher(
            @Parameter(description = "Publisher ID") @PathVariable Integer id,
            @Valid @RequestBody CreatePublisherRequest request) {
        PublisherResponse updatedPublisher = publisherService.updatePublisher(id, request);
        return ResponseEntity.ok(updatedPublisher);
    }
    
    /**
     * Xóa publisher
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete publisher", description = "Delete a publisher by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publisher deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Publisher not found")
    })
    public ResponseEntity<Void> deletePublisher(
            @Parameter(description = "Publisher ID") @PathVariable Integer id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Tìm kiếm publishers theo tên
     */
    @GetMapping("/search")
    @Operation(summary = "Search publishers by name", description = "Search publishers by name (case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<PublisherResponse>> searchPublishersByName(
            @Parameter(description = "Publisher name to search") @RequestParam String name) {
        List<PublisherResponse> publishers = publisherService.searchPublishersByName(name);
        return ResponseEntity.ok(publishers);
    }
    
    /**
     * Kiểm tra publisher có tồn tại không
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if publisher exists", description = "Check if a publisher exists by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<Boolean> publisherExists(
            @Parameter(description = "Publisher ID") @PathVariable Integer id) {
        boolean exists = publisherService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
