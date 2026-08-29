package com.itmentorcommunityplatform.mentorservice.dto;

public record MentorResponseDto(
        Long id,
        Long mentorTelegramUserId,
        String telegramUrl,
        MentorDescriptionResponseDto description,
        boolean isActive,
        boolean inserted
) {
}
