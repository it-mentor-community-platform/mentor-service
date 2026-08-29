package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.*;
import com.itmentorcommunityplatform.mentorservice.dto.*;
import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.exception.*;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.GuaranteedReviewsPriceRepository;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private static final CharSequence UNIQUE_CONSTRAINT_NAME = "idx_mentors_unique";

    private final TelegramUrlValidator telegramUrlValidator;
    private final ProjectTypeValidator projectTypeValidator;
    private final MentorsRepository mentorsRepository;
    private final ProgrammingLanguagesRepository programmingLanguagesRepository;
    private final ServicesRepository servicesRepository;
    private final GuaranteedReviewsPriceRepository guaranteedReviewsPriceRepository;
    private final ServiceHttpClient httpClient;
    private final MentorMapper mentorMapper;
    private final TransactionTemplate transactionTemplate;

    public GuaranteedReviewsPrices insertGuaranteedReviewPrice(AddPriceForGuaranteedReviewRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());
        projectTypeValidator.validate(request.projectType());

        ProfileWithTelegramIdDto responseDto = httpClient.getProfileByTgUrl(request.telegramUrl())
                .orElseThrow(ProfileNotFoundException::new);

        Mentor mentor = mentorsRepository.findByMentorTelegramUserId(responseDto.telegramUserId())
                .orElseThrow(MentorNotFoundException::new);

        GuaranteedReviewsPrices price = GuaranteedReviewsPrices.builder()
                .mentorId(mentor.getId())
                .projectType(request.projectType())
                .language(request.language())
                .priceUsd(request.priceUsd())
                .build();

        try {
            return guaranteedReviewsPriceRepository.save(price);
        } catch (DbActionExecutionException e) {
            if (e.getCause() instanceof DuplicateKeyException) {
                throw new GuaranteedReviewPriceAlreadyExistsException();
            }
            throw e;
        }
    }

    public List<MentorDto> searchActiveMentorsByLanguageAndProjectType(String language, String projectType) {
        return mentorMapper.toMentorDtoList(mentorsRepository.findActiveMentorsByProgrammingLanguageAndProjectType(language, projectType));
    }

    public MentorResponseDto createMentorWithDescription(AddMentorWithDescriptionRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());

        Optional<ProfileWithTelegramIdDto> profileOpt = httpClient.getProfileByTgUrl(request.telegramUrl());

        if (profileOpt.isEmpty()) {
            try {
                httpClient.createProfile(request.mentorTelegramUserId(), request.telegramUrl());
            } catch (Exception e) {
                log.error("Failed to create profile in profile-service for telegramUserId={}. Interrupting flow.",
                        request.mentorTelegramUserId(), e);
                throw e;
            }
        }
        return transactionTemplate.execute(status -> {
            Mentor mentor = Mentor.builder()
                    .mentorTelegramUserId(request.mentorTelegramUserId())
                    .telegramUrl(request.telegramUrl())
                    .isActive(false)
                    .mentorDescription(buildDescription(request.description()))
                    .programmingLanguages(buildLanguages(request.programmingLanguages()))
                    .services(buildServices(request.services()))
                    .build();

            Mentor savedMentor = saveMentorOrThrowIfDuplicate(mentor);
            log.info("Mentor with telegram url: {} created successfully", request.telegramUrl());
            return mentorMapper.toMentorResponseDto(savedMentor, true);
        });
    }


    @Transactional
    public MentorResponseDto updateMentorWithDescription(Long telegramId, MentorDescriptionDto requestDescription) {
        validMentorDescription(requestDescription);

        Mentor mentor = mentorsRepository.findByMentorTelegramUserId(telegramId).orElseThrow(() ->
                new MentorNotFoundException("Mentor not found!")
        );

        MentorDescriptionDto oldDescriptionDto = mentorMapper.mapDescription(
                mentor.getMentorDescription());

        mentor.setMentorDescription(
                updateDescription(requestDescription, oldDescriptionDto));

        Mentor savedMentor = saveMentorOrThrowIfDuplicate(mentor);
        log.info("Mentor with telegram url: {} updated successfully", mentor.getTelegramUrl());
        return mentorMapper.toMentorResponseDto(savedMentor, true);
    }

    @Transactional
    public void updateMentorProfile(UserAuthenticatedEvent event) {
        boolean isActive = event.getRoles().contains("MENTOR");
        Optional<Mentor> possibleMentor = mentorsRepository.findByMentorTelegramUserId(event.getTelegramUserId());
        if (possibleMentor.isEmpty()) {
            log.info("Mentor with telegram id: {} not found", event.getTelegramUserId());
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

    private Mentor saveMentorOrThrowIfDuplicate(Mentor mentor) {
        try {
            return mentorsRepository.save(mentor);
        } catch (DbActionExecutionException e) {
            String rootMessage = getRootMessageFromDbException(e);
            if (rootMessage != null && rootMessage.contains(UNIQUE_CONSTRAINT_NAME)) {
                throw new MentorDuplicateException("Mentor with given telegramUserId or url already exists");
            }
            throw e;
        }
    }

    private Set<MentorProgrammingLanguage> buildLanguages(List<String> languages) {
        return languages.stream()
                .map(name -> programmingLanguagesRepository.findIdByName(name)
                        .orElseGet(() -> programmingLanguagesRepository.save(
                                new ProgrammingLanguage(null, name)).getId()))
                .map(id -> new MentorProgrammingLanguage(AggregateReference.to(id)))
                .collect(Collectors.toSet());
    }

    private Set<com.itmentorcommunityplatform.mentorservice.domain.MentorService> buildServices(List<String> services) {
        return services.stream()
                .map(name -> servicesRepository.findIdByName(name)
                        .orElseGet(() -> servicesRepository.save(
                                new com.itmentorcommunityplatform.mentorservice.domain.Service(null, name)).getId()))
                .map(id -> new com.itmentorcommunityplatform.mentorservice.domain.MentorService(AggregateReference.to(id)))
                .collect(Collectors.toSet());
    }

    private MentorDescription buildDescription(MentorDescriptionDto mentorDescriptionDto) {
        MentorDescription description = new MentorDescription();
        description.setName(mentorDescriptionDto.name());
        description.setCost(mentorDescriptionDto.cost());
        description.setDescription(mentorDescriptionDto.description());
        return description;
    }

    private MentorDescription updateDescription(MentorDescriptionDto mentorDescriptionDtoNew, MentorDescriptionDto mentorDescriptionDtoOld) {
        MentorDescription description = new MentorDescription();

        description.setName(coalesce(
                mentorDescriptionDtoNew.name(), mentorDescriptionDtoOld.name()));
        description.setCost(coalesce(
                mentorDescriptionDtoNew.cost(), mentorDescriptionDtoOld.cost()));
        description.setDescription(coalesce(
                mentorDescriptionDtoNew.description(), mentorDescriptionDtoOld.description()));
        return description;
    }

    private String getRootMessageFromDbException(DbActionExecutionException e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }


    private static String resolveTelegramUrl(UserAuthenticatedEvent event, Mentor mentor) {
        return event.getTelegramUsername() != null && !event.getTelegramUsername().isBlank() ?
                "https://t.me/" + event.getTelegramUsername() : mentor.getTelegramUrl();
    }

    private static void validMentorDescription(MentorDescriptionDto mentorDescriptionDtoNew) {
        if (mentorDescriptionDtoNew.name() == null &&
                mentorDescriptionDtoNew.cost() == null &&
                mentorDescriptionDtoNew.description() == null) {
            throw new MentorDescriptionEmptyException("Mentor description is empty!");
        }
    }

    private static String coalesce(String newValue, String oldValue) {
        return newValue == null ? oldValue : newValue;
    }
}
