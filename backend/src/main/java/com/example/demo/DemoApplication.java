package com.example.demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * This is a demo application that provides a RESTful API for a simple ToDo list
 * without persistence.
 * The endpoint "/" returns a list of tasks.
 * The endpoint "/tasks" adds a new unique task.
 * The endpoint "/delete" suppresses a task from the list.
 * The task description transferred from the (React) client is provided as a
 * request body in a JSON structure.
 * The data is converted to a task object using Jackson and added to the list of
 * tasks.
 * All endpoints are annotated with @CrossOrigin to enable cross-origin
 * requests.
 *
 * @author luh
 */
@RestController
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private List<Task> tasks = new ArrayList<>();

	private static class UpdateTaskRequest {
		private String taskdescription;
		private String newTaskdescription;

		public String getTaskdescription() {
			return taskdescription;
		}

		public void setTaskdescription(String taskdescription) {
			this.taskdescription = taskdescription;
		}

		public String getNewTaskdescription() {
			return newTaskdescription;
		}

		public void setNewTaskdescription(String newTaskdescription) {
			this.newTaskdescription = newTaskdescription;
		}
	}

	private static class ToggleTaskRequest {
		private String taskdescription;
		private boolean done;

		public String getTaskdescription() {
			return taskdescription;
		}

		public void setTaskdescription(String taskdescription) {
			this.taskdescription = taskdescription;
		}

		public boolean getDone() {
			return done;
		}

		public void setDone(boolean done) {
			this.done = done;
		}
	}

	@CrossOrigin
	@GetMapping("/")
	public List<Task> getTasks() {

		System.out.println("API EP '/' returns task-list of size " + tasks.size() + ".");
		if (tasks.size() > 0) {
			int i = 1;
			for (Task task : tasks) {
				System.out.println("-task " + (i++) + ":" + task.getTaskdescription());
			}
		}
		return tasks; // actual task list (internally converted to a JSON stream)
	}

	@CrossOrigin
	@PostMapping("/tasks")
	public String addTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/tasks': '" + taskdescription + "'");
		ObjectMapper mapper = new ObjectMapper();
		try {
			Task task;
			task = mapper.readValue(taskdescription, Task.class);
			for (Task t : tasks) {
				if (t.getTaskdescription().equals(task.getTaskdescription())) {
					System.out.println(">>>task: '" + task.getTaskdescription() + "' already exists!");
					return "redirect:/"; // duplicates will be ignored
				}
			}
			System.out.println("...adding task: '" + task.getTaskdescription() + "'");
			tasks.add(task);
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
			Task task;
			task = mapper.readValue(taskdescription, Task.class);
			Iterator<Task> it = tasks.iterator();
			while (it.hasNext()) {
				Task t = it.next();
				if (t.getTaskdescription().equals(task.getTaskdescription())) {
					System.out.println("...deleting task: '" + task.getTaskdescription() + "'");
					it.remove();
					return "redirect:/";
				}
			}
			System.out.println(">>>task: '" + task.getTaskdescription() + "' not found!");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/update")
	public String updateTask(@RequestBody String updatePayload) {
		System.out.println("API EP '/update': '" + updatePayload + "'");
		ObjectMapper mapper = new ObjectMapper();
		try {
			UpdateTaskRequest updateRequest = mapper.readValue(updatePayload, UpdateTaskRequest.class);
			String oldDescription = updateRequest.getTaskdescription();
			String newDescription = updateRequest.getNewTaskdescription();

			if (oldDescription == null || newDescription == null || newDescription.trim().isEmpty()) {
				return "redirect:/";
			}

			for (Task t : tasks) {
				if (t.getTaskdescription().equals(newDescription)) {
					System.out.println(">>>task: '" + newDescription + "' already exists!");
					return "redirect:/";
				}
			}

			for (Task t : tasks) {
				if (t.getTaskdescription().equals(oldDescription)) {
					System.out.println("...updating task: '" + oldDescription + "' -> '" + newDescription + "'");
					t.setTaskdescription(newDescription);
					return "redirect:/";
				}
			}

			System.out.println(">>>task: '" + oldDescription + "' not found!");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/toggle-done")
	public String toggleDone(@RequestBody String togglePayload) {
		System.out.println("API EP '/toggle-done': '" + togglePayload + "'");
		ObjectMapper mapper = new ObjectMapper();
		try {
			ToggleTaskRequest toggleRequest = mapper.readValue(togglePayload, ToggleTaskRequest.class);
			String description = toggleRequest.getTaskdescription();
			for (Task t : tasks) {
				if (t.getTaskdescription().equals(description)) {
					t.setDone(toggleRequest.getDone());
					return "redirect:/";
				}
			}
			System.out.println(">>>task: '" + description + "' not found!");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

}
