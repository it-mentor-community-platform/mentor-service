package com.itmentorcommunityplatform.mentorservice.dto;

public record MentorUpsertResult(
        Long id,
        Long mentorTelegramUserId,
        String telegramUrl,
        boolean isActive,
        boolean inserted
) {
}
