package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.Service;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ServicesRepository extends CrudRepository<Service, Integer> {

    @Query("""
            INSERT INTO services (name)
            VALUES (:name)
            ON CONFLICT (lower(name))
            DO UPDATE 
            SET name = EXCLUDED.name
            RETURNING id;
            """)
    Long upsertServices(String name);

    @Modifying
    @Query("""
            DELETE FROM mentors_services
            WHERE mentor_id = :mentorId
            """)
    void deleteMentorsServices(Long mentorId);

    @Modifying
    @Query("""
            INSERT INTO mentors_services (mentor_id, service_id)
            VALUES (:mentorId, :serviceId)
            """)
    void insertMentorsServices(Long mentorId, Long serviceId);
}
