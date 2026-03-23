package com.itmentorcommunityplatform.mentorservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("mentors_services")
public class MentorService {
    private AggregateReference<Service, Long> serviceId;
}
