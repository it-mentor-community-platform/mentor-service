package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorSynchronizationService {

    private static final String MENTOR_ROLE = "MENTOR";
    private static final String TELEGRAM_URL_PREFIX = "https://t.me/";

    private final MentorsRepository mentorsRepository;

    @Transactional
    public void updateMentorProfile(UserAuthenticatedEvent event) {
        Optional<Mentor> possibleMentor =
                mentorsRepository.findByMentorTelegramUserId(
                        event.getTelegramUserId()
                );

        if (possibleMentor.isEmpty()) {
            log.info(
                    "Mentor with telegram id: {} not found",
                    event.getTelegramUserId()
            );
            return;
        }

        Mentor mentor = possibleMentor.get();

        boolean isActive = event.getRoles().contains(MENTOR_ROLE);
        String telegramUrl = resolveTelegramUrl(event, mentor);

        if (mentor.getTelegramUrl().equals(telegramUrl)
                && mentor.isActive() == isActive) {
            log.info(
                    "Nothing to update for mentor with telegram id: {}",
                    mentor.getMentorTelegramUserId()
            );
            return;
        }

        Mentor updatedMentor = mentorsRepository.updateMentor(
                event.getTelegramUserId(),
                telegramUrl,
                isActive
        );

        log.info(
                "Mentor with telegram id: {} has been updated",
                updatedMentor.getMentorTelegramUserId()
        );
    }

    private String resolveTelegramUrl(
            UserAuthenticatedEvent event,
            Mentor mentor
    ) {
        if (event.getTelegramUsername() == null
                || event.getTelegramUsername().isBlank()) {
            return mentor.getTelegramUrl();
        }

        return TELEGRAM_URL_PREFIX + event.getTelegramUsername();
    }
}