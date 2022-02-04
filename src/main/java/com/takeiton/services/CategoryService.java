package com.takeiton.services;

import com.takeiton.models.HistoryEvents;
import com.takeiton.models.ICategoryCount;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.HistoryRepository;
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

    public List<ICategoryCount> countByStatus(String owner, String status) {
        List<ICategoryCount> categoryCounts = historyRepository.findAllByOwnerAndEventAndValue(owner, HistoryEvents.STATUS_CHANGE.name(), status);

        return categoryCounts;
    }

    public List<ICategoryCount> countOverdue(String owner) {
        List<ICategoryCount> categoryCounts = historyRepository.findAllOverDue(owner);
        return categoryCounts;
    }
}
