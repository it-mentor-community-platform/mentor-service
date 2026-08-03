package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.MentorDescription;
import com.itmentorcommunityplatform.mentorservice.domain.MentorProgrammingLanguage;
import com.itmentorcommunityplatform.mentorservice.domain.type.InsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.*;
import com.itmentorcommunityplatform.mentorservice.domain.type.UpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final MentorMapper mentorMapper;

    @Transactional
    public InsertResult insertGuaranteedReviewPrice(AddPriceForGuaranteedReviewRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());
        projectTypeValidator.validate(request.projectType());

        ProfileWithTelegramIdDto responseDto = httpClient.getProfileByTgUrl(request.telegramUrl())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profile with given Telegram URL not found"));

        Mentor mentor = mentorsRepository.getMentorByMentorTelegramUserId(responseDto.telegramUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mentor not found"
                ));

        boolean inserted = mentorsRepository.insertPriceForGuaranteedReviews(request.priceUsd(),
                request.projectType(),
                mentor.getId(),
                request.language());

        return inserted ? InsertResult.CREATED : InsertResult.ALREADY_EXISTS;
    }

    public List<MentorDto> searchActiveMentorsByLanguageAndProjectType(String language, String projectType){
        return mentorMapper.toMentorDtoList(mentorsRepository.findActiveMentorsByProgrammingLanguageAndProjectType(language, projectType));
    }

    @Transactional
    public UpsertResult upsertMentorWithDescription(AddMentorWithDescriptionRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());

        MentorUpsertResult mentorUpsertResult = mentorsRepository.upsertMentor(
                request.mentorTelegramUserId(),
                request.telegramUrl());

        boolean inserted = mentorUpsertResult.inserted();

        Mentor mentor = mentorsRepository.findById(mentorUpsertResult.id()).orElseThrow();
        mentor.setMentorDescription(buildDescription(request, mentor));
        mentor.setProgrammingLanguages(buildLanguages(request.programmingLanguages()));
        mentor.setServices(buildServices(request.services()));

        mentorsRepository.save(mentor);
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

    private Set<MentorProgrammingLanguage> buildLanguages(List<String> languages) {
        return languages.stream()
                .map(programmingLanguagesRepository::upsertProgrammingLanguage)
                .map(id -> new MentorProgrammingLanguage(AggregateReference.to(id)))
                .collect(Collectors.toSet());
    }

    private Set<com.itmentorcommunityplatform.mentorservice.domain.MentorService> buildServices(List<String> services) {
        return services.stream()
                .map(servicesRepository::upsertService)
                .map(id -> new com.itmentorcommunityplatform.mentorservice.domain.MentorService(AggregateReference.to(id)))
                .collect(Collectors.toSet());
    }

    private MentorDescription buildDescription(AddMentorWithDescriptionRequest request, Mentor mentor) {
        MentorDescription description = mentor.getMentorDescription();

        if (description != null) {
            description.setMentorUserId(mentor.getId());
            description.setName(request.description().name());
            description.setCost(request.description().cost());
            description.setDescription(request.description().description());
        } else {
            description = new MentorDescription();
            description.setName(request.description().name());
            description.setCost(request.description().cost());
            description.setDescription(request.description().description());
        }
        return description;
    }

    private static String resolveTelegramUrl(UserAuthenticatedEvent event, Mentor mentor) {
        return event.getTelegramUsername() != null && !event.getTelegramUsername().isBlank() ?
                "https://t.me/" + event.getTelegramUsername() : mentor.getTelegramUrl();
    }
}