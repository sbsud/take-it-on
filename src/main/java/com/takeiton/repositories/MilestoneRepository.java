package com.takeiton.repositories;

import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends CrudRepository<Milestone, Long> {
    Optional<Milestone> findByIdAndOwner(long id, AppUser owner);

    List<Milestone> findAllByOwner(AppUser owner);

}
