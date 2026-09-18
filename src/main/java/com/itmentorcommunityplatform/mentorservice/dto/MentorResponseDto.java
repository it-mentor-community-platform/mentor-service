package com.itmentorcommunityplatform.mentorservice.dto;

import java.util.List;

public record MentorResponseDto(
        Long id,
        Long mentorTelegramUserId,
        String telegramUrl,
        MentorDescriptionResponseDto description,
        List<String> programmingLanguages,
        List<String> services
) {
}
