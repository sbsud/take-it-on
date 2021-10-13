package com.takeiton.controllers;

import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.services.OwnedMilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api")
public class MilestoneController {

    @Autowired
    OwnedMilestoneService milestoneService;

    @PostMapping(value = "/objective/{objectiveId}/milestone")
    public ResponseEntity<Milestone> createMilestone(@PathVariable(value = "objectiveId") Long objectiveId, @RequestBody @Valid Milestone milestone, Principal principal) {
        Milestone createdMilestone;
        try {
            createdMilestone = milestoneService.createMilestone(objectiveId, milestone, principal.getName());
        } catch (AccessDeniedException accessDeniedException) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(createdMilestone);
    }

    @GetMapping(value = "/milestone/{milestoneId}")
    public ResponseEntity<Milestone> getMilestone(@PathVariable(value = "milestoneId") Long milestoneId, Principal principal) {
        Optional<Milestone> milestone = milestoneService.findById(milestoneId, principal.getName());
        return milestone.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/milestone")
    public Iterable<Milestone> getAllMilestones(Principal principal) {
        return milestoneService.findAll(principal.getName());
    }

    @GetMapping(value = "objective/{objectiveId}/milestone")
    public Iterable<Milestone> getAllMilestones(@PathVariable Long objectiveId, Principal principal) {
        return milestoneService.findMilestonesForObjective(objectiveId, principal.getName());
    }
}
