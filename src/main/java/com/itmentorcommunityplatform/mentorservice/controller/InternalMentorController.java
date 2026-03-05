package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PostAddMentorWithDescription;
import com.itmentorcommunityplatform.mentorservice.docs.PostAddPricesForGuaranteedReviews;
import com.itmentorcommunityplatform.mentorservice.domain.type.UpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.ApiMessageResponse;
import com.itmentorcommunityplatform.mentorservice.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class InternalMentorController {

    private final MentorService mentorService;

    @PostMapping("/internal/guaranteed-review")
    @PostAddPricesForGuaranteedReviews
    public ResponseEntity<ApiMessageResponse> addPriceForGuaranteedReview(
            @RequestBody AddPriceForGuaranteedReviewRequest request) {

        UpsertResult upsertResult = mentorService.upsertMentorAndGuaranteedReviewsPrices(request);

        if (upsertResult.equals(UpsertResult.CREATED)) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiMessageResponse("Created successfully"));
        }

        return ResponseEntity
                .ok()
                .body(new ApiMessageResponse("Update successfully"));
    }

    @PostMapping("/internal/mentor")
    @PostAddMentorWithDescription
    public ResponseEntity<ApiMessageResponse> upsertMentorWithDescription(
            @RequestBody @Valid AddMentorWithDescriptionRequest request) {

        UpsertResult upsertResult = mentorService.upsertMentorWithDescription(request);

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