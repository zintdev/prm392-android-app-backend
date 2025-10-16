package com.example.backend.dto.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponse {
    private Integer id;
    private String artistType;
    private String artistName;
    private Integer debutYear;
}
