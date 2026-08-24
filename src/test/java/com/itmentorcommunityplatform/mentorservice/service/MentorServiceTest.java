package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionDto;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDuplicateException;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.GuaranteedReviewsPriceRepository;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
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
class MentorServiceTest {

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
    @Mock
    private GuaranteedReviewsPriceRepository guaranteedReviewsPriceRepository;

    private MentorService mentorService;

    private AddMentorWithDescriptionRequest request;

    private TelegramUrlValidator telegramUrlValidator;

    private ProjectTypeValidator projectTypeValidator;

    @BeforeEach
    void setUp() {
        telegramUrlValidator = new TelegramUrlValidator();
        projectTypeValidator = new ProjectTypeValidator();

        mentorService = new MentorService(
                telegramUrlValidator,
                projectTypeValidator,
                mentorsRepository,
                programmingLanguagesRepository,
                servicesRepository,
                guaranteedReviewsPriceRepository,
                httpClient,
                mentorMapper,
                transactionTemplate
        );

        MentorDescriptionDto descriptionDto = new MentorDescriptionDto("Peter Parker", "100", "Description");
        request = new AddMentorWithDescriptionRequest(
                12345L,
                "https://t.me/test_mentor",
                descriptionDto,
                List.of("Java"),
                List.of("Code Review")
        );

        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> action = invocation.getArgument(0);
            return action.doInTransaction(mock(TransactionStatus.class));
        });
    }

    @Test
    void createMentorWithDescription_whenProfileNotFound_shouldCreateProfileAndMentor() {
        mockLanguagesAndServices();
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.empty());

        mentorService.createMentorWithDescription(request);

        verify(httpClient).createProfile(request.mentorTelegramUserId(), request.telegramUrl());
        verify(mentorsRepository).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenProfileExists_shouldNotCreateProfileAndSaveMentor() {
        mockLanguagesAndServices();
        ProfileWithTelegramIdDto profileDto = new ProfileWithTelegramIdDto(12345L, null);
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.of(profileDto));

        mentorService.createMentorWithDescription(request);

        verify(httpClient, never()).createProfile(any(), any());
        verify(mentorsRepository).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenHttpClientFails_shouldRethrowException() {
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Profile service error"))
                .when(httpClient).createProfile(request.mentorTelegramUserId(), request.telegramUrl());

        assertThrows(RuntimeException.class, () -> mentorService.createMentorWithDescription(request));

        verify(mentorsRepository, never()).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenDuplicateKeyInDb_shouldThrowConflictException() {
        mockLanguagesAndServices();
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.of(
                new ProfileWithTelegramIdDto(12345L, null)));

        RuntimeException rootCause = new RuntimeException("duplicate key value violates unique constraint idx_mentors_unique");
        DbActionExecutionException dbException = new DbActionExecutionException(null, rootCause);

        when(mentorsRepository.save(any(Mentor.class))).thenThrow(dbException);

        assertThrows(MentorDuplicateException.class, () -> mentorService.createMentorWithDescription(request));
    }

    @Test
    void createMentorWithDescription_whenTelegramUrlInvalid_shouldThrowException() {
        AddMentorWithDescriptionRequest invalidRequest =
                new AddMentorWithDescriptionRequest(
                        12345L,
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
                () -> mentorService.createMentorWithDescription(invalidRequest)
        );

        verifyNoInteractions(httpClient);
        verifyNoInteractions(mentorsRepository);
    }

    private void mockLanguagesAndServices() {
        when(programmingLanguagesRepository.findIdByName("Java")).thenReturn(Optional.of(1L));
        when(servicesRepository.findIdByName("Code Review")).thenReturn(Optional.of(1L));
    }
}