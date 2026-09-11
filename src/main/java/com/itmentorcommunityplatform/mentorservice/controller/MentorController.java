package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PatchUpdateMentorDescription;
import com.itmentorcommunityplatform.mentorservice.dto.*;
import com.itmentorcommunityplatform.mentorservice.exception.InvalidTelegramIdException;
import com.itmentorcommunityplatform.mentorservice.service.CurrentMentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorController {

    private final CurrentMentorService currentMentorService;

    @PatchMapping("/mentor/description")
    @PatchUpdateMentorDescription
    public ResponseEntity<MentorDescriptionResponseDto> patchMentorWithDescription(
            @Valid @RequestBody MentorDescriptionRequestDto request,
            @RequestHeader("X-Telegram-User-Id") String telegramId
    ) {
        Long parsedTelegramId = parseTelegramIdFromString(telegramId);

        MentorDescriptionResponseDto response = currentMentorService.updateMentorDescription(
                parsedTelegramId, request);

        return ResponseEntity.ok(response);
    }

    private @NonNull Long parseTelegramIdFromString(String telegramId) {
        try {
            return Long.valueOf(telegramId);
        } catch (NumberFormatException e) {
            throw new InvalidTelegramIdException("X-Telegram-User-Id must be valid numeric!");
        }
    }
}