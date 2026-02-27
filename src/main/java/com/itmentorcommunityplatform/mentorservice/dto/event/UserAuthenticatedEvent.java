package com.itmentorcommunityplatform.mentorservice.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthenticatedEvent {

    @JsonProperty("telegram_user_id")
    private Long telegramUserId;

    @JsonProperty("telegram_username")
    private String telegramUsername;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("roles")
    private List<String> roles;
}
