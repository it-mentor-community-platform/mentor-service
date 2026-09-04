package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionDto;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
    private GuaranteedReviewPriceService guaranteedReviewPriceService;

    private MentorService mentorService;

    private AddMentorWithDescriptionRequest request;

    @BeforeEach
    void setUp() {
        TelegramUrlValidator telegramUrlValidator = new TelegramUrlValidator();

        mentorService = new MentorService(
                telegramUrlValidator,
                mentorsRepository,
                programmingLanguagesRepository,
                servicesRepository,
                guaranteedReviewPriceService,
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
    void addGuaranteedReviewPrice_whenMentorExists_shouldSavePrice() {
        AddPriceForGuaranteedReviewRequest request =
                new AddPriceForGuaranteedReviewRequest(
                        "https://t.me/test_mentor",
                        "Java",
                        "PET_PROJECT",
                        10
                );

        ProfileWithTelegramIdDto profile =
                new ProfileWithTelegramIdDto(12345L, null);

        Mentor mentor = Mentor.builder()
                .id(1L)
                .mentorTelegramUserId(12345L)
                .build();

        GuaranteedReviewsPrices expected =
                GuaranteedReviewsPrices.builder()
                        .mentorId(1L)
                        .language("Java")
                        .projectType("PET_PROJECT")
                        .priceUsd(10)
                        .build();

        when(httpClient.getProfileByTgUrl(request.telegramUrl()))
                .thenReturn(Optional.of(profile));

        when(mentorsRepository.getMentorByMentorTelegramUserId(12345L))
                .thenReturn(Optional.of(mentor));

        when(guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        )).thenReturn(expected);

        GuaranteedReviewsPrices result =
                mentorService.addGuaranteedReviewPrice(request);

        assertSame(expected, result);

        verify(guaranteedReviewPriceService).save(
                mentor,
                "Java",
                "PET_PROJECT",
                10
        );
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