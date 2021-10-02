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
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Objective {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Transient
    @JsonInclude
    private final String type = getClass().getSimpleName();

    @NotNull
    private String name;

    private String description;

    private String doneCriteria;

    @NotNull
    private Date dueDate;

    @NotNull
    @OneToOne
    @JsonIgnore
    private AppUser owner;

    @OneToMany
    @JsonIgnore
    private List<Milestone> milestones;

    @OneToMany
    @JsonIgnore
    private List<Task> tasks;

    @Transient
    @JsonInclude
    private StatusAggregate milestoneStatusAggregates = new StatusAggregate();

    @Transient
    @JsonInclude
    private StatusAggregate taskStatusAggregates = new StatusAggregate();

    private String status;
}
