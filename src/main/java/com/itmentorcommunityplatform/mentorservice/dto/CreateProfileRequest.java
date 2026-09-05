package com.itmentorcommunityplatform.mentorservice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateProfileRequest(
        Long telegramUserId,
        ProfileDetails details
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ProfileDetails(
            String telegramUrl
    ) {
    }
}