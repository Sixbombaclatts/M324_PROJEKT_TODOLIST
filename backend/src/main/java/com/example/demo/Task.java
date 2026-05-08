package com.example.demo;

import jakarta.persistence.*;

/**
 * Task Entity für persistente Speicherung in der Datenbank
 * 
 * @author luh
 */
@Entity
@Table(name = "tasks")
public class Task {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "description", nullable = false, unique = true, length = 500)
	private String taskdescription; // must have the EXACT name as his React state property and may not be ignored!

	public Task() {
    }
	
	public Task(String taskdescription) {
		this.taskdescription = taskdescription;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTaskdescription() { // do not apply camel-case here! Its a Bean!
		return taskdescription;
	}

	public void setTaskdescription(String taskdescription) { // do not apply camel-case here! Its a Bean!
		this.taskdescription = taskdescription;
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", taskdescription='" + taskdescription + '\'' +
				'}';
	}

}