package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.dto.MentorDto;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorSearchService {

    private final MentorsRepository mentorsRepository;
    private final MentorMapper mentorMapper;

    public List<MentorDto> searchActiveMentorsByLanguageAndProjectType(
            String language,
            String projectType
    ) {
        return mentorMapper.toMentorDtoList(
                mentorsRepository
                        .findActiveMentorsByProgrammingLanguageAndProjectType(
                                language,
                                projectType
                        )
        );
    }
}
