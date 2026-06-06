package com.itmentorcommunityplatform.mentorservice.producer;


import com.itmentorcommunityplatform.mentorservice.dto.event.MentorNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorNotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.notification-mentors-project-submitted}")
    private String mentorsNotificationTopic;


    public void notificateMentors(MentorNotificationEvent event) {
        log.info("Kafka Producer: Starting mentor notification event: {}", event);
        try {
            kafkaTemplate.send(mentorsNotificationTopic, event);
            log.info("Kafka Producer: Successfully processed notification event for mentors {}", event.getMentors());
        } catch (Exception e) {
            log.error("Kafka Producer: Error processing notification for mentors {}", event.getMentors(), e);
            throw e;
        }
    }

}
