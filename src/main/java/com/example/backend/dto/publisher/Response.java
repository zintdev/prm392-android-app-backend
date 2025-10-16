package com.example.backend.dto.publisher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PublisherResponse", description = "Publisher response body")
public class Response {
    
    private Integer id;
    private String name;
    private Integer foundedYear;
}
