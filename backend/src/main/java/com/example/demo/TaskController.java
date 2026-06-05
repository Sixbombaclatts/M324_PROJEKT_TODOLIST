package com.example.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class TaskController {

	private final TaskRepository taskRepository;
	private final ObjectMapper mapper = new ObjectMapper();

	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	private static class UpdateTaskRequest {
		private String taskdescription;
		private String newTaskdescription;
		private String dueDate;
		private boolean reminderEnabled;

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

		public String getDueDate() {
			return dueDate;
		}

		public void setDueDate(String dueDate) {
			this.dueDate = dueDate;
		}

		public boolean getReminderEnabled() {
			return reminderEnabled;
		}

		public void setReminderEnabled(boolean reminderEnabled) {
			this.reminderEnabled = reminderEnabled;
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
		return taskRepository.findAll();
	}

	@CrossOrigin
	@GetMapping("/tasks")
	public List<Task> getAllTasks() {
		return taskRepository.findAll();
	}

	@CrossOrigin
	@PostMapping("/tasks")
	public ResponseEntity<?> addTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/tasks': '" + taskdescription + "'");
		try {
			Task task = mapper.readValue(taskdescription, Task.class);
			normalizeTask(task);

			if (taskRepository.findByTaskdescription(task.getTaskdescription()).isPresent()) {
				System.out.println(">>>task: '" + task.getTaskdescription() + "' already exists!");
				return ResponseEntity.ok("redirect:/");
			}

			Task saved = taskRepository.save(task);
			System.out.println("...adding task: '" + saved.getTaskdescription() + "'");
			return ResponseEntity.status(HttpStatus.CREATED).body(saved);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@CrossOrigin
	@PostMapping("/delete")
	public String delTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/delete': '" + taskdescription + "'");
		try {
			Task task = mapper.readValue(taskdescription, Task.class);
			var existing = taskRepository.findByTaskdescription(task.getTaskdescription());
			if (existing.isPresent()) {
				System.out.println("...deleting task: '" + task.getTaskdescription() + "'");
				taskRepository.delete(existing.get());
				return "redirect:/";
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
		try {
			UpdateTaskRequest updateRequest = mapper.readValue(updatePayload, UpdateTaskRequest.class);
			String oldDescription = updateRequest.getTaskdescription();
			String newDescription = updateRequest.getNewTaskdescription();

			if (oldDescription == null || newDescription == null || newDescription.trim().isEmpty()) {
				return "redirect:/";
			}

			if (taskRepository.findByTaskdescription(newDescription).isPresent()) {
				System.out.println(">>>task: '" + newDescription + "' already exists!");
				return "redirect:/";
			}

			var existing = taskRepository.findByTaskdescription(oldDescription);
			if (existing.isPresent()) {
				Task t = existing.get();
				System.out.println("...updating task: '" + oldDescription + "' -> '" + newDescription + "'");
				t.setTaskdescription(newDescription);
				t.setDueDate(updateRequest.getDueDate() == null ? "" : updateRequest.getDueDate());
				if (t.getDueDate().isBlank()) {
					t.setReminderEnabled(false);
				} else {
					t.setReminderEnabled(updateRequest.getReminderEnabled());
				}
				taskRepository.save(t);
				return "redirect:/";
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
		try {
			ToggleTaskRequest toggleRequest = mapper.readValue(togglePayload, ToggleTaskRequest.class);
			String description = toggleRequest.getTaskdescription();
			var existing = taskRepository.findByTaskdescription(description);
			if (existing.isPresent()) {
				Task t = existing.get();
				t.setDone(toggleRequest.getDone());
				taskRepository.save(t);
				return "redirect:/";
			}
			System.out.println(">>>task: '" + description + "' not found!");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	private void normalizeTask(Task task) {
		if (task.getDueDate() == null) {
			task.setDueDate("");
		}
		if (task.getDueDate().isBlank()) {
			task.setReminderEnabled(false);
		}
	}

}
