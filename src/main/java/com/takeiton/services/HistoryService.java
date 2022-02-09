package com.takeiton.services;

import com.takeiton.models.History;
import com.takeiton.models.ICategoryCount;
import com.takeiton.models.IHistory;
import com.takeiton.repositories.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HistoryService {
    @Autowired
    HistoryRepository historyRepository;


    public List<IHistory> getHistory(String owner, Long entityId) {
        return historyRepository.findAllByOwnerAndEntityId(owner, Long.toString(entityId));
    }
}
