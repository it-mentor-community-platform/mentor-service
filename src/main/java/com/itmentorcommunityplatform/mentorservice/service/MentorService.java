package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.type.UpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorUpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private final TelegramUrlValidator telegramUrlValidator;
    private final ProjectTypeValidator projectTypeValidator;
    private final MentorsRepository mentorsRepository;
    private final ProgrammingLanguagesRepository programmingLanguagesRepository;
    private final ServicesRepository servicesRepository;
    private final ServiceHttpClient httpClient;

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
    public UpsertResult upsertMentorWithDescription(AddMentorWithDescriptionRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());

        MentorUpsertResult mentorUpsertResult = mentorsRepository.upsertMentor(
                request.mentorTelegramUserId(),
                request.telegramUrl());

        boolean inserted = mentorUpsertResult.inserted();

        mentorsRepository.upsertMentorDescription(
                mentorUpsertResult.id(),
                request.description().name(),
                request.description().cost(),
                request.description().description());

        upsertLanguages(mentorUpsertResult.id(), request.programmingLanguages());
        upsertServices(mentorUpsertResult.id(), request.services());
        log.info("Mentor with telegram url: {} has been upserted", request.telegramUrl());

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

    private void upsertLanguages(Long mentorId, List<String> languages) {
        List<Long> languagesIds = languages.stream().map(programmingLanguagesRepository::upsertProgrammingLanguage).toList();
        programmingLanguagesRepository.deleteMentorsProgrammingLanguages(mentorId);
        languagesIds.forEach(languageId -> programmingLanguagesRepository.insertMentorsProgrammingLanguage(mentorId, languageId));
    }

    private void upsertServices(Long mentorId, List<String> services) {
        List<Long> servicesIds = services.stream().map(servicesRepository::upsertServices).toList();
        servicesRepository.deleteMentorsServices(mentorId);
        servicesIds.forEach(serviceId -> servicesRepository.insertMentorsServices(mentorId, serviceId));
    }

    private static String resolveTelegramUrl(UserAuthenticatedEvent event, Mentor mentor) {
        return event.getTelegramUsername() != null && !event.getTelegramUsername().isBlank() ?
                "https://t.me/" + event.getTelegramUsername() : mentor.getTelegramUrl();
    }
}