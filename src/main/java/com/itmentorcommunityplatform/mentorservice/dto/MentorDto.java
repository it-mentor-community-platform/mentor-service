package com.itmentorcommunityplatform.mentorservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public record MentorDto(
        Long mentorTelegramUserId,
        String mentorTelegramProfileUrl
) {
}
