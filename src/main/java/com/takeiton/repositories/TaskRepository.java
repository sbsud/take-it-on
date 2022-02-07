package com.takeiton.repositories;

import com.takeiton.models.AppUser;
import com.takeiton.models.Task;
import org.springframework.data.repository.CrudRepository;
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
}