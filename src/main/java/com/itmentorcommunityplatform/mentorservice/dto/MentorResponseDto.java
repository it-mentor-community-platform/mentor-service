package com.itmentorcommunityplatform.mentorservice.dto;

public record MentorResponseDto(
        Long id,
        Long mentorTelegramUserId,
        String telegramUrl,
        boolean isActive,
        boolean inserted
) {
}
