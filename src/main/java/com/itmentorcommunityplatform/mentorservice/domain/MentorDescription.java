package com.itmentorcommunityplatform.mentorservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mentor_descriptions")
public class MentorDescription {

    @Id
    private Long id;

    @Column("mentor_user_id")
    private Long mentorUserId;

    @Column("name")
    private String name;

    @Column("cost")
    private String cost;

    @Column("description")
    private String description;
}
