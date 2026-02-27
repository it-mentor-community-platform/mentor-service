package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.type.UpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private final TelegramUrlValidator telegramUrlValidator;
    private final ProjectTypeValidator projectTypeValidator;
    private final MentorsRepository mentorsRepository;
    private final ServiceHttpClient httpClient;

    private static String resolveTelegramUrl(UserAuthenticatedEvent event, Mentor mentor) {
        return event.getTelegramUsername() != null && !event.getTelegramUsername().isBlank() ?
                "https://t.me/" + event.getTelegramUsername() : mentor.getTelegramUrl();
    }

    @Transactional
    public UpsertResult upsertMentorAndGuaranteedReviewsPrices(AddPriceForGuaranteedReviewRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());
        projectTypeValidator.validate(request.projectType());

        ProfileWithTelegramIdDto responseDto = httpClient.getProfileByTgUrl(request.telegramUrl())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profile with given Telegram URL not found"));

        mentorsRepository.upsertMentor(responseDto.telegramUserId(), request.telegramUrl());
        boolean inserted = mentorsRepository.updatePriceForGuaranteedReviews(request.priceUsd(),
                request.projectType(),
                responseDto.telegramUserId(),
                request.language());

        return inserted ? UpsertResult.CREATED : UpsertResult.UPDATED;
    }

    @Transactional
    public void updateMentorProfile(UserAuthenticatedEvent event) {
        boolean isActive = event.getRoles().contains("MENTOR");
        Optional<Mentor> possibleMentor = mentorsRepository.getMentorByMentorTelegramUserId(event.getTelegramUserId());
        if (possibleMentor.isEmpty()) {
            log.info("Mentor with with telegram id: {} not found", event.getTelegramUserId());
            return;
        }

        Mentor mentor = possibleMentor.get();
        String telegramUrl = resolveTelegramUrl(event, mentor);
        if (mentor.getTelegramUrl().equals(telegramUrl) && mentor.isActive() == isActive) {
            log.info("Nothing to update for mentor with telegram id: {}", mentor.getMentorTelegramUserId());
            return;
        }

        Mentor updated = mentorsRepository.updateMentor(event.getTelegramUserId(), telegramUrl, isActive);
        log.info("Mentor with telegram id: {} has been updated", updated.getMentorTelegramUserId());
    }
}