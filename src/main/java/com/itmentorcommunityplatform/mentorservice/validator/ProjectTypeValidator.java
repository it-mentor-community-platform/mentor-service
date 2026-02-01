package com.itmentorcommunityplatform.mentorservice.validator;


import com.itmentorcommunityplatform.mentorservice.domain.type.RoadmapProjectType;
import org.springframework.stereotype.Component;

@Component
public class ProjectTypeValidator {

    public void validate(String value) {

        try {
            RoadmapProjectType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException( "Invalid project type " + value);
        }


    }

}
