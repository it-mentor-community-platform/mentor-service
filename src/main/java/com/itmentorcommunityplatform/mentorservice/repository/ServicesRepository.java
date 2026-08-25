package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.Service;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ServicesRepository extends CrudRepository<Service, Integer> {

    @Query("""
            SELECT id 
            FROM services 
            WHERE lower(name) = lower(:name)
            """)
    Optional<Long> findIdByName(String name);
}
