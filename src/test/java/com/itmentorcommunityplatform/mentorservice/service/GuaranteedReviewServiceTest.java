package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.exception.MentorNotFoundException;
import com.itmentorcommunityplatform.mentorservice.exception.ProfileNotFoundException;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuaranteedReviewServiceTest {

    @Mock
    private TelegramUrlValidator telegramUrlValidator;

    @Mock
    private MentorsRepository mentorsRepository;

    @Mock
    private ServiceHttpClient httpClient;

    @Mock
    private GuaranteedReviewPriceService guaranteedReviewPriceService;

    private GuaranteedReviewService guaranteedReviewService;

    @BeforeEach
    void setUp() {
        guaranteedReviewService = new GuaranteedReviewService(
                telegramUrlValidator,
                mentorsRepository,
                httpClient,
                guaranteedReviewPriceService
        );
    }

    @Test
    void insertGuaranteedReviewPrice_whenProfileAndMentorExist_shouldSavePrice() {
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

        when(mentorsRepository.findByMentorTelegramUserId(
                profile.telegramUserId()
        )).thenReturn(Optional.of(mentor));

        when(guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        )).thenReturn(expected);

        GuaranteedReviewsPrices result =
                guaranteedReviewService.insertGuaranteedReviewPrice(request);

        assertSame(expected, result);

        verify(telegramUrlValidator)
                .validate(request.telegramUrl());

        verify(httpClient)
                .getProfileByTgUrl(request.telegramUrl());

        verify(mentorsRepository)
                .findByMentorTelegramUserId(profile.telegramUserId());

        verify(guaranteedReviewPriceService).save(
                mentor,
                "Java",
                "PET_PROJECT",
                10
        );
    }

    @Test
    void insertGuaranteedReviewPrice_whenProfileNotFound_shouldThrowProfileNotFoundException() {
        AddPriceForGuaranteedReviewRequest request =
                new AddPriceForGuaranteedReviewRequest(
                        "https://t.me/test_mentor",
                        "Java",
                        "PET_PROJECT",
                        10
                );

        when(httpClient.getProfileByTgUrl(request.telegramUrl()))
                .thenReturn(Optional.empty());

        assertThrows(
                ProfileNotFoundException.class,
                () -> guaranteedReviewService.insertGuaranteedReviewPrice(request)
        );

        verify(telegramUrlValidator)
                .validate(request.telegramUrl());

        verify(httpClient)
                .getProfileByTgUrl(request.telegramUrl());

        verifyNoInteractions(mentorsRepository);
        verifyNoInteractions(guaranteedReviewPriceService);
    }

    @Test
    void insertGuaranteedReviewPrice_whenMentorNotFound_shouldThrowMentorNotFoundException() {
        AddPriceForGuaranteedReviewRequest request =
                new AddPriceForGuaranteedReviewRequest(
                        "https://t.me/test_mentor",
                        "Java",
                        "PET_PROJECT",
                        10
                );

        ProfileWithTelegramIdDto profile =
                new ProfileWithTelegramIdDto(12345L, null);

        when(httpClient.getProfileByTgUrl(request.telegramUrl()))
                .thenReturn(Optional.of(profile));

        when(mentorsRepository.findByMentorTelegramUserId(
                profile.telegramUserId()
        )).thenReturn(Optional.empty());

        assertThrows(
                MentorNotFoundException.class,
                () -> guaranteedReviewService.insertGuaranteedReviewPrice(request)
        );

        verifyNoInteractions(guaranteedReviewPriceService);
    }
}