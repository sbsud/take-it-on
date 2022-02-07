package com.takeiton.services;

import com.takeiton.models.ICompletionRate;
import com.takeiton.repositories.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompletionService {

    @Autowired
    HistoryRepository historyRepository;

    public List<ICompletionRate> getCount(String owner) {
        return historyRepository.findCompletionRate(owner);
    }
}
