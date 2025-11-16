package com.example.backend.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffRequest {

    @NotNull
    @Min(1)
    private Integer staffId;

    @NotNull
    @Min(1)
    private Integer actorUserId;
}
