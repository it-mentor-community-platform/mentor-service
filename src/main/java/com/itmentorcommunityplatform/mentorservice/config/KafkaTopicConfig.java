package com.itmentorcommunityplatform.mentorservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.notification-mentors-project-submitted}")
    private String mentorsNotificationTopic;

    @Bean
    public NewTopic mentorsNotificationTopic() {
        return TopicBuilder.name(mentorsNotificationTopic)
                .build();
    }
}
