package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuaranteedReviewsPriceRepository extends CrudRepository<GuaranteedReviewsPrices, Long> {
}
