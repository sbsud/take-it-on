package com.takeiton.repositories;


import com.takeiton.models.History;
import com.takeiton.models.ICategoryCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query(value = "select h.category as category, COUNT(h.category) AS count FROM History h " +
            "where h.owner =:owner AND h.event =:event AND h.value =:value " +
            "GROUP BY h.category ORDER BY COUNT(h.category) ASC")
    List<ICategoryCount> findAllByOwnerAndEventAndValue(@Param("owner") String owner, @Param("event") String event, @Param("value") String value);

    @Query(value= "select h.category as category, COUNT(h.category) as count FROM History h " +
            "where h.owner =:owner AND h.dueDate < CURRENT_DATE+7 " +
            "GROUP BY h.category ORDER BY COUNT(h.category) ASC")
    List<ICategoryCount> findAllOverDue(@Param("owner") String owner);
}
//select h.category as category, COUNT(h.category) as count FROM com.takeiton.models.History h where h.owner =:owner AND (h.dueDate < :date OR (h.dueDate - :date < 7) GROUP BY h.category ORDER BY COUNT(h.category) ASC