package com.itmentorcommunityplatform.mentorservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileDetailsDto {

    @JsonProperty("github_profile_url")
    private String githubProfile;
    @JsonProperty("telegram_url")
    private String telegramUrl;

}
