package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PostAddGuaranteedReviewPrice;
import com.itmentorcommunityplatform.mentorservice.docs.PostAddPricesForGuaranteedReviews;
import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.dto.AddGuaranteedReviewPriceRequest;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.exception.MissingMentorRoleException;
import com.itmentorcommunityplatform.mentorservice.service.CurrentMentorService;
import com.itmentorcommunityplatform.mentorservice.service.GuaranteedReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class GuaranteedReviewController {

    private static final String MENTOR_ROLE = "MENTOR";

    private final GuaranteedReviewService guaranteedReviewService;
    private final CurrentMentorService currentMentorService;

    @PostMapping("/internal/guaranteed-review")
    @PostAddPricesForGuaranteedReviews
    public ResponseEntity<GuaranteedReviewsPrices> addPriceForGuaranteedReview(
            @RequestBody AddPriceForGuaranteedReviewRequest request) {

        GuaranteedReviewsPrices savedPrice = guaranteedReviewService.insertGuaranteedReviewPrice(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedPrice);
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
}
