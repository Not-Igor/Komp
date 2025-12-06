package com.egor.back_end.dto.competition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompetitionCreateDto(
        @NotBlank(message = "Title is required")
        @Size(max = 50, message = "Title must not exceed 50 characters")
        String title,
        
        @NotBlank(message = "Icon is required")
        String icon,
        
        @NotNull(message = "Participant IDs are required")
        @Size(min = 1, message = "At least one participant is required")
        List<Long> participantIds
) {}
