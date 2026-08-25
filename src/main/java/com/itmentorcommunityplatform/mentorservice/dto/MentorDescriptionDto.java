package com.itmentorcommunityplatform.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;

public record MentorDescriptionDto(
        @NotBlank(message = "Description name is required")
        String name,

        @NotBlank(message = "Description cost is required")
        String cost,

        String description
) {
}
