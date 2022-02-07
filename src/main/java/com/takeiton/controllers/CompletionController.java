package com.takeiton.controllers;

import com.takeiton.models.ICategoryCount;
import com.takeiton.models.ICompletionRate;
import com.takeiton.services.CompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/completion")
public class CompletionController {

    @Autowired
    CompletionService completionService;

    @GetMapping("/count")
    public List<ICompletionRate> getCompletionRate(@RequestParam(required = false) String status, Principal principal) {
        return completionService.getCount(principal.getName());
    }
}
