package com.takeiton.controllers;

import com.takeiton.models.Objective;
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
    public List<Objective> getAllObjectives(@RequestParam(required = false) boolean rollup, Principal principal) {
        return ownedObjectiveService.findAll(principal.getName(), rollup);
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

}