package com.itmentorcommunityplatform.mentorservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDto;
import com.itmentorcommunityplatform.mentorservice.dto.event.MentorNotificationEvent;
import com.itmentorcommunityplatform.mentorservice.dto.event.ProjectCreatedEvent;
import com.itmentorcommunityplatform.mentorservice.producer.MentorNotificationProducer;
import com.itmentorcommunityplatform.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCreatedConsumer {


    private final MentorService mentorService;
    private final MentorNotificationProducer mentorNotificationProducer;

    @KafkaListener(topics = "${spring.kafka.topic.projects-project-created}", groupId = "mentor-service-cg")
    public void consumerProjectCreatedEvent(ProjectCreatedEvent event) throws JsonProcessingException {
        log.info("Kafka Consumer: Received user's project create event: {}", event);
        try {
            List<MentorDto> mentors = mentorService.searchMentorsByLanguageAndProjectType(event.getProgrammingLanguage(), event.getRoadmapProject());
            mentorNotificationProducer.notificateMentors(
                    MentorNotificationEvent.builder()
                            .project(event)
                            .mentors(mentors)
                            .build()
            );
            log.info("Kafka Consumer: Successfully processed event for user's project {}", event.getGithubRepositoryUrl());
        } catch (Exception e) {
            log.error("Kafka Consumer: Error processing event for user's project {}", event.getGithubRepositoryUrl(), e);
            throw e;
        }
    }

}
