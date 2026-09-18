package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.*;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionDto;
import com.itmentorcommunityplatform.mentorservice.dto.MentorResponseDto;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDuplicateException;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalMentorService {

    private static final String TELEGRAM_USER_ID_UNIQUE_INDEX = "idx_mentors_unique";
    private static final String TELEGRAM_URL_UNIQUE_INDEX = "idx_mentors_telegram_url_unique";

    private final TelegramUrlValidator telegramUrlValidator;
    private final MentorsRepository mentorsRepository;
    private final ProgrammingLanguagesRepository programmingLanguagesRepository;
    private final ServicesRepository servicesRepository;
    private final ServiceHttpClient httpClient;
    private final MentorMapper mentorMapper;
    private final TransactionTemplate transactionTemplate;

    public MentorResponseDto createMentorWithDescription(AddMentorWithDescriptionRequest request) {
        telegramUrlValidator.validate(request.telegramUrl());

        Optional<ProfileWithTelegramIdDto> profile =
                httpClient.getProfileByTgUrl(request.telegramUrl());

        if (profile.isEmpty()) {
            try {
                httpClient.createProfile(
                        request.mentorTelegramUserId(),
                        request.telegramUrl()
                );
            } catch (Exception e) {
                log.error(
                        "Failed to create profile in profile-service for telegramUserId={}. Interrupting flow.",
                        request.mentorTelegramUserId(),
                        e
                );
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

            log.info(
                    "Mentor with telegram url: {} created successfully",
                    request.telegramUrl()
            );

            return mentorMapper.toMentorResponseDto(savedMentor, request);
        });
    }

    private Mentor saveMentorOrThrowIfDuplicate(Mentor mentor) {
        try {
            return mentorsRepository.save(mentor);
        } catch (DbActionExecutionException e) {
            String rootMessage = getRootMessageFromDbException(e);

            if (rootMessage != null
                    && rootMessage.contains(TELEGRAM_USER_ID_UNIQUE_INDEX)) {
                throw new MentorDuplicateException(
                        "Mentor with given telegramUserId already exists"
                );
            }

            if (rootMessage != null
                    && rootMessage.contains(TELEGRAM_URL_UNIQUE_INDEX)) {
                throw new MentorDuplicateException(
                        "Mentor with given telegram url already exists"
                );
            }

            throw e;
        }
    }

    private Set<MentorProgrammingLanguage> buildLanguages(List<String> languages) {
        return languages.stream()
                .map(name -> programmingLanguagesRepository.findIdByName(name)
                        .orElseGet(() -> programmingLanguagesRepository.save(
                                new ProgrammingLanguage(null, name)
                        ).getId()))
                .map(id -> new MentorProgrammingLanguage(AggregateReference.to(id)))
                .collect(Collectors.toSet());
    }

    private Set<com.itmentorcommunityplatform.mentorservice.domain.MentorService> buildServices(
            List<String> services
    ) {
        return services.stream()
                .map(name -> servicesRepository.findIdByName(name)
                        .orElseGet(() -> servicesRepository.save(
                                new com.itmentorcommunityplatform.mentorservice.domain.Service(
                                        null,
                                        name
                                )
                        ).getId()))
                .map(id -> new MentorService(
                        AggregateReference.to(id)
                ))
                .collect(Collectors.toSet());
    }

    private MentorDescription buildDescription(MentorDescriptionDto descriptionDto) {
        MentorDescription description = new MentorDescription();
        description.setName(descriptionDto.name());
        description.setCost(descriptionDto.cost());
        description.setDescription(descriptionDto.description());

        return description;
    }

    private String getRootMessageFromDbException(DbActionExecutionException e) {
        Throwable cause = e;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage();
    }
}