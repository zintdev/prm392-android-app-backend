package com.example.backend.controller;

import com.example.backend.dto.artist.Request;
import com.example.backend.dto.artist.Response;
import com.example.backend.service.ArtistService;
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
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "APIs for managing artists")
public class ArtistController {
    
    private final ArtistService artistService;
    
    /**
     * Tạo mới một artist
     */
    @PostMapping
    @Operation(summary = "Create a new artist", description = "Create a new artist with name, type and debut year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artist created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Artist name already exists")
    })
    public ResponseEntity<Response> createArtist(@Valid @RequestBody Request request) {
        Response response = artistService.createArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lấy tất cả artists
     */
    @GetMapping
    @Operation(summary = "Get all artists", description = "Retrieve a list of all artists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of artists")
    })
    public ResponseEntity<List<Response>> getAllArtists() {
        List<Response> artists = artistService.getAllArtists();
        return ResponseEntity.ok(artists);
    }
    
    /**
     * Lấy artist theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get artist by ID", description = "Retrieve a specific artist by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artist found"),
            @ApiResponse(responseCode = "404", description = "Artist not found")
    })
    public ResponseEntity<Response> getArtistById(
            @Parameter(description = "Artist ID") @PathVariable Integer id) {
        Response artist = artistService.getArtistById(id);
        return ResponseEntity.ok(artist);
    }
    
    /**
     * Cập nhật artist
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update artist", description = "Update an existing artist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artist updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Artist not found")
    })
    public ResponseEntity<Response> updateArtist(
            @Parameter(description = "Artist ID") @PathVariable Integer id,
            @Valid @RequestBody Request request) {
        Response updatedArtist = artistService.updateArtist(id, request);
        return ResponseEntity.ok(updatedArtist);
    }
    
    /**
     * Xóa artist
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete artist", description = "Delete an artist by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artist deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Artist not found")
    })
    public ResponseEntity<Void> deleteArtist(
            @Parameter(description = "Artist ID") @PathVariable Integer id) {
        artistService.deleteArtist(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Tìm kiếm artists theo tên
     */
    @GetMapping("/search")
    @Operation(summary = "Search artists by name", description = "Search artists by name (case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<Response>> searchArtistsByName(
            @Parameter(description = "Artist name to search") @RequestParam String name) {
        List<Response> artists = artistService.searchArtistsByName(name);
        return ResponseEntity.ok(artists);
    }
    
    /**
     * Lấy artists theo loại
     */
    @GetMapping("/type/{artistType}")
    @Operation(summary = "Get artists by type", description = "Retrieve artists by their type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artists found by type")
    })
    public ResponseEntity<List<Response>> getArtistsByType(
            @Parameter(description = "Artist type") @PathVariable String artistType) {
        List<Response> artists = artistService.getArtistsByType(artistType);
        return ResponseEntity.ok(artists);
    }
    
    /**
     * Kiểm tra artist có tồn tại không
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if artist exists", description = "Check if an artist exists by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<Boolean> artistExists(
            @Parameter(description = "Artist ID") @PathVariable Integer id) {
        boolean exists = artistService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
