package com.takeiton.controllers;

import com.takeiton.models.History;
import com.takeiton.models.ICategoryCount;
import com.takeiton.models.IHistory;
import com.takeiton.services.CategoryService;
import com.takeiton.services.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/history")
public class HistoryController {

    @Autowired
    HistoryService historyService;

    @GetMapping(value = "{id}")
    public List<IHistory> getHistory(@PathVariable(value = "id") Long id, Principal principal) {
        return historyService.getHistory(principal.getName(), id);
    }
}
