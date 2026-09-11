package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.*;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDuplicateException;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalMentorServiceTest {

    public static final long TELEGRAM_MENTOR_ID = 12345L;

    @Mock
    private MentorsRepository mentorsRepository;
    @Mock
    private ProgrammingLanguagesRepository programmingLanguagesRepository;
    @Mock
    private ServicesRepository servicesRepository;
    @Mock
    private ServiceHttpClient httpClient;
    @Mock
    private MentorMapper mentorMapper;
    @Mock
    private TransactionTemplate transactionTemplate;

    private InternalMentorService internalMentorService;

    private AddMentorWithDescriptionRequest mentorRequest;

    private MentorDescriptionRequestDto descriptionRequest;

    @BeforeEach
    void setUp() {
        TelegramUrlValidator telegramUrlValidator = new TelegramUrlValidator();

        internalMentorService = new InternalMentorService(
                telegramUrlValidator,
                mentorsRepository,
                programmingLanguagesRepository,
                servicesRepository,
                httpClient,
                mentorMapper,
                transactionTemplate
        );

        MentorDescriptionDto descriptionDto =
                new MentorDescriptionDto(
                        "Peter Parker",
                        "100",
                        "Description"
                );

        mentorRequest = new AddMentorWithDescriptionRequest(
                TELEGRAM_MENTOR_ID,
                "https://t.me/test_mentor",
                descriptionDto,
                List.of("Java"),
                List.of("Code Review")
        );

        lenient().when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> action = invocation.getArgument(0);
                    return action.doInTransaction(mock(TransactionStatus.class));
                });
    }

    @Test
    void createMentorWithDescription_whenProfileNotFound_shouldCreateProfileAndMentor() {
        mockLanguagesAndServices();
        when(httpClient.getProfileByTgUrl(mentorRequest.telegramUrl())).thenReturn(Optional.empty());

        internalMentorService.createMentorWithDescription(mentorRequest);

        verify(httpClient).createProfile(mentorRequest.mentorTelegramUserId(), mentorRequest.telegramUrl());
        verify(mentorsRepository).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenProfileExists_shouldNotCreateProfileAndSaveMentor() {
        mockLanguagesAndServices();
        ProfileWithTelegramIdDto profileDto = new ProfileWithTelegramIdDto(TELEGRAM_MENTOR_ID, null);
        when(httpClient.getProfileByTgUrl(mentorRequest.telegramUrl())).thenReturn(Optional.of(profileDto));

        internalMentorService.createMentorWithDescription(mentorRequest);

        verify(httpClient, never()).createProfile(any(), any());
        verify(mentorsRepository).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenHttpClientFails_shouldRethrowException() {
        when(httpClient.getProfileByTgUrl(mentorRequest.telegramUrl())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Profile service error"))
                .when(httpClient).createProfile(mentorRequest.mentorTelegramUserId(), mentorRequest.telegramUrl());

        assertThrows(RuntimeException.class, () -> internalMentorService.createMentorWithDescription(mentorRequest));

        verify(mentorsRepository, never()).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenDuplicateKeyInDb_shouldThrowConflictException() {
        mockLanguagesAndServices();
        when(httpClient.getProfileByTgUrl(mentorRequest.telegramUrl())).thenReturn(Optional.of(
                new ProfileWithTelegramIdDto(TELEGRAM_MENTOR_ID, null)));

        RuntimeException rootCause = new RuntimeException("duplicate key value violates unique constraint idx_mentors_unique");
        DbActionExecutionException dbException = new DbActionExecutionException(null, rootCause);

        when(mentorsRepository.save(any(Mentor.class))).thenThrow(dbException);

        assertThrows(MentorDuplicateException.class, () -> internalMentorService.createMentorWithDescription(mentorRequest));
    }

    @Test
    void createMentorWithDescription_whenTelegramUrlInvalid_shouldThrowException() {
        AddMentorWithDescriptionRequest invalidRequest =
                new AddMentorWithDescriptionRequest(
                        TELEGRAM_MENTOR_ID,
                        "https://t.me/@test_mentor",
                        new MentorDescriptionDto(
                                "Peter Parker",
                                "100",
                                "Description"
                        ),
                        List.of("Java"),
                        List.of("Code Review")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> internalMentorService.createMentorWithDescription(invalidRequest)
        );

        verifyNoInteractions(httpClient);
        verifyNoInteractions(mentorsRepository);
    }

    private void mockLanguagesAndServices() {
        when(programmingLanguagesRepository.findIdByName("Java")).thenReturn(Optional.of(1L));
        when(servicesRepository.findIdByName("Code Review")).thenReturn(Optional.of(1L));
    }
}