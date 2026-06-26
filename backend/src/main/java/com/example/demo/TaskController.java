package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
	public ResponseEntity<?> getTasks(@RequestParam(name = "api", defaultValue = "v1") String apiVersion) {
		return getAllTasks(apiVersion);
	}

	@CrossOrigin
	@GetMapping("/tasks")
	public ResponseEntity<?> getAllTasks(@RequestParam(name = "api", defaultValue = "v1") String apiVersion) {
		ApiVersion version = ApiVersion.from(apiVersion);
		List<Task> tasks = taskRepository.findAll();
		if (version == ApiVersion.V2) {
			return ResponseEntity.ok(new TaskV2ListResponse(tasks));
		}
		return ResponseEntity.ok(tasks);
	}

	@CrossOrigin
	@PostMapping("/tasks")
	public ResponseEntity<?> addTask(
			@RequestParam(name = "api", defaultValue = "v1") String apiVersion,
			@RequestBody String taskdescription) {
		ApiVersion version = ApiVersion.from(apiVersion);
		System.out.println("API EP '/tasks' [" + version.getValue() + "]: '" + taskdescription + "'");
		try {
			Task task = mapper.readValue(taskdescription, Task.class);
			normalizeTask(task);

			if (taskRepository.findByTaskdescription(task.getTaskdescription()).isPresent()) {
				System.out.println(">>>task: '" + task.getTaskdescription() + "' already exists!");
				if (version == ApiVersion.V2) {
					return ResponseEntity.ok(Map.of(
							"apiVersion", "v2",
							"action", "duplicate",
							"description", task.getTaskdescription()));
				}
				return ResponseEntity.ok("redirect:/");
			}

			Task saved = taskRepository.save(task);
			System.out.println("...adding task: '" + saved.getTaskdescription() + "'");
			if (version == ApiVersion.V2) {
				return ResponseEntity.status(HttpStatus.CREATED).body(TaskV2Dto.from(saved));
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(saved);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@CrossOrigin
	@PostMapping("/delete")
	public ResponseEntity<?> delTask(
			@RequestParam(name = "api", defaultValue = "v1") String apiVersion,
			@RequestBody String taskdescription) {
		ApiVersion version = ApiVersion.from(apiVersion);
		System.out.println("API EP '/delete' [" + version.getValue() + "]: '" + taskdescription + "'");
		try {
			Task task = mapper.readValue(taskdescription, Task.class);
			var existing = taskRepository.findByTaskdescription(task.getTaskdescription());
			if (existing.isPresent()) {
				System.out.println("...deleting task: '" + task.getTaskdescription() + "'");
				taskRepository.delete(existing.get());
				if (version == ApiVersion.V2) {
					return ResponseEntity.ok(Map.of(
							"apiVersion", "v2",
							"action", "deleted",
							"description", task.getTaskdescription()));
				}
				return ResponseEntity.ok("redirect:/");
			}
			System.out.println(">>>task: '" + task.getTaskdescription() + "' not found!");
			if (version == ApiVersion.V2) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
						"apiVersion", "v2",
						"action", "not_found",
						"description", task.getTaskdescription()));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok("redirect:/");
	}

	@CrossOrigin
	@PostMapping("/update")
	public ResponseEntity<?> updateTask(
			@RequestParam(name = "api", defaultValue = "v1") String apiVersion,
			@RequestBody String updatePayload) {
		ApiVersion version = ApiVersion.from(apiVersion);
		System.out.println("API EP '/update' [" + version.getValue() + "]: '" + updatePayload + "'");
		try {
			UpdateTaskRequest updateRequest = mapper.readValue(updatePayload, UpdateTaskRequest.class);
			String oldDescription = updateRequest.getTaskdescription();
			String newDescription = updateRequest.getNewTaskdescription();

			if (oldDescription == null || newDescription == null || newDescription.trim().isEmpty()) {
				return version == ApiVersion.V2
						? ResponseEntity.badRequest().body(Map.of("apiVersion", "v2", "action", "invalid_request"))
						: ResponseEntity.ok("redirect:/");
			}

			if (taskRepository.findByTaskdescription(newDescription).isPresent()) {
				System.out.println(">>>task: '" + newDescription + "' already exists!");
				if (version == ApiVersion.V2) {
					return ResponseEntity.ok(Map.of(
							"apiVersion", "v2",
							"action", "duplicate",
							"description", newDescription));
				}
				return ResponseEntity.ok("redirect:/");
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
				if (version == ApiVersion.V2) {
					return ResponseEntity.ok(Map.of(
							"apiVersion", "v2",
							"action", "updated",
							"description", newDescription));
				}
				return ResponseEntity.ok("redirect:/");
			}

			System.out.println(">>>task: '" + oldDescription + "' not found!");
			if (version == ApiVersion.V2) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
						"apiVersion", "v2",
						"action", "not_found",
						"description", oldDescription));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok("redirect:/");
	}

	@CrossOrigin
	@PostMapping("/toggle-done")
	public ResponseEntity<?> toggleDone(
			@RequestParam(name = "api", defaultValue = "v1") String apiVersion,
			@RequestBody String togglePayload) {
		ApiVersion version = ApiVersion.from(apiVersion);
		System.out.println("API EP '/toggle-done' [" + version.getValue() + "]: '" + togglePayload + "'");
		try {
			ToggleTaskRequest toggleRequest = mapper.readValue(togglePayload, ToggleTaskRequest.class);
			String description = toggleRequest.getTaskdescription();
			var existing = taskRepository.findByTaskdescription(description);
			if (existing.isPresent()) {
				Task t = existing.get();
				t.setDone(toggleRequest.getDone());
				taskRepository.save(t);
				if (version == ApiVersion.V2) {
					return ResponseEntity.ok(Map.of(
							"apiVersion", "v2",
							"action", "toggled",
							"description", description,
							"done", toggleRequest.getDone()));
				}
				return ResponseEntity.ok("redirect:/");
			}
			System.out.println(">>>task: '" + description + "' not found!");
			if (version == ApiVersion.V2) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
						"apiVersion", "v2",
						"action", "not_found",
						"description", description));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok("redirect:/");
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
