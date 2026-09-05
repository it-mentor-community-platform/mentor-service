package com.itmentorcommunityplatform.mentorservice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddGuaranteedReviewPriceRequest(
        @NotBlank(message = "Language must not be blank")
        String language,

        @NotBlank(message = "Project type must not be blank")
        String projectType,

        @NotNull(message = "Price must not be null")
        @Positive(message = "Price must be greater than zero")
        Integer priceUsd
) {
}
