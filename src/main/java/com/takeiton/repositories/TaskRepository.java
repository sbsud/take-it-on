package com.takeiton.repositories;

import com.takeiton.models.AppUser;
import com.takeiton.models.ICategoryCount;
import com.takeiton.models.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    Optional<Task> findByIdAndOwner(long id, AppUser owner);

    List<Task> findAllByOwner(AppUser owner);

    List<Task> findAllByOwnerAndStatus(AppUser owner, String status);

    List<Task> findAllByOwnerAndCategory(AppUser owner, String category);

    List<Task> findAllByOwnerAndStatusAndCategory(AppUser owner, String status, String category);

    @Query(value = "select t.category as category, COUNT(t.category) AS count FROM Task t, AppUser u " +
            "where t.owner =:owner AND t.status =:status " +
            "GROUP BY t.category ORDER BY COUNT(t.category) ASC")
    List<ICategoryCount> findAllByOwnerAndStatusGroupBy(@Param("owner") AppUser owner, @Param("status") String status);

    @Query(value= "select t.category as category, COUNT(t.category) as count FROM Task t " +
            "where t.owner =:owner AND t.status != 'COMPLETED' AND t.dueDate < CURRENT_DATE+7 " +
            "GROUP BY t.category ORDER BY COUNT(t.category) ASC")
    List<ICategoryCount> findAllOverDue(@Param("owner") AppUser owner);

    @Query(value= "select t FROM Task t " +
            "where t.owner =:owner AND t.status != 'COMPLETED' AND t.category =:category AND t.dueDate < CURRENT_DATE+7")
    List<Task> findAllOverDueTasksByCategory(@Param("owner") AppUser owner, @Param("category") String category);
}