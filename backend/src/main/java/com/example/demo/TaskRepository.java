package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository für Task-CRUD-Operationen
 * JpaRepository bietet automatisch: findAll(), save(), delete(), etc.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	
	// Zusätzliche Methoden können hier definiert werden, z.B.:
	// Task findByTaskdescription(String description);
}
