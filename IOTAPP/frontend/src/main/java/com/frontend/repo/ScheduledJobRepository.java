package com.frontend.repo;


import backend.entity.job.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {

	Optional<ScheduledJob> findByuid(String uid);

	void deleteByuid(String uid);

	Optional<ScheduledJob> findByJobName(String jobName);
}