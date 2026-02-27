package com.itmentorcommunityplatform.mentorservice.consumer;

import com.itmentorcommunityplatform.mentorservice.dto.event.UserAuthenticatedEvent;
import com.itmentorcommunityplatform.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserAuthenticatedConsumer {

    private final MentorService mentorService;

    @KafkaListener(topics = "${spring.kafka.topic.auth-user-authenticated}", groupId = "mentor-service-cg")
    public void consumeUserAuthenticatedEvent(UserAuthenticatedEvent event) {
        log.info("Kafka Consumer: Received user authenticated event: {}", event);
        try {
            mentorService.updateMentorProfile(event);
            log.info("Kafka Consumer: Successfully processed event for user {}", event.getTelegramUserId());
         } catch (Exception e) {
            log.error("Kafka Consumer: Error processing event for user {}", event.getTelegramUserId(), e);
            throw e;
        }
    }
}
