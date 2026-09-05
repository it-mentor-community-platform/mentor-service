package com.itmentorcommunityplatform.mentorservice.validator;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentorProgrammingLanguageValidator {

    private final ProgrammingLanguagesRepository programmingLanguagesRepository;

    public void validate(Mentor mentor, String language) {
        Long languageId = programmingLanguagesRepository
                .findIdByName(language)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid programming language " + language
                        )
                );

        boolean mentorHasLanguage = mentor.getProgrammingLanguages().stream()
                .anyMatch(mentorLanguage ->
                        mentorLanguage.getProgrammingLanguageId()
                                .getId()
                                .equals(languageId)
                );

        if (!mentorHasLanguage) {
            throw new IllegalArgumentException(
                    "Mentor does not have programming language " + language
            );
        }
    }
}
