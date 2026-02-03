package com.itmentorcommunityplatform.mentorservice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddPriceForGuaranteedReviewRequest(
        String telegramUrl,
        String language,
        String projectType,
        Integer priceUsd
) {
}
