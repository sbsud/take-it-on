package com.takeiton.controllers;

import com.takeiton.models.ICategoryCount;
import com.takeiton.services.CategoryService;
import com.takeiton.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("/completed/count")
    public List<ICategoryCount> getCategoryCount(Principal principal) {
        return categoryService.countByStatus(principal.getName(), "COMPLETED");
    }

    @GetMapping("/inprogress/count")
    public List<ICategoryCount> getCategoryInprogressCount(Principal principal) {
        return categoryService.countIncompletes(principal.getName(), Status.IN_PROGRESS.toString());
    }

    @GetMapping("/notstarted/count")
    public List<ICategoryCount> getCategoryNotStartedCount(Principal principal) {
        return categoryService.countIncompletes(principal.getName(), Status.NOT_STARTED.toString());
    }


    @GetMapping("/overdue/count")
    public List<ICategoryCount> getCategoryOverdueCount(Principal principal) {
        return categoryService.countOverdue(principal.getName());
    }

}
