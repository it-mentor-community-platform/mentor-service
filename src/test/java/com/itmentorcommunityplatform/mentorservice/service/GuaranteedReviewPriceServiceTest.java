package com.itmentorcommunityplatform.mentorservice.service;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.exception.GuaranteedReviewPriceAlreadyExistsException;
import com.itmentorcommunityplatform.mentorservice.repository.GuaranteedReviewsPriceRepository;
import com.itmentorcommunityplatform.mentorservice.validator.MentorProgrammingLanguageValidator;
import com.itmentorcommunityplatform.mentorservice.validator.ProjectTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuaranteedReviewPriceServiceTest {

    @Mock
    private ProjectTypeValidator projectTypeValidator;

    @Mock
    private MentorProgrammingLanguageValidator mentorProgrammingLanguageValidator;

    @Mock
    private GuaranteedReviewsPriceRepository guaranteedReviewsPriceRepository;

    private GuaranteedReviewPriceService guaranteedReviewPriceService;

    @BeforeEach
    void setUp() {
        guaranteedReviewPriceService = new GuaranteedReviewPriceService(
                projectTypeValidator,
                mentorProgrammingLanguageValidator,
                guaranteedReviewsPriceRepository
        );
    }

    @Test
    void save_whenRequestValid_shouldSaveGuaranteedReviewPrice() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .build();

        GuaranteedReviewsPrices savedPrice =
                GuaranteedReviewsPrices.builder()
                        .mentorId(1L)
                        .language("Java")
                        .projectType("PET_PROJECT")
                        .priceUsd(10)
                        .build();

        when(guaranteedReviewsPriceRepository.save(any(GuaranteedReviewsPrices.class)))
                .thenReturn(savedPrice);

        GuaranteedReviewsPrices result = guaranteedReviewPriceService.save(
                mentor,
                "Java",
                "PET_PROJECT",
                10
        );

        assertSame(savedPrice, result);

        verify(projectTypeValidator).validate("PET_PROJECT");
        verify(mentorProgrammingLanguageValidator).validate(mentor, "Java");

        ArgumentCaptor<GuaranteedReviewsPrices> captor =
                ArgumentCaptor.forClass(GuaranteedReviewsPrices.class);

        verify(guaranteedReviewsPriceRepository).save(captor.capture());

        GuaranteedReviewsPrices price = captor.getValue();

        assertEquals(1L, price.getMentorId());
        assertEquals("Java", price.getLanguage());
        assertEquals("PET_PROJECT", price.getProjectType());
        assertEquals(10, price.getPriceUsd());
    }

    @Test
    void save_whenProjectTypeValidationFails_shouldNotSavePrice() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .build();

        doThrow(new IllegalArgumentException())
                .when(projectTypeValidator)
                .validate("INVALID");

        assertThrows(
                IllegalArgumentException.class,
                () -> guaranteedReviewPriceService.save(
                        mentor,
                        "Java",
                        "INVALID",
                        10
                )
        );

        verifyNoInteractions(mentorProgrammingLanguageValidator);
        verifyNoInteractions(guaranteedReviewsPriceRepository);
    }

    @Test
    void save_whenMentorDoesNotHaveLanguage_shouldNotSavePrice() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .build();

        doThrow(new IllegalArgumentException())
                .when(mentorProgrammingLanguageValidator)
                .validate(mentor, "Python");

        assertThrows(
                IllegalArgumentException.class,
                () -> guaranteedReviewPriceService.save(
                        mentor,
                        "Python",
                        "PET_PROJECT",
                        10
                )
        );

        verify(projectTypeValidator).validate("PET_PROJECT");
        verifyNoInteractions(guaranteedReviewsPriceRepository);
    }

    @Test
    void save_whenPriceAlreadyExists_shouldThrowGuaranteedReviewPriceAlreadyExistsException() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .build();

        DuplicateKeyException duplicateKeyException =
                new DuplicateKeyException("Duplicate");

        DbActionExecutionException dbException =
                new DbActionExecutionException(null, duplicateKeyException);

        when(guaranteedReviewsPriceRepository.save(any(GuaranteedReviewsPrices.class)))
                .thenThrow(dbException);

        assertThrows(
                GuaranteedReviewPriceAlreadyExistsException.class,
                () -> guaranteedReviewPriceService.save(
                        mentor,
                        "Java",
                        "PET_PROJECT",
                        10
                )
        );
    }

    @Test
    void save_whenUnexpectedDatabaseExceptionOccurs_shouldRethrowException() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .build();

        RuntimeException cause = new RuntimeException("Database error");

        DbActionExecutionException dbException =
                new DbActionExecutionException(null, cause);

        when(guaranteedReviewsPriceRepository.save(any(GuaranteedReviewsPrices.class)))
                .thenThrow(dbException);

        DbActionExecutionException thrown = assertThrows(
                DbActionExecutionException.class,
                () -> guaranteedReviewPriceService.save(
                        mentor,
                        "Java",
                        "PET_PROJECT",
                        10
                )
        );

        assertSame(dbException, thrown);
    }
}