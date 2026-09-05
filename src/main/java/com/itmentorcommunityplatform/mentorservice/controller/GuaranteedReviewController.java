package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PostAddPricesForGuaranteedReviews;
import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.service.GuaranteedReviewService;
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
public class GuaranteedReviewController {

    private final GuaranteedReviewService guaranteedReviewService;

    @PostMapping("/internal/guaranteed-review")
    @PostAddPricesForGuaranteedReviews
    public ResponseEntity<GuaranteedReviewsPrices> addPriceForGuaranteedReview(
            @RequestBody AddPriceForGuaranteedReviewRequest request) {

        GuaranteedReviewsPrices savedPrice = guaranteedReviewService.insertGuaranteedReviewPrice(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedPrice);
    }
}
