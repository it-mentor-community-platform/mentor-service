package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PostAddGuaranteedReviewPrice;
import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.dto.AddGuaranteedReviewPriceRequest;
import com.itmentorcommunityplatform.mentorservice.exception.UserIsNotMentorException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itmentorcommunityplatform.mentorservice.service.MentorService;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorController {

    private static final String MENTOR_ROLE = "MENTOR";


    private final MentorService mentorService;

    @PostMapping("/guaranteed-review")
    @PostAddGuaranteedReviewPrice
    public ResponseEntity<GuaranteedReviewsPrices> addGuaranteedReviewPrice(
            @RequestHeader("X-Telegram-User-Id") Long telegramUserId,
            @RequestHeader("X-User-Roles") List<String> roles,
            @RequestBody @Valid AddGuaranteedReviewPriceRequest request) {

        if (!roles.contains(MENTOR_ROLE)) {
            throw new UserIsNotMentorException();
        }

        GuaranteedReviewsPrices savedPrice =
                mentorService.addGuaranteedReviewPriceForCurrentMentor(telegramUserId,request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedPrice);
    }
}