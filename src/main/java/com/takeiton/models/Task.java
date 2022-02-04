package com.takeiton.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {


    @Transient
    @JsonInclude
    public boolean hasItems = false;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Transient
    @JsonInclude
    private final String type = getClass().getSimpleName();

    private String name;

    private String description;

    private String doneCriteria;

    private Date dueDate;

    private String parentId;
    private String clientId;
    private String parentType;
    @OneToOne
    @NotNull
    @JsonIgnore
    private AppUser owner;

    private String category;

    private String status;
}
