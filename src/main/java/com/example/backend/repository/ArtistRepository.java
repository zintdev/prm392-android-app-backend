package com.example.backend.repository;

import com.example.backend.domain.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Integer> {
    
    // Find artists by name containing (case insensitive)
    List<Artist> findByArtistNameContainingIgnoreCase(String name);
    
    // Find artists by type
    List<Artist> findByArtistType(String artistType);
    
    // Find artists by debut year range
    List<Artist> findByDebutYearBetween(Integer startYear, Integer endYear);
    
    // Custom query to find artists with specific criteria
    @Query("SELECT a FROM Artist a WHERE a.artistName LIKE %:name% AND a.artistType = :type")
    List<Artist> findByNameAndType(@Param("name") String name, @Param("type") String type);
}
