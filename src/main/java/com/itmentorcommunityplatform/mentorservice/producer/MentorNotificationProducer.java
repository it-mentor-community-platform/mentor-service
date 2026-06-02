package com.itmentorcommunityplatform.mentorservice.producer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmentorcommunityplatform.mentorservice.dto.event.MentorNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorNotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.notification-mentors-project-submitted}")
    private String mentorsNotificationTopic;


    public void notificateMentors(MentorNotificationEvent event) throws JsonProcessingException {
        log.info("Kafka Producer: Starting mentor notification event: {}", event);
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(mentorsNotificationTopic, message);
            log.info("Kafka Consumer: Successfully processed notification event for mentors {}", event.getMentors());
        } catch (Exception e) {
            log.error("Kafka Producer: Error processing notification for mentors {}", event.getMentors(), e);
            throw e;
        }
    }

}
