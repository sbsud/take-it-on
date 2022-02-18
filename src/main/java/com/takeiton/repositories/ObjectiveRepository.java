package com.takeiton.repositories;

import com.takeiton.models.AppUser;
import com.takeiton.models.Objective;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectiveRepository extends CrudRepository<Objective, Long> {
    List<Objective> findAllByOwner(AppUser owner);

    List<Objective> findAllByOwnerAndNameLike(AppUser owner, String name);

    Optional<Objective> findByIdAndOwner(long id, AppUser owner);

    List<Objective> findAllByOwnerAndStatus(AppUser owner, String status);
}
