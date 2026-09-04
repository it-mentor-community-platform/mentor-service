package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PatchUpdateMentorDescription;
import com.itmentorcommunityplatform.mentorservice.docs.PostAddMentorWithDescription;
import com.itmentorcommunityplatform.mentorservice.docs.PostAddPricesForGuaranteedReviews;
import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionDto;
import com.itmentorcommunityplatform.mentorservice.dto.MentorResponseDto;
import com.itmentorcommunityplatform.mentorservice.exception.InvalidTelegramIdException;
import com.itmentorcommunityplatform.mentorservice.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class InternalMentorController {

    private final MentorService mentorService;

    @PostMapping("/internal/guaranteed-review")
    @PostAddPricesForGuaranteedReviews
    public ResponseEntity<GuaranteedReviewsPrices> addPriceForGuaranteedReview(
            @RequestBody AddPriceForGuaranteedReviewRequest request) {

        GuaranteedReviewsPrices savedPrice = mentorService.insertGuaranteedReviewPrice(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedPrice);
    }

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
    public ResponseEntity<MentorResponseDto> patchMentorWithDescription(
            @RequestBody MentorDescriptionDto request,
            @RequestHeader("X-Telegram-User-Id") String telegramId
    ) {
        Long parsedTelegramId = parseTelegramIdFromString(telegramId);

        MentorResponseDto response = mentorService.updateMentorWithDescription(
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