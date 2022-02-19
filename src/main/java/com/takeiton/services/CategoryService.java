package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.models.HistoryEvents;
import com.takeiton.models.ICategoryCount;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.HistoryRepository;
import com.takeiton.repositories.TaskRepository;
import com.takeiton.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    AppUserRepository appUserRepository;
    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    TaskRepository taskRepository;


    public List<ICategoryCount> countByStatus(String owner, String status) {
        List<ICategoryCount> categoryCounts = historyRepository.findAllByOwnerAndEventAndValue(owner, HistoryEvents.STATUS_CHANGE.name(), status);

        return categoryCounts;
    }

    public List<ICategoryCount> countOverdue(String owner) {
        AppUser appUser = appUserRepository.findById(owner).get();
        List<ICategoryCount> categoryCounts = taskRepository.findAllOverDue(appUser);
        return categoryCounts;
    }


    public List<ICategoryCount> countIncompletes(String owner, String status) {
        AppUser appUser = appUserRepository.findById(owner).get();

        List<ICategoryCount> inCompleteCount = taskRepository.findAllByOwnerAndStatusGroupBy(appUser, status);
        return inCompleteCount;
    }
}
