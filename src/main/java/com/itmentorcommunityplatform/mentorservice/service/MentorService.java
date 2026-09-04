package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.MentorDescription;
import com.itmentorcommunityplatform.mentorservice.domain.MentorProgrammingLanguage;
import com.itmentorcommunityplatform.mentorservice.domain.ProgrammingLanguage;
import com.itmentorcommunityplatform.mentorservice.dto.*;
import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.exception.*;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.GuaranteedReviewsPriceRepository;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.MentorProgrammingLanguageValidator;
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

    private final TelegramUrlValidator telegramUrlValidator;
    private final ProjectTypeValidator projectTypeValidator;
    private final MentorProgrammingLanguageValidator mentorProgrammingLanguageValidator;
    private final MentorsRepository mentorsRepository;
    private final ProgrammingLanguagesRepository programmingLanguagesRepository;
    private final ServicesRepository servicesRepository;
    private final GuaranteedReviewsPriceRepository guaranteedReviewsPriceRepository;
    private final ServiceHttpClient httpClient;
    private final MentorMapper mentorMapper;
    private final TransactionTemplate transactionTemplate;

    public GuaranteedReviewsPrices insertGuaranteedReviewPrice(
            AddPriceForGuaranteedReviewRequest request
    ) {
        telegramUrlValidator.validate(request.telegramUrl());

        ProfileWithTelegramIdDto profile = httpClient
                .getProfileByTgUrl(request.telegramUrl())
                .orElseThrow(ProfileNotFoundException::new);

        Mentor mentor = mentorsRepository
                .getMentorByMentorTelegramUserId(profile.telegramUserId())
                .orElseThrow(MentorNotFoundException::new);

        return saveGuaranteedReviewPrice(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        );
    }

    public GuaranteedReviewsPrices addGuaranteedReviewPriceForCurrentMentor(
            Long telegramUserId,
            AddGuaranteedReviewPriceRequest request
    ) {
        Mentor mentor = mentorsRepository
                .getMentorByMentorTelegramUserId(telegramUserId)
                .orElseThrow(UserIsNotMentorException::new);

        return saveGuaranteedReviewPrice(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        );
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
                    .mentorDescription(buildDescription(request))
                    .programmingLanguages(buildLanguages(request.programmingLanguages()))
                    .services(buildServices(request.services()))
                    .build();

            try {
                Mentor savedMentor = mentorsRepository.save(mentor);
                log.info("Mentor with telegram url: {} created successfully", request.telegramUrl());
                return mentorMapper.toMentorResponseDto(savedMentor, true);
            } catch (DbActionExecutionException e) {
                Throwable cause = e;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                String rootMessage = cause.getMessage();

                if (rootMessage != null && rootMessage.contains("idx_mentors_unique")) {
                    throw new MentorDuplicateException("Mentor with given telegramUserId or url already exists");
                }
                throw e;
            }
        });
    }

    @Transactional
    public void updateMentorProfile(UserAuthenticatedEvent event) {
        boolean isActive = event.getRoles().contains("MENTOR");
        Optional<Mentor> possibleMentor = mentorsRepository.getMentorByMentorTelegramUserId(event.getTelegramUserId());
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

    private MentorDescription buildDescription(AddMentorWithDescriptionRequest request) {
        MentorDescription description = new MentorDescription();
        description.setName(request.description().name());
        description.setCost(request.description().cost());
        description.setDescription(request.description().description());
        return description;
    }

    private static String resolveTelegramUrl(UserAuthenticatedEvent event, Mentor mentor) {
        return event.getTelegramUsername() != null && !event.getTelegramUsername().isBlank() ?
                "https://t.me/" + event.getTelegramUsername() : mentor.getTelegramUrl();
    }
    private GuaranteedReviewsPrices saveGuaranteedReviewPrice(
            Mentor mentor,
            String language,
            String projectType,
            Integer priceUsd
    ) {
        projectTypeValidator.validate(projectType);
        mentorProgrammingLanguageValidator.validate(mentor, language);

        GuaranteedReviewsPrices price = GuaranteedReviewsPrices.builder()
                .mentorId(mentor.getId())
                .projectType(projectType)
                .language(language)
                .priceUsd(priceUsd)
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
}
