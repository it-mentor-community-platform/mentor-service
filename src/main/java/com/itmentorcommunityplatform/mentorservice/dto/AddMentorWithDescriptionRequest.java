package com.itmentorcommunityplatform.mentorservice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record AddMentorWithDescriptionRequest(
        @NotNull(message = "Mentor telegram user id is required")
        Long mentorTelegramUserId,

        @NotBlank(message = "Telegram url id is required")
        String telegramUrl,

        @NotNull(message = "Description is required")
        @Valid
        MentorDescriptionDto description,

        @NotEmpty(message = "Programming languages are required")
        List<String> programmingLanguages,

        @NotEmpty(message = "Services are required")
        List<String> services
) {
}
