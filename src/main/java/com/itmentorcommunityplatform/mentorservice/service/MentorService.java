package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.type.UpsertResult;
import com.itmentorcommunityplatform.mentorservice.dto.AddPriceForGuaranteedReviewRequest;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private final MentorsRepository mentorsRepository;
    private final TelegramUrlValidator telegramUrlValidator;
    private final ProjectTypeValidator projectTypeValidator;

    @Transactional
    public UpsertResult upsertMentorAndGuaranteedReviewsPrices(Long telegramUserId, AddPriceForGuaranteedReviewRequest request) {

        telegramUrlValidator.validate(request.telegramUrl());
        projectTypeValidator.validate(request.projectType());

        Mentor mentor = mentorsRepository.findByMentorTelegramUserId(telegramUserId);

        boolean created;

        if (mentor == null) {
            mentor = new Mentor();
            mentor.setMentorTelegramUserId(telegramUserId);
            mentor.setTelegramUrl(request.telegramUrl());
            mentor.setActive(true);
            mentor.setPrices(new HashSet<>());
            created = true;
        } else {
            created = false;
        }

        boolean priceUpdate = updatePriceForGuaranteedReviews(mentor.getPrices(), request);
        mentorsRepository.save(mentor);

        log.info("Price for guaranteed reviews and mentor successfully upsert mentorTelegramUserId={}, projectType={}",
                telegramUserId, request.projectType());

        return (created || priceUpdate)
                ? UpsertResult.CREATED
                : UpsertResult.UPDATED;

    }


    private boolean updatePriceForGuaranteedReviews(Set<GuaranteedReviewsPrices> guaranteedReviewsPrices, AddPriceForGuaranteedReviewRequest request) {

        GuaranteedReviewsPrices existing = guaranteedReviewsPrices.stream()
                .filter(p -> p.getProjectType().equals(request.projectType()) &&
                        p.getLanguage().equals(request.language()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setPriceUsd(request.priceUsd());
            return false;
        }

        GuaranteedReviewsPrices created = new GuaranteedReviewsPrices();
        created.setProjectType(request.projectType());
        created.setLanguage(request.language());
        created.setPriceUsd(request.priceUsd());
        guaranteedReviewsPrices.add(created);

        return true;

    }


}
