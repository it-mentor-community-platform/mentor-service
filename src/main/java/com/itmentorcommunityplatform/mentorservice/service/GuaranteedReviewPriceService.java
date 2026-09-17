package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.UpdatePriceForGuaranteedReview;
import com.itmentorcommunityplatform.mentorservice.exception.GuaranteedReviewPriceAlreadyExistsException;
import com.itmentorcommunityplatform.mentorservice.exception.GuaranteedReviewPriceNotFoundException;
import com.itmentorcommunityplatform.mentorservice.exception.MentorDoesNotExistException;
import com.itmentorcommunityplatform.mentorservice.exception.MissingMentorRoleException;
import com.itmentorcommunityplatform.mentorservice.repository.GuaranteedReviewsPriceRepository;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.validator.MentorProgrammingLanguageValidator;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuaranteedReviewPriceService {
    private final MentorsRepository mentorsRepository;
    private final ProjectTypeValidator projectTypeValidator;
    private final MentorProgrammingLanguageValidator mentorProgrammingLanguageValidator;
    private final GuaranteedReviewsPriceRepository guaranteedReviewsPriceRepository;

    @Transactional
    public GuaranteedReviewsPrices updateGuaranteedReviewPriceByGuaranteedReviewId(
            Long telegramUserId,
            Long guaranteedReviewId,
            UpdatePriceForGuaranteedReview request
    ) {
        Mentor mentor = mentorsRepository
                .findByMentorTelegramUserId(telegramUserId)
                .orElseThrow(MentorDoesNotExistException::new);

        GuaranteedReviewsPrices reviewPrice = guaranteedReviewsPriceRepository
                .findByIdAndMentorId(guaranteedReviewId, mentor.getId())
                .orElseThrow(GuaranteedReviewPriceNotFoundException::new);

        reviewPrice.setPriceUsd(request.priceUsd());

        return guaranteedReviewsPriceRepository.save(reviewPrice);
    }

    public GuaranteedReviewsPrices save(
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
