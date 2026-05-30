package com.itmentorcommunityplatform.mentorservice.dto.event;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.itmentorcommunityplatform.mentorservice.domain.type.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProjectCreatedEvent {

    private String authorTelegramProfileUrl;
    private String githubRepositoryUrl;
    private String programmingLanguage;
    private String roadmapProject;
    private Long addedTimestamp;
    private DataSourceType projectSourceType;

}
