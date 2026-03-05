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
@Table(name = "programming_languages")
public class ProgrammingLanguage {

    @Id
    private Long id;

    @Column("name")
    private String name;
}
