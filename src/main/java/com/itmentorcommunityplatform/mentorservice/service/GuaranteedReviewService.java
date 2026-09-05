package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.exception.GuaranteedReviewPriceAlreadyExistsException;
import com.itmentorcommunityplatform.mentorservice.exception.MentorNotFoundException;
import com.itmentorcommunityplatform.mentorservice.exception.ProfileNotFoundException;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.repository.GuaranteedReviewsPriceRepository;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuaranteedReviewService {

    private final TelegramUrlValidator telegramUrlValidator;
    private final MentorsRepository mentorsRepository;
    private final ServiceHttpClient httpClient;
    private final GuaranteedReviewPriceService guaranteedReviewPriceService;

    public GuaranteedReviewsPrices insertGuaranteedReviewPrice(
            AddPriceForGuaranteedReviewRequest request
    ) {
        telegramUrlValidator.validate(request.telegramUrl());

        ProfileWithTelegramIdDto profile = httpClient
                .getProfileByTgUrl(request.telegramUrl())
                .orElseThrow(ProfileNotFoundException::new);

        Mentor mentor = mentorsRepository
                .findByMentorTelegramUserId(profile.telegramUserId())
                .orElseThrow(MentorNotFoundException::new);

        return guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        );
    }
}
