package com.takeiton.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone {


    @Transient
    @JsonInclude
    public boolean hasItems = true;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Transient
    @JsonInclude
    private final String type = getClass().getSimpleName();

    private String clientId;

    private String category;

    private String name;

    private String description;

    private String doneCriteria;

    private Date dueDate;

    private String parentId;

    private String parentType;

    @OneToOne
    @NotNull
    @JsonIgnore
    private AppUser owner;

    private Long parentObjectiveId;

    @OneToMany
    @JsonIgnore
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Transient
    @JsonInclude
    @Builder.Default
    private StatusAggregate taskStatusAggregates = new StatusAggregate();

    private String status;
}
