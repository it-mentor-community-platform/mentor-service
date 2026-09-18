package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.MentorDescription;
import com.itmentorcommunityplatform.mentorservice.dto.AddGuaranteedReviewPriceRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionRequestDto;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDoesNotExistException;
import com.itmentorcommunityplatform.mentorservice.exception.MissingMentorRoleException;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentMentorServiceTest {

    private static final long TELEGRAM_MENTOR_ID = 12345L;

    @Mock
    private MentorsRepository mentorsRepository;

    @Mock
    private GuaranteedReviewPriceService guaranteedReviewPriceService;

    @Mock
    private MentorMapper mentorMapper;

    private CurrentMentorService currentMentorService;

    @BeforeEach
    void setUp() {
        currentMentorService = new CurrentMentorService(
                mentorsRepository,
                guaranteedReviewPriceService,
                mentorMapper
        );
    }

    @Test
    void updateMentorDescription_whenRequestValid_shouldUpdateDescription() {
        String newName = "Simple Parker";
        String newCost = "300";
        String newDescription = "Some description 2";

        MentorDescription updatedDescription = new MentorDescription(
                1L,
                TELEGRAM_MENTOR_ID,
                newName,
                newCost,
                newDescription
        );

        MentorDescriptionRequestDto request =
                new MentorDescriptionRequestDto(
                        newName,
                        newCost,
                        newDescription
                );

        when(mentorsRepository.updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                newName,
                newCost,
                newDescription
        )).thenReturn(Optional.of(updatedDescription));

        currentMentorService.updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                request
        );

        verify(mentorsRepository).updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                newName,
                newCost,
                newDescription
        );

        verify(mentorMapper).mapDescriptionToDto(updatedDescription);
    }

    @Test
    void updateMentorDescription_whenRequestHasOneField_shouldUpdateOneField() {
        String newName = "Simple Parker";

        MentorDescription updatedDescription = new MentorDescription(
                1L,
                TELEGRAM_MENTOR_ID,
                newName,
                "900",
                "mentorOldDescription"
        );

        MentorDescriptionRequestDto request =
                new MentorDescriptionRequestDto(
                        newName,
                        null,
                        null
                );

        when(mentorsRepository.updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                newName,
                null,
                null
        )).thenReturn(Optional.of(updatedDescription));

        currentMentorService.updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                request
        );

        verify(mentorsRepository).updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                newName,
                null,
                null
        );

        verify(mentorMapper).mapDescriptionToDto(updatedDescription);
    }

    @Test
    void updateMentorDescription_whenMentorNotFound_shouldThrowMentorDoesNotExistException() {
        MentorDescriptionRequestDto request =
                new MentorDescriptionRequestDto(
                        "Simple Parker",
                        "22",
                        "not null"
                );

        when(mentorsRepository.updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                request.name(),
                request.cost(),
                request.description()
        )).thenReturn(Optional.empty());

        assertThrows(
                MentorDoesNotExistException.class,
                () -> currentMentorService.updateMentorDescription(
                        TELEGRAM_MENTOR_ID,
                        request
                )
        );

        verify(mentorsRepository).updateMentorDescription(
                TELEGRAM_MENTOR_ID,
                request.name(),
                request.cost(),
                request.description()
        );

        verifyNoInteractions(mentorMapper);
    }

    @Test
    void addGuaranteedReviewPrice_whenMentorExists_shouldSavePrice() {
        Long telegramUserId = 12345L;

        AddGuaranteedReviewPriceRequest request =
                new AddGuaranteedReviewPriceRequest(
                        "Java",
                        "SIMULATION",
                        20
                );

        Mentor mentor = Mentor.builder()
                .id(1L)
                .mentorTelegramUserId(telegramUserId)
                .build();

        GuaranteedReviewsPrices savedPrice =
                new GuaranteedReviewsPrices();

        when(mentorsRepository.findByMentorTelegramUserId(telegramUserId))
                .thenReturn(Optional.of(mentor));

        when(guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        )).thenReturn(savedPrice);

        GuaranteedReviewsPrices result =
                currentMentorService.addGuaranteedReviewPrice(
                        telegramUserId,
                        request
                );

        assertSame(savedPrice, result);

        verify(mentorsRepository)
                .findByMentorTelegramUserId(telegramUserId);

        verify(guaranteedReviewPriceService).save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        );
    }

    @Test
    void addGuaranteedReviewPrice_whenMentorNotFound_shouldThrowMissingMentorRoleException() {
        Long telegramUserId = 12345L;

        AddGuaranteedReviewPriceRequest request =
                new AddGuaranteedReviewPriceRequest(
                        "Java",
                        "SIMULATION",
                        20
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

        verify(mentorsRepository)
                .findByMentorTelegramUserId(telegramUserId);

        verifyNoInteractions(guaranteedReviewPriceService);
    }
}