package com.itmentorcommunityplatform.mentorservice.dto.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserAuthenticatedEvent {
    private Long telegramUserId;
    private String telegramUsername;
    private String firstName;
    private String lastName;
    private List<String> roles;
}