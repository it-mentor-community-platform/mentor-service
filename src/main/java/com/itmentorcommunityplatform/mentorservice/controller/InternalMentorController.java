package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PostAddPricesForGuranteedReviews;
import com.itmentorcommunityplatform.mentorservice.domain.type.UpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.ApiMessageResponse;
import com.itmentorcommunityplatform.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class InternalMentorController {

    private final MentorService mentorService;

    @PostMapping("/internal/guaranteed-review")
    @PostAddPricesForGuranteedReviews
    public ResponseEntity<ApiMessageResponse> addPriceForGuaranteedReview(
            @RequestHeader("X-Telegram-User-Id") Long telegramUserId,
            @RequestBody AddPriceForGuaranteedReviewRequest request) {

        UpsertResult upsertResult = mentorService.upsertMentorAndGuaranteedReviewsPrices(telegramUserId, request);

        if (upsertResult.equals(UpsertResult.CREATED)) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiMessageResponse("Created successfully"));
        }

        return ResponseEntity
                .ok()
                .body(new ApiMessageResponse("Update successfully"));
    }

}
