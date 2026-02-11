package com.itmentorcommunityplatform.mentorservice.httpclient;

import com.itmentorcommunityplatform.mentorservice.config.MentorServiceProperties;
import com.itmentorcommunityplatform.mentorservice.dto.ProfileWithTelegramIdDto;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceHttpClient {

    private final WebClient webClient;
    private final MentorServiceProperties properties;
    private final Counter failedAttempts;

    public Optional<ProfileWithTelegramIdDto> getProfileByTgUrl(String telegramUrl) {

        String url = UriComponentsBuilder
                .fromUriString(properties.getProfileServiceBaseUrl())
                .path("/api/profile/internal/profile/by-telegram-url")
                .queryParam("url", telegramUrl)
                .toUriString();

        try {

            return webClient.get()
                    .uri(url)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(ProfileWithTelegramIdDto.class);
                        } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.empty();
                        }
                        return response.createException().flatMap(Mono::error);
                    })
                    .blockOptional();

        } catch (Exception e) {
            failedAttempts.increment();
            log.error("Failed calling profile-service. url={}", telegramUrl, e);
            throw e;

        }
    }
}
