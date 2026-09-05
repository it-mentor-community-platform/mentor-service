package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddGuaranteedReviewPriceRequest;
import com.itmentorcommunityplatform.mentorservice.exception.MissingMentorRoleException;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
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
class CurrentMentorServiceTest {

    @Mock
    private MentorsRepository mentorsRepository;

    @Mock
    private GuaranteedReviewPriceService guaranteedReviewPriceService;

    private CurrentMentorService currentMentorService;

    @BeforeEach
    void setUp() {
        currentMentorService = new CurrentMentorService(
                mentorsRepository,
                guaranteedReviewPriceService
        );
    }

    @Test
    void addGuaranteedReviewPrice_whenMentorExists_shouldSavePrice() {
        Long telegramUserId = 12345L;

        AddGuaranteedReviewPriceRequest request =
                new AddGuaranteedReviewPriceRequest(
                        "Java",
                        "PET_PROJECT",
                        10
                );

        Mentor mentor = Mentor.builder()
                .id(1L)
                .mentorTelegramUserId(telegramUserId)
                .build();

        GuaranteedReviewsPrices expectedPrice =
                GuaranteedReviewsPrices.builder()
                        .mentorId(mentor.getId())
                        .language("Java")
                        .projectType("PET_PROJECT")
                        .priceUsd(10)
                        .build();

        when(mentorsRepository.findByMentorTelegramUserId(telegramUserId))
                .thenReturn(Optional.of(mentor));

        when(guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        )).thenReturn(expectedPrice);

        GuaranteedReviewsPrices result =
                currentMentorService.addGuaranteedReviewPrice(
                        telegramUserId,
                        request
                );

        assertSame(expectedPrice, result);

        verify(mentorsRepository)
                .findByMentorTelegramUserId(telegramUserId);

        verify(guaranteedReviewPriceService).save(
                mentor,
                "Java",
                "PET_PROJECT",
                10
        );
    }

    @Test
    void addGuaranteedReviewPrice_whenMentorNotFound_shouldThrowUserIsNotMentorException() {
        Long telegramUserId = 12345L;

        AddGuaranteedReviewPriceRequest request =
                new AddGuaranteedReviewPriceRequest(
                        "Java",
                        "PET_PROJECT",
                        10
                );

        when(mentorsRepository.findByMentorTelegramUserId(telegramUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                MissingMentorRoleException.class,
                () -> currentMentorService.addGuaranteedReviewPrice(
                        telegramUserId,
                        request
                )
        );

        verifyNoInteractions(guaranteedReviewPriceService);
    }
}