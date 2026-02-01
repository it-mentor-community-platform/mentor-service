package com.itmentorcommunityplatform.mentorservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "guaranteed_reviews_prices")
public class GuaranteedReviewsPrices {

    @Column("project_type")
    private String projectType;

    @Column("language")
    private String language;

    @Column("price_usd")
    private int priceUsd;

}
