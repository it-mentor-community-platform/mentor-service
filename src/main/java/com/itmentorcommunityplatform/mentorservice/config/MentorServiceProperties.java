package com.itmentorcommunityplatform.mentorservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mentorservice")
@Data
public class MentorServiceProperties {

    private String profileServiceBaseUrl;
}
