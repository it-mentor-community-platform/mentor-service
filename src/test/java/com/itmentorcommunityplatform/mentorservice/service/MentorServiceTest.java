package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDescriptionDto;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import com.itmentorcommunityplatform.mentorservice.httpclient.ServiceHttpClient;
import com.itmentorcommunityplatform.mentorservice.mapper.MentorMapper;
import com.itmentorcommunityplatform.mentorservice.repository.MentorsRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ProgrammingLanguagesRepository;
import com.itmentorcommunityplatform.mentorservice.repository.ServicesRepository;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import com.itmentorcommunityplatform.mentorservice.validator.TelegramUrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorServiceTest {

    @Mock
    private TelegramUrlValidator telegramUrlValidator;
    @Mock
    private ProjectTypeValidator projectTypeValidator;
    @Mock
    private MentorsRepository mentorsRepository;
    @Mock
    private ProgrammingLanguagesRepository programmingLanguagesRepository;
    @Mock
    private ServicesRepository servicesRepository;
    @Mock
    private ServiceHttpClient httpClient;
    @Mock
    private MentorMapper mentorMapper;

    @InjectMocks
    private MentorService mentorService;

    private AddMentorWithDescriptionRequest request;

    @BeforeEach
    void setUp() {
        MentorDescriptionDto descriptionDto = new MentorDescriptionDto("Peter Parker", "100", "Description");
        request = new AddMentorWithDescriptionRequest(
                12345L,
                "https://t.me/test_mentor",
                descriptionDto,
                List.of("Java"),
                List.of("Code Review")
        );
    }

    @Test
    void createMentorWithDescription_whenProfileNotFound_shouldCreateProfileAndMentor() {
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.empty());

        mentorService.createMentorWithDescription(request);

        verify(telegramUrlValidator).validate(request.telegramUrl());
        verify(httpClient).createProfile(request.mentorTelegramUserId(), request.telegramUrl());
        verify(mentorsRepository).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenProfileExists_shouldNotCreateProfileAndSaveMentor() {
        ProfileWithTelegramIdDto profileDto = new ProfileWithTelegramIdDto(12345L, null);
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.of(profileDto));

        mentorService.createMentorWithDescription(request);

        verify(telegramUrlValidator).validate(request.telegramUrl());
        verify(httpClient, never()).createProfile(any(), any());
        verify(mentorsRepository).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenHttpClientFails_shouldRethrowException() {
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Profile service error"))
                .when(httpClient).createProfile(request.mentorTelegramUserId(), request.telegramUrl());

        assertThrows(RuntimeException.class, () -> mentorService.createMentorWithDescription(request));

        verify(mentorsRepository, never()).save(any(Mentor.class));
    }

    @Test
    void createMentorWithDescription_whenDuplicateKeyInDb_shouldThrowConflictException() {
        when(httpClient.getProfileByTgUrl(request.telegramUrl())).thenReturn(Optional.of(new ProfileWithTelegramIdDto(12345L, null)));
        when(mentorsRepository.save(any(Mentor.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThrows(DataIntegrityViolationException.class, () -> mentorService.createMentorWithDescription(request));
    }
}