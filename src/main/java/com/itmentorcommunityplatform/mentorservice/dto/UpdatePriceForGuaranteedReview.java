package com.itmentorcommunityplatform.mentorservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdatePriceForGuaranteedReview(
        @JsonProperty("priceUsd")
        @NotNull(message = "Price must not be null")
        @Positive(message = "Price must be greater than zero")
        Integer priceUsd
) {
}
