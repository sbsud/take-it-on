package com.takeiton.controllers;

import com.takeiton.models.ICategoryCount;
import com.takeiton.services.CategoryService;
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

    @GetMapping("/count")
    public List<ICategoryCount> getCategoryCount(@RequestParam(required = false) String status, Principal principal) {
        return categoryService.countByStatus(principal.getName(), status);
    }
    @GetMapping("/overdue/count")
    public List<ICategoryCount> getCategoryOverdueCount(Principal principal) {
        return categoryService.countOverdue(principal.getName());
    }

}
