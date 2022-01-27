package com.takeiton.controllers;

import com.takeiton.models.Objective;
import com.takeiton.models.StatusRollup;
import com.takeiton.services.OwnedObjectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/objective")
public class ObjectiveController {

    @Autowired
    OwnedObjectiveService ownedObjectiveService;

    @GetMapping
    public List<Objective> getAllObjectives(@RequestParam(required = false) boolean rollup, @RequestParam(required = false) String status, Principal principal) {
        return ownedObjectiveService.findAllByStatus(principal.getName(), rollup, status);
    }

    @GetMapping(value = "/statusRollup")
    public StatusRollup getAllObjectivesStatusRollup(Principal principal) {
        return ownedObjectiveService.findAllStatusRollup(principal.getName());
    }

    @GetMapping(value = "{objectiveId}")
    public ResponseEntity<Objective> getObjective(@PathVariable(value = "objectiveId") Long objectiveId, Principal principal) {
        Optional<Objective> objective = ownedObjectiveService.findById(objectiveId, principal.getName());
        return objective.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Objective createObjective(@RequestBody @Valid Objective objective, Principal principal) {
        return ownedObjectiveService.createObjective(objective, principal.getName());
    }

    @PutMapping(value = "{objectiveId}")
    public ResponseEntity<Objective> updateObjective(@PathVariable(value = "objectiveId") Long objectiveId, @RequestBody @Valid Objective objective, Principal principal){
        Optional<Objective> savedObjective = ownedObjectiveService.save(objectiveId, objective, principal.getName());
        return savedObjective.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build());
    }

}