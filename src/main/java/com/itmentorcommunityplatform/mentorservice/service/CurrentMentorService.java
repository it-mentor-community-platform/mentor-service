package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.MentorDescription;
import com.itmentorcommunityplatform.mentorservice.dto.AddGuaranteedReviewPriceRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionRequestDto;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionResponseDto;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDescriptionEmptyException;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDoesNotExistException;
import com.itmentorcommunityplatform.mentorservice.exception.MissingMentorRoleException;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentMentorService {

    private final MentorsRepository mentorsRepository;
    private final GuaranteedReviewPriceService guaranteedReviewPriceService;
    private final MentorMapper mentorMapper;

    public GuaranteedReviewsPrices addGuaranteedReviewPrice(
            Long telegramUserId,
            AddGuaranteedReviewPriceRequest request
    ) {
        Mentor mentor = mentorsRepository
                .findByMentorTelegramUserId(telegramUserId)
                .orElseThrow(MissingMentorRoleException::new);

        return guaranteedReviewPriceService.save(
                mentor,
                request.language(),
                request.projectType(),
                request.priceUsd()
        );
    }

    public MentorDescriptionResponseDto updateMentorDescription(Long telegramId, MentorDescriptionRequestDto requestDescription) {
        validateMentorDescription(requestDescription);

        MentorDescription updatedMentorDescription = mentorsRepository.updateMentorDescription(telegramId,
                requestDescription.name(),
                requestDescription.cost(),
                requestDescription.description()
        ).orElseThrow(() -> new MentorDoesNotExistException("Mentor with this telegramId not found!"));

        log.info("Mentor with name: {} updated successfully", updatedMentorDescription.getName());
        return mentorMapper.mapDescriptionToDto(updatedMentorDescription);
    }

    private static void validateMentorDescription(MentorDescriptionRequestDto mentorDescriptionDtoNew) {
        if (isValueNullOrBlank(mentorDescriptionDtoNew.name()) &&
                isValueNullOrBlank(mentorDescriptionDtoNew.cost()) &&
                isValueNullOrBlank(mentorDescriptionDtoNew.description())) {
            throw new MentorDescriptionEmptyException("Mentor description is empty!");
        }
    }
    private static boolean isValueNullOrBlank(String value) {
        return value == null || value.isBlank();
    }
}
