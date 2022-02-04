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
public class Objective {


    @Transient
    @JsonInclude
    public boolean hasItems = true;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String clientId;

    @Transient
    @JsonInclude
    private final String type = getClass().getSimpleName();

    @NotNull
    private String name;

    private String description;

    private String doneCriteria;

    private String category;

    @NotNull
    private Date dueDate;

    @NotNull
    @OneToOne
    @JsonIgnore
    private AppUser owner;

    @OneToMany
    @JsonIgnore
    @Builder.Default
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany
    @JsonIgnore
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Transient
    @JsonInclude
    @Builder.Default
    private StatusAggregate milestoneStatusAggregates = StatusAggregate.builder().build();

    @Transient
    @JsonInclude
    @Builder.Default
    private StatusAggregate taskStatusAggregates = StatusAggregate.builder().build();

    private String status;
}
