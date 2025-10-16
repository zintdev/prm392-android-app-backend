package com.example.backend.dto.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArtistRequest {
    
    @NotBlank(message = "Artist type is required")
    private String artistType;
    
    @NotBlank(message = "Artist name is required")
    private String artistName;
    
    @NotNull(message = "Debut year is required")
    @Min(value = 1900, message = "Debut year must be at least 1900")
    private Integer debutYear;
}
