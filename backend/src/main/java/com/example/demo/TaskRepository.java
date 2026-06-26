package com.example.demo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface TaskRepository extends JpaRepository<Task, Long> {

	Optional<Task> findByTaskdescription(String taskdescription);

}
