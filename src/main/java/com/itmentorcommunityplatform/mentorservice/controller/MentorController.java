package com.itmentorcommunityplatform.mentorservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorController {

    private static final String MENTOR_ROLE = "MENTOR";
    private final MentorService mentorService;

    @PostMapping("/internal/mentor")
    @PostAddMentorWithDescription
    public ResponseEntity<MentorResponseDto> createMentorWithDescription(
            @RequestBody @Valid AddMentorWithDescriptionRequest request) {

        MentorResponseDto response = mentorService.createMentorWithDescription(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/mentor/description")
    @PatchUpdateMentorDescription
    public ResponseEntity<MentorDescriptionResponseDto> patchMentorWithDescription(
            @Valid @RequestBody MentorDescriptionRequestDto request,
            @RequestHeader("X-Telegram-User-Id") String telegramId
    ) {
        Long parsedTelegramId = parseTelegramIdFromString(telegramId);

        MentorDescriptionResponseDto response = mentorService.updateMentorDescription(
                parsedTelegramId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/guaranteed-review")
    @PostAddGuaranteedReviewPrice
    public ResponseEntity<GuaranteedReviewsPrices> addGuaranteedReviewPrice(
            @RequestHeader("X-Telegram-User-Id") Long telegramUserId,
            @RequestHeader("X-User-Roles") List<String> roles,
            @RequestBody @Valid AddGuaranteedReviewPriceRequest request) {

        if (!roles.contains(MENTOR_ROLE)) {
            throw new MissingMentorRoleException();
        }

        GuaranteedReviewsPrices savedPrice =
                currentMentorService.addGuaranteedReviewPrice(telegramUserId,request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedPrice);
    }

    private @NonNull Long parseTelegramIdFromString(String telegramId) {
        try {
            return Long.valueOf(telegramId);
        } catch (NumberFormatException e) {
            throw new InvalidTelegramIdException("X-Telegram-User-Id must be valid numeric!");
        }
    }
}