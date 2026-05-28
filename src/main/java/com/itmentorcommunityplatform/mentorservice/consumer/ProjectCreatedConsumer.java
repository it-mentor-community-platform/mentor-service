package com.itmentorcommunityplatform.mentorservice.consumer;

import com.itmentorcommunityplatform.mentorservice.dto.event.ProjectCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCreatedConsumer {


    @KafkaListener(topics = "${spring.kafka.topic.projects-project-created}", groupId = "mentor-service-cg")
    public void consumerProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("Kafka Consumer: Received user's project create event: {}", event);
        try {
            log.info("Kafka Consumer: Successfully processed event for user's project {}", event.getGithubRepositoryUrl());
        } catch (Exception e) {
            log.error("Kafka Consumer: Error processing event for user's project {}", event.getGithubRepositoryUrl(), e);
            throw e;
        }
    }

}
