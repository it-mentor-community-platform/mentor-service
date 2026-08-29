package com.itmentorcommunityplatform.mentorservice.mapper;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.domain.MentorDescription;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionDto;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDto;
import com.itmentorcommunityplatform.mentorservice.dto.MentorResponseDto;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MentorMapper {

    public List<MentorDto> toMentorDtoList(List<Mentor> mentors){
        List<MentorDto> result = new ArrayList<>();
        for (Mentor mentor : mentors){
            MentorDto mentorDto = new MentorDto(mentor.getMentorTelegramUserId(), mentor.getTelegramUrl());
            result.add(mentorDto);
        }
        return result;
    }

    public MentorResponseDto toMentorResponseDto(Mentor mentor, boolean created) {
        return new MentorResponseDto(
                mentor.getId(),
                mentor.getMentorTelegramUserId(),
                mentor.getTelegramUrl(),
                mentor.isActive(),
                created
        );
    }

    public @NonNull MentorDescriptionDto mapDescription(MentorDescription oldDescription) {
        return new MentorDescriptionDto(
                oldDescription.getName(),
                oldDescription.getCost(),
                oldDescription.getDescription()
        );
    }
}
