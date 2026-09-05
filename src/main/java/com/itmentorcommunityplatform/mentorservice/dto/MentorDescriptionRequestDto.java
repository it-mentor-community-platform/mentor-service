package com.itmentorcommunityplatform.mentorservice.dto;

import jakarta.validation.constraints.AssertTrue;

public record MentorDescriptionRequestDto(

        String name,

        String cost,

        String description
) {

    @AssertTrue(message = "Request body must not be empty")
    private boolean isNotEmpty() {
        return name != null || cost != null || description != null;
    }


}
