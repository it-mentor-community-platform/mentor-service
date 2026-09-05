package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddGuaranteedReviewPriceRequest;
import com.itmentorcommunityplatform.mentorservice.exception.MissingMentorRoleException;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentMentorService {
    private final MentorsRepository mentorsRepository;
    private final GuaranteedReviewPriceService guaranteedReviewPriceService;

    public GuaranteedReviewsPrices addGuaranteedReviewPrice(
            Long telegramUserId,
            AddGuaranteedReviewPriceRequest request
    ) {
        Mentor mentor = mentorsRepository
                .findByMentorTelegramUserId(telegramUserId)
                .orElseThrow(MissingMentorRoleException::new);

        return guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        );
    }
}
