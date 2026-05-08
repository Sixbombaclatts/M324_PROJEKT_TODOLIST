package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST API für ToDo-List mit Datenbank-Persistierung
 * 
 * @author luh
 */
@RestController
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	private TaskRepository taskRepository;

	@CrossOrigin
	@GetMapping("/")
	public List<Task> getTasks() {
		System.out.println("API EP '/' returns task-list");
		List<Task> tasks = taskRepository.findAll();
		System.out.println("Anzahl Tasks in DB: " + tasks.size());
		if (tasks.size() > 0) {
			int i = 1;
			for (Task task : tasks) {
				System.out.println("-task " + (i++) + ":" + task.getTaskdescription());
			}
		}
		return tasks;
	}

	@CrossOrigin
	@PostMapping("/tasks")
	public String addTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/tasks': '" + taskdescription + "'");
		ObjectMapper mapper = new ObjectMapper();
		try {
			Task task = mapper.readValue(taskdescription, Task.class);
			
			// Prüfe auf Duplikate
			boolean exists = taskRepository.findAll().stream()
					.anyMatch(t -> t.getTaskdescription().equals(task.getTaskdescription()));
			
			if (exists) {
				System.out.println(">>>task: '" + task.getTaskdescription() + "' already exists!");
				return "redirect:/"; // duplicates will be ignored
			}
			
			System.out.println("...adding task: '" + task.getTaskdescription() + "'");
			taskRepository.save(task);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/delete")
	public String delTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/delete': '" + taskdescription + "'");
		ObjectMapper mapper = new ObjectMapper();
		try {
			Task task = mapper.readValue(taskdescription, Task.class);
			
			// Suche Task in Datenbank und lösche ihn
			Task toDelete = taskRepository.findAll().stream()
					.filter(t -> t.getTaskdescription().equals(task.getTaskdescription()))
					.findFirst()
					.orElse(null);
			
			if (toDelete != null) {
				System.out.println("...deleting task: '" + toDelete.getTaskdescription() + "'");
				taskRepository.delete(toDelete);
				return "redirect:/";
			} else {
				System.out.println(">>>task: '" + task.getTaskdescription() + "' not found!");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

}
