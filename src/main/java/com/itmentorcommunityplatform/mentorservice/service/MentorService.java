package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private final TelegramUrlValidator telegramUrlValidator;
    private final ProjectTypeValidator projectTypeValidator;
    private final MentorsRepository mentorsRepository;

    @Transactional
    public void upsertMentorAndGuaranteedReviewsPrices(Long telegramUserId, AddPriceForGuaranteedReviewRequest request) {

        telegramUrlValidator.validate(request.telegramUrl());
        projectTypeValidator.validate(request.projectType());

        mentorsRepository.upsertMentor(telegramUserId, request.telegramUrl());
        mentorsRepository.updatePriceForGuaranteedReviews(request.priceUsd(), request.projectType(), telegramUserId, request.language());
    }
}