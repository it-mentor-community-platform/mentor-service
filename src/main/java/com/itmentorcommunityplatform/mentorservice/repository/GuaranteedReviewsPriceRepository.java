package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuaranteedReviewsPriceRepository extends CrudRepository<GuaranteedReviewsPrices, Long> {

    Optional<GuaranteedReviewsPrices> findByIdAndMentorId(Long id, Long mentorId);
}
