package com.itmentorcommunityplatform.mentorservice.controller;

import com.itmentorcommunityplatform.mentorservice.docs.PostAddMentorWithDescription;
import com.itmentorcommunityplatform.mentorservice.dto.AddMentorWithDescriptionRequest;
import com.itmentorcommunityplatform.mentorservice.dto.MentorResponseDto;
import com.itmentorcommunityplatform.mentorservice.service.InternalMentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor/internal")
@RequiredArgsConstructor
public class InternalMentorController {

    private final InternalMentorService internalMentorService;

    @PostMapping("/mentor")
    @PostAddMentorWithDescription
    public ResponseEntity<MentorResponseDto> createMentorWithDescription(
            @RequestBody @Valid AddMentorWithDescriptionRequest request) {

        MentorResponseDto response = internalMentorService.createMentorWithDescription(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
