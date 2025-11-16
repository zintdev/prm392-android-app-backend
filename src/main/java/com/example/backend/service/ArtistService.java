package com.example.backend.service;

import com.example.backend.domain.entity.Artist;
import com.example.backend.dto.artist.CreateArtistRequest;
import com.example.backend.dto.artist.ArtistResponse;
import com.example.backend.exception.custom.ArtistAlreadyExistsException;
import com.example.backend.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {
    
    private final ArtistRepository artistRepository;
    
    // CREATE
    @Transactional
    public ArtistResponse createArtist(CreateArtistRequest request) {
        // Kiểm tra xem artist name đã tồn tại chưa
        if (artistRepository.findByArtistNameContainingIgnoreCase(request.getArtistName()).stream()
                .anyMatch(artist -> artist.getArtistName().equalsIgnoreCase(request.getArtistName()))) {
            throw new ArtistAlreadyExistsException(request.getArtistName());
        }


        Artist artist = Artist.builder()
                .artistType(request.getArtistType())
                .artistName(request.getArtistName())
                .debutYear(request.getDebutYear())
                .build();
        
        // No images handling for artist

        try {
            Artist savedArtist = artistRepository.save(artist);
            return mapToResponse(savedArtist);
        }
        catch (DataIntegrityViolationException e) {
            throw new ArtistAlreadyExistsException(request.getArtistName(), e);
        }

    }
    
    // READ - Get by ID
    public ArtistResponse getArtistById(Integer id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + id));
        return mapToResponse(artist);
    }
    
    // READ - Get all without pagination
    public List<ArtistResponse> getAllArtists() {
        List<Artist> artists = artistRepository.findAll();
        return artists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // READ - Search by name
    public List<ArtistResponse> searchArtistsByName(String name) {
        List<Artist> artists = artistRepository.findByArtistNameContainingIgnoreCase(name);
        return artists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // READ - Get by artist type
    public List<ArtistResponse> getArtistsByType(String artistType) {
        List<Artist> artists = artistRepository.findByArtistType(artistType);
        return artists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // UPDATE
    @Transactional
    public ArtistResponse updateArtist(Integer id, CreateArtistRequest request) {
        // Kiểm tra xem artist name đã tồn tại chưa (trừ chính artist hiện tại)
        if (artistRepository.findByArtistNameContainingIgnoreCase(request.getArtistName()).stream()
                .anyMatch(artist -> artist.getArtistName().equalsIgnoreCase(request.getArtistName()) 
                        && !artist.getId().equals(id))) {
            throw new ArtistAlreadyExistsException(request.getArtistName());
        }

        Artist artist = Artist.builder()
                .id(id) // Giữ nguyên ID
                .artistType(request.getArtistType())
                .artistName(request.getArtistName())
                .debutYear(request.getDebutYear())
                .build();

        // No images handling for artist

        try {
            Artist savedArtist = artistRepository.save(artist);
            return mapToResponse(savedArtist);
        } catch (DataIntegrityViolationException e) {
            throw new ArtistAlreadyExistsException(request.getArtistName(), e);
        }
    }
    
    // DELETE
    @Transactional
    public void deleteArtist(Integer id) {
        if (!artistRepository.existsById(id)) {
            throw new RuntimeException("Artist not found with id: " + id);
        }
        artistRepository.deleteById(id);
    }
    
    // Helper method to map Entity to Response DTO
    private ArtistResponse mapToResponse(Artist artist) {
        return ArtistResponse.builder()
                .id(artist.getId())
                .artistType(artist.getArtistType())
                .artistName(artist.getArtistName())
                .debutYear(artist.getDebutYear())
                .build();
    }
    
    // Helper method to check if artist exists
    public boolean existsById(Integer id) {
        return artistRepository.existsById(id);
    }
}
